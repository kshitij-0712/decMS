package com.decms.repository;

import com.decms.model.CustodyLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustodyLogRepository extends JpaRepository<CustodyLog, String> {

    List<CustodyLog> findByEvidenceEvidenceIdOrderByTimestampDesc(String evidenceId);

    List<CustodyLog> findTop10ByEvidenceEvidenceIdOrderByTimestampDesc(String evidenceId);

    List<CustodyLog> findTop20ByOrderByTimestampDesc();

    List<CustodyLog> findByEvidenceCaseIdOrderByTimestampAsc(String caseId);

    List<CustodyLog> findByActorUserIdOrderByTimestampDesc(String actorId);

    List<CustodyLog> findBySuspiciousTrueOrderByTimestampDesc();
}
