package com.decms.forensic.service;

import com.decms.model.ActionType;
import com.decms.model.CustodyLog;
import com.decms.model.Evidence;
import com.decms.model.Role;
import com.decms.model.User;
import com.decms.repository.CustodyLogRepository;
import com.decms.repository.EvidenceRepository;
import com.decms.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustodyLogService {

    private final CustodyLogRepository custodyLogRepository;
    private final EvidenceRepository evidenceRepository;
    private final UserRepository userRepository;

    public CustodyLogService(CustodyLogRepository custodyLogRepository,
                            EvidenceRepository evidenceRepository,
                            UserRepository userRepository) {
        this.custodyLogRepository = custodyLogRepository;
        this.evidenceRepository = evidenceRepository;
        this.userRepository = userRepository;
    }

    public CustodyLog createEntry(String evidenceId,
                                  String actorId,
                                  Role actorRole,
                                  ActionType actionType,
                                  String ipAddress) {
        Evidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Evidence not found: " + evidenceId));

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new IllegalArgumentException("Actor not found: " + actorId));

        CustodyLog log = new CustodyLog();
        log.setEvidence(evidence);
        log.setActor(actor);
        log.setActorRole(actorRole);
        log.setActionType(actionType);
        log.setIpAddress(ipAddress);
        log.setSuspicious(false);

        return custodyLogRepository.save(log);
    }

    public List<CustodyLog> getRecentForEvidence(String evidenceId) {
        return custodyLogRepository.findTop10ByEvidenceEvidenceIdOrderByTimestampDesc(evidenceId);
    }
}
