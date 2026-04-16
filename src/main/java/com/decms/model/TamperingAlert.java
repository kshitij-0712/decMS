package com.decms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tampering_alerts")
public class TamperingAlert {

    @Id
    @Column(name = "alert_id", nullable = false, updatable = false, length = 36)
    private String alertId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evidence_id", nullable = false)
    private Evidence evidence;

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;

    @Column(name = "stored_hash", nullable = false, length = 64)
    private String storedHash;

    @Column(name = "computed_hash", nullable = false, length = 64)
    private String computedHash;

    @Column(name = "notified_to", columnDefinition = "TEXT")
    private String notifiedTo;

    @PrePersist
    public void onCreate() {
        if (this.alertId == null || this.alertId.isBlank()) {
            this.alertId = UUID.randomUUID().toString();
        }
        if (this.detectedAt == null) {
            this.detectedAt = LocalDateTime.now();
        }
    }

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public Evidence getEvidence() {
        return evidence;
    }

    public void setEvidence(Evidence evidence) {
        this.evidence = evidence;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
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

    public String getNotifiedTo() {
        return notifiedTo;
    }

    public void setNotifiedTo(String notifiedTo) {
        this.notifiedTo = notifiedTo;
    }
}
