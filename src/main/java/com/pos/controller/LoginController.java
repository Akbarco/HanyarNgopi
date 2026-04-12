package com.pos.controller;

import com.pos.model.User;
import com.pos.service.AuthService;
import com.pos.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;

    private final AuthService authService = new AuthService();

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
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("POS - Dashboard");
            stage.setWidth(1100);
            stage.setHeight(700);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Gagal membuka dashboard.");
        }
    }
}
