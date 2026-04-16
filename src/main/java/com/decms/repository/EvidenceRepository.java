package com.decms.repository;

import com.decms.model.Evidence;
import com.decms.model.EvidenceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvidenceRepository extends JpaRepository<Evidence, String> {
    List<Evidence> findByCaseIdContainingIgnoreCaseOrderByUploadTimestampDesc(String caseId);

    List<Evidence> findByStatusOrderByUploadTimestampDesc(EvidenceStatus status);

    List<Evidence> findByCaseIdContainingIgnoreCaseAndStatusOrderByUploadTimestampDesc(String caseId, EvidenceStatus status);

    List<Evidence> findAllByOrderByUploadTimestampDesc();

    List<Evidence> findByUploadedBy_UserIdOrderByUploadTimestampDesc(String uploadedByUserId);

    List<Evidence> findByUploadedBy_UserIdAndCaseIdContainingIgnoreCaseOrderByUploadTimestampDesc(String uploadedByUserId, String caseId);

    List<Evidence> findByUploadedBy_UserIdAndStatusOrderByUploadTimestampDesc(String uploadedByUserId, EvidenceStatus status);

    List<Evidence> findByUploadedBy_UserIdAndCaseIdContainingIgnoreCaseAndStatusOrderByUploadTimestampDesc(
            String uploadedByUserId,
            String caseId,
            EvidenceStatus status
    );
}
