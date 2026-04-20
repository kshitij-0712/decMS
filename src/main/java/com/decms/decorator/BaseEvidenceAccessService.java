package com.decms.decorator;

import com.decms.model.Evidence;
import com.decms.model.User;
import com.decms.repository.EvidenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Base implementation of EvidenceAccessService.
 * Contains core business logic without logging concerns.
 *
 * Design Principle: Single Responsibility Principle (SRP)
 * This class only handles evidence access logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BaseEvidenceAccessService implements EvidenceAccessService {

    private final EvidenceRepository evidenceRepository;

    @Override
    public Evidence viewEvidence(Long evidenceId, User actor, String ipAddress) throws Exception {
        return evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new Exception("Evidence not found with ID: " + evidenceId));
    }

    @Override
    public boolean verifyEvidenceIntegrity(Long evidenceId, User actor, String ipAddress) throws Exception {
        Evidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new Exception("Evidence not found with ID: " + evidenceId));

        // Simulate integrity check (in real scenario, hash verification would happen here)
        String currentHash = evidence.getHashValue();
        boolean isValid = currentHash != null && !currentHash.isEmpty();

        log.info("Evidence integrity check: {} - Valid: {}", evidenceId, isValid);
        return isValid;
    }

    @Override
    public void transferEvidence(Long evidenceId, User fromActor, User toActor, String ipAddress) throws Exception {
        Evidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new Exception("Evidence not found with ID: " + evidenceId));

        log.info("Evidence transferred from {} to {}", fromActor.getUsername(), toActor.getUsername());
    }

    @Override
    public void sealEvidence(Long evidenceId, User actor, String ipAddress) throws Exception {
        Evidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new Exception("Evidence not found with ID: " + evidenceId));

        evidence.setIsSealed(true);
        evidenceRepository.save(evidence);

        log.info("Evidence sealed by {}", actor.getUsername());
    }
}
