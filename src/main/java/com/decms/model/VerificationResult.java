package com.decms.model;

public class VerificationResult {

    private final boolean valid;
    private final String storedHash;
    private final String computedHash;
    private final String message;

    public VerificationResult(boolean valid, String storedHash, String computedHash, String message) {
        this.valid = valid;
        this.storedHash = storedHash;
        this.computedHash = computedHash;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public String getStoredHash() {
        return storedHash;
    }

    public String getComputedHash() {
        return computedHash;
    }

    public String getMessage() {
        return message;
    }
}
