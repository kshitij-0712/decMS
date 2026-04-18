package com.decms.repository;

import com.decms.model.AccessRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccessRequestRepository extends JpaRepository<AccessRequest, String> {
    List<AccessRequest> findByRequester_UserIdOrderByRequestedAtDesc(String requesterUserId);
}
