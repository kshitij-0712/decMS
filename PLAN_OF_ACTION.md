# Plan of action: Digital Evidence & Chain-of-Custody Management System

## Team & ownership mapping

| Member | PRN | Role subfolder | Major use case (1 each) | Minor use case (1 each) |
|--------|-----|----------------|------------------------|------------------------|
| Kshitij | PES2UG23CS290 | `investigator/` | UC-01 Upload & Seal Digital Evidence | UC-06 View Evidence Details |
| Kishore H N | PES2UG23CS278 | `forensic/` | UC-02 Maintain Chain-of-Custody Log | UC-08 View Custody History |
| Likith N | PES2UG23CS306 | `legal/` | UC-03 Verify Evidence Integrity | UC-05 Request Evidence Access |
| Khizer Pasha | PES2UG23CS275 | `admin/` | UC-04 Generate Audit Report | UC-07 Manage Users & Roles |

> **Guideline check**: 4 major + 4 minor = 8 use cases. 1 major + 1 minor per member. Each member owns full-stack (UI + backend + DB) for their use cases.

---

## 1. Technology stack

| Layer | Technology | Justification |
|-------|-----------|---------------|
| **Framework** | Spring Boot 3.x + Spring MVC | MVC architecture pattern (mandatory) |
| **View** | Thymeleaf templates | Server-side rendering, desktop/web app |
| **Database** | MySQL 8 / PostgreSQL | Relational DB for structured evidence metadata |
| **ORM** | Spring Data JPA + Hibernate | Persistence layer |
| **Security** | Spring Security + BCrypt | RBAC authentication & role-based authorization |
| **Hashing** | Java `MessageDigest` (SHA-256) | Cryptographic hash for evidence sealing |
| **PDF export** | iText / Apache PDFBox | UC-04 audit report export |
| **File storage** | Local filesystem (configurable) | Evidence file storage |
| **Build tool** | Maven | Dependency management |
| **Version control** | Git + GitHub | Public repository (mandatory for submission) |

---

## 2. Architecture: MVC pattern

```
┌─────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                     │
│         Thymeleaf Templates (.html) per role             │
│  investigator/  │  forensic/  │  legal/  │  admin/       │
└────────┬────────┴──────┬──────┴────┬─────┴──────┬───────┘
         │               │           │             │
┌────────▼───────────────▼───────────▼─────────────▼──────┐
│                    CONTROLLER LAYER                       │
│  Spring MVC Controllers (1 per role subfolder)           │
│  EvidenceController │ CustodyController │ LegalController│
│  AdminController    │ AuthController (shared)            │
└────────┬────────────┴──────┬───────┴─────────────┬──────┘
         │                   │                     │
┌────────▼───────────────────▼─────────────────────▼──────┐
│                     SERVICE LAYER                         │
│  Business logic, design patterns applied here            │
│  EvidenceService │ CustodyService │ VerificationService  │
│  ReportService   │ UserService    │ HashService          │
└────────┬─────────┴───────┬────────┴──────────────┬──────┘
         │                 │                       │
┌────────▼─────────────────▼───────────────────────▼──────┐
│                    REPOSITORY LAYER                       │
│  Spring Data JPA Repositories                            │
│  EvidenceRepo │ HashRecordRepo │ CustodyLogRepo          │
│  AccessRequestRepo │ AuditReportRepo │ UserRepo          │
└────────┬─────────────────┬───────────────────────┬──────┘
         │                 │                       │
┌────────▼─────────────────▼───────────────────────▼──────┐
│                      DATABASE                            │
│  MySQL / PostgreSQL                                      │
│  Tables: users, evidence, hash_records, custody_logs,    │
│  access_requests, audit_reports, tampering_alerts        │
└─────────────────────────────────────────────────────────┘
```

> **Guideline check**: MVC architecture pattern (2 marks). Spring MVC enforces Controller → Service → Repository → DB separation.

---

## 3. Design patterns (4 required: creational + structural + behavioral + framework-enforced)

