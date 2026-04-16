package com.decms.repository;

import com.decms.model.AuditReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditReportRepository extends JpaRepository<AuditReport, String> {
}
