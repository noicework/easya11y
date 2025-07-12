package work.noice.easya11y.endpoints;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.rest.AbstractEndpoint;
import info.magnolia.rest.EndpointDefinition;
import work.noice.easya11y.models.AccessibilityScanResult;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST endpoint for initiating accessibility scans and storing results.
 */
@Path("/easya11y/scan")
public class AccessibilityScanEndpoint extends AbstractEndpoint<EndpointDefinition> {

    private static final Logger log = LoggerFactory.getLogger(AccessibilityScanEndpoint.class);
    private static final String WEBSITE_WORKSPACE = "website";
    private static final String SCAN_RESULTS_WORKSPACE = "easya11y";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // Temporary storage for scan parameters
    private static final Map<String, String> scanWcagLevels = new HashMap<>();
    
    @Inject
    public AccessibilityScanEndpoint(EndpointDefinition definition) {
        super(definition);
    }

    /**
     * Initiate a scan for a specific page.
     *
     * @param pagePath The path of the page to scan
     * @return Response with page URL and scan ID
     */
    @POST
    @Path("/initiate")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response initiateScan(Map<String, String> request) {
        String pagePath = request.get("pagePath");
        String wcagLevel = request.get("wcagLevel");
        
        if (pagePath == null || pagePath.isEmpty()) {
            return buildErrorResponse("Page path is required", Response.Status.BAD_REQUEST);
        }
        
        if (wcagLevel == null || wcagLevel.isEmpty()) {
            wcagLevel = "AA"; // Default to AA if not provided
        }
        
        try {
            // Generate scan ID
            String scanId = UUID.randomUUID().toString();
            
            // Store WCAG level for this scan
            scanWcagLevels.put(scanId, wcagLevel);
            
            // Build page URL directly - let the rendered page handle whether it exists
            String contextPath = MgnlContext.getContextPath();
            String pageUrl = MgnlContext.getWebContext().getRequest().getScheme() + "://" + 
                           MgnlContext.getWebContext().getRequest().getServerName() + ":" +
                           MgnlContext.getWebContext().getRequest().getServerPort() +
                           contextPath + pagePath + ".html";
            
            // Try to get page title from JCR if available, otherwise use page name from path
            String pageTitle = pagePath.substring(pagePath.lastIndexOf('/') + 1);
            try {
                Session websiteSession = MgnlContext.getJCRSession(WEBSITE_WORKSPACE);
                if (websiteSession.nodeExists(pagePath)) {
                    Node pageNode = websiteSession.getNode(pagePath);
                    pageTitle = PropertyUtil.getString(pageNode, "title", pageTitle);
                }
            } catch (Exception e) {
                log.warn("Could not get page title from JCR for path: {}, using default: {}", pagePath, pageTitle);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("scanId", scanId);
            response.put("pagePath", pagePath);
            response.put("pageUrl", pageUrl);
            response.put("pageTitle", pageTitle);
            response.put("wcagLevel", wcagLevel);
            response.put("message", "Scan initiated. Use the URL to scan the page with axe-core.");
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            log.error("Error initiating scan", e);
            return buildErrorResponse("Error initiating scan: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Store scan results from axe-core.
     *
     * @param scanResults The scan results from axe-core
     * @return Response indicating success or failure
     */
    @POST
    @Path("/results")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response storeScanResults(JsonNode scanResults) {
        try {
            // Extract basic information
            String scanId = scanResults.get("scanId").asText();
            String pagePath = scanResults.get("pagePath").asText();
            String pageUrl = scanResults.get("pageUrl").asText();
            String pageTitle = scanResults.get("pageTitle").asText();
            
            // Get WCAG level from request or use stored value from scan initiation
            String wcagLevel = scanResults.has("wcagLevel") ? 
                scanResults.get("wcagLevel").asText() : 
                scanWcagLevels.getOrDefault(scanId, "AA");
            
            // Get score from frontend (required)
            if (!scanResults.has("score")) {
                return buildErrorResponse("Score is required from frontend calculation", Response.Status.BAD_REQUEST);
            }
            Double frontendScore = scanResults.get("score").asDouble();
            
            // Validate score range
            if (frontendScore < 0 || frontendScore > 100) {
                return buildErrorResponse("Score must be between 0 and 100", Response.Status.BAD_REQUEST);
            }
            
            // Clean up temporary storage
            scanWcagLevels.remove(scanId);
            
            // Get axe results
            JsonNode axeResults = scanResults.get("axeResults");
            if (axeResults == null) {
                return buildErrorResponse("No axe results provided", Response.Status.BAD_REQUEST);
            }
            
            // Create scan result model
            AccessibilityScanResult result = new AccessibilityScanResult(pagePath, pageUrl);
            result.setId(scanId);
            result.setPageTitle(pageTitle);
            result.setWcagLevel(wcagLevel);
            result.setScannerVersion(axeResults.get("toolOptions").get("version").asText("unknown"));
            
            // Process violations
            JsonNode violations = axeResults.get("violations");
            if (violations != null && violations.isArray()) {
                processScanItems(violations, result.getViolations(), AccessibilityScanResult.Violation.class, result);
            }
            
            // Process passes
            JsonNode passes = axeResults.get("passes");
            if (passes != null && passes.isArray()) {
                List<AccessibilityScanResult.Pass> passList = new ArrayList<>();
                int totalPassElements = 0;
                for (JsonNode pass : passes) {
                    AccessibilityScanResult.Pass p = new AccessibilityScanResult.Pass();
                    p.setId(pass.get("id").asText());
                    p.setDescription(pass.get("description").asText());
                    p.setHelp(pass.get("help").asText());
                    int nodeCount = pass.get("nodes").size();
                    p.setNodeCount(nodeCount);
                    totalPassElements += nodeCount;
                    passList.add(p);
                }
                result.setPasses(passList);
                
                // Calculate total elements (elements that passed + elements with issues)
                result.setTotalElements(totalPassElements + result.getElementsWithIssues());
            }
            
            // Set the frontend-calculated score
            result.setScore(frontendScore);
            
            // Store in JCR
            Session scanSession = MgnlContext.getJCRSession(SCAN_RESULTS_WORKSPACE);
            String scanResultPath = "/scanResults" + pagePath;
            
            // Create parent nodes if needed
            ensureParentNodes(scanSession, scanResultPath);
            
            // Create or update scan result node
            Node scanNode;
            if (scanSession.nodeExists(scanResultPath)) {
                scanNode = scanSession.getNode(scanResultPath);
            } else {
                Node parentNode = scanSession.getNode(scanResultPath.substring(0, scanResultPath.lastIndexOf('/')));
                scanNode = parentNode.addNode(scanResultPath.substring(scanResultPath.lastIndexOf('/') + 1), "mgnl:content");
            }
            
            // Store scan data
            scanNode.setProperty("scanId", scanId);
            scanNode.setProperty("pageUrl", pageUrl);
            scanNode.setProperty("pageTitle", pageTitle);
            scanNode.setProperty("scanDate", new Date().getTime());
            scanNode.setProperty("wcagLevel", wcagLevel);
            scanNode.setProperty("score", result.getScore());
            scanNode.setProperty("violationCount", result.getViolations().size());
            scanNode.setProperty("passCount", result.getPasses().size());
            scanNode.setProperty("totalElements", result.getTotalElements());
            scanNode.setProperty("elementsWithIssues", result.getElementsWithIssues());
            
            // Store violations summary
            for (Map.Entry<String, Integer> entry : result.getViolationsByImpact().entrySet()) {
                scanNode.setProperty("violations_" + entry.getKey(), entry.getValue());
            }
            
            // Store detailed results as JSON
            scanNode.setProperty("fullResults", objectMapper.writeValueAsString(result));
            
            scanSession.save();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("scanId", scanId);
            response.put("score", result.getScore());
            response.put("violationCount", result.getViolations().size());
            response.put("passCount", result.getPasses().size());
            response.put("totalElements", result.getTotalElements());
            response.put("elementsWithIssues", result.getElementsWithIssues());
            response.put("message", "Scan results stored successfully");
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            log.error("Error storing scan results", e);
            return buildErrorResponse("Error storing scan results: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Process scan items (violations, incomplete, etc.)
     */
    private <T> void processScanItems(JsonNode items, List<T> targetList, Class<T> itemClass, AccessibilityScanResult result) {
        if (!items.isArray()) return;
        
        for (JsonNode item : items) {
            try {
                if (itemClass == AccessibilityScanResult.Violation.class) {
                    AccessibilityScanResult.Violation violation = new AccessibilityScanResult.Violation();
                    violation.setId(item.get("id").asText());
                    violation.setImpact(item.get("impact").asText());
                    violation.setDescription(item.get("description").asText());
                    violation.setHelp(item.get("help").asText());
                    violation.setHelpUrl(item.get("helpUrl").asText());
                    
                    // Process tags
                    JsonNode tags = item.get("tags");
                    if (tags != null && tags.isArray()) {
                        List<String> tagList = new ArrayList<>();
                        for (JsonNode tag : tags) {
                            tagList.add(tag.asText());
                            // Update WCAG level counts
                            if (tag.asText().matches("wcag2?a{1,3}")) {
                                String level = tag.asText().toUpperCase().replaceAll("WCAG2?", "");
                                result.getViolationsByWcagLevel().merge(level, 1, Integer::sum);
                            }
                        }
                        violation.setTags(tagList);
                    }
                    
                    // Process nodes
                    JsonNode nodes = item.get("nodes");
                    if (nodes != null && nodes.isArray()) {
                        List<AccessibilityScanResult.Node> nodeList = new ArrayList<>();
                        for (JsonNode node : nodes) {
                            AccessibilityScanResult.Node n = new AccessibilityScanResult.Node();
                            
                            // Handle target array
                            JsonNode targetArray = node.get("target");
                            if (targetArray != null && targetArray.isArray() && targetArray.size() > 0) {
                                n.setTarget(targetArray.get(0).asText());
                            }
                            
                            n.setHtml(node.get("html").asText(""));
                            n.setFailureSummary(node.get("failureSummary").asText(""));
                            n.setImpact(node.get("impact").asText(""));
                            
                            nodeList.add(n);
                        }
                        violation.setNodes(nodeList);
                        result.setElementsWithIssues(result.getElementsWithIssues() + nodeList.size());
                    }
                    
                    // Update impact counts
                    result.getViolationsByImpact().merge(violation.getImpact(), 1, Integer::sum);
                    
                    targetList.add((T) violation);
                }
            } catch (Exception e) {
                log.warn("Error processing scan item", e);
            }
        }
    }
    
    /**
     * Ensure parent nodes exist for a given path.
     */
    private void ensureParentNodes(Session session, String path) throws RepositoryException {
        String[] parts = path.split("/");
        String currentPath = "";
        
        for (int i = 1; i < parts.length - 1; i++) {
            currentPath += "/" + parts[i];
            if (!session.nodeExists(currentPath)) {
                Node parentNode = session.getNode(currentPath.substring(0, currentPath.lastIndexOf('/')));
                parentNode.addNode(parts[i], "mgnl:folder");
            }
        }
    }
    
    /**
     * Build an error response.
     */
    private Response buildErrorResponse(String message, Response.Status status) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        
        return Response.status(status).entity(result).build();
    }
}