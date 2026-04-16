package com.decms.investigator.dto;

import java.time.LocalDateTime;

public class EvidenceListItemResponse {

    private final String evidenceId;
    private final String caseId;
    private final String evidenceType;
    private final long fileSize;
    private final String status;
    private final LocalDateTime uploadedAt;

    public EvidenceListItemResponse(String evidenceId,
                                    String caseId,
                                    String evidenceType,
                                    long fileSize,
                                    String status,
                                    LocalDateTime uploadedAt) {
        this.evidenceId = evidenceId;
        this.caseId = caseId;
        this.evidenceType = evidenceType;
        this.fileSize = fileSize;
        this.status = status;
        this.uploadedAt = uploadedAt;
    }

    public String getEvidenceId() {
        return evidenceId;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getEvidenceType() {
        return evidenceType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
}
