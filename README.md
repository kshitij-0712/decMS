# Digital Evidence and Chain-of-Custody Management System (DECMS)

This repository contains a Spring Boot MVC web application for secure digital evidence handling across investigator, forensic, legal, and admin roles.

## Objective

Build a single integrated system that supports end-to-end digital evidence lifecycle management with integrity verification, immutable custody logs, role-based access control, and audit reporting.

## Team Ownership

| Role | Member | PRN | Module |
|------|--------|-----|--------|
| Investigator | Kshitij | PES2UG23CS290 | `investigator/` |
| Forensic Analyst | Kishore H N | PES2UG23CS278 | `forensic/` |
| Legal Officer | Likith N | PES2UG23CS306 | `legal/` |
| Administrator | Khizer Pasha | PES2UG23CS275 | `admin/` |

## Use Cases

### Major
- **UC-01** Upload and Seal Digital Evidence
- **UC-02** Maintain Chain-of-Custody Log
- **UC-03** Verify Evidence Integrity
- **UC-04** Generate Audit Report

### Minor
- **UC-05** Request Evidence Access
- **UC-06** View Evidence Details
- **UC-07** Manage Users and Roles
- **UC-08** View Custody History

## Architecture and Stack

- Architecture: Spring MVC (`Controller -> Service -> Repository -> DB`)
- Backend: Spring Boot 3.x, Spring MVC, Spring Data JPA, Spring Security
- View: Thymeleaf
- Database: MySQL 8 / PostgreSQL / H2 (Development)
- Build: Maven
- Hashing: SHA-256 (`MessageDigest`)
- Report export: iText or Apache PDFBox

## Design Patterns

- Factory Method (creational): `UserFactory`
- Decorator (structural): `LoggingDecorator`
- Observer (behavioral): tampering and suspicious access listeners
- MVC (framework-enforced): Spring MVC layers

## SOLID Principles Applied

- SRP: evidence, hashing, and file storage are separated by responsibility
- OCP: custody logging pipeline supports new action types via extension
- LSP: role-specific user types are substitutable for `User`
- DIP: services depend on interfaces (for example, `ReportFormatter`)

## Default Credentials (Development)

Default users are automatically seeded for testing:

| Role | Username | Password |
|------|----------|----------|
| Investigator | `inv-001` | `password` |
| Forensic Analyst | `foren-001` | `password` |
| Legal Officer | `legal-001` | `password` |
| Administrator | `admin-001` | `password` |

## Local Setup & Quick Start

1. Install Java 17+ and Maven 3.9+.
2. Ensure you are in the root directory.
3. Start the application with:
   ```bash
   mvn clean spring-boot:run
   ```
4. Access the application at `http://localhost:8080`.

## Status

**Implementation Complete.** Module-wise implementation has been merged into `main` after integration testing. The project is fully functional across all four core roles.
