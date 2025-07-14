package work.noice.easya11y.jobs;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.PropertyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.noice.easya11y.services.ServerSideAccessibilityScanner;
import info.magnolia.objectfactory.Components;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Scheduled job for running automated accessibility scans.
 * This job is triggered by Magnolia's scheduler module based on the configured cron expression.
 */
public class ScheduledAccessibilityScanJob implements Job {
    
    private static final Logger log = LoggerFactory.getLogger(ScheduledAccessibilityScanJob.class);
    private static final String SCAN_RESULTS_WORKSPACE = "easya11y";
    private static final String CONFIG_NODE_PATH = "/configuration";
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Starting scheduled accessibility scan job");
        
        try {
            // Check if scheduled scanning is enabled
            if (!isScheduledScanningEnabled()) {
                log.info("Scheduled scanning is disabled in configuration");
                return;
            }
            
            // Get configuration from database
            Node configNode = getConfigurationNode();
            
            // Check if server-side scanning is enabled
            boolean useServerSideScan = PropertyUtil.getBoolean(configNode, "serverSideScan", true);
            if (!useServerSideScan) {
                log.info("Server-side scanning is disabled. Scheduled scans require server-side scanning to be enabled.");
                return;
            }
            
            // Get job parameters from configuration
            String wcagLevel = PropertyUtil.getString(configNode, "wcagLevel", "AA");
            String scanPaths = PropertyUtil.getString(configNode, "scanPaths", "");
            String excludePaths = PropertyUtil.getString(configNode, "excludePaths", "");
            boolean sendEmail = PropertyUtil.getBoolean(configNode, "emailEnabled", false);
            boolean sendDigest = PropertyUtil.getBoolean(configNode, "emailDigest", true);
            
            // Parse scan and exclude paths
            List<String> scanPathList = scanPaths.isEmpty() ? Collections.emptyList() :
                Arrays.stream(scanPaths.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            
            List<String> excludePathList = excludePaths.isEmpty() ? Collections.emptyList() :
                Arrays.stream(excludePaths.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            
            // Get pages to scan with filtering
            List<String> pagesToScan = findPagesToScan(scanPathList, excludePathList);
            
            if (pagesToScan.isEmpty()) {
                log.warn("No pages found to scan after applying filters");
                return;
            }
            
            log.info("Found {} pages to scan after filtering", pagesToScan.size());
            
            // Use ServerSideAccessibilityScanner directly for batch scanning
            ServerSideAccessibilityScanner scanner = Components.getComponent(ServerSideAccessibilityScanner.class);
            
            // Build URL map
            Map<String, String> urlMap = new HashMap<>();
            for (String pagePath : pagesToScan) {
                urlMap.put(pagePath, buildPageUrl(pagePath));
            }
            
            // Execute batch scan
            Map<String, JsonNode> results = scanner.scanUrls(urlMap, wcagLevel);
            
            // Store results using the scan endpoint
            ObjectMapper objectMapper = new ObjectMapper();
            for (Map.Entry<String, JsonNode> entry : results.entrySet()) {
                String pagePath = entry.getKey();
                JsonNode axeResults = entry.getValue();
                
                if (!axeResults.has("error")) {
                    // Prepare scan data
                    Map<String, Object> scanData = new HashMap<>();
                    scanData.put("scanId", UUID.randomUUID().toString());
                    scanData.put("pagePath", pagePath);
                    scanData.put("pageUrl", urlMap.get(pagePath));
                    scanData.put("pageTitle", getPageTitle(pagePath));
                    scanData.put("wcagLevel", wcagLevel);
                    scanData.put("score", calculateScore(axeResults));
                    scanData.put("axeResults", axeResults);
                    
                    // Store results
                    storeScanResults(scanData);
                }
            }
            
            log.info("Scheduled accessibility scan completed successfully for {} pages", results.size());
            
            // Send email notifications if enabled
            if (sendEmail) {
                // TODO: Implement email notification
                log.info("Email notifications would be sent here");
            }
            
        } catch (Exception e) {
            log.error("Error during scheduled accessibility scan", e);
            throw new JobExecutionException("Error during scheduled scan: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if scheduled scanning is enabled in the configuration.
     */
    private boolean isScheduledScanningEnabled() {
        try {
            Session session = MgnlContext.getJCRSession(SCAN_RESULTS_WORKSPACE);
            if (session.nodeExists(CONFIG_NODE_PATH)) {
                Node configNode = session.getNode(CONFIG_NODE_PATH);
                return PropertyUtil.getBoolean(configNode, "scanScheduleEnabled", false);
            }
        } catch (RepositoryException e) {
            log.error("Error checking scheduled scanning configuration", e);
        }
        return false;
    }
    
    /**
     * Get the configuration node.
     */
    private Node getConfigurationNode() throws RepositoryException {
        Session session = MgnlContext.getJCRSession(SCAN_RESULTS_WORKSPACE);
        if (session.nodeExists(CONFIG_NODE_PATH)) {
            return session.getNode(CONFIG_NODE_PATH);
        }
        throw new RepositoryException("Configuration node not found");
    }
    
    /**
     * Find pages to scan with filtering.
     */
    private List<String> findPagesToScan(List<String> scanPaths, List<String> excludePaths) throws Exception {
        List<String> pages = new ArrayList<>();
        Session session = MgnlContext.getJCRSession("website");
        
        // Find all pages
        String query = "SELECT * FROM [mgnl:page] WHERE ISDESCENDANTNODE('/')";
        javax.jcr.query.Query jcrQuery = session.getWorkspace().getQueryManager()
            .createQuery(query, javax.jcr.query.Query.JCR_SQL2);
        
        javax.jcr.query.QueryResult result = jcrQuery.execute();
        NodeIterator nodes = result.getNodes();
        
        while (nodes.hasNext()) {
            Node node = nodes.nextNode();
            String path = node.getPath();
            
            // Apply scan path filter
            if (!scanPaths.isEmpty()) {
                boolean shouldInclude = scanPaths.stream()
                    .anyMatch(scanPath -> path.equals(scanPath) || path.startsWith(scanPath + "/"));
                if (!shouldInclude) {
                    continue;
                }
            }
            
            // Apply exclude path filter
            if (!excludePaths.isEmpty()) {
                boolean shouldExclude = excludePaths.stream()
                    .anyMatch(excludePath -> path.equals(excludePath) || path.startsWith(excludePath + "/"));
                if (shouldExclude) {
                    continue;
                }
            }
            
            pages.add(path);
        }
        
        return pages;
    }
    
    /**
     * Build page URL.
     */
    private String buildPageUrl(String pagePath) {
        // Build regular page URL - authentication will be handled by Selenium
        String contextPath = MgnlContext.getContextPath();
        // Context path contains the full base URL including scheme, host, and port
        return contextPath + pagePath + ".html";
    }
    
    /**
     * Get page title.
     */
    private String getPageTitle(String pagePath) {
        try {
            Session session = MgnlContext.getJCRSession("website");
            if (session.nodeExists(pagePath)) {
                Node pageNode = session.getNode(pagePath);
                return PropertyUtil.getString(pageNode, "title", 
                    pagePath.substring(pagePath.lastIndexOf('/') + 1));
            }
        } catch (Exception e) {
            log.warn("Could not get page title for: {}", pagePath);
        }
        return pagePath.substring(pagePath.lastIndexOf('/') + 1);
    }
    
    /**
     * Calculate accessibility score.
     */
    private double calculateScore(JsonNode axeResults) {
        int totalViolations = 0;
        double weightedViolations = 0.0;
        
        if (axeResults.has("violations")) {
            JsonNode violations = axeResults.get("violations");
            for (JsonNode violation : violations) {
                String impact = violation.get("impact").asText();
                int nodeCount = violation.get("nodes").size();
                totalViolations += nodeCount;
                
                switch (impact) {
                    case "critical":
                        weightedViolations += nodeCount * 10;
                        break;
                    case "serious":
                        weightedViolations += nodeCount * 5;
                        break;
                    case "moderate":
                        weightedViolations += nodeCount * 2;
                        break;
                    case "minor":
                        weightedViolations += nodeCount * 1;
                        break;
                }
            }
        }
        
        // Calculate total elements tested
        int totalElements = 0;
        if (axeResults.has("passes")) {
            JsonNode passes = axeResults.get("passes");
            for (JsonNode pass : passes) {
                totalElements += pass.get("nodes").size();
            }
        }
        totalElements += totalViolations;
        
        if (totalElements == 0) {
            return 100.0;
        }
        
        double score = 100.0 - (weightedViolations / totalElements * 100.0);
        return Math.max(0, Math.min(100, score));
    }
    
    /**
     * Store scan results.
     */
    private void storeScanResults(Map<String, Object> scanData) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode scanResults = objectMapper.valueToTree(scanData);
            
            // Use the AccessibilityScanEndpoint to store results
            work.noice.easya11y.endpoints.AccessibilityScanEndpoint endpoint = 
                Components.getComponent(work.noice.easya11y.endpoints.AccessibilityScanEndpoint.class);
            endpoint.storeScanResults(scanResults);
            
        } catch (Exception e) {
            log.error("Error storing scan results for page: {}", scanData.get("pagePath"), e);
        }
    }
}