package com.decms.repository;

import com.decms.model.HashRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HashRecordRepository extends JpaRepository<HashRecord, String> {
    Optional<HashRecord> findByEvidenceEvidenceId(String evidenceId);

    boolean existsByHashValue(String hashValue);
}
