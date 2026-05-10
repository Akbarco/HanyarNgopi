package com.pos.controller;

import com.pos.service.AuthService;
import com.pos.util.AlertUtil;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
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
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller untuk shell utama setelah login.
 *
 * Tugasnya mengatur sidebar, menampilkan nama user dari AuthService,
 * lalu memuat halaman anak seperti dashboard, menu, stok, kasir, hutang, dan laporan.
 */
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

    /** Menyiapkan nama user, daftar tombol navigasi, logo, lalu membuka beranda dashboard. */
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

    /** Memberi style aktif pada tombol sidebar yang sedang dipilih. */
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
    /** Membuka ringkasan dashboard home di area konten utama. */
    public void openDashboardHome() {
        setActiveNav(btnDashboard);
        loadView("/com/pos/view/dashboard_home.fxml");
    }

    @FXML
    /** Membuka halaman pengelolaan menu. */
    public void openMenu() {
        setActiveNav(btnMenu);
        loadView("/com/pos/view/menu.fxml");
    }

    @FXML
    /** Membuka halaman pengelolaan stok bahan/menu. */
    public void openStock() {
        setActiveNav(btnStok);
        loadView("/com/pos/view/stock.fxml");
    }

    @FXML
    /** Membuka halaman transaksi kasir dan riwayat penjualan. */
    public void openTransaksi() {
        setActiveNav(btnTransaksi);
        loadView("/com/pos/view/kasir.fxml");
    }

    @FXML
    /** Membuka halaman pencatatan hutang dan piutang. */
    public void openHutang() {
        setActiveNav(btnHutang);
        loadView("/com/pos/view/debt.fxml");
    }

    @FXML
    /** Membuka halaman laporan penjualan, stok, dan hutang-piutang. */
    public void openLaporan() {
        setActiveNav(btnLaporan);
        loadView("/com/pos/view/laporan.fxml");
    }


    @FXML
    /** Menghapus session login lalu mengembalikan aplikasi ke layar login. */
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
            forceFullWindow(stage);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Gagal logout.");
        }
    }

    /** Memuat file FXML halaman anak ke dalam contentArea dashboard. */
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

    /** Mengambil logo dari resources aplikasi dan memasangnya ke ImageView. */
    private void loadLogo() {
        if (logoImage == null) {
            return;
        }

        InputStream stream = getClass().getResourceAsStream("/com/pos/view/image/logo.jpeg");
        if (stream != null) {
            logoImage.setImage(new Image(stream));
        }
    }

    /** Mengubah huruf pertama username menjadi kapital agar tampil lebih rapi. */
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /** Memaksa window dashboard memenuhi layar setelah scene diganti. */
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
