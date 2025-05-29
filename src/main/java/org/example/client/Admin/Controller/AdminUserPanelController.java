package org.example.client.Admin.Controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.client.Admin.Service.AdminUserPanelService;
import org.example.client.core.common.dto.ProfileResponse;
import org.example.client.core.common.dto.UserRoleEnum;
import org.example.client.core.common.util.SessionManager;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AdminUserPanelController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilterCombo;
    @FXML private TableView<ProfileResponse> usersTable;
    @FXML private TableColumn<ProfileResponse, Long> idColumn;
    @FXML private TableColumn<ProfileResponse, String> usernameColumn;
    @FXML private TableColumn<ProfileResponse, String> firstNameColumn;
    @FXML private TableColumn<ProfileResponse, String> lastNameColumn;
    @FXML private TableColumn<ProfileResponse, String> emailColumn;
    @FXML private TableColumn<ProfileResponse, String> phoneNumberColumn;
    @FXML private TableColumn<ProfileResponse, String> roleColumn;

    private final ObservableList<String> availableRoles = FXCollections.observableArrayList(
            UserRoleEnum.BENEFICIARY.name(),
            UserRoleEnum.SOCIAL_WORKER.name(),
            UserRoleEnum.ADMIN.name()
    );

    private ObservableList<ProfileResponse> allUsers = FXCollections.observableArrayList();
    private ObservableList<ProfileResponse> filteredUsers = FXCollections.observableArrayList();
    private List<ProfileResponse> originalUsers;

    private final AdminUserPanelService adminUserService = new AdminUserPanelService();

    @FXML
    public void initialize() {
        try {
            checkAdminPermissions();
            setupTable();
            initializeFilters();
            loadUsers();
        } catch (SecurityException e) {
            showErrorAndExit(e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource("/org/example/client/MainMenu.fxml")
            ));
            Parent root = loader.load();
            Stage stage = (Stage) usersTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Главное меню");
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить главное меню: " + e.getMessage());
        }
    }

    private void checkAdminPermissions() {
        String role = SessionManager.getRole();
        if (!UserRoleEnum.ADMIN.name().equals(role)) {
            throw new SecurityException("Доступ запрещен. Требуются права администратора.");
        }
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        roleColumn.setCellFactory(ComboBoxTableCell.forTableColumn(availableRoles));
        roleColumn.setEditable(true);

        roleColumn.setOnEditCommit(event -> {
            ProfileResponse editedUser = event.getRowValue();
            String newRole = event.getNewValue();
            UserRoleEnum role = UserRoleEnum.fromString(newRole);
            if (role != null) {
                editedUser.setRole(role.name());
                usersTable.refresh();
            }
        });

        usersTable.setEditable(true);
        usersTable.setItems(filteredUsers);
    }

    private void initializeFilters() {
        roleFilterCombo.getItems().clear();
        roleFilterCombo.getItems().add("Все");
        roleFilterCombo.getSelectionModel().selectFirst();
        roleFilterCombo.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldV, newV) -> applyFilters()
        );
    }

    private void loadUsers() {
        adminUserService.fetchAllUsersAsync(
                users -> Platform.runLater(() -> {
                    allUsers.setAll(users);
                    originalUsers = users.stream()
                            .map(u -> {
                                ProfileResponse copy = new ProfileResponse();
                                copy.setId(u.getId());
                                copy.setRole(u.getRole());
                                return copy;
                            })
                            .collect(Collectors.toList());
                    updateRolesFilter(users);
                    applyFilters();
                }),
                error -> Platform.runLater(() -> showAlert("Ошибка загрузки", error))
        );
    }

    private void updateRolesFilter(List<ProfileResponse> users) {
        Set<String> uniqueRoles = users.stream()
                .map(ProfileResponse::getRole)
                .filter(role -> UserRoleEnum.fromString(role) != null)
                .collect(Collectors.toSet());

        ObservableList<String> filterRoles = FXCollections.observableArrayList();
        filterRoles.add("Все");
        filterRoles.addAll(uniqueRoles);
        roleFilterCombo.setItems(filterRoles);
        roleFilterCombo.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    private void applyFilters() {
        String searchText = searchField.getText();
        String selectedRole = roleFilterCombo.getValue();
        filteredUsers.setAll(
                allUsers.stream()
                        .filter(user -> matchesSearch(user, searchText))
                        .filter(user -> matchesRole(user, selectedRole))
                        .collect(Collectors.toList())
        );
    }

    private boolean matchesSearch(ProfileResponse user, String searchText) {
        if (searchText == null || searchText.isEmpty()) return true;

        String lowerCaseFilter = searchText.toLowerCase();

        return safeContains(user.getUsername(), lowerCaseFilter)
                || safeContains(user.getFirstName(), lowerCaseFilter)
                || safeContains(user.getLastName(), lowerCaseFilter)
                || safeContains(user.getEmail(), lowerCaseFilter)
                || safeContains(user.getPhoneNumber(), lowerCaseFilter);
    }

    private boolean safeContains(String fieldValue, String searchTerm) {
        return fieldValue != null && fieldValue.toLowerCase().contains(searchTerm);
    }

    private boolean matchesRole(ProfileResponse user, String selectedRole) {
        if (selectedRole == null || selectedRole.equals("Все")) {
            return true;
        }
        return user.getRole() != null &&
                user.getRole().equalsIgnoreCase(selectedRole);
    }

    @FXML
    private void handleSaveChanges() {
        List<ProfileResponse> changedUsers = filteredUsers.stream()
                .filter(this::isRoleChanged)
                .peek(user -> {
                    if (UserRoleEnum.fromString(user.getRole()) == null) {
                        throw new IllegalArgumentException("Недопустимая роль: " + user.getRole());
                    }
                })
                .collect(Collectors.toList());

        if (changedUsers.isEmpty()) {
            showAlert("Информация", "Нет изменений для сохранения");
            return;
        }

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(changedUsers.size());

        changedUsers.forEach(user -> {
            String originalRole = getOriginalRoleFromSnapshot(user.getId());
            adminUserService.updateUserRoleAsync(
                    user.getId(), user.getRole(),
                    updatedUser -> {
                        updateUserInAllUsers(updatedUser);
                        successCount.incrementAndGet();
                        latch.countDown();
                        Platform.runLater(() -> {
                            applyFilters();
                            usersTable.refresh();
                        });
                    },
                    error -> {
                        revertUserRole(user.getId(), originalRole);
                        errorCount.incrementAndGet();
                        latch.countDown();
                        Platform.runLater(() -> {
                            usersTable.refresh();
                            showAlert("Ошибка", error);
                        });
                    }
            );
        });

        new Thread(() -> {
            try {
                latch.await();
                Platform.runLater(() -> {
                    showAlert("Результат", String.format(
                            "Обновлено: %d/%d | Ошибок: %d",
                            successCount.get(), changedUsers.size(), errorCount.get()
                    ));
                    loadUsers();
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private boolean isRoleChanged(ProfileResponse user) {
        return originalUsers.stream()
                .filter(orig -> orig.getId().equals(user.getId()))
                .findFirst()
                .map(orig -> !Objects.equals(orig.getRole(), user.getRole()))
                .orElse(false);
    }

    private String getOriginalRoleFromSnapshot(Long userId) {
        return originalUsers.stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .map(ProfileResponse::getRole)
                .orElse(null);
    }

    private void updateUserInAllUsers(ProfileResponse updatedUser) {
        for (int i = 0; i < allUsers.size(); i++) {
            if (allUsers.get(i).getId().equals(updatedUser.getId())) {
                allUsers.set(i, updatedUser);
                break;
            }
        }
    }

    private void revertUserRole(Long userId, String originalRole) {
        allUsers.stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .ifPresent(u -> u.setRole(originalRole));
        filteredUsers.stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .ifPresent(u -> u.setRole(originalRole));
    }

    @FXML
    private void handleDeleteUser() {
        ProfileResponse selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) return;
        if (selectedUser.getId().equals(SessionManager.getUserId())) {
            showAlert("Ошибка", "Нельзя удалить собственный аккаунт");
            return;
        }
        adminUserService.deleteUserAsync(
                selectedUser.getId(),
                success -> Platform.runLater(() -> {
                    allUsers.remove(selectedUser);
                    applyFilters();
                    showAlert("Успех", "Пользователь удален");
                }),
                error -> Platform.runLater(() -> showAlert("Ошибка", error))
        );
    }

    private void showErrorAndExit(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        System.exit(1);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML private void handleRefresh() { loadUsers(); }

    @FXML private void handleReset() {
        searchField.clear();
        roleFilterCombo.getSelectionModel().select("Все");
        applyFilters();
    }
}