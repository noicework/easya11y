-- Add page_uuid column to audit_scans
ALTER TABLE audit_scans ADD COLUMN page_uuid VARCHAR(36);

-- Create index for efficient UUID lookups
CREATE INDEX idx_audit_scans_page_uuid ON audit_scans(page_uuid);

-- Create page registry table to track UUID-to-path mappings
CREATE TABLE audit_page_registry (
    id BIGSERIAL PRIMARY KEY,
    page_uuid VARCHAR(36) NOT NULL UNIQUE,
    current_path VARCHAR(1024) NOT NULL,
    original_path VARCHAR(1024),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for page registry
CREATE INDEX idx_page_registry_page_uuid ON audit_page_registry(page_uuid);
CREATE INDEX idx_page_registry_current_path ON audit_page_registry(current_path);

-- Create path history table to track page moves
CREATE TABLE audit_page_path_history (
    id BIGSERIAL PRIMARY KEY,
    page_uuid VARCHAR(36) NOT NULL,
    old_path VARCHAR(1024) NOT NULL,
    new_path VARCHAR(1024) NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index for path history
CREATE INDEX idx_history_page_uuid ON audit_page_path_history(page_uuid);

-- Create function to auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_audit_page_registry_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for auto-updating updated_at
CREATE TRIGGER trigger_audit_page_registry_updated_at
    BEFORE UPDATE ON audit_page_registry
    FOR EACH ROW
    EXECUTE FUNCTION update_audit_page_registry_updated_at();
