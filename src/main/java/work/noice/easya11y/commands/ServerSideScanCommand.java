package work.noice.easya11y.commands;

import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import work.noice.easya11y.services.ServerSideAccessibilityScanner;
import work.noice.easya11y.endpoints.AccessibilityScanEndpoint;
import info.magnolia.objectfactory.Components;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Session;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Magnolia command for triggering server-side accessibility scans.
 * Can be used in scheduled jobs or triggered manually from AdminCentral.
 */
public class ServerSideScanCommand extends MgnlCommand {
    
    private static final Logger log = LoggerFactory.getLogger(ServerSideScanCommand.class);
    private static final String WEBSITE_WORKSPACE = "website";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private AccessibilityScanEndpoint scanEndpoint;
    
    // Command parameters
    private String pagePath;
    private String pagePattern;
    private String wcagLevel = "AA";
    private boolean sendEmail = true;
    private boolean sendDigest = true;
    private int maxPages = 50;
    
    @Inject
    public void setScanEndpoint(AccessibilityScanEndpoint scanEndpoint) {
        this.scanEndpoint = scanEndpoint;
    }
    
    @Override
    public boolean execute(Context context) throws Exception {
        log.info("Executing server-side accessibility scan command");
        
        // Get scan endpoint instance
        if (scanEndpoint == null) {
            scanEndpoint = Components.getComponent(AccessibilityScanEndpoint.class);
        }
        
        try {
            ServerSideAccessibilityScanner scanner = Components.getComponent(ServerSideAccessibilityScanner.class);
            
            if (pagePath != null && !pagePath.isEmpty()) {
                // Single page scan
                String pageUrl = buildPageUrl(pagePath);
                JsonNode results = scanner.scanUrl(pageUrl, wcagLevel);
                log.info("Single page scan completed for: {} with {} violations", 
                        pagePath, results.get("violations").size());
                
            } else if (pagePattern != null && !pagePattern.isEmpty()) {
                // Batch scan based on pattern
                List<String> pagePaths = findPagesByPattern(pagePattern);
                
                if (pagePaths.isEmpty()) {
                    log.warn("No pages found matching pattern: {}", pagePattern);
                    return false;
                }
                
                // Limit pages if needed
                if (pagePaths.size() > maxPages) {
                    log.info("Found {} pages, limiting to {}", pagePaths.size(), maxPages);
                    pagePaths = pagePaths.subList(0, maxPages);
                }
                
                Map<String, String> urlMap = new HashMap<>();
                for (String path : pagePaths) {
                    urlMap.put(path, buildPageUrl(path));
                }
                
                Map<String, JsonNode> results = scanner.scanUrls(urlMap, wcagLevel);
                log.info("Batch scan completed for {} pages", results.size());
                
            } else {
                // Scan all pages (limited by maxPages)
                List<String> allPages = findAllPages();
                
                if (allPages.isEmpty()) {
                    log.warn("No pages found to scan");
                    return false;
                }
                
                if (allPages.size() > maxPages) {
                    log.info("Found {} pages, limiting to {}", allPages.size(), maxPages);
                    allPages = allPages.subList(0, maxPages);
                }
                
                Map<String, String> urlMap = new HashMap<>();
                for (String path : allPages) {
                    urlMap.put(path, buildPageUrl(path));
                }
                
                Map<String, JsonNode> results = scanner.scanUrls(urlMap, wcagLevel);
                log.info("Batch scan completed for {} pages", results.size());
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("Error executing scan command", e);
            throw e;
        }
    }
    
    private String buildPageUrl(String pagePath) {
        // Build regular page URL - authentication will be handled by Selenium
        try {
            String contextPath = MgnlContext.getContextPath();
            // Context path contains the full base URL including scheme, host, and port
            return contextPath + pagePath + ".html";
        } catch (Exception e) {
            // Fallback
            return "http://localhost:8080/magnoliaAuthor" + pagePath + ".html";
        }
    }
    
    private List<String> findPagesByPattern(String pattern) throws Exception {
        List<String> pages = new ArrayList<>();
        Session session = MgnlContext.getJCRSession(WEBSITE_WORKSPACE);
        
        // Convert simple pattern to JCR query
        String query = "SELECT * FROM [mgnl:page] WHERE ISDESCENDANTNODE('/') ";
        
        if (pattern.contains("*")) {
            // Simple wildcard support
            String likePattern = pattern.replace("*", "%");
            query += "AND NAME() LIKE '" + likePattern + "'";
        } else {
            // Exact path or path prefix
            query += "AND (LOCALNAME() = '" + pattern + "' OR ISDESCENDANTNODE('" + pattern + "'))";
        }
        
        javax.jcr.query.Query jcrQuery = session.getWorkspace().getQueryManager()
            .createQuery(query, javax.jcr.query.Query.JCR_SQL2);
        
        javax.jcr.query.QueryResult result = jcrQuery.execute();
        javax.jcr.NodeIterator nodes = result.getNodes();
        
        while (nodes.hasNext()) {
            Node node = nodes.nextNode();
            pages.add(node.getPath());
        }
        
        return pages;
    }
    
    protected List<String> findAllPages() throws Exception {
        List<String> pages = new ArrayList<>();
        Session session = MgnlContext.getJCRSession(WEBSITE_WORKSPACE);
        
        // Find all pages
        String query = "SELECT * FROM [mgnl:page] WHERE ISDESCENDANTNODE('/')";
        javax.jcr.query.Query jcrQuery = session.getWorkspace().getQueryManager()
            .createQuery(query, javax.jcr.query.Query.JCR_SQL2);
        
        javax.jcr.query.QueryResult result = jcrQuery.execute();
        javax.jcr.NodeIterator nodes = result.getNodes();
        
        while (nodes.hasNext()) {
            Node node = nodes.nextNode();
            pages.add(node.getPath());
        }
        
        return pages;
    }
    
    // Getters and setters for command parameters
    public String getPagePath() {
        return pagePath;
    }
    
    public void setPagePath(String pagePath) {
        this.pagePath = pagePath;
    }
    
    public String getPagePattern() {
        return pagePattern;
    }
    
    public void setPagePattern(String pagePattern) {
        this.pagePattern = pagePattern;
    }
    
    public String getWcagLevel() {
        return wcagLevel;
    }
    
    public void setWcagLevel(String wcagLevel) {
        this.wcagLevel = wcagLevel;
    }
    
    public boolean isSendEmail() {
        return sendEmail;
    }
    
    public void setSendEmail(boolean sendEmail) {
        this.sendEmail = sendEmail;
    }
    
    public boolean isSendDigest() {
        return sendDigest;
    }
    
    public void setSendDigest(boolean sendDigest) {
        this.sendDigest = sendDigest;
    }
    
    public int getMaxPages() {
        return maxPages;
    }
    
    public void setMaxPages(int maxPages) {
        this.maxPages = maxPages;
    }
}