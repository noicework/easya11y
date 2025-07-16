-- Create audit_scans table
CREATE TABLE audit_scans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scan_id VARCHAR(255) NOT NULL UNIQUE,
    page_url TEXT NOT NULL,
    page_title VARCHAR(1000),
    page_path VARCHAR(1000),
    scan_date TIMESTAMP NOT NULL,
    wcag_version VARCHAR(50),
    wcag_level VARCHAR(10),
    score INT,
    violation_count INT DEFAULT 0,
    pass_count INT DEFAULT 0,
    incomplete_count INT DEFAULT 0,
    inapplicable_count INT DEFAULT 0,
    total_elements INT DEFAULT 0,
    elements_with_issues INT DEFAULT 0,
    violations_critical INT DEFAULT 0,
    violations_serious INT DEFAULT 0,
    violations_moderate INT DEFAULT 0,
    violations_minor INT DEFAULT 0,
    scan_duration_ms BIGINT,
    scan_type VARCHAR(50),
    browser_info VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_scan_date (scan_date),
    INDEX idx_page_url (page_url(255)),
    INDEX idx_page_path (page_path(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create audit_violations table
CREATE TABLE audit_violations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scan_id VARCHAR(255) NOT NULL,
    violation_id VARCHAR(255) NOT NULL,
    description TEXT,
    help TEXT,
    help_url VARCHAR(1000),
    impact VARCHAR(50),
    tags JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (scan_id) REFERENCES audit_scans(scan_id) ON DELETE CASCADE,
    INDEX idx_scan_id (scan_id),
    INDEX idx_impact (impact)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create audit_violation_nodes table
CREATE TABLE audit_violation_nodes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    violation_id BIGINT NOT NULL,
    html TEXT,
    target JSON,
    failure_summary TEXT,
    element_selector VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (violation_id) REFERENCES audit_violations(id) ON DELETE CASCADE,
    INDEX idx_violation_id (violation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create audit_passes table
CREATE TABLE audit_passes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scan_id VARCHAR(255) NOT NULL,
    pass_id VARCHAR(255) NOT NULL,
    description TEXT,
    help TEXT,
    help_url VARCHAR(1000),
    impact VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (scan_id) REFERENCES audit_scans(scan_id) ON DELETE CASCADE,
    INDEX idx_scan_id (scan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create audit_incomplete table
CREATE TABLE audit_incomplete (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scan_id VARCHAR(255) NOT NULL,
    incomplete_id VARCHAR(255) NOT NULL,
    description TEXT,
    help TEXT,
    help_url VARCHAR(1000),
    impact VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (scan_id) REFERENCES audit_scans(scan_id) ON DELETE CASCADE,
    INDEX idx_scan_id (scan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create audit_configuration table
CREATE TABLE audit_configuration (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(255) NOT NULL UNIQUE,
    config_value TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create audit_scan_history_summary view for quick historical queries
CREATE VIEW audit_scan_history_summary AS
SELECT 
    DATE(scan_date) as scan_day,
    COUNT(*) as total_scans,
    AVG(score) as avg_score,
    MIN(score) as min_score,
    MAX(score) as max_score,
    SUM(violation_count) as total_violations,
    SUM(violations_critical) as total_critical,
    SUM(violations_serious) as total_serious
FROM audit_scans
GROUP BY DATE(scan_date);