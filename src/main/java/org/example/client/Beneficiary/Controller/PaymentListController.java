package org.example.client.Beneficiary.Controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.client.Beneficiary.Service.BeneficiaryPaymentService;
import org.example.client.Beneficiary.dto.PaymentStatusEnum;
import org.example.client.Beneficiary.dto.Response.ApproverInfo;
import org.example.client.Beneficiary.dto.Response.BeneficiaryShortInfo;
import org.example.client.Beneficiary.dto.Response.PaymentResponse;
import org.example.client.SocialWorker.Service.SocialWorkerPaymentService;
import org.example.client.SocialWorker.dto.Request.PaymentStatusUpdateRequest;
import org.example.client.core.common.dto.UserRoleEnum;
import org.example.client.core.common.util.SceneSwitcherUtil;
import org.example.client.core.common.util.SessionManager;
import org.example.client.core.common.util.ShowAlertUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PaymentListController {

    @FXML private Label roleLabel;
    @FXML private Button refreshButton;
    @FXML private TableView<PaymentResponse> paymentsTable;
    @FXML private TableColumn<PaymentResponse, String> typeColumn;
    @FXML private TableColumn<PaymentResponse, String> amountColumn;
    @FXML private TableColumn<PaymentResponse, String> dateColumn;
    @FXML private TableColumn<PaymentResponse, String> statusColumn;
    @FXML private TableColumn<PaymentResponse, String> beneficiaryColumn;
    @FXML private TableColumn<PaymentResponse, String> approverColumn;
    @FXML private Label departmentLabel;
    @FXML private Label commentLabel;
    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Button detailsButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private final BeneficiaryPaymentService beneficiaryPaymentService = new BeneficiaryPaymentService();
    private final SocialWorkerPaymentService socialWorkerPaymentService = new SocialWorkerPaymentService();
    private UserRoleEnum userRole;
    private AnchorPane mainMenuContent;

    @FXML
    public void initialize() {
        checkAuthentication();
        initializeUserRole();
        setupUIByRole();
        setupTableColumns();
        setupSelectionListener();
        setupButtons();
        loadPayments();
    }

    private void checkAuthentication() {
        if (!SessionManager.isAuthenticated()) {
            ShowAlertUtil.showErrorAlert("Ошибка авторизации", "Требуется вход в систему");
            navigateToAuth();
        }
    }

    private void initializeUserRole() {
        String role = SessionManager.getRole();
        userRole = UserRoleEnum.fromString(role);
    }

    private void setupUIByRole() {
        boolean isSocialWorker = userRole == UserRoleEnum.SOCIAL_WORKER;
        boolean isBeneficiary = userRole == UserRoleEnum.BENEFICIARY;

        approverColumn.setVisible(isSocialWorker);
        approveButton.setVisible(isSocialWorker);
        rejectButton.setVisible(isSocialWorker);
        editButton.setVisible(isBeneficiary);
        deleteButton.setVisible(isBeneficiary);
        roleLabel.setText(userRole.getDisplayName());
    }

    private void setupTableColumns() {
        typeColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getType()));

        amountColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(formatAmount(cd.getValue().getAmount())));

        dateColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(formatDate(cd.getValue().getDate())));

        statusColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(getStatusDisplayName(cd.getValue().getStatus())));

        beneficiaryColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(getBeneficiaryName(cd.getValue().getBeneficiary())));

        approverColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(getApproverName(cd.getValue().getApprover())));
    }

    private String formatAmount(BigDecimal amount) {
        return amount != null ?
                String.format("%,.2f ₽", amount) :
                "0.00 ₽";
    }

    private String formatDate(LocalDate date) {
        return date != null ?
                date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) :
                "Дата не указана";
    }

    private String getStatusDisplayName(PaymentStatusEnum status) {
        return switch (status) {
            case PENDING -> "На рассмотрении";
            case APPROVED -> "Подтверждена";
            case REJECTED -> "Отклонена";
            case COMPLETED -> "Завершена";
            default -> "Неизвестный статус";
        };
    }

    private String getBeneficiaryName(BeneficiaryShortInfo beneficiary) {
        return beneficiary != null ?
                beneficiary.getFullName() :
                "Неизвестный получатель";
    }

    private String getApproverName(ApproverInfo approver) {
        return approver != null ?
                String.format("%s %s", approver.getFirstName(), approver.getLastName()) :
                "Не подтверждено";
    }

    private void setupSelectionListener() {
        paymentsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    showPaymentDetails(newVal);
                    updateButtonsState();
                }
        );
    }

    private void showPaymentDetails(PaymentResponse payment) {
        if (payment != null) {
            departmentLabel.setText(payment.getDepartment());
            commentLabel.setText(payment.getComment() != null ?
                    payment.getComment() : "Нет комментария");
        }
    }

    private void setupButtons() {
        refreshButton.setOnAction(e -> loadPayments());
        approveButton.setOnAction(e -> updatePaymentStatus(PaymentStatusEnum.APPROVED));
        rejectButton.setOnAction(e -> updatePaymentStatus(PaymentStatusEnum.REJECTED));
        detailsButton.setOnAction(e -> showPaymentDetailsDialog());
        deleteButton.setOnAction(e -> handleDeletePayment());
        editButton.setOnAction(e -> handleEditPayment());
    }

    private void loadPayments() {
        new Thread(() -> {
            try {
                List<PaymentResponse> payments = userRole == UserRoleEnum.SOCIAL_WORKER ?
                        socialWorkerPaymentService.getPendingPayments() :
                        beneficiaryPaymentService.getAllPaymentsByBeneficiary(SessionManager.getUserId());

                Platform.runLater(() -> {
                    paymentsTable.setItems(FXCollections.observableArrayList(payments));
                    paymentsTable.refresh();
                    updateButtonsState();
                });
            } catch (Exception ex) {
                Platform.runLater(() ->
                        ShowAlertUtil.showErrorAlert("Ошибка загрузки", ex.getMessage()));
            }
        }).start();
    }

    private void updateButtonsState() {
        PaymentResponse selected = paymentsTable.getSelectionModel().getSelectedItem();
        boolean isEditable = selected != null && isPaymentEditable(selected);

        editButton.setDisable(!isEditable);
        deleteButton.setDisable(!isEditable);
    }

    private boolean isPaymentEditable(PaymentResponse payment) {
        return payment.getStatus() == PaymentStatusEnum.PENDING;
    }

    private void handleDeletePayment() {
        PaymentResponse selected = paymentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowAlertUtil.showErrorAlert("Ошибка", "Выберите выплату для удаления");
            return;
        }

        if (!isPaymentEditable(selected)) {
            ShowAlertUtil.showErrorAlert("Ошибка", "Нельзя удалить выплату в текущем статусе");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Подтверждение удаления");
        confirmation.setHeaderText("Вы уверены, что хотите удалить эту выплату?");
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deletePayment(selected.getId());
            }
        });
    }

    private void deletePayment(Long paymentId) {
        new Thread(() -> {
            try {
                beneficiaryPaymentService.deletePayment(paymentId);
                Platform.runLater(() -> {
                    paymentsTable.getItems().removeIf(p -> p.getId().equals(paymentId));
                    ShowAlertUtil.showSuccessAlert("Выплата успешно удалена");
                });
            } catch (Exception ex) {
                Platform.runLater(() ->
                        ShowAlertUtil.showErrorAlert("Ошибка удаления", ex.getMessage()));
            }
        }).start();
    }

    private void handleEditPayment() {
        PaymentResponse selected = paymentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowAlertUtil.showErrorAlert("Ошибка", "Выберите выплату для редактирования");
            return;
        }

        if (!isPaymentEditable(selected)) {
            ShowAlertUtil.showErrorAlert("Ошибка", "Редактирование невозможно для текущего статуса");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/client/Beneficiary/EditPaymentDialog.fxml")
            );
            DialogPane pane = loader.load();
            EditPaymentDialogController controller = loader.getController();
            controller.setPaymentData(selected);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Редактирование выплаты");
            dialog.initModality(Modality.APPLICATION_MODAL);

            // 1) Найдём ваш ButtonType с ButtonData.APPLY
            ButtonType applyType = dialog.getDialogPane()
                    .getButtonTypes()
                    .stream()
                    .filter(bt -> bt.getButtonData() == ButtonBar.ButtonData.APPLY)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("В FXML нет кнопки APPLY"));

            // 2) Получим сам Button и переименуем/навесим фильтр
            Button applyBtn = (Button) dialog.getDialogPane().lookupButton(applyType);
            applyBtn.setText("Сохранить"); // можно переименовать
            applyBtn.addEventFilter(ActionEvent.ACTION, event -> {
                controller.handleSave();
                if (!controller.isSaved()) {
                    event.consume(); // не закрываем диалог, если сохранение упало
                }
            });

            // 3) Теперь ждём именно APPLY
            dialog.showAndWait().ifPresent(response -> {
                if (response == applyType && controller.isSaved()) {
                    loadPayments();
                }
            });

        } catch (IOException e) {
            ShowAlertUtil.showErrorAlert("Ошибка", "Не удалось открыть форму редактирования");
        }
    }

    private void updatePaymentStatus(PaymentStatusEnum status) {
        PaymentResponse selected = paymentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowAlertUtil.showErrorAlert("Ошибка", "Выберите выплату");
            return;
        }

        PaymentStatusUpdateRequest request = new PaymentStatusUpdateRequest();
        request.setStatus(status);
        request.setSocialWorkerId(SessionManager.getUserId());

        new Thread(() -> {
            try {
                PaymentResponse updated = socialWorkerPaymentService.updatePaymentStatus(
                        selected.getId(), request
                );
                Platform.runLater(() -> {
                    paymentsTable.getItems().replaceAll(p ->
                            p.getId().equals(updated.getId()) ? updated : p);
                    paymentsTable.refresh();
                    ShowAlertUtil.showSuccessAlert("Статус успешно обновлен");
                });
            } catch (Exception ex) {
                Platform.runLater(() ->
                        ShowAlertUtil.showErrorAlert("Ошибка обновления", ex.getMessage()));
            }
        }).start();
    }

    private void showPaymentDetailsDialog() {
        PaymentResponse selected = paymentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String details = String.format(
                """
                Детали выплаты:
                ------------------
                ID: %d
                Тип: %s
                Сумма: %s
                Статус: %s
                Дата: %s
                Отдел: %s
                Комментарий: %s
                """,
                selected.getId(),
                selected.getType(),
                formatAmount(selected.getAmount()),
                getStatusDisplayName(selected.getStatus()),
                formatDate(selected.getDate()),
                selected.getDepartment(),
                selected.getComment() != null ? selected.getComment() : "Нет комментария"
        );

        ShowAlertUtil.showInfoAlert("Подробности выплаты", details);
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) paymentsTable.getScene().getWindow();
        stage.close();
    }

    private void navigateToAuth() {
        try {
            SceneSwitcherUtil.switchTo("/org/example/client/Auth.fxml", "Авторизация");
        } catch (IOException e) {
            System.err.println("Ошибка перехода: " + e.getMessage());
        }
    }

    public void setMainMenuContent(AnchorPane content) {
        this.mainMenuContent = content;
    }

    public void setUserRole(UserRoleEnum userRole) {
        this.userRole = userRole;
        setupUIByRole();
        roleLabel.setText(userRole.getDisplayName());
    }
}