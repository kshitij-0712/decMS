package com.decms.decorator;

import com.decms.model.Evidence;
import com.decms.model.User;

/**
 * Interface for evidence access service.
 * Demonstrates OCP: implementations can be extended or decorated without modification.
 *
 * Design Pattern: Strategy/Template for decorator to work on.
 */
public interface EvidenceAccessService {

    /**
     * View evidence details.
     */
    Evidence viewEvidence(Long evidenceId, User actor, String ipAddress) throws Exception;

    /**
     * Verify evidence integrity.
     */
    boolean verifyEvidenceIntegrity(Long evidenceId, User actor, String ipAddress) throws Exception;

    /**
     * Transfer evidence to another party.
     */
    void transferEvidence(Long evidenceId, User fromActor, User toActor, String ipAddress) throws Exception;

    /**
     * Seal/protect evidence from modification.
     */
    void sealEvidence(Long evidenceId, User actor, String ipAddress) throws Exception;
}
