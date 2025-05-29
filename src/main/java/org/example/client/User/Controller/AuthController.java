package org.example.client.User.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.example.client.User.Service.AuthService;
import org.example.client.User.Service.ProfileService;
import org.example.client.User.Service.RegistrationService;
import org.example.client.User.dto.Request.LoginRequest;
import org.example.client.User.dto.Request.RegistrationRequest;
import org.example.client.User.dto.Response.AuthResponse;
import org.example.client.core.common.dto.UserRoleEnum;
import org.example.client.core.common.util.SceneSwitcherUtil;
import org.example.client.core.common.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    // --- Login form ---
    @FXML private TextField loginUsername;
    @FXML private PasswordField loginPassword;

    // --- Registration form ---
    @FXML private TextField regFirstName;
    @FXML private TextField regLastName;
    @FXML private TextField regUsername;
    @FXML private PasswordField regPassword;
    @FXML private ComboBox<UserRoleEnum> regRoleComboBox;

    // --- Common elements ---
    @FXML private Label statusMessage;
    @FXML private VBox loginForm;
    @FXML private VBox registerForm;

    private final AuthService authService = new AuthService();
    private final RegistrationService registrationService = new RegistrationService();
    private final ProfileService profileService = new ProfileService();

    @FXML
    public void initialize() {
        log.debug("AuthController.initialize(): bound? {}", loginUsername != null);

        loginUsername.textProperty().addListener((obs, o, n) ->
                log.debug("loginUsername changed: '{}' (len={})", n, n.length()));

        regRoleComboBox.getItems().setAll(UserRoleEnum.values());
        regRoleComboBox.setValue(UserRoleEnum.BENEFICIARY);
        regRoleComboBox.setCellFactory(lv -> new ListCell<UserRoleEnum>() {
            @Override
            protected void updateItem(UserRoleEnum item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.name().replace("_", " "));
            }
        });
        regRoleComboBox.setButtonCell(new ListCell<UserRoleEnum>() {
            @Override
            protected void updateItem(UserRoleEnum item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.name().replace("_", " "));
            }
        });
    }

    @FXML
    private void handleLogin() {
        String username = loginUsername.getText().trim();
        String password = loginPassword.getText().trim();
        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password are required!");
            return;
        }

        setFormsDisabled(true);
        authService.loginAsync(new LoginRequest(username, password))
                .thenAcceptAsync(response -> Platform.runLater(() -> {
                    setFormsDisabled(false);
                    if (response != null && response.getToken() != null) {
                        onLoginSuccess(response);
                    } else {
                        showError("Invalid credentials. Please try again.");
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        setFormsDisabled(false);
                        showError("Login error: " + rootCause(ex));
                    });
                    return null;
                });
    }

    private void onLoginSuccess(AuthResponse response) {
        try {
            SessionManager.initSession(response.getToken());
            log.info("Session initialized for user '{}'", SessionManager.getUsername());
            loadProfileAndSwitchScene();
        } catch (Exception e) {
            log.error("Session init failed", e);
            showError("Session initialization error: " + e.getMessage());
        }
    }

    private void loadProfileAndSwitchScene() {
        Platform.runLater(() -> {
            try {
                SceneSwitcherUtil.switchTo(
                        "/org/example/client/MainMenu.fxml",
                        "Главное меню",
                        800,
                        600
                );
            } catch (IOException e) {
                log.error("Ошибка загрузки главного меню", e);
                showError("Ошибка загрузки интерфейса: " + e.getMessage());
            }
        });
    }


    @FXML
    private void handleRegister() {
        RegistrationRequest req = buildRegistrationRequest();
        if (req == null) return;

        setFormsDisabled(true);
        registrationService.registerAsync(req)
                .thenAcceptAsync(r -> Platform.runLater(() -> {
                    setFormsDisabled(false);
                    showSuccess("Registration successful! Please log in.");
                    switchToLogin();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        setFormsDisabled(false);
                        showError("Registration failed: " + rootCause(ex));
                    });
                    return null;
                });
    }

    private RegistrationRequest buildRegistrationRequest() {
        String first = regFirstName.getText().trim();
        String last = regLastName.getText().trim();
        String user = regUsername.getText().trim();
        String pass = regPassword.getText().trim();
        UserRoleEnum role = regRoleComboBox.getValue();

        if (first.isEmpty() || last.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            showError("All fields are required!");
            return null;
        }
        if (pass.length() < 8) {
            showError("Password must be at least 8 characters");
            return null;
        }

        return new RegistrationRequest(user, pass, first, last, role);
    }

    // Остальные методы без изменений
    private void setFormsDisabled(boolean disabled) {
        loginForm.setDisable(disabled);
        registerForm.setDisable(disabled);
    }

    private String rootCause(Throwable ex) {
        Throwable c = ex;
        while (c.getCause() != null) c = c.getCause();
        return c.getMessage();
    }

    private void showSuccess(String msg) {
        statusMessage.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
        statusMessage.setText(msg);
    }

    private void showError(String msg) {
        statusMessage.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
        statusMessage.setText(msg);
    }

    @FXML
    private void switchToLogin() {
        clearRegistrationForm();
        try {
            SceneSwitcherUtil.switchTo(
                    "/org/example/client/Auth.fxml",
                    "Авторизация"
            );
        } catch (IOException e) {
            log.error("Ошибка переключения на логин", e);
            showError("Ошибка переключения экрана");
        }
    }

    @FXML
    private void switchToRegister() {
        clearLoginForm();
        loginForm.setVisible(false);
        registerForm.setVisible(true);
        statusMessage.setText("");
    }

    private void clearRegistrationForm() {
        regFirstName.clear();
        regLastName.clear();
        regUsername.clear();
        regPassword.clear();
        regRoleComboBox.setValue(UserRoleEnum.BENEFICIARY);
    }

    private void clearLoginForm() {
        loginUsername.clear();
        loginPassword.clear();
    }
}