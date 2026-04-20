package com.decms.common.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SuspiciousAccessListener implements EvidenceEventListener {

    private static final Logger log = LoggerFactory.getLogger(SuspiciousAccessListener.class);

    @Override
    public void onEvent(String eventType, String evidenceId, String message) {
        if (!"SUSPICIOUS_ACCESS".equals(eventType)) {
            return;
        }

        log.warn("Suspicious access event for evidence {}: {}", evidenceId, message);
    }
}