| # | Pattern | Type | Owner | Where applied |
|---|---------|------|-------|---------------|
| 1 | **Factory Method** | Creational | Kshitij | `UserFactory` — creates `Investigator`, `ForensicAnalyst`, `LegalOfficer`, `Administrator` objects based on role enum. Used during registration and user management. |
| 2 | **Decorator** | Structural | Kishore | `LoggingDecorator` wraps `EvidenceAccessService`. Every evidence operation (view, verify, transfer) is transparently decorated with custody log creation. The decorator adds UC-13, UC-14, UC-15 behavior without modifying the core service. |
| 3 | **Observer** | Behavioral | Likith | `TamperingAlertObserver` — when `VerificationService` detects a hash mismatch (UC-19), it notifies registered observers (Administrator notification service, audit log writer). Also used for UC-16 suspicious access alerts. |
| 4 | **MVC** | Framework-enforced | Khizer | Spring MVC itself — `@Controller` classes handle HTTP requests, Thymeleaf renders views, `@Service` classes contain business logic, `@Entity` classes are the model. |

> **Guideline check**: 4 patterns total. Creational (Factory Method) + Structural (Decorator) + Behavioral (Observer) + Framework-enforced (MVC). Each owned by a different member.

---

## 4. Design principles (4 required: 1 per member)

