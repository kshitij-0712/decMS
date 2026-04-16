package com.decms.common.observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

@Component
public class EvidenceEventPublisher {

    private final List<EvidenceEventListener> listeners = new CopyOnWriteArrayList<>();

    public EvidenceEventPublisher(List<EvidenceEventListener> discoveredListeners) {
        if (discoveredListeners != null) {
            listeners.addAll(discoveredListeners);
        }
    }

    public void registerListener(EvidenceEventListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void unregisterListener(EvidenceEventListener listener) {
        listeners.remove(listener);
    }

    public void publish(String eventType, String evidenceId, String message) {
        for (EvidenceEventListener listener : listeners) {
            listener.onEvent(eventType, evidenceId, message);
        }
    }
}
