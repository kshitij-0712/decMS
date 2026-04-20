package com.decms.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Investigator user role.
 */
@Entity
@DiscriminatorValue("INVESTIGATOR")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Investigator extends User {

    private String department;

    @Override
    public String getRole() {
        return "INVESTIGATOR";
    }
}
