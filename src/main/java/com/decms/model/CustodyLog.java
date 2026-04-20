package com.decms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * CustodyLog entity for tracking chain of custody.
 * Every evidence action is recorded with actor, action, timestamp, and IP.
 *
 * Core entity for UC-02 (Maintain Chain-of-Custody Log) and UC-08 (View Custody History).
 */
@Entity
@Table(name = "custody_logs", indexes = {
        @Index(name = "idx_evidence_id", columnList = "evidence_id"),
        @Index(name = "idx_actor_id", columnList = "actor_id"),
        @Index(name = "idx_timestamp", columnList = "timestamp_recorded")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustodyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evidence_id", nullable = false)
    private Long evidenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private ActionType action;

    @Column(name = "timestamp_recorded", nullable = false)
    private LocalDateTime timestampRecorded;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "is_suspicious", nullable = false)
    private Boolean isSuspicious = false;

    @Column(name = "anomaly_reason", columnDefinition = "TEXT")
    private String anomalyReason;

    /**
     * Factory method to create a custody log entry.
     */
    public static CustodyLog createEntry(
            Long evidenceId,
            User actor,
            ActionType action,
            String ipAddress,
            String details
    ) {
        CustodyLog log = new CustodyLog();
        log.setEvidenceId(evidenceId);
        log.setActor(actor);
        log.setAction(action);
        log.setIpAddress(ipAddress);
        log.setDetails(details);
        log.setTimestampRecorded(LocalDateTime.now());
        log.setIsSuspicious(false);
        return log;
    }
}
