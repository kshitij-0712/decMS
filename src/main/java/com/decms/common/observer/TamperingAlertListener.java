package com.decms.common.observer;

import com.decms.model.CustodyLog;
import com.decms.model.Evidence;
import com.decms.model.TamperingAlert;
import com.decms.repository.CustodyLogRepository;
import com.decms.repository.EvidenceRepository;
import com.decms.repository.TamperingAlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class TamperingAlertListener implements EvidenceEventListener {

    private static final Logger log = LoggerFactory.getLogger(TamperingAlertListener.class);
    
    private final TamperingAlertRepository tamperingAlertRepository;
    private final EvidenceRepository evidenceRepository;
    private final CustodyLogRepository custodyLogRepository;

    public TamperingAlertListener(TamperingAlertRepository tamperingAlertRepository,
                                  EvidenceRepository evidenceRepository,
                                  CustodyLogRepository custodyLogRepository) {
        this.tamperingAlertRepository = tamperingAlertRepository;
        this.evidenceRepository = evidenceRepository;
        this.custodyLogRepository = custodyLogRepository;
    }

    @Override
    public void onEvent(String eventType, String evidenceId, String message) {
        if (!"TAMPERING_DETECTED".equals(eventType)) {
            return;
        }

        log.warn("Tampering alert raised for evidence {}: {}", evidenceId, message);
        
        evidenceRepository.findById(evidenceId).ifPresent(evidence -> {
            TamperingAlert alert = new TamperingAlert();
            alert.setEvidence(evidence);
            alert.setDetectedAt(LocalDateTime.now());
            alert.setStoredHash("N/A"); // Ideally passed in message or retrieved
            alert.setComputedHash("N/A");
            alert.setNotifiedTo("ADMIN, FORENSIC_ANALYST");
            tamperingAlertRepository.save(alert);
        });

        // Mark the most recent custody log for this evidence as suspicious
        List<CustodyLog> recentLogs = custodyLogRepository.findTop10ByEvidenceEvidenceIdOrderByTimestampDesc(evidenceId);
        if (!recentLogs.isEmpty()) {
            CustodyLog recentLog = recentLogs.get(0);
            recentLog.setSuspicious(true);
            custodyLogRepository.save(recentLog);
        }
    }
}
