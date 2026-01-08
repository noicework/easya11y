package work.noice.easya11y.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import work.noice.easya11y.models.AccessibilityScanResult;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.sql.*;
import java.util.*;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database-based implementation of StorageService.
 * Stores accessibility scan results in MySQL or PostgreSQL.
 */
@Singleton
public class DatabaseStorageService implements StorageService {
    
    private static final Logger log = LoggerFactory.getLogger(DatabaseStorageService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final DatabaseConnectionManager connectionManager;
    
    @Inject
    public DatabaseStorageService(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
    
    @Override
    public String storeScanResult(AccessibilityScanResult scanResult, String pagePath) {
        // Generate a new unique ID if duplicate exists
        String scanId = scanResult.getId();
        try (Connection conn = connectionManager.getConnection()) {
            // Check if scan ID already exists
            String checkSql = "SELECT COUNT(*) FROM audit_scans WHERE scan_id = ?";
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setString(1, scanId);
                ResultSet rs = checkPs.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    // Generate new ID if duplicate found
                    scanId = UUID.randomUUID().toString();
                    log.warn("Duplicate scan ID found, generating new ID: {}", scanId);
                }
            }
        } catch (SQLException e) {
            log.error("Error checking for duplicate scan ID", e);
        }
        
        String sql = "INSERT INTO audit_scans (scan_id, page_url, page_title, page_path, scan_date, " +
                    "wcag_version, wcag_level, score, violation_count, pass_count, incomplete_count, " +
                    "inapplicable_count, total_elements, elements_with_issues, violations_critical, " +
                    "violations_serious, violations_moderate, violations_minor, scan_type, browser_info) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, scanId);
            ps.setString(2, scanResult.getPageUrl());
            ps.setString(3, scanResult.getPageTitle());
            ps.setString(4, pagePath);
            ps.setTimestamp(5, new Timestamp(new Date().getTime()));
            ps.setString(6, scanResult.getWcagVersion());
            ps.setString(7, scanResult.getWcagLevel());
            ps.setDouble(8, scanResult.getScore());
            ps.setInt(9, scanResult.getViolations().size());
            ps.setInt(10, scanResult.getPasses().size());
            ps.setInt(11, scanResult.getIncomplete() != null ? scanResult.getIncomplete().size() : 0);
            ps.setInt(12, scanResult.getInapplicable() != null ? scanResult.getInapplicable().size() : 0);
            ps.setInt(13, scanResult.getTotalElements());
            ps.setInt(14, scanResult.getElementsWithIssues());
            ps.setInt(15, scanResult.getViolationsByImpact().getOrDefault("critical", 0));
            ps.setInt(16, scanResult.getViolationsByImpact().getOrDefault("serious", 0));
            ps.setInt(17, scanResult.getViolationsByImpact().getOrDefault("moderate", 0));
            ps.setInt(18, scanResult.getViolationsByImpact().getOrDefault("minor", 0));
            ps.setString(19, scanResult.getScanType());
            ps.setString(20, scanResult.getScannerVersion());
            
            ps.executeUpdate();
            
            // Store violations
            storeViolations(conn, scanId, scanResult.getViolations());
            
            // Store passes (simplified - not storing all nodes)
            storePasses(conn, scanId, scanResult.getPasses());
            
            // Store incomplete checks
            if (scanResult.getIncomplete() != null) {
                storeIncomplete(conn, scanId, scanResult.getIncomplete());
            }
            
            return scanId;
            
        } catch (SQLException e) {
            log.error("Error storing scan result", e);
            throw new RuntimeException("Failed to store scan result", e);
        }
    }
    
    private void storeViolations(Connection conn, String scanId, List<AccessibilityScanResult.Violation> violations) throws SQLException {
        String sql = "INSERT INTO audit_violations (scan_id, violation_id, description, help, help_url, impact, tags) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        String nodeSql = "INSERT INTO audit_violation_nodes (violation_id, html, target, failure_summary, element_selector) " +
                        "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement nodePs = conn.prepareStatement(nodeSql)) {
            
            for (AccessibilityScanResult.Violation violation : violations) {
                ps.setString(1, scanId);
                ps.setString(2, violation.getId());
                ps.setString(3, violation.getDescription());
                ps.setString(4, violation.getHelp());
                ps.setString(5, violation.getHelpUrl());
                ps.setString(6, violation.getImpact());
                try {
                    ps.setString(7, objectMapper.writeValueAsString(violation.getTags()));
                } catch (Exception e) {
                    ps.setString(7, "[]");
                }
                
                ps.executeUpdate();
                
                // Get generated ID
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    long violationId = rs.getLong(1);
                    
                    // Store nodes
                    if (violation.getNodes() != null) {
                        for (AccessibilityScanResult.Node node : violation.getNodes()) {
                            nodePs.setLong(1, violationId);
                            nodePs.setString(2, node.getHtml());
                            // Convert target to JSON array format
                            String targetJson = "[\"" + node.getTarget().replace("\"", "\\\"") + "\"]";
                            nodePs.setString(3, targetJson);
                            nodePs.setString(4, node.getFailureSummary());
                            nodePs.setString(5, node.getTarget());
                            nodePs.addBatch();
                        }
                    }
                }
            }
            
            nodePs.executeBatch();
        }
    }
    
    private void storePasses(Connection conn, String scanId, List<AccessibilityScanResult.Pass> passes) throws SQLException {
        String sql = "INSERT INTO audit_passes (scan_id, pass_id, description, help, help_url, impact) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (AccessibilityScanResult.Pass pass : passes) {
                ps.setString(1, scanId);
                ps.setString(2, pass.getId());
                ps.setString(3, pass.getDescription());
                ps.setString(4, pass.getHelp());
                ps.setString(5, pass.getHelpUrl());
                ps.setString(6, pass.getImpact());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
    
    private void storeIncomplete(Connection conn, String scanId, List<AccessibilityScanResult.Incomplete> incompletes) throws SQLException {
        String sql = "INSERT INTO audit_incomplete (scan_id, incomplete_id, description, help, help_url, impact) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (AccessibilityScanResult.Incomplete incomplete : incompletes) {
                ps.setString(1, scanId);
                ps.setString(2, incomplete.getId());
                ps.setString(3, incomplete.getDescription());
                ps.setString(4, incomplete.getHelp());
                ps.setString(5, incomplete.getHelpUrl());
                ps.setString(6, incomplete.getImpact());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
    
    @Override
    public List<AccessibilityScanResult> getScanResultsForPage(String pagePath, int limit) {
        String sql = "SELECT * FROM audit_scans WHERE page_path = ? ORDER BY scan_date DESC LIMIT ?";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, pagePath);
            ps.setInt(2, limit > 0 ? limit : 10);
            
            ResultSet rs = ps.executeQuery();
            List<AccessibilityScanResult> results = new ArrayList<>();
            
            while (rs.next()) {
                results.add(mapResultSetToScanResult(rs, conn));
            }
            
            return results;
            
        } catch (SQLException e) {
            log.error("Error retrieving scan results for page: " + pagePath, e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<AccessibilityScanResult> getAllScanResults(Map<String, Object> filterParams, int offset, int limit) {
        StringBuilder sql = new StringBuilder("SELECT * FROM audit_scans WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        // Add filters
        if (filterParams != null) {
            if (filterParams.containsKey("minScore")) {
                sql.append(" AND score >= ?");
                params.add(filterParams.get("minScore"));
            }
            if (filterParams.containsKey("wcagLevel")) {
                sql.append(" AND wcag_level = ?");
                params.add(filterParams.get("wcagLevel"));
            }
            if (filterParams.containsKey("startDate")) {
                sql.append(" AND scan_date >= ?");
                params.add(filterParams.get("startDate"));
            }
            if (filterParams.containsKey("endDate")) {
                sql.append(" AND scan_date <= ?");
                params.add(filterParams.get("endDate"));
            }
            if (filterParams.containsKey("pagePath")) {
                sql.append(" AND page_path LIKE ?");
                params.add("%" + filterParams.get("pagePath") + "%");
            }
        }
        
        sql.append(" ORDER BY scan_date DESC LIMIT ? OFFSET ?");
        params.add(limit > 0 ? limit : 50);
        params.add(offset);
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = ps.executeQuery();
            List<AccessibilityScanResult> results = new ArrayList<>();
            
            while (rs.next()) {
                results.add(mapResultSetToScanResult(rs, conn));
            }
            
            return results;
            
        } catch (SQLException e) {
            log.error("Error retrieving all scan results", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public Optional<AccessibilityScanResult> getScanResultById(String scanId) {
        String sql = "SELECT * FROM audit_scans WHERE scan_id = ?";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, scanId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToScanResult(rs, conn));
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            log.error("Error retrieving scan result by ID: " + scanId, e);
            return Optional.empty();
        }
    }
    
    @Override
    public int deleteOldScanResults(int daysToKeep) {
        String sql = "DELETE FROM audit_scans WHERE scan_date < ?";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -daysToKeep);
            ps.setTimestamp(1, new Timestamp(cal.getTimeInMillis()));
            
            return ps.executeUpdate();
            
        } catch (SQLException e) {
            log.error("Error deleting old scan results", e);
            return 0;
        }
    }
    
    @Override
    public Map<String, Object> getScanStatistics() {
        String sql = "SELECT COUNT(*) as total_scans, AVG(score) as avg_score, " +
                    "MIN(score) as min_score, MAX(score) as max_score, " +
                    "SUM(violation_count) as total_violations, " +
                    "SUM(violations_critical) as total_critical, " +
                    "SUM(violations_serious) as total_serious " +
                    "FROM audit_scans";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ResultSet rs = ps.executeQuery();
            Map<String, Object> stats = new HashMap<>();
            
            if (rs.next()) {
                stats.put("totalScans", rs.getInt("total_scans"));
                stats.put("averageScore", rs.getDouble("avg_score"));
                stats.put("minScore", rs.getDouble("min_score"));
                stats.put("maxScore", rs.getDouble("max_score"));
                stats.put("totalViolations", rs.getInt("total_violations"));
                stats.put("totalCritical", rs.getInt("total_critical"));
                stats.put("totalSerious", rs.getInt("total_serious"));
            }
            
            return stats;
            
        } catch (SQLException e) {
            log.error("Error calculating scan statistics", e);
            return Collections.emptyMap();
        }
    }
    
    @Override
    public List<Map<String, Object>> getHistoricalTrends(String pagePath, int days) {
        String sql = "SELECT * FROM audit_scan_history_summary ";
        List<Object> params = new ArrayList<>();
        
        if (pagePath != null) {
            // Need to join with audit_scans to filter by page
            sql = "SELECT DATE(scan_date) as scan_day, COUNT(*) as total_scans, " +
                 "AVG(score) as avg_score, MIN(score) as min_score, MAX(score) as max_score, " +
                 "SUM(violation_count) as total_violations, SUM(violations_critical) as total_critical, " +
                 "SUM(violations_serious) as total_serious " +
                 "FROM audit_scans WHERE page_path = ? ";
            params.add(pagePath);
        }
        
        if (days > 0) {
            String whereClause = pagePath != null ? "AND" : "WHERE";
            sql += whereClause + " scan_day >= ? ";
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -days);
            params.add(new java.sql.Date(cal.getTimeInMillis()));
        }
        
        if (pagePath != null) {
            sql += "GROUP BY DATE(scan_date) ";
        }
        
        sql += "ORDER BY scan_day DESC";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = ps.executeQuery();
            List<Map<String, Object>> trends = new ArrayList<>();
            
            while (rs.next()) {
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", rs.getDate("scan_day"));
                dayData.put("totalScans", rs.getInt("total_scans"));
                dayData.put("avgScore", rs.getDouble("avg_score"));
                dayData.put("minScore", rs.getDouble("min_score"));
                dayData.put("maxScore", rs.getDouble("max_score"));
                dayData.put("totalViolations", rs.getInt("total_violations"));
                dayData.put("totalCritical", rs.getInt("total_critical"));
                dayData.put("totalSerious", rs.getInt("total_serious"));
                trends.add(dayData);
            }
            
            return trends;
            
        } catch (SQLException e) {
            log.error("Error retrieving historical trends", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public void storeConfiguration(String key, String value) {
        String sql = "INSERT INTO audit_configuration (config_key, config_value) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE config_value = ?, updated_at = CURRENT_TIMESTAMP";
        
        if (connectionManager.getDatabaseType().equals("postgresql")) {
            sql = "INSERT INTO audit_configuration (config_key, config_value) VALUES (?, ?) " +
                 "ON CONFLICT (config_key) DO UPDATE SET config_value = ?, updated_at = CURRENT_TIMESTAMP";
        }
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, key);
            ps.setString(2, value);
            ps.setString(3, value);
            ps.executeUpdate();
            
        } catch (SQLException e) {
            log.error("Error storing configuration: " + key, e);
            throw new RuntimeException("Failed to store configuration", e);
        }
    }
    
    @Override
    public Optional<String> getConfiguration(String key) {
        String sql = "SELECT config_value FROM audit_configuration WHERE config_key = ?";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return Optional.of(rs.getString("config_value"));
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            log.error("Error retrieving configuration: " + key, e);
            return Optional.empty();
        }
    }
    
    @Override
    public Map<String, String> getAllConfiguration() {
        String sql = "SELECT config_key, config_value FROM audit_configuration";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ResultSet rs = ps.executeQuery();
            Map<String, String> config = new HashMap<>();
            
            while (rs.next()) {
                config.put(rs.getString("config_key"), rs.getString("config_value"));
            }
            
            return config;
            
        } catch (SQLException e) {
            log.error("Error retrieving all configuration", e);
            return Collections.emptyMap();
        }
    }
    
    @Override
    public boolean testConnection() {
        return connectionManager.testConnection();
    }
    
    @Override
    public String getStorageType() {
        return connectionManager.getDatabaseType();
    }
    
    private AccessibilityScanResult mapResultSetToScanResult(ResultSet rs, Connection conn) throws SQLException {
        String scanId = rs.getString("scan_id");
        AccessibilityScanResult result = new AccessibilityScanResult(
            rs.getString("page_path"),
            rs.getString("page_url")
        );
        
        result.setId(scanId);
        result.setPageTitle(rs.getString("page_title"));
        result.setWcagLevel(rs.getString("wcag_level"));
        result.setWcagVersion(rs.getString("wcag_version"));
        result.setScore(rs.getDouble("score"));
        result.setScannerVersion(rs.getString("browser_info"));
        result.setScanType(rs.getString("scan_type"));
        result.setTotalElements(rs.getInt("total_elements"));
        result.setElementsWithIssues(rs.getInt("elements_with_issues"));
        
        // Load violations
        result.setViolations(loadViolations(conn, scanId));
        
        // Load passes
        result.setPasses(loadPasses(conn, scanId));
        
        // Load incomplete
        result.setIncomplete(loadIncomplete(conn, scanId));
        
        return result;
    }
    
    private List<AccessibilityScanResult.Violation> loadViolations(Connection conn, String scanId) throws SQLException {
        String sql = "SELECT * FROM audit_violations WHERE scan_id = ?";
        List<AccessibilityScanResult.Violation> violations = new ArrayList<>();
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, scanId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                AccessibilityScanResult.Violation violation = new AccessibilityScanResult.Violation();
                violation.setId(rs.getString("violation_id"));
                violation.setDescription(rs.getString("description"));
                violation.setHelp(rs.getString("help"));
                violation.setHelpUrl(rs.getString("help_url"));
                violation.setImpact(rs.getString("impact"));
                
                // Load tags
                String tagsJson = rs.getString("tags");
                if (tagsJson != null) {
                    @SuppressWarnings("unchecked")
                    List<String> tags = objectMapper.readValue(tagsJson, List.class);
                    violation.setTags(tags);
                }
                
                // Load nodes
                violation.setNodes(loadViolationNodes(conn, rs.getLong("id")));
                
                violations.add(violation);
            }
        } catch (Exception e) {
            log.error("Error loading violations", e);
        }
        
        return violations;
    }
    
    private List<AccessibilityScanResult.Node> loadViolationNodes(Connection conn, long violationId) throws SQLException {
        String sql = "SELECT * FROM audit_violation_nodes WHERE violation_id = ?";
        List<AccessibilityScanResult.Node> nodes = new ArrayList<>();
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, violationId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                AccessibilityScanResult.Node node = new AccessibilityScanResult.Node();
                node.setHtml(rs.getString("html"));
                node.setFailureSummary(rs.getString("failure_summary"));
                node.setTarget(rs.getString("target"));
                
                nodes.add(node);
            }
        } catch (Exception e) {
            log.error("Error loading violation nodes", e);
        }
        
        return nodes;
    }
    
    private List<AccessibilityScanResult.Pass> loadPasses(Connection conn, String scanId) throws SQLException {
        String sql = "SELECT * FROM audit_passes WHERE scan_id = ?";
        List<AccessibilityScanResult.Pass> passes = new ArrayList<>();
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, scanId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                AccessibilityScanResult.Pass pass = new AccessibilityScanResult.Pass();
                pass.setId(rs.getString("pass_id"));
                pass.setDescription(rs.getString("description"));
                pass.setHelp(rs.getString("help"));
                pass.setHelpUrl(rs.getString("help_url"));
                pass.setImpact(rs.getString("impact"));
                passes.add(pass);
            }
        }
        
        return passes;
    }
    
    private List<AccessibilityScanResult.Incomplete> loadIncomplete(Connection conn, String scanId) throws SQLException {
        String sql = "SELECT * FROM audit_incomplete WHERE scan_id = ?";
        List<AccessibilityScanResult.Incomplete> incompletes = new ArrayList<>();
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, scanId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                AccessibilityScanResult.Incomplete incomplete = new AccessibilityScanResult.Incomplete();
                incomplete.setId(rs.getString("incomplete_id"));
                incomplete.setDescription(rs.getString("description"));
                incomplete.setHelp(rs.getString("help"));
                incomplete.setHelpUrl(rs.getString("help_url"));
                incomplete.setImpact(rs.getString("impact"));
                incompletes.add(incomplete);
            }
        }
        
        return incompletes;
    }
}