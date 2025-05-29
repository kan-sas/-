package org.example.client.core.common.dto;


import lombok.Data;

import java.util.Map;

@Data
public class ErrorResponse {
    private int status;         // HTTP-статус (400, 404)
    private String code;        // Внутренний код ошибки ("INVALID_PAYMENT")
    private String message;     // "Сумма выплаты должна быть положительной"
    private Map<String, String> details;
}
