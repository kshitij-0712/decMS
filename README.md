# legal Branch - Legal Module

This branch is dedicated to the Legal role implementation in the Digital Evidence and Chain-of-Custody Management System.

## Branch Objective

Deliver the complete Legal vertical slice (`controller + service + views`) for evidence integrity verification and controlled access request workflows.

## Owner

- Member: Likith N
- PRN: PES2UG23CS306
- Module folder: `legal/`

## Owned Use Cases

- Major UC-03: Verify Evidence Integrity
- Minor UC-05: Request Evidence Access

## Planned Scope

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

## Integration Dependencies

- Uses `HashService` and stored hash records for integrity checks.
- Logs verification and access events through `CustodyLogService`.
- Publishes mismatch and suspicious events to observer listeners.
- Creates `AccessRequest` entities consumed by admin approval workflow.

## Pattern and Principle Ownership

- Design pattern contribution: Observer (`common/observer/*`)
- Design principle focus: Liskov Substitution Principle (LSP)

## Done Criteria

- Legal officer can verify evidence integrity with clear pass/fail output.
- Hash mismatch produces tampering alerts through observer notifications.
- Access request lifecycle supports submit and status tracking until admin resolution.
