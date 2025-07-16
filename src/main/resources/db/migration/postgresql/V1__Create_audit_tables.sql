-- Create audit_scans table
CREATE TABLE audit_scans (
    id BIGSERIAL PRIMARY KEY,
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_scan_date ON audit_scans(scan_date);
CREATE INDEX idx_page_url ON audit_scans(page_url);
CREATE INDEX idx_page_path ON audit_scans(page_path);

-- Create audit_violations table
CREATE TABLE audit_violations (
    id BIGSERIAL PRIMARY KEY,
    scan_id VARCHAR(255) NOT NULL,
    violation_id VARCHAR(255) NOT NULL,
    description TEXT,
    help TEXT,
    help_url VARCHAR(1000),
    impact VARCHAR(50),
    tags JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (scan_id) REFERENCES audit_scans(scan_id) ON DELETE CASCADE
);

CREATE INDEX idx_violations_scan_id ON audit_violations(scan_id);
CREATE INDEX idx_violations_impact ON audit_violations(impact);

-- Create audit_violation_nodes table
CREATE TABLE audit_violation_nodes (
    id BIGSERIAL PRIMARY KEY,
    violation_id BIGINT NOT NULL,
    html TEXT,
    target JSONB,
    failure_summary TEXT,
    element_selector VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (violation_id) REFERENCES audit_violations(id) ON DELETE CASCADE
);

CREATE INDEX idx_violation_nodes_violation_id ON audit_violation_nodes(violation_id);

-- Create audit_passes table
CREATE TABLE audit_passes (
    id BIGSERIAL PRIMARY KEY,
    scan_id VARCHAR(255) NOT NULL,
    pass_id VARCHAR(255) NOT NULL,
    description TEXT,
    help TEXT,
    help_url VARCHAR(1000),
    impact VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (scan_id) REFERENCES audit_scans(scan_id) ON DELETE CASCADE
);

CREATE INDEX idx_passes_scan_id ON audit_passes(scan_id);

-- Create audit_incomplete table
CREATE TABLE audit_incomplete (
    id BIGSERIAL PRIMARY KEY,
    scan_id VARCHAR(255) NOT NULL,
    incomplete_id VARCHAR(255) NOT NULL,
    description TEXT,
    help TEXT,
    help_url VARCHAR(1000),
    impact VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (scan_id) REFERENCES audit_scans(scan_id) ON DELETE CASCADE
);

CREATE INDEX idx_incomplete_scan_id ON audit_incomplete(scan_id);

-- Create audit_configuration table
CREATE TABLE audit_configuration (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(255) NOT NULL UNIQUE,
    config_value TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_config_key ON audit_configuration(config_key);

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_audit_configuration_updated_at 
BEFORE UPDATE ON audit_configuration
FOR EACH ROW 
EXECUTE FUNCTION update_updated_at_column();

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