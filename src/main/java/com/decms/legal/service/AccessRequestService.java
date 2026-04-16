package com.decms.legal.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.decms.common.observer.EvidenceEventPublisher;
import com.decms.legal.dto.AccessRequestForm;

@Service
public class AccessRequestService {

    private static final Set<String> BLOCKED_STATUS = Set.of("ARCHIVED");

    private final EvidenceEventPublisher eventPublisher;
    private final List<AccessRequestRecord> requests = new ArrayList<>();

    public AccessRequestService(EvidenceEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public AccessRequestRecord submitRequest(AccessRequestForm form, String requesterId) {
        String evidenceStatus = resolveEvidenceStatus(form.getEvidenceId());
        if (BLOCKED_STATUS.contains(evidenceStatus)) {
            throw new IllegalArgumentException("Access cannot be requested for archived evidence.");
        }

        AccessRequestRecord record = new AccessRequestRecord(
                "REQ-" + (requests.size() + 1),
                form.getEvidenceId(),
                requesterId,
                form.getReason(),
                form.getCaseNumber(),
                "PENDING",
                LocalDateTime.now());

        requests.add(record);

        if (form.getReason() != null && form.getReason().toLowerCase().contains("urgent")) {
            eventPublisher.publish(
                    "SUSPICIOUS_ACCESS",
                    form.getEvidenceId(),
                    "Urgent access request flagged for manual review.");
        }

        return record;
    }

    public List<AccessRequestRecord> getRequestsByRequester(String requesterId) {
        List<AccessRequestRecord> result = new ArrayList<>();
        for (AccessRequestRecord request : requests) {
            if (request.requesterId().equals(requesterId)) {
                result.add(request);
            }
        }
        return result;
    }

    private String resolveEvidenceStatus(String evidenceId) {
        if (evidenceId == null) {
            return "UNKNOWN";
        }
        if (evidenceId.endsWith("9")) {
            return "ARCHIVED";
        }
        return "VERIFIED";
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
