package org.example.client.User.dto.Request;

public class UpdateProfileRequest {
    private String currentPassword; // Только для смены пароля
    private String newPassword;
    private String email;
    private String phoneNumber;

    public UpdateProfileRequest(String currentPassword, String newPassword, String email, String phoneNumber) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
