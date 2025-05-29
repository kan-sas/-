package org.example.client.Beneficiary.dto.Response;

import lombok.Data;
import org.example.client.Beneficiary.dto.PaymentStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentResponse {
    private Long id;
    private BeneficiaryShortInfo beneficiary;
    private String type;
    private BigDecimal amount;
    private LocalDate date;
    private String department;
    private String comment;
    private PaymentStatusEnum status;
    private ApproverInfo approver;
}