| # | Principle | Owner | How applied |
|---|-----------|-------|-------------|
| 1 | **Single Responsibility Principle (SRP)** | Kshitij | `EvidenceService` handles only evidence upload/seal logic. `HashService` handles only hashing. `FileStorageService` handles only file I/O. Each class has exactly one reason to change. |
| 2 | **Open/Closed Principle (OCP)** | Kishore | `CustodyLogService` is open for extension (new action types like ARCHIVE, EXPORT can be added via `ActionType` enum) but closed for modification — the logging pipeline doesn't change when new action types are introduced. The Decorator pattern on the access service also demonstrates OCP. |
| 3 | **Liskov Substitution Principle (LSP)** | Likith | All user subtypes (`Investigator`, `ForensicAnalyst`, `LegalOfficer`, `Administrator`) extend abstract `User` and can be used interchangeably wherever `User` is expected (e.g., in `CustodyLog.actorId` references, Spring Security's `UserDetails` implementation). |
| 4 | **Dependency Inversion Principle (DIP)** | Khizer | Controllers depend on `Service` interfaces, not concrete implementations. `ReportService` depends on `ReportFormatter` interface — swappable between `HtmlReportFormatter` and `PdfReportFormatter` without changing the service. All dependencies injected via Spring's `@Autowired`. |

> **Guideline check**: 4 principles, 1 per member. All from SOLID. Each demonstrated with concrete project-specific examples.

---

## 5. Project folder structure

```
digital-evidence-system/
│
├── pom.xml
├── README.md
│
├── src/main/java/com/decms/
│   │
│   ├── DigitalEvidenceApplication.java          # Spring Boot entry point
│   │
│   ├── config/
│   │   ├── SecurityConfig.java                  # Spring Security, RBAC setup
│   │   └── WebMvcConfig.java                    # MVC configuration
│   │
│   ├── model/                                   # SHARED entities (Model in MVC)
│   │   ├── User.java                            # Abstract base entity
│   │   ├── Role.java                            # Enum: INVESTIGATOR, FORENSIC_ANALYST, LEGAL_OFFICER, ADMINISTRATOR
│   │   ├── Evidence.java                        # Entity
│   │   ├── EvidenceStatus.java                  # Enum: SEALED, ANALYZED, VERIFIED, ARCHIVED
│   │   ├── HashRecord.java                      # Entity
│   │   ├── CustodyLog.java                      # Entity
│   │   ├── ActionType.java                      # Enum: UPLOAD, ACCESS, VERIFY, TRANSFER, VIEW
│   │   ├── AccessRequest.java                   # Entity
│   │   ├── RequestStatus.java                   # Enum: PENDING, APPROVED, DENIED
│   │   ├── AuditReport.java                     # Entity
│   │   ├── TamperingAlert.java                  # Entity
│   │   └── VerificationResult.java              # Entity/DTO
│   │
│   ├── repository/                              # SHARED Spring Data JPA repos
│   │   ├── UserRepository.java
│   │   ├── EvidenceRepository.java
│   │   ├── HashRecordRepository.java
│   │   ├── CustodyLogRepository.java
│   │   ├── AccessRequestRepository.java
│   │   ├── AuditReportRepository.java
│   │   └── TamperingAlertRepository.java
│   │
│   ├── common/                                  # SHARED utilities & patterns
│   │   ├── factory/
│   │   │   └── UserFactory.java                 # Creational: Factory Method
│   │   ├── decorator/
│   │   │   ├── EvidenceAccessService.java       # Interface
│   │   │   ├── BaseEvidenceAccessService.java   # Concrete implementation
│   │   │   └── LoggingDecorator.java            # Structural: Decorator
│   │   ├── observer/
│   │   │   ├── EvidenceEventPublisher.java      # Subject
│   │   │   ├── EvidenceEventListener.java       # Observer interface
│   │   │   ├── TamperingAlertListener.java      # Concrete observer
│   │   │   └── SuspiciousAccessListener.java    # Concrete observer
│   │   └── service/
│   │       ├── HashService.java                 # SHA-256 hashing utility
│   │       ├── FileStorageService.java          # File I/O utility
│   │       └── AuthService.java                 # Authentication shared service
│   │
│   │   # ─────────────────────────────────────────────
│   │   # ROLE-BASED SUBFOLDERS (each member owns one)
│   │   # Each contains Controller + Service + Views
│   │   # ─────────────────────────────────────────────
│   │
│   ├── investigator/                            # KSHITIJ (CS290)
│   │   ├── controller/
│   │   │   └── InvestigatorController.java      # @Controller for UC-01, UC-06
│   │   ├── service/
│   │   │   ├── EvidenceUploadService.java       # UC-01 business logic
│   │   │   └── EvidenceViewService.java         # UC-06 business logic
│   │   └── dto/
│   │       ├── EvidenceUploadRequest.java       # Form binding DTO
│   │       └── EvidenceDetailsResponse.java     # View response DTO
│   │
│   ├── forensic/                                # KISHORE H N (CS278)
│   │   ├── controller/
│   │   │   └── ForensicController.java          # @Controller for UC-02, UC-08
│   │   ├── service/
│   │   │   ├── CustodyLogService.java           # UC-02 business logic
│   │   │   └── CustodyHistoryService.java       # UC-08 business logic
│   │   └── dto/
│   │       ├── CustodyLogEntry.java             # DTO for log display
│   │       └── CustodyFilterRequest.java        # Filter/pagination DTO
│   │
│   ├── legal/                                   # LIKITH N (CS306)
│   │   ├── controller/
│   │   │   └── LegalController.java             # @Controller for UC-03, UC-05
│   │   ├── service/
│   │   │   ├── VerificationService.java         # UC-03 business logic
│   │   │   └── AccessRequestService.java        # UC-05 business logic
│   │   └── dto/
│   │       ├── VerificationResponse.java        # Verification result DTO
│   │       └── AccessRequestForm.java           # Access request form DTO
│   │
│   └── admin/                                   # KHIZER PASHA (CS275)
│       ├── controller/
│       │   └── AdminController.java             # @Controller for UC-04, UC-07
│       ├── service/
│       │   ├── ReportService.java               # UC-04 business logic
│       │   ├── ReportFormatter.java             # Interface (DIP)
│       │   ├── HtmlReportFormatter.java         # Concrete formatter
│       │   ├── PdfReportFormatter.java          # Concrete formatter (UC-23)
│       │   └── UserManagementService.java       # UC-07 business logic
│       └── dto/
│           ├── AuditReportResponse.java         # Report display DTO
│           └── UserManagementForm.java          # User CRUD form DTO
│
├── src/main/resources/
│   ├── application.properties                   # DB config, file storage path, server port
│   │
│   ├── templates/                               # Thymeleaf views (View in MVC)
│   │   ├── layout/
│   │   │   ├── base.html                        # Shared layout (navbar, sidebar, footer)
│   │   │   └── fragments.html                   # Reusable Thymeleaf fragments
│   │   │
│   │   ├── auth/
│   │   │   ├── login.html                       # Shared login page
│   │   │   └── access-denied.html               # 403 page
│   │   │
│   │   ├── investigator/                        # KSHITIJ's views
│   │   │   ├── upload-evidence.html             # UC-01: upload form
│   │   │   ├── upload-success.html              # UC-01: success confirmation
│   │   │   ├── evidence-list.html               # UC-06: evidence listing
│   │   │   └── evidence-details.html            # UC-06: detail view
│   │   │
│   │   ├── forensic/                            # KISHORE's views
│   │   │   ├── custody-dashboard.html           # UC-02: log monitoring dashboard
│   │   │   ├── custody-log-entry.html           # UC-02: individual log entry view
│   │   │   ├── custody-history.html             # UC-08: full history with filters
│   │   │   └── flag-anomaly.html                # UC-16: flag suspicious access
│   │   │
│   │   ├── legal/                               # LIKITH's views
│   │   │   ├── verify-integrity.html            # UC-03: verification page
│   │   │   ├── verification-result.html         # UC-03: result display (pass/fail)
│   │   │   ├── request-access.html              # UC-05: access request form
│   │   │   └── request-status.html              # UC-05: request tracking
│   │   │
│   │   └── admin/                               # KHIZER's views
│   │       ├── report-generator.html            # UC-04: report selection
│   │       ├── report-view.html                 # UC-04: formatted report display
│   │       ├── user-management.html             # UC-07: user CRUD dashboard
│   │       ├── user-form.html                   # UC-07: create/edit user form
│   │       └── access-requests.html             # UC-05: approval queue (admin side)
│   │
│   └── static/
│       ├── css/
│       │   └── styles.css                       # Shared CSS
│       ├── js/
│       │   └── app.js                           # Shared JS (form validation etc.)
│       └── images/
│           └── logo.png
│
├── src/main/resources/db/
│   └── schema.sql                               # Database schema DDL
│
└── src/test/java/com/decms/                     # Unit tests per module
    ├── investigator/
    ├── forensic/
    ├── legal/
    └── admin/
```

> **Guideline check**: Each role in its own subfolder with controller + service + views. Shared model/repository/patterns are in common packages. All merged into a single Spring Boot application.

---

## 6. Database schema

```sql
-- Users table (abstract User entity with role-based polymorphism)
CREATE TABLE users (
    user_id       VARCHAR(36) PRIMARY KEY,        -- UUID
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    department    VARCHAR(100),
    role          ENUM('INVESTIGATOR','FORENSIC_ANALYST','LEGAL_OFFICER','ADMINISTRATOR') NOT NULL,
    status        ENUM('ACTIVE','INACTIVE') DEFAULT 'ACTIVE',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Evidence table
CREATE TABLE evidence (
    evidence_id      VARCHAR(36) PRIMARY KEY,
    case_id          VARCHAR(50) NOT NULL,
    description      TEXT,
    file_type        VARCHAR(50) NOT NULL,
    file_size        BIGINT NOT NULL,
    file_path        VARCHAR(500) NOT NULL,
    status           ENUM('SEALED','ANALYZED','VERIFIED','ARCHIVED') DEFAULT 'SEALED',
    uploaded_by      VARCHAR(36) NOT NULL,
    upload_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (uploaded_by) REFERENCES users(user_id)
);

-- Hash records table (1:1 with evidence)
CREATE TABLE hash_records (
    hash_id      VARCHAR(36) PRIMARY KEY,
    evidence_id  VARCHAR(36) UNIQUE NOT NULL,
    hash_value   VARCHAR(64) NOT NULL,             -- SHA-256 = 64 hex chars
    algorithm    VARCHAR(20) DEFAULT 'SHA-256',
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (evidence_id) REFERENCES evidence(evidence_id)
);

-- Immutable custody logs table
CREATE TABLE custody_logs (
    log_id       VARCHAR(36) PRIMARY KEY,
    evidence_id  VARCHAR(36) NOT NULL,
    actor_id     VARCHAR(36) NOT NULL,
    actor_role   VARCHAR(30) NOT NULL,
    action_type  ENUM('UPLOAD','ACCESS','VERIFY','TRANSFER','VIEW') NOT NULL,
    ip_address   VARCHAR(45),
    timestamp    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_suspicious BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (evidence_id) REFERENCES evidence(evidence_id),
    FOREIGN KEY (actor_id) REFERENCES users(user_id)
);

-- Access requests table
CREATE TABLE access_requests (
    request_id   VARCHAR(36) PRIMARY KEY,
    evidence_id  VARCHAR(36) NOT NULL,
    requester_id VARCHAR(36) NOT NULL,
    reason       TEXT NOT NULL,
    case_number  VARCHAR(50),
    status       ENUM('PENDING','APPROVED','DENIED') DEFAULT 'PENDING',
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at  TIMESTAMP NULL,
    resolved_by  VARCHAR(36) NULL,
    FOREIGN KEY (evidence_id) REFERENCES evidence(evidence_id),
    FOREIGN KEY (requester_id) REFERENCES users(user_id),
    FOREIGN KEY (resolved_by) REFERENCES users(user_id)
);

-- Audit reports table
CREATE TABLE audit_reports (
    report_id    VARCHAR(36) PRIMARY KEY,
    case_id      VARCHAR(50) NOT NULL,
    generated_by VARCHAR(36) NOT NULL,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    content      LONGTEXT NOT NULL,
    format       ENUM('HTML','PDF') DEFAULT 'HTML',
    FOREIGN KEY (generated_by) REFERENCES users(user_id)
);

-- Tampering alerts table
CREATE TABLE tampering_alerts (
    alert_id      VARCHAR(36) PRIMARY KEY,
    evidence_id   VARCHAR(36) NOT NULL,
    detected_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    stored_hash   VARCHAR(64) NOT NULL,
    computed_hash VARCHAR(64) NOT NULL,
    notified_to   TEXT,                             -- JSON array of user IDs
    FOREIGN KEY (evidence_id) REFERENCES evidence(evidence_id)
);
```

---

## 7. Implementation plan — member-by-member

### 7A. Kshitij (CS290) — `investigator/` subfolder

**Major UC-01: Upload & Seal Digital Evidence**

| Step | Component | Details |
|------|-----------|---------|
| 1 | `InvestigatorController.java` | `GET /investigator/upload` → show form. `POST /investigator/upload` → handle multipart file + metadata |
| 2 | `EvidenceUploadService.java` | Validate file (type, size, integrity) — UC-09. Call `HashService.generateHash()` — UC-10. Save metadata via `EvidenceRepository` — UC-11. Set status = SEALED. Trigger `CustodyLogService.createEntry()` — UC-02 |
| 3 | `upload-evidence.html` | Thymeleaf form: file picker, case ID, description, evidence type dropdown |
| 4 | `upload-success.html` | Display evidence ID + SHA-256 hash + sealed status |
| 5 | Error handling | File validation fails → UC-12: redirect with error flash message. Duplicate hash → warn + confirm dialog. Network failure → transaction rollback |

**Minor UC-06: View Evidence Details**

| Step | Component | Details |
|------|-----------|---------|
| 1 | `InvestigatorController.java` | `GET /investigator/evidence` → list. `GET /investigator/evidence/{id}` → detail view |
| 2 | `EvidenceViewService.java` | Fetch metadata + hash + status + custody summary. Check access rights. Log view event via UC-02 |
| 3 | `evidence-list.html` | Table with search/filter by case ID, status |
| 4 | `evidence-details.html` | Full metadata, hash value, status badge, custody chain summary |

**Design pattern owned**: Factory Method (`UserFactory.java`)
**Design principle owned**: SRP

---

### 7B. Kishore H N (CS278) — `forensic/` subfolder

**Major UC-02: Maintain Chain-of-Custody Log**

| Step | Component | Details |
|------|-----------|---------|
| 1 | `ForensicController.java` | `GET /forensic/custody` → dashboard. `GET /forensic/custody/{evidenceId}` → entries for specific evidence |
| 2 | `CustodyLogService.java` | `createEntry(evidenceId, actorId, role, actionType, ipAddress)` — UC-13 + UC-14 + UC-15 combined. Policy rule engine to check suspicious patterns. Trigger `SuspiciousAccessListener` via Observer — UC-16 |
| 3 | `LoggingDecorator.java` | Wraps any `EvidenceAccessService` call → automatically creates custody log entry. Transparent to callers |
| 4 | `custody-dashboard.html` | Live view of recent log entries, flagged entries highlighted |
| 5 | Retry logic | If DB write fails → queue entry in-memory and retry (Spring `@Retryable`) |

**Minor UC-08: View Custody History**

| Step | Component | Details |
|------|-----------|---------|
| 1 | `ForensicController.java` | `GET /forensic/history/{evidenceId}` → full history with filters |
| 2 | `CustodyHistoryService.java` | Query `CustodyLogRepository` by evidenceId. Support filters: date range, actor, action type. Paginate with Spring `Pageable` |
| 3 | `custody-history.html` | Chronological table + filter controls + pagination. Flag anomaly button → POST to flag endpoint |

**Design pattern owned**: Decorator (`LoggingDecorator.java`)
**Design principle owned**: OCP

---

### 7C. Likith N (CS306) — `legal/` subfolder

**Major UC-03: Verify Evidence Integrity**

| Step | Component | Details |
|------|-----------|---------|
| 1 | `LegalController.java` | `GET /legal/verify` → evidence selection. `POST /legal/verify/{evidenceId}` → trigger verification |
| 2 | `VerificationService.java` | Retrieve stored hash — UC-18. Recompute hash of current file — UC-17. Compare. Match → log success. Mismatch → publish event via `EvidenceEventPublisher` → `TamperingAlertListener` fires UC-19. Log result via UC-02 |
| 3 | `verify-integrity.html` | Evidence selector dropdown/search |
| 4 | `verification-result.html` | Green "Integrity Verified ✓" or Red "TAMPERING DETECTED" with alert details |

**Minor UC-05: Request Evidence Access**

| Step | Component | Details |
|------|-----------|---------|
| 1 | `LegalController.java` | `GET /legal/access` → request form. `POST /legal/access` → submit request. `GET /legal/access/status` → track requests |
| 2 | `AccessRequestService.java` | Validate evidence state (SEALED/ANALYZED/VERIFIED allowed, ARCHIVED blocked). Create `AccessRequest` record. Notify admin. On approval → grant read-only access + log via UC-02 |
| 3 | `request-access.html` | Form: evidence selector, reason/purpose, case number |
| 4 | `request-status.html` | Table of submitted requests with status badges (PENDING/APPROVED/DENIED) |

**Design pattern owned**: Observer (`TamperingAlertListener.java`, `SuspiciousAccessListener.java`)
**Design principle owned**: LSP

---

### 7D. Khizer Pasha (CS275) — `admin/` subfolder

**Major UC-04: Generate Audit Report**

| Step | Component | Details |
|------|-----------|---------|
| 1 | `AdminController.java` | `GET /admin/reports` → case selector. `POST /admin/reports/generate` → build report. `GET /admin/reports/{id}/pdf` → PDF download |
| 2 | `ReportService.java` | Compile evidence timeline — UC-20. Retrieve custody records — UC-21. Delegate to `ReportFormatter` interface — UC-22. Optionally export PDF — UC-23 |
| 3 | `ReportFormatter.java` (interface) + `HtmlReportFormatter.java` + `PdfReportFormatter.java` | DIP: `ReportService` depends on interface, not concrete class. Easy to swap formats |
| 4 | `report-generator.html` | Form: case ID or evidence ID selector |
| 5 | `report-view.html` | Formatted audit report with timeline, custody records, verification history. "Export as PDF" button |

**Minor UC-07: Manage Users & Roles**

| Step | Component | Details |
|------|-----------|---------|
| 1 | `AdminController.java` | `GET /admin/users` → user list. `GET /admin/users/new` → create form. `POST /admin/users` → save. `PUT /admin/users/{id}` → update. `DELETE /admin/users/{id}` → deactivate |
| 2 | `UserManagementService.java` | Create user via `UserFactory`. Edit user info/role. Deactivate (guard: prevent last admin deactivation). Log all actions. `GET /admin/access-requests` → approval queue for UC-05 |
| 3 | `user-management.html` | User table with Create/Edit/Deactivate actions |
| 4 | `user-form.html` | Form: name, email, department, role dropdown |
| 5 | `access-requests.html` | Pending access requests table with Approve/Deny buttons |

**Design pattern owned**: MVC (framework-enforced)
**Design principle owned**: DIP

---

## 8. Integration points (how subfolders connect)

These are the cross-cutting interactions between roles that make the system whole:

| From | To | Mechanism | Trigger |
|------|----|-----------|---------|
| `investigator/` UC-01 | `forensic/` UC-02 | `CustodyLogService.createEntry()` | After evidence sealed, first custody log entry created |
| `investigator/` UC-06 | `forensic/` UC-02 | `LoggingDecorator` | Every view event auto-logged |
| `legal/` UC-03 | `forensic/` UC-02 | `CustodyLogService.createEntry()` | Verification event logged |
| `legal/` UC-03 | Observer pattern | `EvidenceEventPublisher.publish()` | Hash mismatch triggers `TamperingAlertListener` |
| `legal/` UC-05 | `admin/` UC-07 | `AccessRequest` entity | Legal officer submits → Admin approves/denies |
| `legal/` UC-05 | `forensic/` UC-02 | `CustodyLogService.createEntry()` | Access grant event logged |
| `admin/` UC-04 | `forensic/` UC-02 | `CustodyLogRepository` | Report reads all custody records |
| `admin/` UC-07 | `common/factory/` | `UserFactory.createUser()` | User creation uses Factory Method |
| All roles | `config/SecurityConfig` | Spring Security `@PreAuthorize` | Role-based URL access control |

---

## 9. Shared components (developed collaboratively)

These are built FIRST before role-specific work begins:

| Component | Responsibility | Built by |
|-----------|---------------|----------|
| `model/*.java` | All JPA entity classes | All (divide equally) |
| `repository/*.java` | All Spring Data interfaces | All (divide equally) |
| `SecurityConfig.java` | RBAC, login/logout, password encoding | Likith (admin owner) |
| `base.html` layout | Shared navbar, sidebar, footer | Any one member |
| `HashService.java` | SHA-256 generation + verification | Khizer + Kshitij (both use it) |
| `FileStorageService.java` | Save/retrieve evidence files | Khizer (primary), others use it |
| `CustodyLogService.java` | Central logging service | Kishore (owns it), others call it |
| `schema.sql` | Database DDL | All review, Likith finalizes |
| `application.properties` | DB connection, file paths, server config | All contribute |

---

## 10. Step-by-step execution order

### Phase A: Foundation (all members, days 1-3)

```
Step 1: Initialize Spring Boot project (start.spring.io)
        Dependencies: Spring Web, Spring Data JPA, Spring Security,
        Thymeleaf, MySQL Driver, Lombok, Spring Boot DevTools

Step 2: Create database + run schema.sql

Step 3: Implement all model/ entities with JPA annotations

Step 4: Implement all repository/ interfaces

Step 5: Implement SecurityConfig.java with role-based access

Step 6: Implement shared base.html Thymeleaf layout

Step 7: Implement AuthController + login.html

Step 8: Implement HashService.java + FileStorageService.java

Step 9: Seed database with test users (1 per role)
```

### Phase B: Individual role implementation (each member, days 4-10)

```
Each member works in their subfolder SIMULTANEOUSLY:

Khizer  → investigator/controller + service + templates (UC-01, UC-06)
Kishore → forensic/controller + service + templates (UC-02, UC-08)
          + LoggingDecorator in common/decorator/
Kshitij → legal/controller + service + templates (UC-03, UC-05)
          + Observer classes in common/observer/
Likith  → admin/controller + service + templates (UC-04, UC-07)
          + UserFactory in common/factory/
          + ReportFormatter interface + implementations
```

### Phase C: Integration & cross-cutting wiring (all members, days 11-13)

```
Step 1: Wire CustodyLogService calls into UC-01, UC-03, UC-05, UC-06
Step 2: Wire LoggingDecorator around evidence access operations
Step 3: Wire Observer pattern: TamperingAlertListener + SuspiciousAccessListener
Step 4: Wire UC-05 approval flow: LegalController → AccessRequest → AdminController
Step 5: Wire UC-04 report generation pulling from all custody logs
Step 6: Test all cross-role flows end-to-end
```

### Phase D: Polish & submission (all members, days 14-15)

```
Step 1: Screenshots with white backgrounds and populated data
Step 2: Write report document (PDF) with all required sections
Step 3: Ensure GitHub repository is public
Step 4: Prepare presentation/demo
Step 5: Final testing of all 8 use cases as integrated application
```

---

## 11. Verification checklist

### First pass — guideline compliance

| # | Requirement | Status | Evidence |
|---|------------|--------|----------|
| 1 | Java implementation | ✅ | Spring Boot 3.x (Java 17+) |
| 2 | MVC framework | ✅ | Spring MVC with `@Controller`, `@Service`, Thymeleaf |
| 3 | 4 major use cases | ✅ | UC-01, UC-02, UC-03, UC-04 |
| 4 | 4 minor use cases | ✅ | UC-05, UC-06, UC-07, UC-08 |
| 5 | 1 major + 1 minor per member | ✅ | See ownership table in section header |
| 6 | Each member owns full use case (not just UI/backend) | ✅ | Each subfolder has controller + service + templates |
| 7 | 4 design patterns (C+S+B+framework) | ✅ | Factory Method + Decorator + Observer + MVC |
| 8 | 4 design principles (1 per member) | ✅ | SRP + OCP + LSP + DIP |
| 9 | Desktop or Web application | ✅ | Web application (Spring Boot + Thymeleaf) |
| 10 | Database persistence | ✅ | MySQL/PostgreSQL with Spring Data JPA |
| 11 | Merged single application | ✅ | Single Spring Boot app, role subfolders as packages |
| 12 | Public GitHub repository | ✅ | Planned in Phase D |
| 13 | Report PDF with all sections | ✅ | Planned in Phase D |
| 14 | Screenshots with white background | ✅ | Planned in Phase D |
| 15 | Equal participation | ✅ | 1 major + 1 minor + 1 pattern + 1 principle per member |

### Second pass — UML diagram traceability

| Diagram | Traces to implementation |
|---------|------------------------|
| Use Case Diagram | All 8 use cases mapped to controllers. All include/extend relationships mapped to service method calls |
| Class Diagram | All entity classes in `model/`. All enums present. All associations match JPA `@ManyToOne`/`@OneToOne`/`@OneToMany` annotations |
| Activity Diagrams (×4) | Each activity flow maps to a controller method → service method chain. Decision points map to `if` checks in service layer |
| State Diagrams (×4) | Evidence status transitions enforced in `Evidence.java` via state validation methods. `CustodyLog` immutability enforced by no update/delete repository methods. `AccessRequest` status transitions in `AccessRequestService` |

### Third pass — cross-role integration verification

| Integration | Verified? | How |
|-------------|-----------|-----|
| UC-01 triggers UC-02 | ✅ | `EvidenceUploadService` calls `CustodyLogService.createEntry()` after seal |
| UC-03 triggers UC-02 | ✅ | `VerificationService` calls `CustodyLogService.createEntry()` after verify |
| UC-03 triggers UC-19 | ✅ | `VerificationService` publishes event → `TamperingAlertListener` fires |
| UC-05 requires admin approval | ✅ | `AccessRequestService` creates PENDING record → `AdminController` shows approval queue |
| UC-06 triggers UC-02 | ✅ | `LoggingDecorator` wraps view operation → auto-creates custody log |
| UC-04 reads from UC-02 data | ✅ | `ReportService` queries `CustodyLogRepository` for all entries |
| UC-07 uses Factory Method | ✅ | `UserManagementService` calls `UserFactory.createUser(role)` |
| UC-16 triggered by UC-02 | ✅ | `CustodyLogService` checks policy rules → `SuspiciousAccessListener` fires |

---

## 12. Risk mitigation

| Risk | Mitigation |
|------|-----------|
| Git merge conflicts between members | Each member works in their own subfolder package. Shared model/repository finalized in Phase A before branching |
| Integration breaks | Define service interfaces early. Use Spring dependency injection so stubs can be swapped for real implementations |
| Database schema changes late | Finalize schema in Phase A. Use Flyway/Liquibase for migrations if needed |
| File upload size limits | Configure `spring.servlet.multipart.max-file-size` in `application.properties` |
| Presentation time constraints | Each member demos their own 2 use cases + explains their pattern + principle. 2.5 minutes each = 10 minutes total |

---

## Summary

This is a single Spring Boot web application where each team member owns a complete vertical slice (controller + service + views) in their own subfolder, connected through shared models, repositories, and cross-cutting design patterns. The four design patterns (Factory Method, Decorator, Observer, MVC) and four design principles (SRP, OCP, LSP, DIP) are naturally integrated into the evidence management domain rather than being bolted on artificially.
