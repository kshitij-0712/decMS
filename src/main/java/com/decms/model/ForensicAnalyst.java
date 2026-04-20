package com.decms.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Forensic Analyst user role.
 */
@Entity
@DiscriminatorValue("FORENSIC")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ForensicAnalyst extends User {

    private String specialization;

    @Override
    public String getRole() {
        return "FORENSIC";
    }
}
