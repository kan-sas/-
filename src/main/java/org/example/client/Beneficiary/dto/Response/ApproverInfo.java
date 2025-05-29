package org.example.client.Beneficiary.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApproverInfo {
    private Long id;
    private String firstName;
    private String lastName;
}
