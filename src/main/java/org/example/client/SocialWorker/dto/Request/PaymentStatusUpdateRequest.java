package org.example.client.SocialWorker.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.client.Beneficiary.dto.PaymentStatusEnum;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentStatusUpdateRequest {
    private PaymentStatusEnum status;
    private Long socialWorkerId;
}