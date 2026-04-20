package com.decms.repository;

import com.decms.model.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Evidence entity.
 */
@Repository
public interface EvidenceRepository extends JpaRepository<Evidence, Long> {

    /**
     * Find evidence by case number.
     */
    Optional<Evidence> findByCaseNumber(String caseNumber);

    /**
     * Find evidence by case ID.
     */
    Optional<Evidence> findByCaseId(String caseId);
}
