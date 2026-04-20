package com.decms.admin.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AuditReportResponse
 *
 * DTO returned by ReportService and passed to report-view.html.
 * Contains the compiled report data ready for display or PDF export.
 *
 * Demonstrates DIP (Khizer) — ReportService builds this DTO
 * and passes it to the ReportFormatter interface.
 * The formatter renders it without knowing service internals.
 */
public class AuditReportResponse {

    private String reportId;
    private String caseId;
    private String generatedByName;
    private LocalDateTime generatedAt;
    private String format;
    private int totalEvidenceItems;
    private int totalCustodyEvents;
    private int verifiedCount;
    private int tamperingAlertCount;
    private int pendingAccessRequests;
    private List<CustodyEventRow> custodyTimeline;
    private List<EvidenceRow> evidenceItems;

    // ── Constructors ─────────────────────────────────────────────

    public AuditReportResponse() {}

    // ── Getters & Setters ────────────────────────────────────────

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getCaseId() { return caseId; }
    public void setCaseId(String caseId) { this.caseId = caseId; }

    public String getGeneratedByName() { return generatedByName; }
    public void setGeneratedByName(String generatedByName) { this.generatedByName = generatedByName; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public int getTotalEvidenceItems() { return totalEvidenceItems; }
    public void setTotalEvidenceItems(int totalEvidenceItems) { this.totalEvidenceItems = totalEvidenceItems; }

    public int getTotalCustodyEvents() { return totalCustodyEvents; }
    public void setTotalCustodyEvents(int totalCustodyEvents) { this.totalCustodyEvents = totalCustodyEvents; }

    public int getVerifiedCount() { return verifiedCount; }
    public void setVerifiedCount(int verifiedCount) { this.verifiedCount = verifiedCount; }

    public int getTamperingAlertCount() { return tamperingAlertCount; }
    public void setTamperingAlertCount(int tamperingAlertCount) { this.tamperingAlertCount = tamperingAlertCount; }

    public int getPendingAccessRequests() { return pendingAccessRequests; }
    public void setPendingAccessRequests(int pendingAccessRequests) { this.pendingAccessRequests = pendingAccessRequests; }

    public List<CustodyEventRow> getCustodyTimeline() { return custodyTimeline; }
    public void setCustodyTimeline(List<CustodyEventRow> custodyTimeline) { this.custodyTimeline = custodyTimeline; }

    public List<EvidenceRow> getEvidenceItems() { return evidenceItems; }
    public void setEvidenceItems(List<EvidenceRow> evidenceItems) { this.evidenceItems = evidenceItems; }

    // ── Inner class: one row in the custody timeline ─────────────

    public static class CustodyEventRow {
        private String timestamp;
        private String evidenceId;
        private String actorName;
        private String actorRole;
        private String actionType;
        private String ipAddress;
        private boolean suspicious;

        public CustodyEventRow() {}

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

        public String getEvidenceId() { return evidenceId; }
        public void setEvidenceId(String evidenceId) { this.evidenceId = evidenceId; }

        public String getActorName() { return actorName; }
        public void setActorName(String actorName) { this.actorName = actorName; }

        public String getActorRole() { return actorRole; }
        public void setActorRole(String actorRole) { this.actorRole = actorRole; }

        public String getActionType() { return actionType; }
        public void setActionType(String actionType) { this.actionType = actionType; }

        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

        public boolean isSuspicious() { return suspicious; }
        public void setSuspicious(boolean suspicious) { this.suspicious = suspicious; }
    }

    // ── Inner class: one evidence item row ───────────────────────

    public static class EvidenceRow {
        private String evidenceId;
        private String description;
        private String fileType;
        private String status;
        private String uploadedBy;
        private String uploadTimestamp;
        private String hashValue;
        private long custodyEventCount;

        public EvidenceRow() {}

        public String getEvidenceId() { return evidenceId; }
        public void setEvidenceId(String evidenceId) { this.evidenceId = evidenceId; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getUploadedBy() { return uploadedBy; }
        public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

        public String getUploadTimestamp() { return uploadTimestamp; }
        public void setUploadTimestamp(String uploadTimestamp) { this.uploadTimestamp = uploadTimestamp; }

        public String getHashValue() { return hashValue; }
        public void setHashValue(String hashValue) { this.hashValue = hashValue; }

        public long getCustodyEventCount() { return custodyEventCount; }
        public void setCustodyEventCount(long custodyEventCount) { this.custodyEventCount = custodyEventCount; }
    }
}
