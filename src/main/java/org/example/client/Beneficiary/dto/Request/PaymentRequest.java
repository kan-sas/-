package org.example.client.Beneficiary.dto.Request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class PaymentRequest {
    private Long beneficiaryId;
    private String type;
    private String amount; // Строка для точного представления BigDecimal
    private String department; // Отдел, осуществляющий выплату
    private String comment;

    public boolean allFieldsNull() {
        return beneficiaryId == null &&
                type == null &&
                amount == null &&
                department == null &&
                comment == null;
    }
}
