package com.decms.repository;

import com.decms.model.CustodyLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for CustodyLog entity.
 * Provides database access for custody chain tracking.
 */
@Repository
public interface CustodyLogRepository extends JpaRepository<CustodyLog, Long> {

    /**
     * Find all custody logs for a specific evidence.
     */
    List<CustodyLog> findByEvidenceIdOrderByTimestampRecordedDesc(Long evidenceId);

    /**
     * Find paginated custody logs for a specific evidence.
     */
    Page<CustodyLog> findByEvidenceIdOrderByTimestampRecordedDesc(Long evidenceId, Pageable pageable);

    /**
     * Find all suspicious custody logs.
     */
    List<CustodyLog> findBySuspiciousTrueOrderByTimestampRecordedDesc();

    /**
     * Find custody logs within a date range for an evidence.
     */
    @Query("SELECT cl FROM CustodyLog cl WHERE cl.evidenceId = :evidenceId " +
           "AND cl.timestampRecorded BETWEEN :startTime AND :endTime " +
           "ORDER BY cl.timestampRecorded DESC")
    List<CustodyLog> findByEvidenceIdAndTimeRange(
            @Param("evidenceId") Long evidenceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * Find custody logs for a specific actor.
     */
    List<CustodyLog> findByActorIdOrderByTimestampRecordedDesc(Long actorId);

    /**
     * Find all suspicious logs (anomalies).
     */
    Page<CustodyLog> findBySuspiciousTrue(Pageable pageable);

    /**
     * Count logs for an evidence in a time window (for anomaly detection).
     */
    @Query("SELECT COUNT(cl) FROM CustodyLog cl WHERE cl.evidenceId = :evidenceId " +
           "AND cl.timestampRecorded >= :startTime")
    Long countByEvidenceIdAndTimeWindow(
            @Param("evidenceId") Long evidenceId,
            @Param("startTime") LocalDateTime startTime
    );
}
