package com.decms.repository;

import com.decms.model.AccessRequest;
import com.decms.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccessRequestRepository extends JpaRepository<AccessRequest, String> {

    // Used by AdminController — approval queue sorted by request time
    List<AccessRequest> findByStatusOrderByRequestedAtAsc(RequestStatus status);

    // Used by ReportService — count pending requests for summary stats
    long countByStatus(RequestStatus status);

    // Used by AccessRequestService — requester history sorted newest first
    List<AccessRequest> findByRequester_UserIdOrderByRequestedAtDesc(String requesterId);

    // Approved requests for legal evidence listing
    List<AccessRequest> findByRequester_UserIdAndStatusOrderByRequestedAtDesc(String requesterId, RequestStatus status);

    // Used by LegalController — requests for a specific evidence item
    List<AccessRequest> findByEvidenceEvidenceId(String evidenceId);
}
