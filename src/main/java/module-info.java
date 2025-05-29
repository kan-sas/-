module org.example.client {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // Сторонние библиотеки
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires okhttp3;
    requires retrofit2;
    requires retrofit2.converter.gson;
    requires java.prefs;
    requires jjwt.api;
    requires java.management;
    requires com.google.gson;
    requires gson.javatime.serialisers;
    requires org.slf4j;
    requires java.logging;
    requires okhttp3.logging;
    requires com.fasterxml.jackson.annotation;
    requires jdk.jfr;
    requires static lombok;

    // Открываем на рефлексию для FXMLLoader только пакеты с контроллерами
    opens org.example.client.User.Controller to javafx.fxml;
    opens org.example.client.Admin.Controller to javafx.fxml;
    opens org.example.client.Beneficiary.Controller to javafx.fxml;

    opens org.example.client to javafx.fxml;

    opens org.example.client.User.dto.Request to com.google.gson;
    opens org.example.client.Admin.dto.Request to com.google.gson;
    opens org.example.client.User.dto.Response to javafx.base, com.google.gson;
    opens org.example.client.core.common.dto to javafx.base, com.google.gson;
    opens org.example.client.Beneficiary.dto.Request to javafx.base, com.google.gson;
    opens org.example.client.Beneficiary.dto.Response to javafx.base, com.google.gson;
    opens org.example.client.Beneficiary.dto to javafx.base, com.google.gson;
    opens org.example.client.SocialWorker.dto.Request to javafx.base, com.google.gson;

    // Экспортируем только публичные API вашего модуля
    exports org.example.client;
    exports org.example.client.User.Controller;
    exports org.example.client.Admin.Controller;
    exports org.example.client.Beneficiary.Controller;

    exports org.example.client.User.Service;

    exports org.example.client.User.dto.Request;
    exports org.example.client.User.dto.Response;
    exports org.example.client.Beneficiary.dto.Request;
    exports org.example.client.Beneficiary.dto.Response;
    exports org.example.client.SocialWorker.dto.Request;

}
