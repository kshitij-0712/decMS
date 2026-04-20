CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    department VARCHAR(100),
    role VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS evidence (
    evidence_id VARCHAR(36) PRIMARY KEY,
    case_id VARCHAR(50) NOT NULL,
    description TEXT,
    file_type VARCHAR(50) NOT NULL,
    file_size BIGINT NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    uploaded_by VARCHAR(36) NOT NULL,
    upload_timestamp TIMESTAMP NOT NULL,
    CONSTRAINT fk_evidence_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS hash_records (
    hash_id VARCHAR(36) PRIMARY KEY,
    evidence_id VARCHAR(36) UNIQUE NOT NULL,
    hash_value VARCHAR(64) NOT NULL,
    algorithm VARCHAR(20) NOT NULL,
    generated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_hash_evidence FOREIGN KEY (evidence_id) REFERENCES evidence(evidence_id)
);

CREATE TABLE IF NOT EXISTS custody_logs (
    log_id VARCHAR(36) PRIMARY KEY,
    evidence_id VARCHAR(36) NOT NULL,
    actor_id VARCHAR(36) NOT NULL,
    actor_role VARCHAR(30) NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    ip_address VARCHAR(45),
    timestamp TIMESTAMP NOT NULL,
    is_suspicious BOOLEAN NOT NULL,
    CONSTRAINT fk_custody_evidence FOREIGN KEY (evidence_id) REFERENCES evidence(evidence_id),
    CONSTRAINT fk_custody_actor FOREIGN KEY (actor_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS access_requests (
    request_id VARCHAR(36) PRIMARY KEY,
    evidence_id VARCHAR(36) NOT NULL,
    requester_id VARCHAR(36) NOT NULL,
    reason TEXT NOT NULL,
    case_number VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP,
    resolved_by VARCHAR(36),
    CONSTRAINT fk_access_req_evidence FOREIGN KEY (evidence_id) REFERENCES evidence(evidence_id),
    CONSTRAINT fk_access_req_requester FOREIGN KEY (requester_id) REFERENCES users(user_id),
    CONSTRAINT fk_access_req_resolved_by FOREIGN KEY (resolved_by) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS audit_reports (
    report_id VARCHAR(36) PRIMARY KEY,
    case_id VARCHAR(50) NOT NULL,
    generated_by VARCHAR(36) NOT NULL,
    generated_at TIMESTAMP NOT NULL,
    content TEXT NOT NULL,
    format VARCHAR(10) NOT NULL,
    CONSTRAINT fk_audit_generated_by FOREIGN KEY (generated_by) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS tampering_alerts (
    alert_id VARCHAR(36) PRIMARY KEY,
    evidence_id VARCHAR(36) NOT NULL,
    detected_at TIMESTAMP NOT NULL,
    stored_hash VARCHAR(64) NOT NULL,
    computed_hash VARCHAR(64) NOT NULL,
    notified_to TEXT,
    CONSTRAINT fk_tampering_evidence FOREIGN KEY (evidence_id) REFERENCES evidence(evidence_id)
);
