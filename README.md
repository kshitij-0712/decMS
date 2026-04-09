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

## Pattern and Principle Ownership

- Design pattern contribution: Factory Method (`common/factory/UserFactory.java`)
- Design principle focus: Single Responsibility Principle (SRP)

## Done Criteria

- Investigator can upload evidence with metadata and receive sealed confirmation.
- Investigator can list and open evidence details with status and custody summary.
- Upload and view actions produce appropriate chain-of-custody log entries.
