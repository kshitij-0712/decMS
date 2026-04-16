package com.decms.investigator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class EvidenceUploadRequest {

    @NotBlank(message = "Case ID is required")
    @Size(max = 50, message = "Case ID cannot exceed 50 characters")
    private String caseId;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotBlank(message = "Evidence type is required")
    @Size(max = 50, message = "Evidence type cannot exceed 50 characters")
    private String evidenceType;

    private MultipartFile evidenceFile;

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(String evidenceType) {
        this.evidenceType = evidenceType;
    }

    public MultipartFile getEvidenceFile() {
        return evidenceFile;
    }

    public void setEvidenceFile(MultipartFile evidenceFile) {
        this.evidenceFile = evidenceFile;
    }
}
