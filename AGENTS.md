# AGENTS.md

## Investigator Agent Playbook (`inves` branch)

This file defines the execution plan for the Investigator owner in the Digital Evidence and Chain-of-Custody Management System.

## 1) Ownership and mission

- Owner: Kshitij (PRN: PES2UG23CS290)
- Branch: `inves`
- Module ownership: `investigator/`
- Major use case: UC-01 Upload and Seal Digital Evidence
- Minor use case: UC-06 View Evidence Details
- Pattern ownership: Factory Method (`common/factory/UserFactory.java`)
- Principle ownership: SRP (Single Responsibility Principle)

Mission: deliver the full Investigator vertical slice (controller + service + views + DB wiring) and ensure it integrates cleanly with forensic, legal, and admin flows.

## 2) Source-of-truth notes

- `README.md` and section 7A of `PLAN_OF_ACTION.md` are the source of truth for investigator ownership.
- Section 10 of `PLAN_OF_ACTION.md` has a role-name swap in phase-B mapping; treat section 7 ownership as authoritative.

## 3) Investigator scope

### Mandatory backend

- `investigator/controller/InvestigatorController.java`
  - `GET /investigator/upload`
  - `POST /investigator/upload`
  - `GET /investigator/evidence`
  - `GET /investigator/evidence/{id}`
- `investigator/service/EvidenceUploadService.java`
- `investigator/service/EvidenceViewService.java`

### Mandatory views

- `templates/investigator/upload-evidence.html`
- `templates/investigator/upload-success.html`
- `templates/investigator/evidence-list.html`
- `templates/investigator/evidence-details.html`

### Shared dependencies consumed

- `HashService` for SHA-256 generation
- `FileStorageService` for evidence file persistence
- `EvidenceRepository` and model layer entities
- `CustodyLogService` for upload/view chain-of-custody logging
- `SecurityConfig` role authorization for `/investigator/**`

## 4) Delivery plan (execution order)

### Phase A - Contract alignment (Day 1)

1. Freeze investigator-facing contracts for `HashService`, `FileStorageService`, and `CustodyLogService`.
2. Confirm entity fields used by UC-01 and UC-06 (`Evidence`, `HashRecord`, custody summary projection).
3. Confirm route-level investigator authorization in security config.

Output:
- Stable method signatures and DTO contracts.

### Phase B - UC-01 implementation (Days 2-3)

1. Build upload form DTO and `GET /investigator/upload` page.
2. Implement `POST /investigator/upload` flow:
   - Validate file type/size and required metadata.
   - Store file using `FileStorageService`.
   - Generate SHA-256 using `HashService`.
   - Persist evidence metadata with `SEALED` status.
   - Persist hash record.
   - Create custody log entry (`UPLOAD`).
3. Add error handling:
   - Validation failures (user feedback)
   - Storage/persistence failure rollback
   - Duplicate hash warning path if required by final domain rules
4. Render `upload-success.html` with evidence ID, hash value, and status.

Output:
- End-to-end upload and seal workflow for investigator.

### Phase C - UC-06 implementation (Days 4-5)

1. Implement `GET /investigator/evidence` for list + search/filter.
2. Implement `GET /investigator/evidence/{id}` for detailed view.
3. Build list and details templates with evidence metadata, status badge, hash, and custody summary.
4. Add custody log entry (`VIEW`) for evidence visibility operations.
5. Enforce access checks in service layer.

Output:
- End-to-end evidence list and details workflow with logging.

### Phase D - Pattern/principle checkpoint (Day 6)

1. Factory Method check: ensure `UserFactory` role creation path is usable by admin user management flow.
2. SRP check:
   - Upload orchestration only in upload service
   - Hash logic only in `HashService`
   - File I/O only in `FileStorageService`
   - Read/composition logic only in view service
3. Ensure controllers remain thin (request mapping + response model only).

Output:
- Pattern and SOLID compliance aligned with project rubric.

