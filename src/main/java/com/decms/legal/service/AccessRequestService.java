package com.decms.legal.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.decms.common.decorator.EvidenceAccessService;
import com.decms.common.observer.EvidenceEventPublisher;
import com.decms.legal.dto.AccessRequestForm;
import com.decms.model.AccessRequest;
import com.decms.model.ActionType;
import com.decms.model.Evidence;
import com.decms.model.EvidenceStatus;
import com.decms.model.RequestStatus;
import com.decms.model.Role;
import com.decms.model.User;
import com.decms.repository.AccessRequestRepository;
import com.decms.repository.EvidenceRepository;
import com.decms.repository.UserRepository;

@Service
public class AccessRequestService {

    private static final Set<EvidenceStatus> BLOCKED_STATUS = Set.of(EvidenceStatus.ARCHIVED);

    private final EvidenceEventPublisher eventPublisher;
    private final AccessRequestRepository accessRequestRepository;
    private final EvidenceRepository evidenceRepository;
    private final UserRepository userRepository;
    private final EvidenceAccessService evidenceAccessService;

    public AccessRequestService(EvidenceEventPublisher eventPublisher,
                                AccessRequestRepository accessRequestRepository,
                                EvidenceRepository evidenceRepository,
                                UserRepository userRepository,
                                EvidenceAccessService evidenceAccessService) {
        this.eventPublisher = eventPublisher;
        this.accessRequestRepository = accessRequestRepository;
        this.evidenceRepository = evidenceRepository;
        this.userRepository = userRepository;
        this.evidenceAccessService = evidenceAccessService;
    }

    @Transactional
    public AccessRequestRecord submitRequest(AccessRequestForm form, String requesterId) {
        if (form == null || form.getEvidenceId() == null || form.getEvidenceId().isBlank()) {
            throw new IllegalArgumentException("Evidence selection is required.");
        }
        if (form.getReason() == null || form.getReason().isBlank()) {
            throw new IllegalArgumentException("Reason is required.");
        }

        Evidence evidence = evidenceRepository.findById(form.getEvidenceId().trim())
                .orElseThrow(() -> new IllegalArgumentException("Selected evidence does not exist."));
        if (BLOCKED_STATUS.contains(evidence.getStatus())) {
            throw new IllegalArgumentException("Access cannot be requested for archived evidence.");
        }

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("Requester not found."));
        if (requester.getRole() != Role.LEGAL_OFFICER) {
            throw new IllegalArgumentException("Only legal officers can request evidence access.");
        }

        AccessRequest request = new AccessRequest();
        request.setEvidence(evidence);
        request.setRequester(requester);
        request.setReason(form.getReason().trim());
        request.setCaseNumber(form.getCaseNumber() == null ? null : form.getCaseNumber().trim());
        request.setStatus(RequestStatus.PENDING);
        AccessRequest savedRequest = accessRequestRepository.save(request);

        evidenceAccessService.recordEvidenceAction(
                evidence.getEvidenceId(),
                requester.getUserId(),
                ActionType.ACCESS,
                null
        );

        AccessRequestRecord record = toRecord(savedRequest);

        if (record.reason() != null && record.reason().toLowerCase().contains("urgent")) {
            eventPublisher.publish(
                    "SUSPICIOUS_ACCESS",
                    record.evidenceId(),
                    "Urgent access request flagged for manual review.");
        }

        return record;
    }

    @Transactional(readOnly = true)
    public List<AccessRequestRecord> getRequestsByRequester(String requesterId) {
        return accessRequestRepository.findByRequester_UserIdOrderByRequestedAtDesc(requesterId)
                .stream()
                .map(this::toRecord)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getApprovedEvidenceIdsForRequester(String requesterId) {
        List<String> approvedEvidenceIds = accessRequestRepository
                .findByRequester_UserIdAndStatusOrderByRequestedAtDesc(requesterId, RequestStatus.APPROVED)
                .stream()
                .map(req -> req.getEvidence().getEvidenceId())
                .toList();
        return new ArrayList<>(new LinkedHashSet<>(approvedEvidenceIds));
    }

    private AccessRequestRecord toRecord(AccessRequest request) {
        return new AccessRequestRecord(
                request.getRequestId(),
                request.getEvidence().getEvidenceId(),
                request.getRequester().getUserId(),
                request.getReason(),
                request.getCaseNumber(),
                request.getStatus().name(),
                request.getRequestedAt()
        );
    }

    public record AccessRequestRecord(
            String requestId,
            String evidenceId,
            String requesterId,
            String reason,
            String caseNumber,
            String status,
            LocalDateTime requestedAt) {
    }
}
