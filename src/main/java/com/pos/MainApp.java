package com.pos;

import com.pos.config.initDatabase;
import com.pos.service.AuthService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        initDatabase.init();

        AuthService authService = new AuthService();
        authService.initDefaultUsers();

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/pos/view/login.fxml")
        );
        Scene scene = new Scene(loader.load(), 960, 720);
        primaryStage.setTitle("Manajemen HanyarNgopi");
        primaryStage.getIcons().add(new Image(
                getClass().getResourceAsStream("/com/pos/view/image/logo.jpeg")
        ));
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(650);
        primaryStage.setResizable(true);
        primaryStage.show();
        primaryStage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