### Phase E - Cross-role integration (Days 7-8)

1. Verify UC-01 triggers forensic custody entries.
2. Verify UC-06 view events are visible in custody history.
3. Verify legal verification can consume investigator-sealed evidence and hash records.
4. Verify admin reports include investigator-origin evidence timeline and custody activity.

Output:
- Investigator features confirmed inside full system workflows.

### Phase F - Test, polish, submission support (Days 9-10)

1. Unit tests for upload and view services.
2. Integration tests for upload -> hash -> persistence -> custody logging.
3. Role-access tests for investigator endpoints.
4. Screenshot capture with populated data for report/demo.
5. README/status updates and PR-ready changelog.

Output:
- Investigator module demo-ready and submission-ready.

## 5) Quality gates (must pass)

- UC-01: upload success shows evidence ID + SHA-256 + `SEALED` status.
- UC-01: failed validation does not persist partial evidence.
- UC-06: investigator can list and open details for authorized evidence.
- UC-06: view operations produce custody logs.
- Integration: forensic and admin modules can read investigator-generated records.
- Security: non-investigator roles cannot access investigator routes unless explicitly allowed.

## 6) Risk register and mitigations

- Contract drift in shared services
  - Mitigation: freeze method signatures before feature completion.
- Partial writes on upload failures
  - Mitigation: transactional boundaries and explicit rollback/error path.
- Integration regressions across branches
  - Mitigation: frequent merge-from-main and smoke tests after each merge.
- Ownership ambiguity from planning-doc mismatch
  - Mitigation: team confirmation note and follow section 7 ownership mapping.

## 7) Branch workflow

- Keep changes focused in `investigator/` and required shared components only.
- Suggested commit slices:
  1. UC-01 controller/service/template + validation
  2. UC-06 controller/service/template + filters/details
  3. Custody integration + test coverage + fixes
- PR description must map code to UC-01 and UC-06 acceptance criteria.

## 8) Done definition (investigator owner)

- All planned investigator endpoints implemented and secured.
- All four investigator templates implemented and wired.
- Upload and view actions logged in chain-of-custody.
- Evidence hashing and storage integrated through shared services.
- Factory Method and SRP contributions are demonstrable in code review.
- Tests and screenshots are ready for final report/presentation.

## 9) Shared UI system plan (cross-role integration ready)

The UI foundation in this branch is intentionally role-agnostic so forensic, legal, and admin teams can plug in their feature logic without redesigning pages.

### Design-system contract

- Base shell components:
  - `templates/layout/fragments.html`
  - `templates/layout/base.html`
- Shared styling and behavior:
  - `static/css/styles.css`
  - `static/js/app.js`
- UI reference page for all teams:
  - `templates/layout/ui-kit.html`

### Reuse rules for all role templates

1. Reuse common shell (`app-shell`, sidebar, topbar, alert stack).
2. Reuse component classes (`card`, `table`, `badge-*`, `timeline`, `form-grid`, `btn-*`).
3. Keep role pages focused on data binding and endpoint wiring; avoid per-role CSS duplication.
4. Add new components to UI kit first, then consume in role templates.
5. Preserve responsive behavior for desktop and mobile as defined in shared CSS breakpoints.

### Integration expectation for teammates

- Forensic team: bind UC-02 and UC-08 data into forensic templates using existing card/table/timeline shells.
- Legal team: bind UC-03 and UC-05 forms/results into legal templates without changing global layout contracts.
- Admin team: bind UC-04 and UC-07 workflows into admin templates and keep factory/report flows inside service layer.

### UI acceptance checks

- Pages render consistently for all 4 roles with shared navigation and spacing.
- Status semantics are visually consistent (`SEALED`, `VERIFIED`, `PENDING`, `DENIED`, etc.).
- No role-specific CSS file is required for baseline workflows.
- UI kit demonstrates all core reusable blocks before final integration merge.
