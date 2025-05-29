package org.example.client.Beneficiary.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.example.client.Beneficiary.Service.BeneficiaryPaymentService;
import org.example.client.Beneficiary.dto.PaymentTypeEnum;
import org.example.client.Beneficiary.dto.Request.PaymentRequest;
import org.example.client.Beneficiary.dto.Response.PaymentResponse;
import org.example.client.core.common.util.ShowAlertUtil;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class CreateBeneficiaryPaymentController {

    @FXML private TextField amountField;
    @FXML private ComboBox<PaymentTypeEnum> typeComboBox;
    @FXML private TextField departmentField;
    @FXML private TextArea commentArea;

    private final BeneficiaryPaymentService paymentService = new BeneficiaryPaymentService();
    private AnchorPane mainMenuContent;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public void setMainMenuContent(AnchorPane content) {
        this.mainMenuContent = content;
    }

    @FXML
    public void initialize() {
        setupTypeComboBox();
    }


    private void setupTypeComboBox() {
        typeComboBox.getItems().setAll(PaymentTypeEnum.values());
        typeComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(PaymentTypeEnum type) {
                return type != null ? type.getDisplayName() : "";
            }
            @Override
            public PaymentTypeEnum fromString(String string) {
                return PaymentTypeEnum.fromDisplayName(string);
            }
        });
    }

    @FXML
    private void handleSubmit() {
        try {
            PaymentRequest request = validateAndBuildRequest();
            PaymentResponse payment = paymentService.createPayment(request);
            ShowAlertUtil.showSuccessAlert(buildSuccessMessage(payment));
            returnToMainMenu();
        } catch (Exception e) {
            handleError(e);
        }
    }

    private String buildSuccessMessage(PaymentResponse payment) {
        String statusDisplay = switch (payment.getStatus()) {
            case PENDING   -> "На рассмотрении";
            case APPROVED  -> "Подтверждена";
            case REJECTED  -> "Отклонена";
            case COMPLETED -> "Завершена";
            default        -> "Неизвестный статус";
        };

        return String.format(
                """
                Выплата успешно создана!
                ------------------------
                ID: %d
                Получатель: %s
                Тип: %s
                Сумма: %s ₽
                Дата: %s
                Статус: %s
                """,
                payment.getId(),
                payment.getBeneficiary().getFullName(),
                payment.getType(),
                payment.getAmount(),
                payment.getDate().format(DATE_FORMATTER),
                statusDisplay
        );
    }

    private PaymentRequest validateAndBuildRequest() {
        // убрали beneficiaryId — сервис сам его подставит
        return PaymentRequest.builder()
                .amount(parseAmount())
                .type(validatePaymentType())
                .department(departmentField.getText().trim())
                .comment(commentArea.getText().trim())
                .build();
    }

    private String parseAmount() {
        String txt = amountField.getText().trim();
        if (txt.isEmpty()) {
            throw new IllegalArgumentException("Поле суммы не может быть пустым");
        }
        try {
            new BigDecimal(txt);
            return txt;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Некорректный формат суммы. Пример: 12345.67");
        }
    }

    private String validatePaymentType() {
        PaymentTypeEnum type = typeComboBox.getValue();
        if (type == null) {
            throw new IllegalArgumentException("Необходимо выбрать тип выплаты");
        }
        return type.name();
    }

    private void handleError(Exception e) {
        String msg = (e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        ShowAlertUtil.showErrorAlert("Ошибка создания выплаты", msg);
    }

    @FXML
    private void handleCancel() {
        returnToMainMenu();
    }
    private void closeWindow() {
        Stage stage = (Stage) amountField.getScene().getWindow();
        stage.close();
    }
    private void returnToMainMenu() {
        if (mainMenuContent != null) {
            mainMenuContent.getChildren().clear();
            Label welcomeLabel = new Label("Добро пожаловать!");
            welcomeLabel.setStyle("-fx-font-size: 20px;");
            AnchorPane.setTopAnchor(welcomeLabel, 20.0);
            AnchorPane.setLeftAnchor(welcomeLabel, 20.0);
            mainMenuContent.getChildren().add(welcomeLabel);
        }
    }
}
