package org.example.client.User.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.client.User.Service.ProfileService;
import org.example.client.core.common.util.SessionManager;

import java.io.IOException;
import java.util.Optional;

public class ProfileController {
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Label statusLabel;

    private final ProfileService profileService = new ProfileService();

    @FXML
    public void initialize() {
        loadProfileData();
    }

    private void loadProfileData() {
        profileService.getProfileAsync()
                .thenAccept(profile -> Platform.runLater(() -> {
                    usernameLabel.setText("Логин: " + profile.getUsername());
                    emailLabel.setText("Email: " + profile.getEmail());
                    phoneLabel.setText("Телефон: " + profile.getPhoneNumber());
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showAlert(
                            "Ошибка",
                            "Не удалось загрузить профиль: " + ex.getCause().getMessage(),
                            Alert.AlertType.ERROR
                    ));
                    return null;
                });
    }

    @FXML
    private void handleUpdatePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();

        if (currentPassword.isEmpty() || newPassword.isEmpty()) {
            statusLabel.setText("Заполните все поля!");
            return;
        }

        profileService.updatePasswordAsync(currentPassword, newPassword)
                .thenAccept(success -> Platform.runLater(() -> {
                    showAlert("Успех", "Пароль изменён", Alert.AlertType.INFORMATION);
                    currentPasswordField.clear();
                    newPasswordField.clear();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showAlert(
                            "Ошибка",
                            "Ошибка изменения пароля: " + ex.getCause().getMessage(),
                            Alert.AlertType.ERROR
                    ));
                    return null;
                });
    }

    @FXML
    private void handleUpdateContacts() {
        String newEmail = emailField.getText();
        String newPhone = phoneField.getText();

        profileService.updateContactInfoAsync(newEmail, newPhone)
                .thenAccept(success -> Platform.runLater(() -> {
                    showAlert("Успех", "Данные обновлены", Alert.AlertType.INFORMATION);
                    loadProfileData();
                    emailField.clear();
                    phoneField.clear();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showAlert(
                            "Ошибка",
                            "Ошибка обновления данных: " + ex.getCause().getMessage(),
                            Alert.AlertType.ERROR
                    ));
                    return null;
                });
    }

    @FXML
    private void handleDeleteProfile() {
        if (showConfirmation("Удаление профиля", "Вы уверены?")) {
            profileService.deleteProfileAsync()
                    .thenAccept(success -> Platform.runLater(() -> {
                        switchToAuthScene();
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> showAlert(
                                "Ошибка",
                                "Ошибка удаления: " + ex.getCause().getMessage(),
                                Alert.AlertType.ERROR
                        ));
                        return null;
                    });
        }
    }
    @FXML
    private void handleBackToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/client/MainMenu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Главное меню");
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось вернуться в меню", Alert.AlertType.ERROR);
        }
    }

    private void switchToAuthScene() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/client/Auth.fxml"));
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Авторизация");
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось перейти к авторизации", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}