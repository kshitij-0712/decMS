package com.decms.repository;

import com.decms.model.AuditReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditReportRepository extends JpaRepository<AuditReport, String> {

    // Used by ReportService — report history, newest first
    List<AuditReport> findAllByOrderByGeneratedAtDesc();

    // Used by ReportService — reports for a specific case
    List<AuditReport> findByCaseIdOrderByGeneratedAtDesc(String caseId);
}
