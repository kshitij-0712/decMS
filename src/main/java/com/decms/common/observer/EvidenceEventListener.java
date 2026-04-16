package com.decms.common.observer;

public interface EvidenceEventListener {
    void onEvent(String eventType, String evidenceId, String message);
}
