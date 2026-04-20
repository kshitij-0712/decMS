# admin Branch - Administration Module

This branch is dedicated to the Admin role implementation in the Digital Evidence and Chain-of-Custody Management System.

## Branch Objective

Deliver the complete Admin vertical slice (`controller + service + views`) for audit report generation and user-role management with extensible formatter support.

## Owner

- Member: Khizer Pasha
- PRN: PES2UG23CS275
- Module folder: `admin/`

## Owned Use Cases

- Major UC-04: Generate Audit Report
- Minor UC-07: Manage Users and Roles

## Planned Scope

- `admin/controller/AdminController.java`
  - `GET /admin/reports`
  - `POST /admin/reports/generate`
  - `GET /admin/reports/{id}/pdf`
  - `GET /admin/users`
  - `GET /admin/users/new`
  - `POST /admin/users`
  - `PUT /admin/users/{id}`
  - `DELETE /admin/users/{id}`
- `admin/service/ReportService.java`
- `admin/service/ReportFormatter.java`
- `admin/service/HtmlReportFormatter.java`
- `admin/service/PdfReportFormatter.java`
- `admin/service/UserManagementService.java`
- `templates/admin/report-generator.html`
- `templates/admin/report-view.html`
- `templates/admin/user-management.html`
- `templates/admin/user-form.html`
- `templates/admin/access-requests.html`

## Integration Dependencies

- Pulls evidence and custody data to compile full audit timelines.
- Consumes legal access requests and exposes approve/deny workflow.
- Uses `UserFactory` for role-aware user creation and updates.
- Applies role-based controls through shared security configuration.

## Pattern and Principle Ownership

- Framework pattern contribution: MVC module implementation with Spring layers
- Design principle focus: Dependency Inversion Principle (DIP)

## Done Criteria

- Admin can generate case/evidence audit reports and export PDF output.
- Admin can create, edit, and deactivate users with role constraints.
- Admin can process pending legal access requests end-to-end.
