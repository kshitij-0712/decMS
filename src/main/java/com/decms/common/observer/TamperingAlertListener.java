package com.decms.common.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TamperingAlertListener implements EvidenceEventListener {

    private static final Logger log = LoggerFactory.getLogger(TamperingAlertListener.class);

    @Override
    public void onEvent(String eventType, String evidenceId, String message) {
        if (!"TAMPERING_DETECTED".equals(eventType)) {
            return;
        }

        log.warn("Tampering alert raised for evidence {}: {}", evidenceId, message);
    }
}
