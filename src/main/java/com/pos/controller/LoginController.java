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
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller untuk layar login.
 *
 * Layar ini menerima username/password, mengirim validasi ke AuthService,
 * lalu membuka dashboard saat akun ditemukan di database.
 */
public class LoginController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private Region loginCard;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;

    private final AuthService authService = new AuthService();

    /** Menyiapkan ukuran kartu login responsif dan fokus awal ke input username. */
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

    /** Mengatur lebar kartu login mengikuti ukuran window. */
    private void updateCardWidth(double sceneWidth) {
        if (loginCard == null) {
            return;
        }

        double targetWidth = Math.max(800, Math.min(920, sceneWidth * 0.90));
        loginCard.setPrefWidth(targetWidth);
        loginCard.setMaxWidth(targetWidth);
    }

    @FXML
    /** Validasi input login lalu memanggil AuthService.login. */
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

    /** Membuka scene dashboard setelah login sukses. */
    private void openDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/pos/view/dashboard.fxml")
            );
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 1280, 800);
            stage.setScene(scene);
            stage.setTitle("HanyarNgopi - Dashboard");
            forceFullWindow(stage);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Gagal membuka dashboard.");
        }
    }

    /** Memaksimalkan window agar aplikasi demo langsung tampil penuh. */
    private void forceFullWindow(Stage stage) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        stage.setMaximized(false);
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        Platform.runLater(() -> {
            stage.setMaximized(true);
            stage.toFront();
        });
    }
}
