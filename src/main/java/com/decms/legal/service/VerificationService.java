package com.decms.legal.service;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.decms.common.decorator.EvidenceAccessService;
import com.decms.common.observer.EvidenceEventPublisher;
import com.decms.common.service.HashService;
import com.decms.legal.dto.VerificationResponse;
import com.decms.model.ActionType;
import com.decms.model.Evidence;
import com.decms.model.HashRecord;
import com.decms.repository.EvidenceRepository;
import com.decms.repository.HashRecordRepository;

@Service
public class VerificationService {

    private final EvidenceEventPublisher eventPublisher;
    private final EvidenceRepository evidenceRepository;
    private final HashRecordRepository hashRecordRepository;
    private final HashService hashService;
    private final EvidenceAccessService evidenceAccessService;

    public VerificationService(EvidenceEventPublisher eventPublisher,
                               EvidenceRepository evidenceRepository,
                               HashRecordRepository hashRecordRepository,
                               HashService hashService,
                               EvidenceAccessService evidenceAccessService) {
        this.eventPublisher = eventPublisher;
        this.evidenceRepository = evidenceRepository;
        this.hashRecordRepository = hashRecordRepository;
        this.hashService = hashService;
        this.evidenceAccessService = evidenceAccessService;
    }

    @Transactional
    public VerificationResponse verifyEvidenceIntegrity(String evidenceId,
                                                       String actorUserId,
                                                       String ipAddress) {
        Evidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Evidence not found: " + evidenceId));

        HashRecord hashRecord = hashRecordRepository.findByEvidenceEvidenceId(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Stored hash not found for evidence: " + evidenceId));

        String storedHash = hashRecord.getHashValue();
        String computedHash = hashService.generateHash(Path.of(evidence.getFilePath()));
        boolean isMatch = storedHash.equals(computedHash);

        VerificationResponse response = new VerificationResponse();
        response.setEvidenceId(evidenceId);
        response.setCaseNumber(evidence.getCaseId());
        response.setStoredHash(storedHash);
        response.setComputedHash(computedHash);
        response.setIntegrityVerified(isMatch);
        response.setStatusMessage(isMatch
                ? "Integrity verified. Stored and computed hashes match."
                : "Tampering detected. Stored and computed hashes do not match.");

        evidenceAccessService.recordEvidenceAction(
                evidenceId,
                actorUserId,
                ActionType.VERIFY,
                ipAddress
        );

        if (!isMatch) {
            eventPublisher.publish(
                    "TAMPERING_DETECTED",
                    evidenceId,
                    "Verification failed due to hash mismatch.");
        }

        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, String> getAvailableEvidenceIds() {
        List<Evidence> evidenceList = evidenceRepository.findAllByOrderByUploadTimestampDesc();
        Map<String, String> result = new LinkedHashMap<>();
        for (Evidence evidence : evidenceList) {
            result.put(evidence.getEvidenceId(), evidence.getCaseId());
        }
        return result;
    }
}
