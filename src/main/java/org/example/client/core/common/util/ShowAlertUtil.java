package org.example.client.core.common.util;

import javafx.scene.control.Alert;

public class ShowAlertUtil {
    public static void showSuccessAlert(String message) {
        createAlert(Alert.AlertType.INFORMATION, "Успех", message);
    }

    public static void showErrorAlert(String title, String message) {
        createAlert(Alert.AlertType.ERROR, title, message);
    }

    public static void showInfoAlert(String title, String message) {
        createAlert(Alert.AlertType.INFORMATION, title, message);
    }

    private static void createAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
