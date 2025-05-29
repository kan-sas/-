package org.example.client;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.client.core.common.util.SceneSwitcherUtil;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage primaryStage) {
        SceneSwitcherUtil.initialize(primaryStage);

        try {
            //
            // Используем правильный путь
            SceneSwitcherUtil.switchTo("/org/example/client/Auth.fxml", "Авторизация");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1); // Важно прервать выполнение при ошибке
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
