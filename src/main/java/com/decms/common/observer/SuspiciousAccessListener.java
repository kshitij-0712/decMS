package com.decms.common.observer;

import com.decms.repository.CustodyLogRepository;
import com.decms.model.CustodyLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SuspiciousAccessListener implements EvidenceEventListener {

    private static final Logger log = LoggerFactory.getLogger(SuspiciousAccessListener.class);
    
    private final CustodyLogRepository custodyLogRepository;

    public SuspiciousAccessListener(CustodyLogRepository custodyLogRepository) {
        this.custodyLogRepository = custodyLogRepository;
    }

    @Override
    public void onEvent(String eventType, String evidenceId, String message) {
        if (!"SUSPICIOUS_ACCESS".equals(eventType)) {
            return;
        }

        log.warn("Suspicious access event for evidence {}: {}", evidenceId, message);
        
        // Mark the most recent custody log for this evidence as suspicious
        List<CustodyLog> recentLogs = custodyLogRepository.findTop10ByEvidenceEvidenceIdOrderByTimestampDesc(evidenceId);
        if (!recentLogs.isEmpty()) {
            CustodyLog recentLog = recentLogs.get(0);
            recentLog.setSuspicious(true);
            custodyLogRepository.save(recentLog);
        }
    }
}
