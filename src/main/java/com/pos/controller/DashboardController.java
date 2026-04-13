package com.pos.controller;

import com.pos.service.AuthService;
import com.pos.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private Label lblUsername;
    @FXML private Button btnDashboard;
    @FXML private Button btnMenu;
    @FXML private Button btnStok;
    @FXML private Button btnTransaksi;
    @FXML private Button btnHutang;
    @FXML private Button btnLaporan;
    @FXML private ImageView logoImage;

    private List<Button> navButtons;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (AuthService.getLoggedInUser() != null && lblUsername != null) {
            String username = AuthService.getLoggedInUser().getUsername();
            lblUsername.setText(capitalize(username));
        }

        navButtons = new ArrayList<>();
        if (btnDashboard != null) navButtons.add(btnDashboard);
        if (btnMenu != null) navButtons.add(btnMenu);
        if (btnStok != null) navButtons.add(btnStok);
        if (btnTransaksi != null) navButtons.add(btnTransaksi);
        if (btnHutang != null) navButtons.add(btnHutang);
        if (btnLaporan != null) navButtons.add(btnLaporan);

        loadLogo();

        javafx.application.Platform.runLater(() -> {
            if (contentArea != null) {
                openDashboardHome();
            }
        });
    }

    private void setActiveNav(Button activeBtn) {
        if (navButtons == null || activeBtn == null) return;
        for (Button btn : navButtons) {
            btn.getStyleClass().removeAll("nav-btn-active");
            if (!btn.getStyleClass().contains("nav-btn")) {
                btn.getStyleClass().add("nav-btn");
            }
        }
        activeBtn.getStyleClass().remove("nav-btn");
        if (!activeBtn.getStyleClass().contains("nav-btn-active")) {
            activeBtn.getStyleClass().add("nav-btn-active");
        }
    }

    @FXML
    public void openDashboardHome() {
        setActiveNav(btnDashboard);
        loadView("/com/pos/view/dashboard_home.fxml");
    }

    @FXML
    public void openMenu() {
        setActiveNav(btnMenu);
        loadView("/com/pos/view/menu.fxml");
    }

    @FXML
    public void openStock() {
        setActiveNav(btnStok);
        loadView("/com/pos/view/stock.fxml");
    }

    @FXML
    public void openTransaksi() {
        setActiveNav(btnTransaksi);
        loadView("/com/pos/view/kasir.fxml");
    }

    @FXML
    public void openHutang() {
        setActiveNav(btnHutang);
        loadView("/com/pos/view/debt.fxml");
    }

    @FXML
    public void openLaporan() {
        setActiveNav(btnLaporan);
        loadView("/com/pos/view/laporan.fxml");
    }


    @FXML
    public void handleLogout() {
        try {
            AuthService.logout();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/pos/view/login.fxml")
            );
            Stage stage = (Stage) contentArea.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 960, 720);
            stage.setScene(scene);
            stage.setTitle("HanyarNgopi");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Gagal logout.");
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(fxmlPath)
            );
            javafx.scene.Node view = loader.load();
            if (view instanceof Region region) {
                region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            }
            StackPane.setAlignment(view, javafx.geometry.Pos.TOP_LEFT);
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Gagal membuka halaman.");
        }
    }

    private void loadLogo() {
        if (logoImage == null) {
            return;
        }

        InputStream stream = getClass().getResourceAsStream("/com/pos/view/image/logo.jpeg");
        if (stream != null) {
            logoImage.setImage(new Image(stream));
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
