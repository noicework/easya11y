package work.noice.easya11y.models;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Model representing an accessibility scan result for a page.
 */
public class AccessibilityScanResult {
    
    private String id;
    private String pagePath;
    private String pageUrl;
    private String pageTitle;
    private Date scanDate;
    private String scannerVersion;
    private String wcagLevel;
    private double score;
    
    // Scan results
    private List<Violation> violations = new ArrayList<>();
    private List<Pass> passes = new ArrayList<>();
    private List<Incomplete> incomplete = new ArrayList<>();
    private List<Inapplicable> inapplicable = new ArrayList<>();
    
    // Summary statistics
    private Map<String, Integer> violationsByImpact = new HashMap<>();
    private Map<String, Integer> violationsByWcagLevel = new HashMap<>();
    private int totalElements;
    private int elementsWithIssues;
    
    // Constructor
    public AccessibilityScanResult() {
        this.scanDate = new Date();
        initializeStatsMaps();
    }
    
    public AccessibilityScanResult(String pagePath, String pageUrl) {
        this();
        this.pagePath = pagePath;
        this.pageUrl = pageUrl;
    }
    
    private void initializeStatsMaps() {
        // Initialize impact levels
        violationsByImpact.put("critical", 0);
        violationsByImpact.put("serious", 0);
        violationsByImpact.put("moderate", 0);
        violationsByImpact.put("minor", 0);
        
        // Initialize WCAG levels
        violationsByWcagLevel.put("A", 0);
        violationsByWcagLevel.put("AA", 0);
        violationsByWcagLevel.put("AAA", 0);
    }
    
    /**
     * Calculate accessibility score based on violations and their impact.
     * Score is 0-100, where 100 is perfect accessibility.
     */
    public void calculateScore() {
        if (totalElements == 0) {
            this.score = 100.0;
            return;
        }
        
        // Calculate weighted score based on violation impact
        double weightedViolations = 
            (violationsByImpact.get("critical") * 10.0) +
            (violationsByImpact.get("serious") * 5.0) +
            (violationsByImpact.get("moderate") * 2.0) +
            (violationsByImpact.get("minor") * 1.0);
        
        // Calculate score (higher violations = lower score)
        double violationRatio = weightedViolations / (totalElements * 10.0);
        this.score = Math.round(Math.max(0, 100.0 - (violationRatio * 100.0)));
    }
    
    /**
     * Inner class representing an accessibility violation.
     */
    public static class Violation {
        private String id;
        private String impact;
        private String description;
        private String help;
        private String helpUrl;
        private List<String> tags;
        private List<Node> nodes;
        
        public Violation() {
            this.tags = new ArrayList<>();
            this.nodes = new ArrayList<>();
        }
        
        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getImpact() { return impact; }
        public void setImpact(String impact) { this.impact = impact; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getHelp() { return help; }
        public void setHelp(String help) { this.help = help; }
        
        public String getHelpUrl() { return helpUrl; }
        public void setHelpUrl(String helpUrl) { this.helpUrl = helpUrl; }
        
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
        
        public List<Node> getNodes() { return nodes; }
        public void setNodes(List<Node> nodes) { this.nodes = nodes; }
    }
    
    /**
     * Inner class representing a passed check.
     */
    public static class Pass {
        private String id;
        private String description;
        private String help;
        private int nodeCount;
        
        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getHelp() { return help; }
        public void setHelp(String help) { this.help = help; }
        
        public int getNodeCount() { return nodeCount; }
        public void setNodeCount(int nodeCount) { this.nodeCount = nodeCount; }
    }
    
    /**
     * Inner class representing an incomplete check.
     */
    public static class Incomplete {
        private String id;
        private String description;
        private String help;
        private List<Node> nodes;
        
        public Incomplete() {
            this.nodes = new ArrayList<>();
        }
        
        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getHelp() { return help; }
        public void setHelp(String help) { this.help = help; }
        
