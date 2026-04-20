package com.decms.admin.service;

import com.decms.admin.dto.AuditReportResponse;
import com.decms.model.*;
import com.decms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ReportService
 *
 * Business logic for UC-04: Generate Audit Report.
 * Owned by Khizer Pasha (Administrator module).
 *
 * Steps:
 *   UC-20: Compile evidence timeline for a case
 *   UC-21: Retrieve all custody records for evidence items
 *   UC-22: Delegate formatting to ReportFormatter (DIP)
 *   UC-23: Export as PDF via PdfReportFormatter
 *
 * Demonstrates DIP (Khizer):
 *   This HIGH-LEVEL service depends on the ReportFormatter
 *   interface — never on HtmlReportFormatter or
 *   PdfReportFormatter directly.
 */
@Service
public class ReportService {

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final EvidenceRepository      evidenceRepository;
    private final CustodyLogRepository    custodyLogRepository;
    private final AuditReportRepository   auditReportRepository;
    private final UserRepository          userRepository;
    private final TamperingAlertRepository tamperingAlertRepository;
    private final AccessRequestRepository  accessRequestRepository;
    private final HashRecordRepository    hashRecordRepository;
    private final HtmlReportFormatter     htmlReportFormatter;
    private final PdfReportFormatter      pdfReportFormatter;

    public ReportService(EvidenceRepository evidenceRepository,
                         CustodyLogRepository custodyLogRepository,
                         AuditReportRepository auditReportRepository,
                         UserRepository userRepository,
                         TamperingAlertRepository tamperingAlertRepository,
                         AccessRequestRepository accessRequestRepository,
                         HashRecordRepository hashRecordRepository,
                         HtmlReportFormatter htmlReportFormatter,
                         PdfReportFormatter pdfReportFormatter) {
        this.evidenceRepository       = evidenceRepository;
        this.custodyLogRepository     = custodyLogRepository;
        this.auditReportRepository    = auditReportRepository;
        this.userRepository           = userRepository;
        this.tamperingAlertRepository = tamperingAlertRepository;
        this.accessRequestRepository  = accessRequestRepository;
        this.hashRecordRepository     = hashRecordRepository;
        this.htmlReportFormatter      = htmlReportFormatter;
        this.pdfReportFormatter       = pdfReportFormatter;
    }

    // ── UC-04: Generate and save a report ────────────────────────

    /**
     * Generates a complete audit report for a given case ID.
     *
     * @param caseId      the case to report on
     * @param format      "HTML" or "PDF"
     * @param adminUserId the username of the admin (from Spring Security principal)
     */
    @Transactional
    public AuditReportResponse generateReport(String caseId,
                                               String format,
                                               String adminUserId) {

        // Fetch admin user
        User admin = userRepository.findById(adminUserId)
                .orElse(null);
        String adminName = admin != null ? admin.getName() : adminUserId;

        // UC-20: Compile evidence timeline
        List<Evidence> evidenceList = evidenceRepository.findByCaseId(caseId);
        if (evidenceList.isEmpty()) {
            throw new RuntimeException("No evidence found for case: " + caseId);
        }

        // UC-21: Retrieve all custody records for this case
        List<CustodyLog> allLogs = custodyLogRepository
                .findByEvidenceCaseIdOrderByTimestampAsc(caseId);

        // Summary stats
        int verifiedCount = (int) evidenceList.stream()
                .filter(e -> e.getStatus() == EvidenceStatus.VERIFIED)
                .count();

        int tamperingCount = evidenceList.stream()
                .mapToInt(e -> tamperingAlertRepository
                        .findByEvidenceEvidenceId(e.getEvidenceId()).size())
                .sum();

        long pendingRequests = accessRequestRepository
                .countByStatus(RequestStatus.PENDING);

        // Build evidence rows
        List<AuditReportResponse.EvidenceRow> evidenceRows = new ArrayList<>();
        for (Evidence e : evidenceList) {
            evidenceRows.add(buildEvidenceRow(e, allLogs));
        }

        // Build custody timeline rows
        List<AuditReportResponse.CustodyEventRow> timelineRows = new ArrayList<>();
        for (CustodyLog log : allLogs) {
            timelineRows.add(buildCustodyRow(log));
        }

        // Assemble DTO
        String reportId = UUID.randomUUID().toString();

        AuditReportResponse response = new AuditReportResponse();
        response.setReportId(reportId);
        response.setCaseId(caseId);
        response.setGeneratedByName(adminName);
        response.setGeneratedAt(LocalDateTime.now());
        response.setFormat(format);
        response.setTotalEvidenceItems(evidenceList.size());
        response.setTotalCustodyEvents(allLogs.size());
        response.setVerifiedCount(verifiedCount);
        response.setTamperingAlertCount(tamperingCount);
        response.setPendingAccessRequests((int) pendingRequests);
        response.setEvidenceItems(evidenceRows);
        response.setCustodyTimeline(timelineRows);

        // UC-22: Delegate to ReportFormatter (DIP)
        ReportFormatter formatter = resolveFormatter(format);
        String content = formatter.format(response);

        // Persist AuditReport entity
        AuditReport reportEntity = new AuditReport();
        reportEntity.setReportId(reportId);
        reportEntity.setCaseId(caseId);
        if (admin != null) reportEntity.setGeneratedBy(admin);
        reportEntity.setContent(content);
        reportEntity.setFormat(format.toUpperCase());
        auditReportRepository.save(reportEntity);

        return response;
    }

