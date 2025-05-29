package org.example.client.Beneficiary.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BeneficiaryShortInfo {
    private Long id;
    private String firstName;
    private String lastName;

    public String getFullName() {
        return String.format("%s %s", firstName, lastName).trim();
    }
}