package com.decms.investigator.dto;

public class EvidenceUploadResult {

    private final String evidenceId;
    private final String hashValue;
    private final String status;
    private final boolean duplicateHash;

    public EvidenceUploadResult(String evidenceId, String hashValue, String status, boolean duplicateHash) {
        this.evidenceId = evidenceId;
        this.hashValue = hashValue;
        this.status = status;
        this.duplicateHash = duplicateHash;
    }

    public String getEvidenceId() {
        return evidenceId;
    }

    public String getHashValue() {
        return hashValue;
    }

    public String getStatus() {
        return status;
    }

    public boolean isDuplicateHash() {
        return duplicateHash;
    }
}
