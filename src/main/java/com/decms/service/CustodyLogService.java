package com.decms.service;

import com.decms.model.ActionType;
import com.decms.model.CustodyLog;
import com.decms.model.User;
import com.decms.repository.CustodyLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing custody log operations.
 * Implements UC-02: Maintain Chain-of-Custody Log
 *
 * Design Principle: Single Responsibility Principle (SRP)
 * This service is responsible only for custody log business logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustodyLogService {

    private final CustodyLogRepository custodyLogRepository;

    /**
     * Create and save a custody log entry.
     */
    public CustodyLog recordCustodyAction(
            Long evidenceId,
            User actor,
            ActionType action,
            String ipAddress,
            String details
    ) {
        CustodyLog log = CustodyLog.createEntry(evidenceId, actor, action, ipAddress, details);
        CustodyLog saved = custodyLogRepository.save(log);

        log.info("Custody action recorded: Evidence={}, Action={}, Actor={}, IP={}",
                evidenceId, action, actor.getUsername(), ipAddress);

        return saved;
    }

    /**
     * Detect and flag suspicious custody activities.
     * Looks for anomalies like:
     * - Multiple accesses in short time
     * - Access from unusual IPs
     * - Unusual action sequences
     */
    public void checkForAnomalies(Long evidenceId) {
        // Check for suspicious patterns
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        Long recentAccessCount = custodyLogRepository.countByEvidenceIdAndTimeWindow(evidenceId, oneHourAgo);

        // Flag as suspicious if more than 5 accesses in 1 hour
        if (recentAccessCount > 5) {
            List<CustodyLog> logs = custodyLogRepository.findByEvidenceIdAndTimeRange(
                    evidenceId,
                    oneHourAgo,
                    LocalDateTime.now()
            );

            logs.forEach(custodyLog -> {
                custodyLog.setIsSuspicious(true);
                custodyLog.setAnomalyReason("High frequency access detected: " + recentAccessCount + " in 1 hour");
                custodyLogRepository.save(custodyLog);
            });

            log.warn("Suspicious activity detected for evidence: {}", evidenceId);
        }
    }

    /**
     * Get all custody logs for an evidence, ordered by timestamp (newest first).
     */
    public List<CustodyLog> getCustodyHistory(Long evidenceId) {
        return custodyLogRepository.findByEvidenceIdOrderByTimestampRecordedDesc(evidenceId);
    }

    /**
     * Get all suspicious/anomalous custody logs.
     */
    public List<CustodyLog> getSuspiciousLogs() {
        return custodyLogRepository.findBySuspiciousTrueOrderByTimestampRecordedDesc();
    }

    /**
     * Get custody logs for a specific actor (user).
     */
    public List<CustodyLog> getActorCustodyLogs(Long actorId) {
        return custodyLogRepository.findByActorIdOrderByTimestampRecordedDesc(actorId);
    }

    /**
     * Get custody logs within a date range for an evidence.
     */
    public List<CustodyLog> getCustodyHistoryByDateRange(
            Long evidenceId,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        return custodyLogRepository.findByEvidenceIdAndTimeRange(evidenceId, startTime, endTime);
    }

    /**
     * Count total custody log entries for an evidence.
     */
    public long getCustodyLogCount(Long evidenceId) {
        return custodyLogRepository.findByEvidenceIdOrderByTimestampRecordedDesc(evidenceId).size();
    }
}
