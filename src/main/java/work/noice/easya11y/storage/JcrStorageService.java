package work.noice.easya11y.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import work.noice.easya11y.models.AccessibilityScanResult;

import javax.inject.Singleton;
import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JCR-based implementation of StorageService.
 * Stores accessibility scan results in a custom JCR workspace.
 */
@Singleton
public class JcrStorageService implements StorageService {
    
    private static final Logger log = LoggerFactory.getLogger(JcrStorageService.class);
    private static final String SCAN_RESULTS_WORKSPACE = "easya11y";
    private static final String SCAN_RESULTS_PATH = "/scanResults";
    private static final String CONFIG_PATH = "/configuration";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String storeScanResult(AccessibilityScanResult scanResult, String pagePath) {
        try {
            Session session = MgnlContext.getJCRSession(SCAN_RESULTS_WORKSPACE);
            String scanResultPath = SCAN_RESULTS_PATH + pagePath;
            
            // Create parent nodes if needed
            ensureParentNodes(session, scanResultPath);
            
            // Create or update scan result node
            Node scanNode;
            if (session.nodeExists(scanResultPath)) {
                scanNode = session.getNode(scanResultPath);
            } else {
                Node parentNode = session.getNode(scanResultPath.substring(0, scanResultPath.lastIndexOf('/')));
                scanNode = parentNode.addNode(scanResultPath.substring(scanResultPath.lastIndexOf('/') + 1), "mgnl:content");
            }
            
            // Store scan data
            scanNode.setProperty("scanId", scanResult.getId());
            scanNode.setProperty("pageUrl", scanResult.getPageUrl());
            scanNode.setProperty("pageTitle", scanResult.getPageTitle());
            scanNode.setProperty("scanDate", new Date().getTime());
            scanNode.setProperty("wcagLevel", scanResult.getWcagLevel());
            scanNode.setProperty("score", scanResult.getScore());
            scanNode.setProperty("violationCount", scanResult.getViolations().size());
            scanNode.setProperty("passCount", scanResult.getPasses().size());
            scanNode.setProperty("totalElements", scanResult.getTotalElements());
            scanNode.setProperty("elementsWithIssues", scanResult.getElementsWithIssues());
            
            // Store violations summary
            for (Map.Entry<String, Integer> entry : scanResult.getViolationsByImpact().entrySet()) {
                scanNode.setProperty("violations_" + entry.getKey(), entry.getValue());
            }
            
            // Store detailed results as JSON
            scanNode.setProperty("fullResults", objectMapper.writeValueAsString(scanResult));
            
            session.save();
            
            return scanResult.getId();
        } catch (Exception e) {
            log.error("Error storing scan result", e);
            throw new RuntimeException("Failed to store scan result", e);
        }
    }
    
