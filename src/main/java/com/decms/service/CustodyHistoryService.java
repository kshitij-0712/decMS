package com.decms.service;

import com.decms.model.CustodyLog;
import com.decms.repository.CustodyLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for viewing and analyzing custody history.
 * Implements UC-08: View Custody History
 *
 * Design Principle: Single Responsibility Principle (SRP)
 * This service is responsible only for custody history retrieval and analysis.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustodyHistoryService {

    private final CustodyLogRepository custodyLogRepository;

    /**
     * Get paginated custody history for an evidence.
     * Useful for forensic analysis and investigation.
     */
    public Page<CustodyLog> getCustodyHistoryPaginated(Long evidenceId, Pageable pageable) {
        Page<CustodyLog> history = custodyLogRepository.findByEvidenceIdOrderByTimestampRecordedDesc(evidenceId, pageable);

        log.debug("Retrieved custody history page for evidence: {}, Page number: {}", evidenceId, pageable.getPageNumber());

        return history;
    }

    /**
     * Get complete custody history for an evidence (all pages).
     */
    public List<CustodyLog> getCompleteCustodyHistory(Long evidenceId) {
        List<CustodyLog> history = custodyLogRepository.findByEvidenceIdOrderByTimestampRecordedDesc(evidenceId);

        log.info("Retrieved complete custody history for evidence: {}, Total entries: {}", evidenceId, history.size());

        return history;
    }

    /**
     * Get flagged/suspicious custody entries for investigation.
     */
    public Page<CustodyLog> getSuspiciousCustodyHistory(Pageable pageable) {
        Page<CustodyLog> suspicious = custodyLogRepository.findBySuspiciousTrue(pageable);

        log.info("Retrieved suspicious custody logs: {} entries found", suspicious.getTotalElements());

        return suspicious;
    }

    /**
     * Generate a summary of custody activity for an evidence.
     */
    public CustodyHistorySummary generateHistorySummary(Long evidenceId) {
        List<CustodyLog> allLogs = custodyLogRepository.findByEvidenceIdOrderByTimestampRecordedDesc(evidenceId);

        if (allLogs.isEmpty()) {
            return null;
        }

        CustodyHistorySummary summary = new CustodyHistorySummary();
        summary.setEvidenceId(evidenceId);
        summary.setTotalAccessCount(allLogs.size());
        summary.setFirstAccessTime(allLogs.get(allLogs.size() - 1).getTimestampRecorded());
        summary.setLastAccessTime(allLogs.get(0).getTimestampRecorded());
        summary.setSuspiciousEntryCount((long) allLogs.stream().filter(CustodyLog::getIsSuspicious).toList().size());
        summary.setUniqueActorsCount(allLogs.stream().map(log -> log.getActor().getId()).distinct().count());

        return summary;
    }

    /**
     * DTO for custody history summary.
     */
    public static class CustodyHistorySummary {
        public Long evidenceId;
        public Integer totalAccessCount;
        public java.time.LocalDateTime firstAccessTime;
        public java.time.LocalDateTime lastAccessTime;
        public Long suspiciousEntryCount;
        public Long uniqueActorsCount;

        // Getters and Setters
        public Long getEvidenceId() {
            return evidenceId;
        }

        public void setEvidenceId(Long evidenceId) {
            this.evidenceId = evidenceId;
        }

        public Integer getTotalAccessCount() {
            return totalAccessCount;
        }

        public void setTotalAccessCount(Integer totalAccessCount) {
            this.totalAccessCount = totalAccessCount;
        }

        public java.time.LocalDateTime getFirstAccessTime() {
            return firstAccessTime;
        }

        public void setFirstAccessTime(java.time.LocalDateTime firstAccessTime) {
            this.firstAccessTime = firstAccessTime;
        }

        public java.time.LocalDateTime getLastAccessTime() {
            return lastAccessTime;
        }

        public void setLastAccessTime(java.time.LocalDateTime lastAccessTime) {
            this.lastAccessTime = lastAccessTime;
        }

        public Long getSuspiciousEntryCount() {
            return suspiciousEntryCount;
        }

        public void setSuspiciousEntryCount(Long suspiciousEntryCount) {
            this.suspiciousEntryCount = suspiciousEntryCount;
        }

        public Long getUniqueActorsCount() {
            return uniqueActorsCount;
        }

        public void setUniqueActorsCount(Long uniqueActorsCount) {
            this.uniqueActorsCount = uniqueActorsCount;
        }
    }
}
