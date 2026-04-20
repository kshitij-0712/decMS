package com.decms.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Legal Officer user role.
 */
@Entity
@DiscriminatorValue("LEGAL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LegalOfficer extends User {

    private String licenseNumber;

    @Override
    public String getRole() {
        return "LEGAL";
    }
}
