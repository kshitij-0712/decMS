-- Database Schema for Digital Evidence & Chain-of-Custody Management System
-- Forensic Module

-- Users table with role-based inheritance
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100),
    enabled BOOLEAN DEFAULT TRUE,
    ip_address VARCHAR(50),
    role VARCHAR(20) NOT NULL,
    
    -- Investigator fields
    department VARCHAR(100),
    
    -- LegalOfficer fields
    license_number VARCHAR(50),
    
    -- Administrator fields
    -- uses department field
    
    -- ForensicAnalyst fields
    specialization VARCHAR(100),
    
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
);

-- Evidence table
CREATE TABLE IF NOT EXISTS evidence (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    case_number VARCHAR(100) UNIQUE NOT NULL,
    case_id VARCHAR(100),
    description LONGTEXT,
    file_path LONGTEXT,
    hash_value LONGTEXT,
    uploaded_by BIGINT,
    is_sealed BOOLEAN DEFAULT FALSE,
    metadata LONGTEXT,
    
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_case_number (case_number),
    INDEX idx_case_id (case_id),
    INDEX idx_uploaded_by (uploaded_by)
);

-- Custody Logs table (Core for UC-02 and UC-08)
CREATE TABLE IF NOT EXISTS custody_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    evidence_id BIGINT NOT NULL,
    actor_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    timestamp_recorded DATETIME NOT NULL,
    ip_address VARCHAR(50),
    details LONGTEXT,
    is_suspicious BOOLEAN DEFAULT FALSE,
    anomaly_reason LONGTEXT,
    
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_evidence_id (evidence_id),
    INDEX idx_actor_id (actor_id),
    INDEX idx_timestamp (timestamp_recorded),
    INDEX idx_suspicious (is_suspicious)
);

-- Hash Records table (for evidence integrity verification)
CREATE TABLE IF NOT EXISTS hash_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    evidence_id BIGINT NOT NULL,
    hash_value LONGTEXT NOT NULL,
    hash_algorithm VARCHAR(20) DEFAULT 'SHA-256',
    verified_by BIGINT,
    verification_timestamp DATETIME,
    is_valid BOOLEAN DEFAULT TRUE,
    
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (evidence_id) REFERENCES evidence(id) ON DELETE CASCADE,
    FOREIGN KEY (verified_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_evidence_id (evidence_id),
    INDEX idx_verification_timestamp (verification_timestamp)
);

-- Access Requests table (for UC-05)
CREATE TABLE IF NOT EXISTS access_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    evidence_id BIGINT NOT NULL,
    requested_by BIGINT NOT NULL,
    request_reason VARCHAR(500),
    request_status VARCHAR(20) DEFAULT 'PENDING',
    approved_by BIGINT,
    approval_timestamp DATETIME,
    
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (evidence_id) REFERENCES evidence(id) ON DELETE CASCADE,
    FOREIGN KEY (requested_by) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_evidence_id (evidence_id),
    INDEX idx_requested_by (requested_by),
    INDEX idx_status (request_status)
);

-- Audit Reports table (for UC-04)
CREATE TABLE IF NOT EXISTS audit_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_title VARCHAR(200) NOT NULL,
    generated_by BIGINT NOT NULL,
    report_content LONGTEXT,
    report_format VARCHAR(20) DEFAULT 'PDF',
    case_id VARCHAR(100),
    
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (generated_by) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_generated_by (generated_by),
    INDEX idx_case_id (case_id)
);

-- Tampering Alerts table (for anomaly detection)
CREATE TABLE IF NOT EXISTS tampering_alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    evidence_id BIGINT NOT NULL,
    alert_message LONGTEXT,
    alert_severity VARCHAR(20) DEFAULT 'MEDIUM',
    acknowledged BOOLEAN DEFAULT FALSE,
    acknowledged_by BIGINT,
    
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (evidence_id) REFERENCES evidence(id) ON DELETE CASCADE,
    FOREIGN KEY (acknowledged_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_evidence_id (evidence_id),
    INDEX idx_acknowledged (acknowledged)
);

-- Insert sample users for testing
INSERT INTO users (username, password, email, full_name, role, department, specialization)
VALUES 
    ('investigator1', '$2a$10$nOUIs5H4.NSmQq76rK3NO.0J8z3MZ6bwKC4m3nn7P8pS44pbiy0Oe', 'investigator1@decms.com', 'John Investigator', 'INVESTIGATOR', 'Crime Lab', NULL),
    ('forensic1', '$2a$10$nOUIs5H4.NSmQq76rK3NO.0J8z3MZ6bwKC4m3nn7P8pS44pbiy0Oe', 'forensic1@decms.com', 'Kishore Forensic', 'FORENSIC', NULL, 'Digital Forensics'),
    ('legal1', '$2a$10$nOUIs5H4.NSmQq76rK3NO.0J8z3MZ6bwKC4m3nn7P8pS44pbiy0Oe', 'legal1@decms.com', 'Likith Legal', 'LEGAL', NULL, NULL),
    ('admin1', '$2a$10$nOUIs5H4.NSmQq76rK3NO.0J8z3MZ6bwKC4m3nn7P8pS44pbiy0Oe', 'admin1@decms.com', 'Khizer Admin', 'ADMIN', 'Administration', NULL)
ON DUPLICATE KEY UPDATE email=VALUES(email);
