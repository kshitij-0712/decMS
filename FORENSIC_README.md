# Digital Evidence & Chain-of-Custody Management System

A comprehensive forensic evidence management system built with Spring Boot, implementing OOAD principles and design patterns.

## Project Overview

This is a group OOAD project for managing digital evidence in forensic investigations with complete chain-of-custody tracking.

**Members:**
- Kshitij (PES2UG23CS290) - Investigator Module
- Kishore H N (PES2UG23CS278) - Forensic Module ✓
- Likith N (PES2UG23CS306) - Legal Module  
- Khizer Pasha (PES2UG23CS275) - Admin Module

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+

### Setup

1. **Clone the repository**
```bash
git clone <repo-url>
cd decMS
```

2. **Configure Database**
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:h2:file:./data/decms;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.username=sa
spring.datasource.password=
```

3. **Build & Run**
```bash
mvn clean install
mvn spring-boot:run
```

4. **Access Application**
- URL: `http://localhost:8080`
- Forensic Dashboard: `http://localhost:8080/forensic/custody`

## Branch Structure

- `main` - Main branch with project setup
- `inves` - Investigator module (Kshitij)
- `foren` - Forensic module (Kishore) ✓ **Active**
- `legal` - Legal module (Likith)
- `admin` - Admin module (Khizer)

## Use Cases

| Member | Major UC | Minor UC |
|--------|----------|----------|
| Kshitij | UC-01 Upload & Seal Evidence | UC-06 View Evidence Details |
| **Kishore** | **UC-02 Custody Log** | **UC-08 Custody History** |
| Likith | UC-03 Verify Evidence Integrity | UC-05 Request Access |
| Khizer | UC-04 Generate Audit Report | UC-07 Manage Users |

## Architecture

```
MVC Pattern Implementation:
┌─────────────────┐
│  Presentation   │ (Thymeleaf Templates)
├─────────────────┤
│  Controller     │ (Spring MVC)
├─────────────────┤
│  Service        │ (Business Logic)
├─────────────────┤
│  Repository     │ (JPA)
├─────────────────┤
│  Database       │ (MySQL)
└─────────────────┘
```

## Design Patterns

1. **Factory Method** (Creational) - UserFactory
2. **Decorator** (Structural) - LoggingDecorator ✓ **Forensic Module**
3. **Observer** (Behavioral) - TamperingAlertObserver
4. **MVC** (Framework) - Spring MVC

## SOLID Principles

1. **SRP** - Single Responsibility
2. **OCP** - Open/Closed ✓ **Forensic Module**
3. **LSP** - Liskov Substitution
4. **DIP** - Dependency Inversion

## Key Features

### Forensic Module
- ✓ Chain of Custody Logging
- ✓ Custody History Viewing
- ✓ Anomaly Detection
- ✓ Decorator Pattern
- ✓ OCP Principle

### Future Modules
- Investigation & Evidence Management
- Legal Verification & Access Control
- Admin Audit Reporting

## Technology Stack

- **Backend**: Spring Boot 3.x, Spring MVC
- **Frontend**: Thymeleaf, HTML5, CSS3
- **Database**: H2 file database (MySQL compatibility mode)
- **ORM**: Hibernate/JPA
- **Security**: Spring Security, BCrypt
- **Build**: Maven

## Database

Tables for Forensic Module:
- `users` - User management with roles
- `evidence` - Evidence items
- `custody_logs` - Chain of custody tracking
- `hash_records` - Integrity verification
- `tampering_alerts` - Anomaly detection

## Development Guide

### For Forensic Module Development

1. **Add a new custody action**
   - Add to `ActionType.java` enum
   - Automatically picked up by logging decorator

2. **Implement new anomaly detection**
   - Edit `CustodyLogService.checkForAnomalies()`
   - Update `CustodyLog.isSuspicious` flag

3. **Create new UI**
   - Add Thymeleaf template in `templates/forensic/`
   - Create controller method with proper role checks

## Testing

Sample users are pre-loaded:
```
foren-001 / password
inv-001 / password
legal-001 / password
admin-001 / password
```

## Contributing

1. Create feature branch from your module branch
2. Make changes and commit
3. Push to GitHub
4. Create pull request to main branch

## Project Documentation

- [Forensic Module README](./FORENSIC_MODULE.md)
- [Plan of Action](./PLAN_OF_ACTION.md)

## License

Educational Project - PES University OOAD Course

## Support

For issues or questions, contact the respective module owner.

---

**Current Status**: Forensic Module ✓ Complete  
**Last Updated**: April 21, 2026
