# inves Branch - Investigator Module

This branch is dedicated to the Investigator role implementation in the Digital Evidence and Chain-of-Custody Management System.

## Branch Objective

Deliver the complete Investigator vertical slice (`controller + service + views`) for evidence intake and evidence visibility while integrating with shared custody and hashing services.

## Owner

- Member: Kshitij
- PRN: PES2UG23CS290
- Module folder: `investigator/`

## Owned Use Cases

- Major UC-01: Upload and Seal Digital Evidence
- Minor UC-06: View Evidence Details

## Planned Scope

- `investigator/controller/InvestigatorController.java`
  - `GET /investigator/upload`
  - `POST /investigator/upload`
  - `GET /investigator/evidence`
  - `GET /investigator/evidence/{id}`
- `investigator/service/EvidenceUploadService.java`
- `investigator/service/EvidenceViewService.java`
- `templates/investigator/upload-evidence.html`
- `templates/investigator/upload-success.html`
- `templates/investigator/evidence-list.html`
- `templates/investigator/evidence-details.html`

## Integration Dependencies

- Calls shared `HashService` for SHA-256 generation.
- Uses shared `FileStorageService` for file persistence.
- Persists evidence data through `EvidenceRepository` and related model classes.
- Triggers custody logging through `CustodyLogService` for upload and view operations.

## Shared UI Foundation for Team Integration

This branch now includes a reusable role-agnostic UI system so all members can attach their feature logic without redesigning layouts.

- Shared shell and fragments:
  - `src/main/resources/templates/layout/base.html`
  - `src/main/resources/templates/layout/fragments.html`
- Shared style/behavior:
  - `src/main/resources/static/css/styles.css`
  - `src/main/resources/static/js/app.js`
- UI reference page:
  - `src/main/resources/templates/layout/ui-kit.html`

### Reuse Contract

- Keep role pages inside the common app shell (`app-shell`, sidebar, topbar, alert stack).
- Reuse shared components (`card`, `table`, `badge-*`, `timeline`, `form-grid`, `btn-*`).
- Avoid role-specific CSS files for baseline workflows.
- Add any new visual component in `ui-kit.html` first, then consume in role templates.
- Preserve responsive behavior from shared breakpoints for desktop/mobile parity.

## Pattern and Principle Ownership

- Design pattern contribution: Factory Method (`common/factory/UserFactory.java`)
- Design principle focus: Single Responsibility Principle (SRP)

## Done Criteria

- Investigator can upload evidence with metadata and receive sealed confirmation.
- Investigator can list and open evidence details with status and custody summary.
- Upload and view actions produce appropriate chain-of-custody log entries.

## Legal Module (Likith N)

### Owner
- Member: Likith N
- PRN: PES2UG23CS306
- Module folder: `legal/`

### Owned Use Cases
- Major UC-03: Verify Evidence Integrity
- Minor UC-05: Request Evidence Access

### Planned Scope
- `legal/controller/LegalController.java`
  - `GET /legal/verify`
  - `POST /legal/verify/{evidenceId}`
  - `GET /legal/access`
  - `POST /legal/access`
  - `GET /legal/access/status`
- `legal/service/VerificationService.java`
- `legal/service/AccessRequestService.java`
- `common/observer/EvidenceEventPublisher.java`
- `common/observer/EvidenceEventListener.java`
- `common/observer/TamperingAlertListener.java`
- `common/observer/SuspiciousAccessListener.java`
- `templates/legal/verify-integrity.html`
- `templates/legal/verification-result.html`
- `templates/legal/request-access.html`
- `templates/legal/request-status.html`

### Integration Dependencies
- Uses `HashService` and stored hash records for integrity checks.
- Logs verification and access events through `CustodyLogService`.
- Publishes mismatch and suspicious events to observer listeners.
- Creates `AccessRequest` entities consumed by admin approval workflow.

### Pattern and Principle Ownership
- Design pattern contribution: Observer (`common/observer/*`)
- Design principle focus: Liskov Substitution Principle (LSP)

### Done Criteria
- Legal officer can verify evidence integrity with clear pass/fail output.
- Hash mismatch produces tampering alerts through observer notifications.
- Access request lifecycle supports submit and status tracking until admin resolution.
