package com.decms.investigator.service;

import com.decms.common.decorator.EvidenceAccessService;
import com.decms.investigator.dto.EvidenceDetailsResponse;
import com.decms.investigator.dto.EvidenceListItemResponse;
import com.decms.model.ActionType;
import com.decms.model.CustodyLog;
import com.decms.model.Evidence;
import com.decms.model.EvidenceStatus;
import com.decms.model.HashRecord;
import com.decms.model.Role;
import com.decms.model.User;
import com.decms.repository.CustodyLogRepository;
import com.decms.repository.EvidenceRepository;
import com.decms.repository.HashRecordRepository;
import com.decms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class EvidenceViewService {

    private final EvidenceRepository evidenceRepository;
    private final HashRecordRepository hashRecordRepository;
    private final CustodyLogRepository custodyLogRepository;
    private final UserRepository userRepository;
    private final EvidenceAccessService evidenceAccessService;

    public EvidenceViewService(EvidenceRepository evidenceRepository,
                               HashRecordRepository hashRecordRepository,
                               CustodyLogRepository custodyLogRepository,
                               UserRepository userRepository,
                               EvidenceAccessService evidenceAccessService) {
        this.evidenceRepository = evidenceRepository;
        this.hashRecordRepository = hashRecordRepository;
        this.custodyLogRepository = custodyLogRepository;
        this.userRepository = userRepository;
        this.evidenceAccessService = evidenceAccessService;
    }

    @Transactional(readOnly = true)
    public List<EvidenceListItemResponse> listEvidence(String caseId, String status) {
        return listEvidence(caseId, status, null);
    }

    @Transactional(readOnly = true)
    public List<EvidenceListItemResponse> listEvidence(String caseId, String status, String investigatorUserId) {
        List<Evidence> evidenceList;

        boolean hasCase = caseId != null && !caseId.isBlank();
        boolean hasStatus = status != null && !status.isBlank();

        boolean hasInvestigatorScope = investigatorUserId != null && !investigatorUserId.isBlank();

        if (hasCase && hasStatus && hasInvestigatorScope) {
            EvidenceStatus parsed = parseStatus(status);
            evidenceList = evidenceRepository.findByUploadedBy_UserIdAndCaseIdContainingIgnoreCaseAndStatusOrderByUploadTimestampDesc(
                    investigatorUserId,
                    caseId.trim(),
                    parsed
            );
        } else if (hasCase && hasInvestigatorScope) {
            evidenceList = evidenceRepository.findByUploadedBy_UserIdAndCaseIdContainingIgnoreCaseOrderByUploadTimestampDesc(
                    investigatorUserId,
                    caseId.trim()
            );
        } else if (hasStatus && hasInvestigatorScope) {
            evidenceList = evidenceRepository.findByUploadedBy_UserIdAndStatusOrderByUploadTimestampDesc(
                    investigatorUserId,
                    parseStatus(status)
            );
        } else if (hasInvestigatorScope) {
            evidenceList = evidenceRepository.findByUploadedBy_UserIdOrderByUploadTimestampDesc(investigatorUserId);
        } else if (hasCase && hasStatus) {
            EvidenceStatus parsed = parseStatus(status);
            evidenceList = evidenceRepository.findByCaseIdContainingIgnoreCaseAndStatusOrderByUploadTimestampDesc(caseId.trim(), parsed);
        } else if (hasCase) {
            evidenceList = evidenceRepository.findByCaseIdContainingIgnoreCaseOrderByUploadTimestampDesc(caseId.trim());
        } else if (hasStatus) {
            evidenceList = evidenceRepository.findByStatusOrderByUploadTimestampDesc(parseStatus(status));
        } else {
            evidenceList = evidenceRepository.findAllByOrderByUploadTimestampDesc();
        }

        return evidenceList.stream()
                .map(ev -> new EvidenceListItemResponse(
                        ev.getEvidenceId(),
                        ev.getCaseId(),
                        ev.getFileType(),
                        ev.getFileSize(),
                        ev.getStatus().name(),
                        ev.getUploadTimestamp()
                ))
                .toList();
    }

    @Transactional
    public EvidenceDetailsResponse getEvidenceDetails(String evidenceId,
                                                      String actorUserId,
                                                      String ipAddress) {
        return getEvidenceDetails(evidenceId, actorUserId, ipAddress, true);
    }

    @Transactional
    public EvidenceDetailsResponse getEvidenceDetails(String evidenceId,
                                                      String actorUserId,
                                                      String ipAddress,
                                                      boolean investigatorOnlyOwnerAccess) {
        Evidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Evidence not found: " + evidenceId));

        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                
        if (actor.getRole() == Role.INVESTIGATOR) {
            if (investigatorOnlyOwnerAccess && !actor.getUserId().equals(evidence.getUploadedBy().getUserId())) {
                throw new IllegalArgumentException("You can view only evidence uploaded by you");
            }
        } else if (actor.getRole() != Role.LEGAL_OFFICER) {
            throw new IllegalArgumentException("You do not have permission to view evidence details");
        }

        HashRecord hashRecord = hashRecordRepository.findByEvidenceEvidenceId(evidenceId)
                .orElseThrow(() -> new IllegalStateException("Hash record missing for evidence " + evidenceId));

        evidenceAccessService.recordEvidenceAction(
                evidenceId,
                actor.getUserId(),
                ActionType.VIEW,
                ipAddress
        );

        List<EvidenceDetailsResponse.CustodyEventResponse> events = custodyLogRepository
                .findTop10ByEvidenceEvidenceIdOrderByTimestampDesc(evidenceId)
                .stream()
                .map(this::toCustodyEvent)
                .toList();

        return new EvidenceDetailsResponse(
                evidence.getEvidenceId(),
                evidence.getCaseId(),
                evidence.getDescription(),
                evidence.getFileType(),
                evidence.getFileSize(),
                evidence.getStatus().name(),
                evidence.getUploadTimestamp(),
                evidence.getUploadedBy().getName(),
                hashRecord.getHashValue(),
                hashRecord.getAlgorithm(),
                events
        );
    }

    private EvidenceStatus parseStatus(String status) {
        try {
            return EvidenceStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }
    }

    private EvidenceDetailsResponse.CustodyEventResponse toCustodyEvent(CustodyLog log) {
        return new EvidenceDetailsResponse.CustodyEventResponse(
                log.getActionType().name(),
                log.getActor().getName(),
                log.getActorRole().name(),
                log.getIpAddress(),
                log.getTimestamp()
        );
    }
}
