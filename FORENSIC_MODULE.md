# Forensic Module - Implementation Summary

## Overview

This is the **Forensic Module** implementation for the Digital Evidence & Chain-of-Custody Management System. 

**Owner**: Kishore H N (PES2UG23CS278)  
**Role**: Forensic Analyst  
**Module**: `forensic/`

## Owned Use Cases

| Use Case | Description |
|----------|-------------|
| **UC-02** (Major) | Maintain Chain-of-Custody Log |
| **UC-08** (Minor) | View Custody History |

## Key Features Implemented

### 1. **Custody Log Management (UC-02)**
- Automatic logging of every evidence access
- Tracks: actor, action, timestamp, IP address, details
- Supports multiple action types (VIEW, TRANSFER, VERIFY, SEAL, etc.)
- Immutable audit trail

### 2. **Custody History Viewing (UC-08)**
- View complete custody chain for any evidence
- Pagination support for large datasets
- Timeline visualization
- Summary statistics (total accesses, unique actors, suspicious entries)
- Date range filtering

### 3. **Anomaly Detection**
- Detects high-frequency access patterns
- Flags suspicious activities
- Anomaly reasoning and explanations
- Separate anomalies dashboard

## Design Patterns & Principles

### Design Pattern: DECORATOR (Structural)
**Location**: `com.decms.common.decorator` package

```
EvidenceAccessService (Interface)
  ↓
BaseEvidenceAccessService (Core Implementation)
  ↓
LoggingDecorator (Wraps with automatic logging)
```

Every evidence access operation is transparently decorated with custody logging without modifying the core service logic.

### Design Principle: Open/Closed Principle (OCP)
- **Open for Extension**: New action types can be added to `ActionType` enum
- **Closed for Modification**: Existing logging pipeline doesn't change
- The decorator pattern demonstrates OCP: new logging behaviors can be added via decorators

### Additional Principles Applied
- **Single Responsibility Principle (SRP)**
  - `CustodyLogService` -> custody logging and retrieval only
  - `LoggingDecorator` -> custody decoration only
  - `BaseEvidenceAccessService` -> base evidence action contract only

- **Liskov Substitution Principle (LSP)**
  - All user types (Investigator, ForensicAnalyst, etc.) are interchangeable

- **Dependency Inversion Principle (DIP)**
  - Controllers depend on service interfaces
  - Services injected via Spring

## Project Structure

```
src/main/java/com/decms/
├── forensic/controller/
│   └── ForensicController.java              # Forensic endpoints
├── forensic/service/
│   └── CustodyLogService.java               # Custody logging + retrieval
├── common/decorator/
│   ├── EvidenceAccessService.java           # Interface
│   ├── BaseEvidenceAccessService.java       # Core implementation
│   └── LoggingDecorator.java                # Decorator with logging
├── model/
│   ├── User.java                            # Base user entity
│   ├── Role.java                            # Role enum
│   ├── UserStatus.java                      # User status enum
│   ├── CustodyLog.java                      # Core entity
│   ├── Evidence.java                        # Evidence entity
│   └── ActionType.java                      # Enum
├── repository/
│   ├── CustodyLogRepository.java            # JPA repository
│   ├── UserRepository.java                  # JPA repository
│   ├── EvidenceRepository.java              # JPA repository
├── config/
│   └── SecurityConfig.java                  # Spring Security setup
└── DigitalEvidenceApplication.java          # Main Spring Boot class

src/main/resources/
├── application.properties                   # App configuration
├── schema.sql                               # Database schema
└── templates/forensic/
    ├── custody-dashboard.html               # Main dashboard
    ├── custody-log-entry.html               # Custody log view
    ├── custody-history.html                 # History timeline
    └── flag-anomaly.html                    # Anomalies view
```

## API Endpoints

### Forensic Controller (`/forensic`)

| Endpoint | Method | Use Case | Description |
|----------|--------|----------|-------------|
| `/forensic/custody` | GET | UC-02 | Custody dashboard with suspicious logs |
| `/forensic/custody/{evidenceId}` | GET | UC-02 | Detailed custody log for an evidence |
| `/forensic/history/{evidenceId}` | GET | UC-08 | Complete custody history with timeline |
| `/forensic/anomalies` | GET | - | Flagged suspicious activities |
| `/forensic/api/custody/{evidenceId}` | GET | - | JSON API for custody logs |
| `/forensic/api/history-summary/{evidenceId}` | GET | - | JSON API for history summary |

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Framework | Spring Boot 3.x |
| Web | Spring MVC + Thymeleaf |
| Database | H2 (file) + JPA/Hibernate |
| Security | Spring Security + BCrypt |
| Build | Maven |

## Database Schema

### Core Tables

**`users`** - RBAC support with role-based inheritance
- Stores all user types (Investigator, Forensic, Legal, Admin)
- Single-table inheritance pattern

**`custody_logs`** - Chain of custody tracking
- Records every evidence access
- Indexes on `evidence_id`, `actor_id`, `timestamp`
- Flags suspicious entries

**`evidence`** - Digital evidence items
- References uploaded evidence
- Stores hash values, file paths, metadata

**`hash_records`** - Integrity verification
- Records hash verification for audit trail

**`tampering_alerts`** - Anomaly detection
- Flags suspicious patterns

## Decorator Pattern Example

```java
// Without decorator - no custody side effect
baseEvidenceAccessService.recordEvidenceAction(evidenceId, actorId, ActionType.VIEW, ipAddress);

// With LoggingDecorator - transparently logs via custody service
loggingDecorator.recordEvidenceAction(evidenceId, actorId, ActionType.VIEW, ipAddress);
```

## Testing & Usage

### Sample Users (Pre-loaded)
```
Forensic User:
  Username: foren-001
  Password: password (BCrypt hashed)
  Role: FORENSIC_ANALYST
```

### How to Run
1. Configure datasource in `src/main/resources/application.properties`
2. Run `DigitalEvidenceApplication.java`
3. Access `http://localhost:8080/forensic/custody`

## Integration with Other Modules

- **Investigator Module**: Uploads evidence → triggers custody logging
- **Legal Module**: Requests access → logged in custody chain
- **Admin Module**: Views audit reports from custody logs

## Future Enhancements

- [ ] Real-time anomaly alerting
- [ ] Advanced pattern detection (ML-based)
- [ ] Export custody reports to PDF
- [ ] Email notifications for suspicious activities
- [ ] Advanced search with filters
- [ ] Analytics dashboard

## Compliance & Audit

✓ Immutable audit trail  
✓ Complete chain of custody  
✓ Tamper detection  
✓ Role-based access control  
✓ IP tracking  
✓ Timestamp validation  

---

**Last Updated**: April 21, 2026  
**Status**: Implementation Complete ✓
