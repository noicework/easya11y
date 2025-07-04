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
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST endpoint for listing and filtering accessibility scan results.
 */
@Path("/easya11y/results")
public class ScanResultsListEndpoint extends AbstractEndpoint<EndpointDefinition> {

    private static final Logger log = LoggerFactory.getLogger(ScanResultsListEndpoint.class);
    private static final String SCAN_RESULTS_WORKSPACE = "easya11y";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Inject
    public ScanResultsListEndpoint(EndpointDefinition definition) {
        super(definition);
    }

    /**
     * Get a list of scan results with filtering options.
     *
     * @param pagePath Filter by page path
     * @param severity Filter by severity (critical, serious, moderate, minor)
     * @param wcagLevel Filter by WCAG level (A, AA, AAA)
     * @param dateFrom Filter by date from (timestamp)
     * @param dateTo Filter by date to (timestamp)
     * @param limit Maximum number of results to return
     * @param offset Offset for pagination
     * @return HTTP response with scan results
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listScanResults(
            @QueryParam("pagePath") String pagePath,
            @QueryParam("severity") String severity,
            @QueryParam("wcagLevel") String wcagLevel,
            @QueryParam("dateFrom") Long dateFrom,
            @QueryParam("dateTo") Long dateTo,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {
        
        try {
            Session session = MgnlContext.getJCRSession(SCAN_RESULTS_WORKSPACE);
            List<Map<String, Object>> results = new ArrayList<>();
            
            // Ensure scanResults node exists
            if (!session.nodeExists("/scanResults")) {
                Node root = session.getRootNode();
                root.addNode("scanResults", "mgnl:folder");
                session.save();
            }
            
            // Build JCR query
            String queryStr = buildQuery(pagePath, severity, wcagLevel, dateFrom, dateTo);
            
            if (queryStr != null) {
                QueryManager queryManager = session.getWorkspace().getQueryManager();
                Query query = queryManager.createQuery(queryStr, Query.JCR_SQL2);
                
                // Apply limit and offset
                if (limit != null && limit > 0) {
                    query.setLimit(limit);
                }
                if (offset != null && offset > 0) {
                    query.setOffset(offset);
                }
                
                QueryResult queryResult = query.execute();
                NodeIterator nodeIterator = queryResult.getNodes();
                
                while (nodeIterator.hasNext()) {
                    Node scanNode = nodeIterator.nextNode();
                    results.add(buildScanResultSummary(scanNode));
                }
            } else {
                // No query, get all results
                collectAllScanResults(session.getNode("/scanResults"), results);
            }
            
            // Sort by scan date descending
            results.sort((a, b) -> {
                Long dateA = (Long) a.get("scanDate");
                Long dateB = (Long) b.get("scanDate");
                return dateB.compareTo(dateA);
            });
            
            // Apply pagination if not done via query
            if (queryStr == null && limit != null && limit > 0) {
                int start = offset != null ? offset : 0;
                int end = Math.min(start + limit, results.size());
                results = results.subList(start, end);
            }
            
            // Calculate summary statistics
            Map<String, Object> summary = new HashMap<>();
            try {
                summary = calculateSummaryStats(session);
            } catch (Exception e) {
                log.warn("Could not calculate summary stats: " + e.getMessage());
                // Return empty stats rather than failing
                summary.put("totalScans", 0);
                summary.put("averageScore", 0.0);
                summary.put("totalCritical", 0L);
                summary.put("totalSerious", 0L);
                summary.put("totalModerate", 0L);
                summary.put("totalMinor", 0L);
                summary.put("totalViolations", 0L);
                summary.put("perfectScorePages", 0L);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalResults", results.size());
            response.put("results", results);
            response.put("summary", summary);
            
            return Response.ok(response).build();
            
        } catch (RepositoryException e) {
            log.error("Error listing scan results", e);
            return buildErrorResponse("Error listing scan results: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get detailed scan result for a specific page.
     *
     * @param pagePath The page path
     * @return HTTP response with detailed scan result
     */
    @GET
    @Path("/detail")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getScanDetail(@QueryParam("pagePath") String pagePath) {
        if (StringUtils.isBlank(pagePath)) {
            return buildErrorResponse("Page path is required", Response.Status.BAD_REQUEST);
        }
        
        try {
            Session session = MgnlContext.getJCRSession(SCAN_RESULTS_WORKSPACE);
            String scanPath = "/scanResults" + pagePath;
            
            if (!session.nodeExists(scanPath)) {
                return buildErrorResponse("No scan results found for page: " + pagePath, Response.Status.NOT_FOUND);
            }
            
            Node scanNode = session.getNode(scanPath);
            
            // Get full results if stored
            if (scanNode.hasProperty("fullResults")) {
                String fullResultsJson = PropertyUtil.getString(scanNode, "fullResults");
                AccessibilityScanResult fullResult = objectMapper.readValue(fullResultsJson, AccessibilityScanResult.class);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("result", fullResult);
                
                return Response.ok(response).build();
            } else {
                // Build from stored properties
                Map<String, Object> result = buildScanResultSummary(scanNode);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("result", result);
                
                return Response.ok(response).build();
            }
            
        } catch (Exception e) {
            log.error("Error getting scan detail", e);
            return buildErrorResponse("Error getting scan detail: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Export scan results as CSV.
     *
     * @return CSV file with scan results
     */
    @GET
    @Path("/export/csv")
    @Produces("text/csv")
    public Response exportCsv(@QueryParam("pagePath") String pagePath) {
        try {
            Session session = MgnlContext.getJCRSession(SCAN_RESULTS_WORKSPACE);
            List<Map<String, Object>> results = new ArrayList<>();
            
            if (StringUtils.isNotBlank(pagePath)) {
                String scanPath = "/scanResults" + pagePath;
                if (session.nodeExists(scanPath)) {
                    results.add(buildScanResultSummary(session.getNode(scanPath)));
                }
            } else {
                collectAllScanResults(session.getNode("/scanResults"), results);
            }
            
            // Build CSV
            StringBuilder csv = new StringBuilder();
            csv.append("Page Path,Page Title,Scan Date,Score,Violations,Critical,Serious,Moderate,Minor\n");
            
            for (Map<String, Object> result : results) {
                csv.append(escapeCsv((String) result.get("pagePath"))).append(",");
                csv.append(escapeCsv((String) result.get("pageTitle"))).append(",");
                csv.append(new Date((Long) result.get("scanDate"))).append(",");
                csv.append(result.get("score")).append(",");
                csv.append(result.get("violationCount")).append(",");
                csv.append(result.get("criticalCount")).append(",");
                csv.append(result.get("seriousCount")).append(",");
                csv.append(result.get("moderateCount")).append(",");
                csv.append(result.get("minorCount")).append("\n");
            }
            
            return Response.ok(csv.toString())
                    .header("Content-Disposition", "attachment; filename=\"accessibility-scan-results.csv\"")
                    .build();
                    
        } catch (RepositoryException e) {
            log.error("Error exporting CSV", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error exporting CSV: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Build JCR query based on filters.
     */
    private String buildQuery(String pagePath, String severity, String wcagLevel, Long dateFrom, Long dateTo) {
        List<String> conditions = new ArrayList<>();
        
        if (StringUtils.isNotBlank(pagePath)) {
            conditions.add("[jcr:path] LIKE '/scanResults" + pagePath + "%'");
        }
        
        if (StringUtils.isNotBlank(severity)) {
            conditions.add("[violations_" + severity + "] > 0");
        }
        
        if (StringUtils.isNotBlank(wcagLevel)) {
            conditions.add("[wcagLevel] = '" + wcagLevel + "'");
        }
        
        if (dateFrom != null) {
            conditions.add("[scanDate] >= " + dateFrom);
        }
        
        if (dateTo != null) {
            conditions.add("[scanDate] <= " + dateTo);
        }
        
        if (conditions.isEmpty()) {
            return null;
        }
        
        return "SELECT * FROM [mgnl:content] WHERE " + String.join(" AND ", conditions);
    }

    /**
     * Collect all scan results recursively.
     */
    private void collectAllScanResults(Node node, List<Map<String, Object>> results) throws RepositoryException {
        if (NodeUtil.isNodeType(node, "mgnl:content") && node.hasProperty("scanId")) {
            results.add(buildScanResultSummary(node));
        }
        
        NodeIterator children = node.getNodes();
        while (children.hasNext()) {
            Node child = children.nextNode();
            if (!child.getName().startsWith("jcr:") && !child.getName().startsWith("mgnl:")) {
                collectAllScanResults(child, results);
            }
        }
    }

    /**
     * Build scan result summary from node.
     */
    private Map<String, Object> buildScanResultSummary(Node scanNode) throws RepositoryException {
        Map<String, Object> summary = new HashMap<>();
        
        // Extract page path from node path
        String nodePath = scanNode.getPath();
        String pagePath = nodePath.replace("/scanResults", "");
        
        summary.put("scanId", PropertyUtil.getString(scanNode, "scanId", ""));
        summary.put("pagePath", pagePath);
        summary.put("pageUrl", PropertyUtil.getString(scanNode, "pageUrl", ""));
        summary.put("pageTitle", PropertyUtil.getString(scanNode, "pageTitle", ""));
        summary.put("scanDate", PropertyUtil.getLong(scanNode, "scanDate", 0L));
        summary.put("wcagLevel", PropertyUtil.getString(scanNode, "wcagLevel", "AA"));
        summary.put("score", scanNode.hasProperty("score") ? scanNode.getProperty("score").getDouble() : 0.0);
        summary.put("violationCount", PropertyUtil.getLong(scanNode, "violationCount", 0L));
        summary.put("passCount", PropertyUtil.getLong(scanNode, "passCount", 0L));
        
        // Violation counts by impact
        summary.put("criticalCount", PropertyUtil.getLong(scanNode, "violations_critical", 0L));
        summary.put("seriousCount", PropertyUtil.getLong(scanNode, "violations_serious", 0L));
        summary.put("moderateCount", PropertyUtil.getLong(scanNode, "violations_moderate", 0L));
        summary.put("minorCount", PropertyUtil.getLong(scanNode, "violations_minor", 0L));
        
        return summary;
    }

    /**
     * Calculate summary statistics for all scan results.
     */
    private Map<String, Object> calculateSummaryStats(Session session) throws RepositoryException {
        Map<String, Object> stats = new HashMap<>();
        
        if (!session.nodeExists("/scanResults")) {
            // Return empty stats if no scan results exist yet
            stats.put("totalScans", 0);
            stats.put("averageScore", 0.0);
            stats.put("totalCritical", 0L);
            stats.put("totalSerious", 0L);
            stats.put("totalModerate", 0L);
            stats.put("totalMinor", 0L);
            stats.put("totalViolations", 0L);
            stats.put("perfectScorePages", 0L);
            return stats;
        }
        
        Node rootNode = session.getNode("/scanResults");
        List<Map<String, Object>> allResults = new ArrayList<>();
        collectAllScanResults(rootNode, allResults);
        
        stats.put("totalScans", allResults.size());
        
        if (!allResults.isEmpty()) {
            // Average score
            double avgScore = allResults.stream()
                    .mapToDouble(r -> (Double) r.get("score"))
                    .average()
                    .orElse(0.0);
            stats.put("averageScore", Math.round(avgScore * 10) / 10.0);
            
            // Total violations by impact
            long totalCritical = allResults.stream()
                    .mapToLong(r -> (Long) r.get("criticalCount"))
                    .sum();
            long totalSerious = allResults.stream()
                    .mapToLong(r -> (Long) r.get("seriousCount"))
                    .sum();
            long totalModerate = allResults.stream()
                    .mapToLong(r -> (Long) r.get("moderateCount"))
                    .sum();
            long totalMinor = allResults.stream()
                    .mapToLong(r -> (Long) r.get("minorCount"))
                    .sum();
            
            stats.put("totalCritical", totalCritical);
            stats.put("totalSerious", totalSerious);
            stats.put("totalModerate", totalModerate);
            stats.put("totalMinor", totalMinor);
            stats.put("totalViolations", totalCritical + totalSerious + totalModerate + totalMinor);
            
            // Pages with perfect score
            long perfectScoreCount = allResults.stream()
                    .filter(r -> (Double) r.get("score") == 100.0)
                    .count();
            stats.put("perfectScorePages", perfectScoreCount);
        } else {
            stats.put("averageScore", 0.0);
            stats.put("totalCritical", 0L);
            stats.put("totalSerious", 0L);
            stats.put("totalModerate", 0L);
            stats.put("totalMinor", 0L);
            stats.put("totalViolations", 0L);
            stats.put("perfectScorePages", 0L);
        }
        
        return stats;
    }

    /**
     * Escape CSV values.
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
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