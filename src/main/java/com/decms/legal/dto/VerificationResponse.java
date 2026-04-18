package com.decms.legal.dto;

public class VerificationResponse {

    private String evidenceId;
    private String storedHash;
    private String computedHash;
    private boolean integrityVerified;
    private String statusMessage;

    public String getEvidenceId() {
        return evidenceId;
    }

    public void setEvidenceId(String evidenceId) {
        this.evidenceId = evidenceId;
    }

    public String getStoredHash() {
        return storedHash;
    }

    public void setStoredHash(String storedHash) {
        this.storedHash = storedHash;
    }

    public String getComputedHash() {
        return computedHash;
    }

    public void setComputedHash(String computedHash) {
        this.computedHash = computedHash;
    }

    public boolean isIntegrityVerified() {
        return integrityVerified;
    }

    public void setIntegrityVerified(boolean integrityVerified) {
        this.integrityVerified = integrityVerified;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