        public List<Node> getNodes() { return nodes; }
        public void setNodes(List<Node> nodes) { this.nodes = nodes; }
    }
    
    /**
     * Inner class representing an inapplicable check.
     */
    public static class Inapplicable {
        private String id;
        private String description;
        
        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
    
    /**
     * Inner class representing a DOM node with issues.
     */
    public static class Node {
        private String target;
        private String html;
        private String failureSummary;
        private String xpath;
        private String impact;
        private List<Any> any;
        private List<All> all;
        private List<None> none;
        
        public Node() {
            this.any = new ArrayList<>();
            this.all = new ArrayList<>();
            this.none = new ArrayList<>();
        }
        
        // Getters and setters
        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }
        
        public String getHtml() { return html; }
        public void setHtml(String html) { this.html = html; }
        
        public String getFailureSummary() { return failureSummary; }
        public void setFailureSummary(String failureSummary) { this.failureSummary = failureSummary; }
        
        public String getXpath() { return xpath; }
        public void setXpath(String xpath) { this.xpath = xpath; }
        
        public String getImpact() { return impact; }
        public void setImpact(String impact) { this.impact = impact; }
        
        public List<Any> getAny() { return any; }
        public void setAny(List<Any> any) { this.any = any; }
        
        public List<All> getAll() { return all; }
        public void setAll(List<All> all) { this.all = all; }
        
        public List<None> getNone() { return none; }
        public void setNone(List<None> none) { this.none = none; }
    }
    
    /**
     * Inner class for check details.
     */
    public static class Any {
        private String id;
        private String message;
        private Object data;
        
        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }
    
    public static class All extends Any {}
    public static class None extends Any {}
    
    // Main class getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getPagePath() { return pagePath; }
    public void setPagePath(String pagePath) { this.pagePath = pagePath; }
    
    public String getPageUrl() { return pageUrl; }
    public void setPageUrl(String pageUrl) { this.pageUrl = pageUrl; }
    
    public String getPageTitle() { return pageTitle; }
    public void setPageTitle(String pageTitle) { this.pageTitle = pageTitle; }
    
    public Date getScanDate() { return scanDate; }
    public void setScanDate(Date scanDate) { this.scanDate = scanDate; }
    
    public String getScannerVersion() { return scannerVersion; }
    public void setScannerVersion(String scannerVersion) { this.scannerVersion = scannerVersion; }
    
    public String getWcagLevel() { return wcagLevel; }
    public void setWcagLevel(String wcagLevel) { this.wcagLevel = wcagLevel; }
    
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    
    public List<Violation> getViolations() { return violations; }
    public void setViolations(List<Violation> violations) { this.violations = violations; }
    
    public List<Pass> getPasses() { return passes; }
    public void setPasses(List<Pass> passes) { this.passes = passes; }
    
    public List<Incomplete> getIncomplete() { return incomplete; }
    public void setIncomplete(List<Incomplete> incomplete) { this.incomplete = incomplete; }
    
    public List<Inapplicable> getInapplicable() { return inapplicable; }
    public void setInapplicable(List<Inapplicable> inapplicable) { this.inapplicable = inapplicable; }
    
    public Map<String, Integer> getViolationsByImpact() { return violationsByImpact; }
    public void setViolationsByImpact(Map<String, Integer> violationsByImpact) { this.violationsByImpact = violationsByImpact; }
    
    public Map<String, Integer> getViolationsByWcagLevel() { return violationsByWcagLevel; }
    public void setViolationsByWcagLevel(Map<String, Integer> violationsByWcagLevel) { this.violationsByWcagLevel = violationsByWcagLevel; }
    
    public int getTotalElements() { return totalElements; }
    public void setTotalElements(int totalElements) { this.totalElements = totalElements; }
    
    public int getElementsWithIssues() { return elementsWithIssues; }
    public void setElementsWithIssues(int elementsWithIssues) { this.elementsWithIssues = elementsWithIssues; }
}