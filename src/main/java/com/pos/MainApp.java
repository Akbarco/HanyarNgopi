package com.pos;

import com.pos.config.initDatabase;


import com.pos.service.AuthService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("Sistem Manajemen Cafe");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setWidth(1280);
        primaryStage.setHeight(800);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}