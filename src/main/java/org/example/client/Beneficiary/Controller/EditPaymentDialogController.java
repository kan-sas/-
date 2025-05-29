package org.example.client.Beneficiary.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.example.client.Beneficiary.Service.BeneficiaryPaymentService;
import org.example.client.Beneficiary.dto.PaymentTypeEnum;
import org.example.client.Beneficiary.dto.Request.PaymentRequest;
import org.example.client.Beneficiary.dto.Response.PaymentResponse;
import org.example.client.core.common.util.ShowAlertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class EditPaymentDialogController {
    @FXML private TextField amountField;
    @FXML private ComboBox<PaymentTypeEnum> typeCombo;
    @FXML private TextField departmentField;
    @FXML private TextArea commentArea;

    private PaymentResponse payment;
    private boolean saved = false;
    private static final Logger logger = LoggerFactory.getLogger(EditPaymentDialogController.class);

    /**
     * Этот метод JavaFX вызовет сразу после загрузки FXML — наполняем ComboBox
     */
    @FXML
    private void initialize() {
        typeCombo.getItems().setAll(PaymentTypeEnum.values());
    }

    public void setPaymentData(PaymentResponse payment) {
        this.payment = payment;
        amountField.setText(String.format(Locale.US, "%.2f", payment.getAmount()));
        // Предположим, payment.getType() возвращает имя enum (например, "ALLOWANCE")
        try {
            typeCombo.getSelectionModel().select(PaymentTypeEnum.valueOf(payment.getType()));
        } catch (IllegalArgumentException e) {
            // если вдруг в ответе не enum-name, пробуем по displayName
            typeCombo.getSelectionModel()
                    .select(PaymentTypeEnum.fromDisplayName(payment.getType()));
        }
        departmentField.setText(payment.getDepartment());
        commentArea.setText(payment.getComment());
    }

    @FXML
    public void handleSave() {
        System.out.println("[EditPaymentDialog] ▶️ handleSave() called");
        try {
            BigDecimal amount = parseAmount(amountField.getText());
            System.out.println("[EditPaymentDialog] 🔄 Parsed amount: " + amount);

            PaymentTypeEnum selectedType = typeCombo.getValue();
            if (selectedType == null) {
                ShowAlertUtil.showErrorAlert("Ошибка", "Выберите тип выплаты");
                return;
            }

            PaymentRequest request = PaymentRequest.builder()
                    .beneficiaryId(payment.getBeneficiary().getId())
                    .amount(amount.toString())
                    .type(selectedType.name())  // отправляем имя enum
                    .department(departmentField.getText())
                    .comment(commentArea.getText())
                    .build();

            System.out.println("[EditPaymentDialog] 🔼 About to call updatePayment() for ID=" + payment.getId());
            new BeneficiaryPaymentService().updatePayment(payment.getId(), request);
            System.out.println("[EditPaymentDialog] ✅ updatePayment() returned");

            saved = true;
        } catch (ParseException e) {
            System.out.println("[EditPaymentDialog] ❌ ParseException: " + e.getMessage());
            ShowAlertUtil.showErrorAlert("Ошибка", "Некорректный формат суммы");
        } catch (Exception e) {
            System.out.println("[EditPaymentDialog] ❌ Exception in handleSave(): " + e.getMessage());
            e.printStackTrace();
            ShowAlertUtil.showErrorAlert("Ошибка", e.getMessage());
        }
    }

    private BigDecimal parseAmount(String input) throws ParseException {
        NumberFormat format = NumberFormat.getInstance(new Locale("ru", "RU"));
        return new BigDecimal(format.parse(input).toString());
    }

    public boolean isSaved() {
        return saved;
    }
}
