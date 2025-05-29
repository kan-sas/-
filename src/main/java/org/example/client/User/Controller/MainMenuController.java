package org.example.client.User.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.client.Beneficiary.Controller.CreateBeneficiaryPaymentController;
import org.example.client.Beneficiary.Controller.PaymentListController;
import org.example.client.Beneficiary.Service.BeneficiaryPaymentService;
import org.example.client.User.Service.ProfileService;
import org.example.client.core.common.dto.UserRoleEnum;
import org.example.client.core.common.util.SceneSwitcherUtil;
import org.example.client.core.common.util.SessionManager;
import org.example.client.core.common.util.ShowAlertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MainMenuController {

    @FXML private VBox leftMenuVBox;
    @FXML private Label menuTitle;
    @FXML private Button btnCreatePayment;
    @FXML private Button btnEditAccount;
    @FXML private Button btnViewPayments;
    @FXML private Button btnLogout;
    @FXML private AnchorPane contentArea;

    private final BeneficiaryPaymentService beneficiaryPaymentService = new BeneficiaryPaymentService();
    private final ProfileService profileService = new ProfileService();
    private UserRoleEnum userRole;
    private static final Logger log = LoggerFactory.getLogger(MainMenuController.class);

    @FXML
    public void initialize() {
        log.info("Инициализация главного меню");
        if (!SessionManager.isAuthenticated()) {
            log.warn("Пользователь не аутентифицирован, перенаправление на авторизацию");
            try {
                SceneSwitcherUtil.switchTo("/Auth.fxml", "Авторизация");
            } catch (IOException e) {
                log.error("Ошибка перехода на авторизацию", e);
                ShowAlertUtil.showErrorAlert("Ошибка", "Не удалось загрузить форму авторизации");
            }
            return;
        }
        loadUserRole();
        configureUIForRole();
    }

    // Исправленный метод загрузки роли
    private void loadUserRole() {
        String roleString = SessionManager.getRole();
        userRole = UserRoleEnum.fromString(roleString);

        if (userRole == null) {
            System.err.println("Неизвестная роль пользователя: " + roleString);
        }
    }

    // Обновленный метод конфигурации интерфейса
    private void configureUIForRole() {
        if (userRole == null) {
            configureDefault();
            return;
        }

        switch (userRole) {
            case BENEFICIARY:
                configureForBeneficiary();
                break;
            case SOCIAL_WORKER:
                configureForSocialWorker();
                break;
            case ADMIN:
                configureForAdmin();
                break;
            default:
                configureDefault();
                break;
        }
    }

    private void configureDefault() {
        menuTitle.setText("Главное меню");
        btnCreatePayment.setVisible(false);
        btnViewPayments.setVisible(false);
    }

    private void configureForBeneficiary() {
        menuTitle.setText("Меню получателя");
        btnCreatePayment.setVisible(true);
        btnViewPayments.setText("Мои выплаты");
        btnViewPayments.setVisible(true);
    }

    private void configureForSocialWorker() {
        menuTitle.setText("Меню соц. работника");
        btnCreatePayment.setVisible(false);
        btnViewPayments.setText("Все выплаты");
        btnViewPayments.setVisible(true);
    }

    private void configureForAdmin() {
        menuTitle.setText("Панель администратора");
        btnCreatePayment.setVisible(false);
        btnViewPayments.setText("Управление выплатами");
        btnViewPayments.setVisible(true);
        addAdminButtons();
    }

    private void addApproveButton() {
        Button btnApprove = new Button("Подтверждение выплат");
        btnApprove.setPrefWidth(150);
        btnApprove.setOnAction(e -> loadApprovalInterface());
        leftMenuVBox.getChildren().add(3, btnApprove);
    }

    private void addAdminButtons() {
        Button btnManageUsers = new Button("Управление пользователями");
        btnManageUsers.setPrefWidth(150);
        btnManageUsers.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnManageUsers.setOnAction(e -> loadAdminPanel());
        leftMenuVBox.getChildren().add(3, btnManageUsers);
    }
    @FXML
    private void handleViewPayments() {
        try {
            // Создаем новое окно
            Stage paymentsStage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/example/client/Beneficiary/PaymentList.fxml"
            ));

            Parent root = loader.load();
            PaymentListController controller = loader.getController();
            controller.setUserRole(userRole);

            // Настраиваем сцену и окно
            paymentsStage.setScene(new Scene(root, 1000, 600));
            paymentsStage.setTitle("Управление выплатами");
            paymentsStage.initModality(Modality.WINDOW_MODAL);
            paymentsStage.initOwner(btnViewPayments.getScene().getWindow());

            paymentsStage.show();

        } catch (IOException e) {
            log.error("Ошибка загрузки интерфейса выплат", e);
            ShowAlertUtil.showErrorAlert("Ошибка", "Не удалось загрузить список выплат");
        }
    }

    @FXML
    private void handleCreatePayment() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/example/client/Beneficiary/CreateBeneficiaryPayment.fxml"
            ));

            Parent paymentForm = loader.load();
            CreateBeneficiaryPaymentController controller = loader.getController();
            controller.setMainMenuContent(contentArea);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(paymentForm);

        } catch (IOException e) {
            log.error("Ошибка загрузки формы создания выплаты", e);
            ShowAlertUtil.showErrorAlert("Ошибка", "Не удалось открыть форму создания выплаты");
        }
    }



    private void loadApprovalInterface() {
        try {
            SceneSwitcherUtil.switchTo("/views/approval_queue.fxml", "Подтверждение выплат");
        } catch (IOException e) {
            ShowAlertUtil.showErrorAlert("Ошибка", "Ошибка загрузки интерфейса подтверждения");
        }
    }


    @FXML
    private void handleEditAccount() {
        try {
            SceneSwitcherUtil.<ProfileController>switchToWithController(
                    "/org/example/client/Profile.fxml",
                    "Редактирование профиля"
            );
        } catch (IOException e) {
            log.error("Ошибка загрузки формы профиля", e);
            ShowAlertUtil.showErrorAlert("Ошибка", "Ошибка загрузки формы профиля");
        }
    }

    private void loadAdminPanel() {
        try {
            SceneSwitcherUtil.switchTo(
                    "/org/example/client/Admin/AdminUserPanel.fxml",
                    "Административная панель"
            );
        } catch (IOException e) {
            ShowAlertUtil.showErrorAlert("Ошибка", "Ошибка загрузки админ-панели");
        }
    }

    @FXML
    private void handleLogout() {
        log.info("Запрос на выход из системы");
        SessionManager.clearSession();
        try {
            SceneSwitcherUtil.switchTo("/org/example/client/Auth.fxml", "Авторизация");
        } catch (IOException e) {
            ShowAlertUtil.showErrorAlert("Ошибка", "Ошибка при выходе из системы");
        }
    }
}