    @Override
    public List<AccessibilityScanResult> getScanResultsForPage(String pagePath, int limit) {
        try {
            Session session = MgnlContext.getJCRSession(SCAN_RESULTS_WORKSPACE);
            String scanResultPath = SCAN_RESULTS_PATH + pagePath;
            
            if (!session.nodeExists(scanResultPath)) {
                return Collections.emptyList();
            }
            
            Node scanNode = session.getNode(scanResultPath);
            String fullResults = PropertyUtil.getString(scanNode, "fullResults");
            
            if (fullResults != null) {
                AccessibilityScanResult result = objectMapper.readValue(fullResults, AccessibilityScanResult.class);
                return Collections.singletonList(result);
            }
            
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error retrieving scan results for page: " + pagePath, e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<AccessibilityScanResult> getAllScanResults(Map<String, Object> filterParams, int offset, int limit) {
        try {
            Session session = MgnlContext.getJCRSession(SCAN_RESULTS_WORKSPACE);
            QueryManager qm = session.getWorkspace().getQueryManager();
            
            // Build JCR SQL2 query
            StringBuilder queryStr = new StringBuilder("SELECT * FROM [mgnl:content] WHERE ISDESCENDANTNODE([/scanResults])");
            
            // Add filters
            if (filterParams != null) {
                if (filterParams.containsKey("minScore")) {
                    queryStr.append(" AND [score] >= ").append(filterParams.get("minScore"));
                }
                if (filterParams.containsKey("wcagLevel")) {
                    queryStr.append(" AND [wcagLevel] = '").append(filterParams.get("wcagLevel")).append("'");
                }
                // Date filtering would require timestamp comparison
            }
            
            queryStr.append(" ORDER BY [scanDate] DESC");
            
            Query query = qm.createQuery(queryStr.toString(), Query.JCR_SQL2);
            
            if (limit > 0) {
                query.setLimit(limit);
            }
            if (offset > 0) {
                query.setOffset(offset);
            }
            
            QueryResult queryResult = query.execute();
            NodeIterator nodes = queryResult.getNodes();
            
            List<AccessibilityScanResult> results = new ArrayList<>();
            while (nodes.hasNext()) {
                Node node = nodes.nextNode();
                String fullResults = PropertyUtil.getString(node, "fullResults");
                if (fullResults != null) {
                    AccessibilityScanResult result = objectMapper.readValue(fullResults, AccessibilityScanResult.class);
                    results.add(result);
                }
            }
            
            return results;
        } catch (Exception e) {
            log.error("Error retrieving all scan results", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public Optional<AccessibilityScanResult> getScanResultById(String scanId) {
        try {
            Session session = MgnlContext.getJCRSession(SCAN_RESULTS_WORKSPACE);
            QueryManager qm = session.getWorkspace().getQueryManager();
            
            String queryStr = "SELECT * FROM [mgnl:content] WHERE [scanId] = '" + scanId + "'";
            Query query = qm.createQuery(queryStr, Query.JCR_SQL2);
            query.setLimit(1);
            
            QueryResult queryResult = query.execute();
            NodeIterator nodes = queryResult.getNodes();
            
            if (nodes.hasNext()) {
                Node node = nodes.nextNode();
                String fullResults = PropertyUtil.getString(node, "fullResults");
                if (fullResults != null) {
                    AccessibilityScanResult result = objectMapper.readValue(fullResults, AccessibilityScanResult.class);
                    return Optional.of(result);
                }
            }
            
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error retrieving scan result by ID: " + scanId, e);
            return Optional.empty();
        }
    }
    
    @Override
    public int deleteOldScanResults(int daysToKeep) {
        // JCR implementation typically doesn't delete old results
        // This could be implemented if needed
        log.info("Delete old scan results not implemented for JCR storage");
        return 0;
    }
    
    @Override
    public Map<String, Object> getScanStatistics() {
        try {
            Session session = MgnlContext.getJCRSession(SCAN_RESULTS_WORKSPACE);
            QueryManager qm = session.getWorkspace().getQueryManager();
            
            String queryStr = "SELECT * FROM [mgnl:content] WHERE ISDESCENDANTNODE([/scanResults])";
            Query query = qm.createQuery(queryStr, Query.JCR_SQL2);
            
            QueryResult queryResult = query.execute();
            NodeIterator nodes = queryResult.getNodes();
            
            int totalScans = 0;
            double totalScore = 0;
            int totalViolations = 0;
            int totalCritical = 0;
            int totalSerious = 0;
            
            while (nodes.hasNext()) {
                Node node = nodes.nextNode();
                totalScans++;
                // PropertyUtil doesn't have getDouble, so get as string and parse
                String scoreStr = PropertyUtil.getString(node, "score", "0");
                totalScore += Double.parseDouble(scoreStr);
                totalViolations += PropertyUtil.getLong(node, "violationCount", 0L);
                totalCritical += PropertyUtil.getLong(node, "violations_critical", 0L);
                totalSerious += PropertyUtil.getLong(node, "violations_serious", 0L);
            }
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalScans", totalScans);
            stats.put("averageScore", totalScans > 0 ? totalScore / totalScans : 0);
            stats.put("totalViolations", totalViolations);
            stats.put("totalCritical", totalCritical);
            stats.put("totalSerious", totalSerious);
            
            return stats;
        } catch (Exception e) {
            log.error("Error calculating scan statistics", e);
            return Collections.emptyMap();
        }
    }
    
    @Override
    public List<Map<String, Object>> getHistoricalTrends(String pagePath, int days) {
        // JCR doesn't have built-in aggregation like SQL
        // This would need to be implemented by fetching all results and processing in Java
        log.info("Historical trends not optimized for JCR storage");
        return Collections.emptyList();
    }
    
    @Override
    public void storeConfiguration(String key, String value) {
        try {
            Session session = MgnlContext.getJCRSession(SCAN_RESULTS_WORKSPACE);
            
            Node configNode;
            if (!session.nodeExists(CONFIG_PATH)) {
                configNode = session.getRootNode().addNode("configuration", "mgnl:content");
            } else {
                configNode = session.getNode(CONFIG_PATH);
            }
            
            configNode.setProperty(key, value);
            session.save();
        } catch (Exception e) {
            log.error("Error storing configuration: " + key, e);
            throw new RuntimeException("Failed to store configuration", e);
        }
    }
    
    @Override
    public Optional<String> getConfiguration(String key) {
        try {
            Session session = MgnlContext.getJCRSession(SCAN_RESULTS_WORKSPACE);
            
            if (!session.nodeExists(CONFIG_PATH)) {
                return Optional.empty();
            }
            
            Node configNode = session.getNode(CONFIG_PATH);
            if (configNode.hasProperty(key)) {
                return Optional.of(configNode.getProperty(key).getString());
            }
            
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error retrieving configuration: " + key, e);
            return Optional.empty();
        }
    }
    
    @Override
    public Map<String, String> getAllConfiguration() {
        try {
            Session session = MgnlContext.getJCRSession(SCAN_RESULTS_WORKSPACE);
            
            if (!session.nodeExists(CONFIG_PATH)) {
                return Collections.emptyMap();
            }
            
            Node configNode = session.getNode(CONFIG_PATH);
            Map<String, String> config = new HashMap<>();
            
            PropertyIterator props = configNode.getProperties();
            while (props.hasNext()) {
                Property prop = props.nextProperty();
                if (!prop.getName().startsWith("jcr:") && !prop.getName().startsWith("mgnl:")) {
                    config.put(prop.getName(), prop.getString());
                }
            }
            
            return config;
        } catch (Exception e) {
            log.error("Error retrieving all configuration", e);
            return Collections.emptyMap();
        }
    }
    
    @Override
    public boolean testConnection() {
        try {
            Session session = MgnlContext.getJCRSession(SCAN_RESULTS_WORKSPACE);
            return session != null && session.isLive();
        } catch (Exception e) {
            log.error("JCR connection test failed", e);
            return false;
        }
    }
    
    @Override
    public String getStorageType() {
        return "jcr";
    }
    
    private void ensureParentNodes(Session session, String path) throws RepositoryException {
        String[] parts = path.split("/");
        String currentPath = "";
        
        for (int i = 1; i < parts.length - 1; i++) {
            currentPath += "/" + parts[i];
            if (!session.nodeExists(currentPath)) {
                Node parentNode = session.getNode(currentPath.substring(0, currentPath.lastIndexOf('/')));
                parentNode.addNode(parts[i], "mgnl:content");
            }
        }
    }
}