package org.example.client.Beneficiary.dto;

public enum PaymentTypeEnum {
    PENSION("Пенсия"),
    ALLOWANCE("Пособие"),
    SUBSIDY("Субсидия"),
    ONE_TIME_PAYMENT("Единовременная выплата"),
    OTHER("Другое");

    private final String displayName;

    PaymentTypeEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PaymentTypeEnum fromDisplayName(String text) {
        for (PaymentTypeEnum type : values()) {
            if (type.displayName.equalsIgnoreCase(text)) {
                return type;
            }
        }
        return null;
    }
}
