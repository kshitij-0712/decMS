# DECMS Integrated Branch (foren)

This branch is the **integration workspace** for the Digital Evidence and Chain-of-Custody Management System, with focus on preserving the Forensic ownership while keeping cross-role flows operational.

## Branch Objective

Integrate role modules into a single Spring Boot MVC application and keep it aligned with:
- `PLAN_OF_ACTION.md` section 7 ownership mapping (authoritative)
- shared UI system contract (layout fragments + shared CSS/JS)
- cross-role integration points (UC-01/03/05/06 -> UC-02, and UC-04 reads custody data)

## Team & Ownership

| Member | PRN | Role subfolder | Major UC | Minor UC |
|---|---|---|---|---|
| Kshitij | PES2UG23CS290 | `investigator/` | UC-01 Upload & Seal Digital Evidence | UC-06 View Evidence Details |
| Kishore H N | PES2UG23CS278 | `forensic/` | UC-02 Maintain Chain-of-Custody Log | UC-08 View Custody History |
| Likith N | PES2UG23CS306 | `legal/` | UC-03 Verify Evidence Integrity | UC-05 Request Evidence Access |
| Khizer Pasha | PES2UG23CS275 | `admin/` | UC-04 Generate Audit Report | UC-07 Manage Users & Roles |

## Module Ownership Notes

- **Forensic ownership (this branch):**
  - `forensic/controller/ForensicController.java`
  - `forensic/service/CustodyLogService.java`
  - Forensic templates under `templates/forensic/`
- **Structural pattern contribution:** Decorator (`common/decorator/*`)
- **SOLID focus:** Open/Closed Principle (OCP)

## Current Scope in this Integrated Branch

- Forensic UC-02 and UC-08 endpoints are wired in the integrated package layout (`com.decms.forensic.*`).
- Investigator, Legal, and Admin modules coexist and call shared repositories/services.
- Shared UI shell is reused across role pages:
  - `templates/layout/fragments.html`
  - `templates/layout/base.html`
  - `static/css/styles.css`
  - `static/js/app.js`

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.9+

### Run

```bash
mvn clean spring-boot:run
```

Default dev login users are seeded by `DataBootstrapConfig`:
- `inv-001` / `password`
- `foren-001` / `password`
- `legal-001` / `password`
- `admin-001` / `password`

## Forensic Endpoints

- `GET /forensic/custody`
- `GET /forensic/custody/{evidenceId}`
- `GET /forensic/history/{evidenceId}`
- `GET /forensic/anomalies`

## Integration Expectations

- UC-01 upload creates custody `UPLOAD` entries.
- UC-06 evidence details access creates custody `VIEW` entries.
- UC-03 verification creates custody `VERIFY` entries and can trigger observer events.
- UC-05 access requests create custody `ACCESS` entries and feed admin approval queues.
- UC-04 report generation reads evidence + custody history for full audit timeline.

## References

- `PLAN_OF_ACTION.md`
- `AGENTS.md`
- `FORENSIC_MODULE.md`
