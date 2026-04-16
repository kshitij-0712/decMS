package com.decms.legal.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.decms.common.observer.EvidenceEventPublisher;
import com.decms.legal.dto.VerificationResponse;

@Service
public class VerificationService {

    private final EvidenceEventPublisher eventPublisher;
    private final Map<String, String> storedHashes = new HashMap<>();

    public VerificationService(EvidenceEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        seedSampleHashes();
    }

    public VerificationResponse verifyEvidenceIntegrity(String evidenceId) {
        String storedHash = storedHashes.get(evidenceId);
        VerificationResponse response = new VerificationResponse();
        response.setEvidenceId(evidenceId);

        if (storedHash == null) {
            response.setIntegrityVerified(false);
            response.setStatusMessage("No stored hash found for the selected evidence.");
            return response;
        }

        String computedHash = computeDeterministicHash(evidenceId);
        boolean isMatch = storedHash.equals(computedHash);

        response.setStoredHash(storedHash);
        response.setComputedHash(computedHash);
        response.setIntegrityVerified(isMatch);
        response.setStatusMessage(isMatch
                ? "Integrity verified. Stored and computed hashes match."
                : "Tampering detected. Stored and computed hashes do not match.");

        if (!isMatch) {
            eventPublisher.publish(
                    "TAMPERING_DETECTED",
                    evidenceId,
                    "Verification failed due to hash mismatch.");
        }

        return response;
    }

    public Map<String, String> getAvailableEvidenceIds() {
        return Map.copyOf(storedHashes);
    }

    private String computeDeterministicHash(String evidenceId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] output = digest.digest((evidenceId + "::evidence-content").getBytes(StandardCharsets.UTF_8));
            return bytesToHex(output);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte singleByte : bytes) {
            builder.append(String.format("%02x", singleByte));
        }
        return builder.toString();
    }

    private void seedSampleHashes() {
        storedHashes.put("EVD-1001", computeDeterministicHash("EVD-1001"));
        storedHashes.put("EVD-1002", computeDeterministicHash("EVD-1002") + "ff");
        storedHashes.put("EVD-1003", computeDeterministicHash("EVD-1003"));
    }
}
