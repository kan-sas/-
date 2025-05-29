package org.example.client.core.common.dto;


public enum UserRoleEnum {
    BENEFICIARY,
    SOCIAL_WORKER,
    ADMIN;

    public static UserRoleEnum fromString(String roleString) {
        if (roleString == null) return null;
        try {
            return valueOf(roleString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String getDisplayName() {
        switch (this) {
            case SOCIAL_WORKER: return "Социальный работник";
            case BENEFICIARY: return "Получатель";
            case ADMIN: return "Администратор";
            default: return "Пользователь";
        }
    }
}