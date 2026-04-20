package com.decms.decorator;

import com.decms.model.ActionType;
import com.decms.model.CustodyLog;
import com.decms.model.Evidence;
import com.decms.model.User;
import com.decms.repository.CustodyLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Decorator for EvidenceAccessService that adds automatic custody logging.
 *
 * Design Pattern: DECORATOR (Structural Pattern)
 * Wraps EvidenceAccessService to add custody logging without modifying the base service.
 * Demonstrates cross-cutting concern: logging is added transparently.
 *
 * Design Principle: Open/Closed Principle (OCP)
 * New logging behaviors can be added by creating new decorators without modifying existing code.
 * The base service is closed for modification but open for extension via decorators.
 *
 * UC Mapping:
 * - UC-02: Automatically maintains custody log for every evidence access
 * - UC-08: Creates audit trail for custody history viewing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoggingDecorator implements EvidenceAccessService {

    private final BaseEvidenceAccessService baseService;
    private final CustodyLogRepository custodyLogRepository;

    /**
     * View evidence and log the access in custody chain.
     */
    @Override
    public Evidence viewEvidence(Long evidenceId, User actor, String ipAddress) throws Exception {
        Evidence result = baseService.viewEvidence(evidenceId, actor, ipAddress);

        // Automatically log this access action
        logCustodyAction(
                evidenceId,
                actor,
                ActionType.VIEW,
                ipAddress,
                "Evidence viewed by " + actor.getFullName()
        );

        log.debug("Custody log entry created for VIEW action on evidence {}", evidenceId);
        return result;
    }

    /**
     * Verify evidence integrity and log the verification in custody chain.
     */
    @Override
    public boolean verifyEvidenceIntegrity(Long evidenceId, User actor, String ipAddress) throws Exception {
        boolean result = baseService.verifyEvidenceIntegrity(evidenceId, actor, ipAddress);

        // Log verification action
        logCustodyAction(
                evidenceId,
                actor,
                ActionType.VERIFY,
                ipAddress,
                "Evidence integrity verified. Result: " + (result ? "PASSED" : "FAILED")
        );

        return result;
    }

    /**
     * Transfer evidence and log the transfer in custody chain.
     */
    @Override
    public void transferEvidence(Long evidenceId, User fromActor, User toActor, String ipAddress) throws Exception {
        baseService.transferEvidence(evidenceId, fromActor, toActor, ipAddress);

        // Log transfer action
        logCustodyAction(
                evidenceId,
                fromActor,
                ActionType.TRANSFER,
                ipAddress,
                "Evidence transferred to " + toActor.getFullName()
        );

        log.debug("Custody log entry created for TRANSFER action on evidence {}", evidenceId);
    }

    /**
     * Seal evidence and log the sealing in custody chain.
     */
    @Override
    public void sealEvidence(Long evidenceId, User actor, String ipAddress) throws Exception {
        baseService.sealEvidence(evidenceId, actor, ipAddress);

        // Log seal action
        logCustodyAction(
                evidenceId,
                actor,
                ActionType.SEAL,
                ipAddress,
                "Evidence sealed/hashed to prevent tampering"
        );

        log.debug("Custody log entry created for SEAL action on evidence {}", evidenceId);
    }

    /**
     * Helper method to create and persist a custody log entry.
     * Demonstrates SRP: custody logging is isolated in this method.
     */
    private void logCustodyAction(Long evidenceId, User actor, ActionType action, String ipAddress, String details) {
        CustodyLog log = CustodyLog.createEntry(
                evidenceId,
                actor,
                action,
                ipAddress,
                details
        );

        custodyLogRepository.save(log);
        log.debug("Custody log saved: Evidence={}, Action={}, Actor={}", evidenceId, action, actor.getUsername());
    }
}
