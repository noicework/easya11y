-- Add page_uuid column to audit_scans
ALTER TABLE audit_scans ADD COLUMN page_uuid VARCHAR(36);

-- Create index for efficient UUID lookups
CREATE INDEX idx_audit_scans_page_uuid ON audit_scans(page_uuid);

-- Create page registry table to track UUID-to-path mappings
CREATE TABLE audit_page_registry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    page_uuid VARCHAR(36) NOT NULL UNIQUE,
    current_path VARCHAR(1024) NOT NULL,
    original_path VARCHAR(1024),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_page_uuid (page_uuid),
    INDEX idx_current_path (current_path(255))
);

-- Create path history table to track page moves
CREATE TABLE audit_page_path_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    page_uuid VARCHAR(36) NOT NULL,
    old_path VARCHAR(1024) NOT NULL,
    new_path VARCHAR(1024) NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_history_page_uuid (page_uuid)
);
