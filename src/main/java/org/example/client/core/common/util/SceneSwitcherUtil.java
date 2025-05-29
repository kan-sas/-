package org.example.client.core.common.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class SceneSwitcherUtil {
    private static Stage primaryStage;

    public static void initialize(Stage stage) {
        primaryStage = stage;
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
    }

    public static void switchTo(String fxmlPath, String title, int width, int height) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneSwitcherUtil.class.getResource(fxmlPath));
        Parent root = loader.load();

        Scene scene = new Scene(root, width, height);

        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    // Добавлены методы для работы с контроллерами
    public static <T> T switchToWithController(String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneSwitcherUtil.class.getResource(fxmlPath));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();

        return loader.getController();
    }

    public static void switchTo(String fxmlPath, String title) throws IOException {
        switchTo(fxmlPath, title, 800, 600);
    }
    public static void showModal(String fxmlPath, String title, int width, int height) throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(SceneSwitcherUtil.class.getResource(fxmlPath));
        Parent root = loader.load();

        stage.setTitle(title);
        stage.setScene(new Scene(root, width, height));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }
}