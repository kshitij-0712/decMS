package com.decms.repository;

import com.decms.model.TamperingAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TamperingAlertRepository extends JpaRepository<TamperingAlert, String> {

    // Used by ReportService — tampering alerts for a specific evidence item
    List<TamperingAlert> findByEvidenceEvidenceId(String evidenceId);
}
