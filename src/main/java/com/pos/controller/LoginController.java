package com.pos.controller;

import com.pos.model.User;
import com.pos.service.AuthService;
import com.pos.util.AlertUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private VBox loginCard;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;

    private final AuthService authService = new AuthService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            if (rootPane != null) {
                rootPane.widthProperty().addListener((obs, oldValue, newValue) ->
                        updateCardWidth(newValue.doubleValue()));
                updateCardWidth(rootPane.getWidth());
            }
            if (txtUsername != null) {
                txtUsername.requestFocus();
            }
        });
    }

    private void updateCardWidth(double sceneWidth) {
        if (loginCard == null) {
            return;
        }

        double targetWidth = Math.max(360, Math.min(560, sceneWidth * 0.74));
        loginCard.setPrefWidth(targetWidth);
        loginCard.setMaxWidth(targetWidth);
    }

    @FXML
    public void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            AlertUtil.showError("Error", "Username dan password tidak boleh kosong!");
            return;
        }

        User user = authService.login(username, password);
        if (user != null) {
            openDashboard();
        } else {
            AlertUtil.showError("Login Gagal", "Username atau password salah!");
        }
    }

    private void openDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/pos/view/dashboard.fxml")
            );
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 1280, 800);
            stage.setScene(scene);
            stage.setTitle("HanyarNgopi - Dashboard");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Gagal membuka dashboard.");
        }
    }
}