    // ── Report history ────────────────────────────────────────────

    public List<AuditReport> getAllReports() {
        return auditReportRepository.findAllByOrderByGeneratedAtDesc();
    }

    public AuditReport getReportById(String reportId) {
        return auditReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found: " + reportId));
    }

    public List<String> getAllCaseIds() {
        return evidenceRepository.findAll()
                .stream()
                .map(Evidence::getCaseId)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // ── Private helpers ───────────────────────────────────────────

    private ReportFormatter resolveFormatter(String format) {
        if ("PDF".equalsIgnoreCase(format)) return pdfReportFormatter;
        return htmlReportFormatter;
    }

    private AuditReportResponse.EvidenceRow buildEvidenceRow(
            Evidence evidence, List<CustodyLog> allLogs) {

        long eventCount = allLogs.stream()
                .filter(l -> l.getEvidence().getEvidenceId()
                              .equals(evidence.getEvidenceId()))
                .count();

        String hashValue = hashRecordRepository
                .findByEvidenceEvidenceId(evidence.getEvidenceId())
                .map(HashRecord::getHashValue)
                .orElse("—");

        AuditReportResponse.EvidenceRow row = new AuditReportResponse.EvidenceRow();
        row.setEvidenceId(evidence.getEvidenceId());
        row.setDescription(evidence.getDescription());
        row.setFileType(evidence.getFileType());
        row.setStatus(evidence.getStatus().name());
        row.setUploadedBy(evidence.getUploadedBy() != null
                ? evidence.getUploadedBy().getName() : "—");
        row.setUploadTimestamp(evidence.getUploadTimestamp() != null
                ? evidence.getUploadTimestamp().format(DISPLAY_FMT) : "—");
        row.setHashValue(hashValue);
        row.setCustodyEventCount(eventCount);
        return row;
    }

    private AuditReportResponse.CustodyEventRow buildCustodyRow(CustodyLog log) {
        // Get actor name - CustodyLog in their codebase has actor as User object
        String actorName = "—";
        try {
            if (log.getActor() != null) {
                actorName = log.getActor().getName();
            }
        } catch (Exception ignored) {}

        AuditReportResponse.CustodyEventRow row = new AuditReportResponse.CustodyEventRow();
        row.setTimestamp(log.getTimestamp() != null
                ? log.getTimestamp().format(DISPLAY_FMT) : "—");
        row.setEvidenceId(log.getEvidence().getEvidenceId());
        row.setActorName(actorName);
        row.setActorRole(log.getActorRole() != null ? log.getActorRole().name() : "—");
        row.setActionType(log.getActionType() != null ? log.getActionType().name() : "—");
        row.setIpAddress(log.getIpAddress() != null ? log.getIpAddress() : "—");
        row.setSuspicious(log.isSuspicious());
        return row;
    }
}
