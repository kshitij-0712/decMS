package com.decms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evidence entity representing a digital piece of evidence.
 * Used across all modules for evidence management.
 */
@Entity
@Table(name = "evidence", indexes = {
        @Index(name = "idx_case_id", columnList = "case_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String caseNumber;

    @Column(nullable = false)
    private String description;

    @Column(name = "file_path", columnDefinition = "TEXT")
    private String filePath;

    @Column(name = "hash_value", columnDefinition = "TEXT")
    private String hashValue;

    @Column(name = "case_id")
    private String caseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @Column(name = "is_sealed", nullable = false)
    private Boolean isSealed = false;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
}
