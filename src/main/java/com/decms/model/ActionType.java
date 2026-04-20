package com.decms.model;

/**
 * Enumeration for custody log action types.
 * Demonstrates OCP: new action types can be added without modifying existing code.
 */
public enum ActionType {
    UPLOAD,           // Evidence uploaded to system
    SEAL,            // Evidence sealed/hashed
    VIEW,            // Evidence viewed
    TRANSFER,        // Evidence transferred to another party
    VERIFY,          // Evidence integrity verified
    ARCHIVE,         // Evidence archived
    EXPORT,          // Evidence exported
    ACCESS_REQUEST,  // Access requested
    ACCESS_GRANTED,  // Access granted
    ACCESS_DENIED;   // Access denied
}
