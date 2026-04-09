# foren Branch - Forensic Module

This branch is dedicated to the Forensic role implementation in the Digital Evidence and Chain-of-Custody Management System.

## Branch Objective

Deliver the complete Forensic vertical slice (`controller + service + views`) for custody tracking and historical traceability, including automatic logging through a decorator.

## Owner

- Member: Kishore H N
- PRN: PES2UG23CS278
- Module folder: `forensic/`

## Owned Use Cases

- Major UC-02: Maintain Chain-of-Custody Log
- Minor UC-08: View Custody History

## Planned Scope

- `forensic/controller/ForensicController.java`
  - `GET /forensic/custody`
  - `GET /forensic/custody/{evidenceId}`
  - `GET /forensic/history/{evidenceId}`
- `forensic/service/CustodyLogService.java`
- `forensic/service/CustodyHistoryService.java`
- `common/decorator/EvidenceAccessService.java`
- `common/decorator/BaseEvidenceAccessService.java`
- `common/decorator/LoggingDecorator.java`
- `templates/forensic/custody-dashboard.html`
- `templates/forensic/custody-log-entry.html`
- `templates/forensic/custody-history.html`
- `templates/forensic/flag-anomaly.html`

## Integration Dependencies

- Receives events from investigator, legal, and admin flows via service calls.
- Writes immutable custody entries through `CustodyLogRepository`.
- Publishes suspicious access events to observer listeners when policy rules match.
- Supports downstream reporting in admin by exposing complete custody timelines.

## Pattern and Principle Ownership

- Design pattern contribution: Decorator (`common/decorator/LoggingDecorator.java`)
- Design principle focus: Open/Closed Principle (OCP)

## Done Criteria

- Every relevant evidence action is recorded with actor, action, timestamp, and IP.
- Forensic dashboard shows latest entries and highlights suspicious records.
- Custody history supports filters and pagination for evidence-specific investigation.
