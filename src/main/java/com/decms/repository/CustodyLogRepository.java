package com.decms.repository;

import com.decms.model.CustodyLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustodyLogRepository extends JpaRepository<CustodyLog, String> {

    // Existing — used by ForensicController
    List<CustodyLog> findByEvidenceEvidenceIdOrderByTimestampDesc(String evidenceId);

    List<CustodyLog> findTop10ByEvidenceEvidenceIdOrderByTimestampDesc(String evidenceId);

    List<CustodyLog> findTop20ByOrderByTimestampDesc();

    // Added — used by ReportService (UC-04) to compile case timeline
    List<CustodyLog> findByEvidenceCaseIdOrderByTimestampAsc(String caseId);
}
