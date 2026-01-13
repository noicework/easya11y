package work.noice.easya11y.endpoints;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.rest.AbstractEndpoint;
import info.magnolia.rest.EndpointDefinition;
import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.config.registry.Registry;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;

import jakarta.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST endpoint for listing pages from the website workspace for accessibility scanning.
 */
@Path("/easya11y/pages")
public class PageListEndpoint extends AbstractEndpoint<EndpointDefinition> {

    private static final Logger log = LoggerFactory.getLogger(PageListEndpoint.class);
    private static final String WEBSITE_WORKSPACE = "website";
    private static final String SCAN_RESULTS_WORKSPACE = "easya11y";
    
    private final TemplateDefinitionRegistry templateRegistry;
    
    @Inject
    public PageListEndpoint(EndpointDefinition definition, TemplateDefinitionRegistry templateRegistry) {
        super(definition);
        this.templateRegistry = templateRegistry;
    }

    /**
     * Get a list of pages from the website workspace for accessibility scanning.
     *
     * @param path Optional path to filter nodes (defaults to root)
     * @param includeStatus Whether to include scan status for each page
     * @return HTTP response with page list
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listPages(
            @QueryParam("path") String path,
            @QueryParam("includeStatus") boolean includeStatus) {
        try {
            String nodePath = StringUtils.isNotBlank(path) ? path : "/";
            log.info("Listing pages at path: {} with status: {}", nodePath, includeStatus);
            
            Session websiteSession = MgnlContext.getJCRSession(WEBSITE_WORKSPACE);
            Session scanSession = includeStatus ? MgnlContext.getJCRSession(SCAN_RESULTS_WORKSPACE) : null;
            
            List<Map<String, Object>> results = new ArrayList<>();
            
            if (websiteSession.nodeExists(nodePath)) {
                Node rootNode = websiteSession.getNode(nodePath);

                // Special handling for root path to ensure first-level pages are collected
                if ("/".equals(nodePath)) {
                    // For root, iterate direct children and collect pages from each
                    NodeIterator children = rootNode.getNodes();
                    while (children.hasNext()) {
                        Node child = children.nextNode();
                        String childName = child.getName();
                        // Skip system nodes
                        if (!childName.startsWith("jcr:") && !childName.startsWith("mgnl:")) {
                            collectPages(child, results, scanSession);
                        }
                    }
                } else {
                    // For non-root paths, use existing behavior
                    collectPages(rootNode, results, scanSession);
                }
            } else {
                log.warn("Path not found: {}", nodePath);
                Map<String, Object> errorInfo = new HashMap<>();
                errorInfo.put("message", "Path not found: " + nodePath);
                results.add(errorInfo);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("path", nodePath);
            result.put("totalPages", results.size());
            result.put("items", results);
            
            return Response.ok(result).build();
            
        } catch (RepositoryException e) {
            log.error("Error listing pages", e);
            return buildErrorResponse("Error listing pages: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Check if a page is a headless page (serves API/JSON responses instead of HTML).
     */
    private boolean isHeadlessPage(Node pageNode) {
        try {
            if (!pageNode.hasProperty("mgnl:template")) {
                return false;
            }
            String templateId = PropertyUtil.getString(pageNode, "mgnl:template");
            DefinitionProvider<TemplateDefinition> provider = templateRegistry.getProvider(templateId);
            if (provider != null && provider.get() != null) {
                TemplateDefinition template = provider.get();
                String subtype = template.getSubtype();
                if (subtype != null) {
                    String subtypeLower = subtype.toLowerCase();
                    if (subtypeLower.contains("headless") || subtypeLower.contains("json") ||
                        subtypeLower.contains("api") || subtypeLower.contains("rest")) {
                        return true;
                    }
                }
                // Also check render type
                String renderType = template.getRenderType();
                if (renderType != null && renderType.toLowerCase().contains("json")) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.debug("Could not determine if page is headless: {}", pageNode, e);
        }
        return false;
    }

    /**
     * Recursively collect page nodes.
     *
     * @param node The starting node
     * @param results The list to populate with page data
     * @param scanSession Optional session to check scan status
     * @throws RepositoryException if there's an error accessing the repository
     */
    private void collectPages(Node node, List<Map<String, Object>> results, Session scanSession) throws RepositoryException {
        log.debug("Collecting pages from node: {} (type: {})", node.getPath(), node.getPrimaryNodeType().getName());

        // Check if this node is a page
        if (NodeUtil.isNodeType(node, "mgnl:page")) {
            Map<String, Object> pageInfo = new HashMap<>();

            pageInfo.put("name", node.getName());
            pageInfo.put("path", node.getPath());
            pageInfo.put("uuid", node.getIdentifier());
            pageInfo.put("type", node.getPrimaryNodeType().getName());
            
            // Get page title
            if (node.hasProperty("title")) {
                pageInfo.put("title", PropertyUtil.getString(node, "title"));
            } else {
                pageInfo.put("title", node.getName());
            }
            
            // Get template information
            if (node.hasProperty("mgnl:template")) {
                String templateId = PropertyUtil.getString(node, "mgnl:template");
                pageInfo.put("template", templateId);
                try {
                    DefinitionProvider<TemplateDefinition> provider = templateRegistry.getProvider(templateId);
                    if (provider != null && provider.get() != null) {
                        pageInfo.put("templateTitle", provider.get().getTitle());
                    }
                } catch (Registry.NoSuchDefinitionException e) {
                    log.debug("Template not found: {}", templateId);
                }
            }

            // Get modification date
            if (node.hasProperty("mgnl:lastModified")) {
                pageInfo.put("lastModified", PropertyUtil.getDate(node, "mgnl:lastModified").getTime());
            }

            // Check if this is a headless page
            boolean isHeadless = isHeadlessPage(node);
            pageInfo.put("isHeadless", isHeadless);

            // Construct page URL
            String contextPath = MgnlContext.getContextPath();
            String pagePath = node.getPath();
            if (isHeadless) {
                pageInfo.put("url", contextPath + pagePath);  // No .html for headless
            } else {
                pageInfo.put("url", contextPath + pagePath + ".html");
            }
            
            // Check scan status if requested
            if (scanSession != null) {
                pageInfo.put("scanStatus", getScanStatus(node.getPath(), scanSession));
            }
            
            results.add(pageInfo);
        }
        
        // Recursively check child nodes
        NodeIterator children = node.getNodes();
        while (children.hasNext()) {
            Node child = children.nextNode();
            // Skip system nodes and areas
            if (!child.getName().startsWith("jcr:") && 
                !child.getName().startsWith("mgnl:") &&
                !NodeUtil.isNodeType(child, "mgnl:area") &&
                !NodeUtil.isNodeType(child, "mgnl:component")) {
                collectPages(child, results, scanSession);
            }
        }
    }
    
    /**
     * Get scan status for a page.
     *
     * @param pagePath The page path
     * @param scanSession Session for scan results
     * @return Scan status information
     */
    private Map<String, Object> getScanStatus(String pagePath, Session scanSession) {
        Map<String, Object> status = new HashMap<>();
        status.put("scanned", false);
        
        try {
            // Convert page path to scan result path
            String scanPath = "/scanResults" + pagePath;
            
            if (scanSession.nodeExists(scanPath)) {
                Node scanNode = scanSession.getNode(scanPath);
                status.put("scanned", true);
                
                if (scanNode.hasProperty("scanDate")) {
                    status.put("lastScanDate", PropertyUtil.getDate(scanNode, "scanDate").getTime());
                }
                
                if (scanNode.hasProperty("violationCount")) {
                    status.put("violationCount", PropertyUtil.getLong(scanNode, "violationCount"));
                }
                
                if (scanNode.hasProperty("score")) {
                    status.put("score", scanNode.getProperty("score").getDouble());
                }
                
                if (scanNode.hasProperty("wcagLevel")) {
                    status.put("wcagLevel", PropertyUtil.getString(scanNode, "wcagLevel"));
                }
            }
        } catch (RepositoryException e) {
            log.debug("Error getting scan status for path: {}", pagePath, e);
        }
        
        return status;
    }
    
    /**
     * Build an error response.
     *
     * @param message The error message
     * @param status The HTTP status
     * @return HTTP response
     */
    private Response buildErrorResponse(String message, Response.Status status) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        
        return Response.status(status).entity(result).build();
    }
}