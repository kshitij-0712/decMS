package com.decms.investigator.dto;

import java.time.LocalDateTime;
import java.util.List;

public class EvidenceDetailsResponse {

    private final String evidenceId;
    private final String caseId;
    private final String description;
    private final String evidenceType;
    private final long fileSize;
    private final String status;
    private final LocalDateTime uploadedAt;
    private final String uploadedByName;
    private final String hashValue;
    private final String hashAlgorithm;
    private final List<CustodyEventResponse> custodyEvents;

    public EvidenceDetailsResponse(String evidenceId,
                                   String caseId,
                                   String description,
                                   String evidenceType,
                                   long fileSize,
                                   String status,
                                   LocalDateTime uploadedAt,
                                   String uploadedByName,
                                   String hashValue,
                                   String hashAlgorithm,
                                   List<CustodyEventResponse> custodyEvents) {
        this.evidenceId = evidenceId;
        this.caseId = caseId;
        this.description = description;
        this.evidenceType = evidenceType;
        this.fileSize = fileSize;
        this.status = status;
        this.uploadedAt = uploadedAt;
        this.uploadedByName = uploadedByName;
        this.hashValue = hashValue;
        this.hashAlgorithm = hashAlgorithm;
        this.custodyEvents = custodyEvents;
    }

    public String getEvidenceId() {
        return evidenceId;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getDescription() {
        return description;
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

    public String getUploadedByName() {
        return uploadedByName;
    }

    public String getHashValue() {
        return hashValue;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public List<CustodyEventResponse> getCustodyEvents() {
        return custodyEvents;
    }

    public static class CustodyEventResponse {
        private final String actionType;
        private final String actorName;
        private final String actorRole;
        private final String ipAddress;
        private final LocalDateTime timestamp;

        public CustodyEventResponse(String actionType,
                                    String actorName,
                                    String actorRole,
                                    String ipAddress,
                                    LocalDateTime timestamp) {
            this.actionType = actionType;
            this.actorName = actorName;
            this.actorRole = actorRole;
            this.ipAddress = ipAddress;
            this.timestamp = timestamp;
        }

        public String getActionType() {
            return actionType;
        }

        public String getActorName() {
            return actorName;
        }

        public String getActorRole() {
            return actorRole;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
}
