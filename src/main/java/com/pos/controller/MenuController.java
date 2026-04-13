package com.pos.controller;

import com.pos.dao.MenuDAO;
import com.pos.model.Menu;
import com.pos.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MenuController implements Initializable {

    @FXML private TableView<Menu> tableMenu;
    @FXML private TableColumn<Menu, String> colNama;
    @FXML private TableColumn<Menu, String> colKategori;
    @FXML private TableColumn<Menu, Double> colHarga;
    @FXML private TableColumn<Menu, Void> colAksi;

    private final MenuDAO menuDAO = new MenuDAO();
    private final ObservableList<Menu> menuList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        loadData();
    }

    private void setupColumns() {
        colNama.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getNamaMenu()));

        colKategori.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        capitalize(data.getValue().getKategori())));

        colHarga.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        data.getValue().getHarga()));

        // Format harga
        colHarga.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText("Rp " + String.format("%,.0f", item));
                    setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold;");
                }
            }
        });

        // Badge kategori
        colKategori.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(item);
                String bg, fg;
                switch (item.toLowerCase()) {
                    case "makanan" -> { bg = "#EEF2FF"; fg = "#4F46E5"; }
                    case "minuman" -> { bg = "#DBEAFE"; fg = "#2563EB"; }
                    default ->        { bg = "#F0FDF4"; fg = "#16A34A"; }
                }
                badge.setStyle(
                        "-fx-background-color: " + bg + ";" +
                                "-fx-text-fill: " + fg + ";" +
                                "-fx-background-radius: 20;" +
                                "-fx-padding: 4 14 4 14;" +
                                "-fx-font-size: 11;" +
                                "-fx-font-weight: bold;"
                );
                setGraphic(badge);
            }
        });

        // Kolom aksi
        colAksi.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("✏  Edit");
            private final Button btnHapus = new Button("🗑  Hapus");

            {
                btnEdit.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-text-fill: #4F46E5;" +
                                "-fx-cursor: hand;" +
                                "-fx-font-size: 13;" +
                                "-fx-font-weight: bold;");

                btnHapus.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-text-fill: #EF4444;" +
                                "-fx-cursor: hand;" +
                                "-fx-font-size: 13;" +
                                "-fx-font-weight: bold;");

                btnEdit.setOnAction(e -> {
                    Menu menu = getTableView().getItems().get(getIndex());
                    showEditDialog(menu);
                });

                btnHapus.setOnAction(e -> {
                    Menu menu = getTableView().getItems().get(getIndex());
                    if (AlertUtil.showConfirm("Hapus Menu",
                            "Yakin hapus \"" + menu.getNamaMenu() + "\"?")) {
                        menuDAO.delete(menu.getIdMenu());
                        AlertUtil.showInfo("Sukses", "Menu berhasil dihapus!");
                        loadData();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(4, btnEdit, btnHapus);
                    box.setAlignment(Pos.CENTER_RIGHT);
                    setGraphic(box);
                    setAlignment(Pos.CENTER_RIGHT);
                }
            }
        });
    }

    private void loadData() {
        menuList.setAll(menuDAO.findAll());
        tableMenu.setItems(menuList);
    }

    @FXML
    public void handleTambahMenu() {
        showTambahDialog();
    }

    private void showTambahDialog() {
        Stage dialog = createDialog("Tambah Menu Baru");

        TextField txtNama = createTextField("Nama menu");
        ComboBox<String> cmbKategori = createKategoriCombo();
        TextField txtHarga = createTextField("Harga (Rp)");

        Button btnBatal = createBtnBatal(dialog);
        Button btnSimpan = new Button("Tambah");
        btnSimpan.setStyle(btnPrimaryStyle());
        btnSimpan.setOnAction(e -> {
            if (validateAndSave(txtNama, cmbKategori, txtHarga, null)) {
                dialog.close();
                loadData();
            }
        });

        VBox root = buildDialogLayout(
                "Tambah Menu Baru", txtNama, cmbKategori, txtHarga, btnBatal, btnSimpan);
        dialog.setScene(new Scene(root, 460, 380));
        dialog.showAndWait();
    }

    private void showEditDialog(Menu menu) {
        Stage dialog = createDialog("Edit Menu");

        TextField txtNama = createTextField("Nama menu");
        txtNama.setText(menu.getNamaMenu());

        ComboBox<String> cmbKategori = createKategoriCombo();
        cmbKategori.setValue(menu.getKategori());

        TextField txtHarga = createTextField("Harga (Rp)");
        txtHarga.setText(String.valueOf((int) menu.getHarga()));

        Button btnBatal = createBtnBatal(dialog);
        Button btnSimpan = new Button("Simpan");
        btnSimpan.setStyle(btnPrimaryStyle());
        btnSimpan.setOnAction(e -> {
            if (validateAndSave(txtNama, cmbKategori, txtHarga, menu)) {
                dialog.close();
                loadData();
            }
        });

        VBox root = buildDialogLayout(
                "Edit Menu", txtNama, cmbKategori, txtHarga, btnBatal, btnSimpan);
        dialog.setScene(new Scene(root, 460, 380));
        dialog.showAndWait();
    }

    private boolean validateAndSave(TextField txtNama, ComboBox<String> cmbKategori,
                                    TextField txtHarga, Menu existing) {
        String nama = txtNama.getText().trim();
        String kategori = cmbKategori.getValue();
        String hargaStr = txtHarga.getText().trim();

        if (nama.isEmpty() || kategori == null || hargaStr.isEmpty()) {
            AlertUtil.showError("Validasi", "Semua field harus diisi!");
            return false;
        }

        double harga;
        try {
            harga = Double.parseDouble(hargaStr);
            if (harga <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            AlertUtil.showError("Validasi", "Harga harus berupa angka positif!");
            return false;
        }

        if (existing == null) {
            menuDAO.insert(new Menu(0, nama, harga, kategori));
            AlertUtil.showInfo("Sukses", "Menu berhasil ditambahkan!");
        } else {
            existing.setNamaMenu(nama);
            existing.setHarga(harga);
            existing.setKategori(kategori);
            menuDAO.update(existing);
            AlertUtil.showInfo("Sukses", "Menu berhasil diupdate!");
        }
        return true;
    }

    // ===== HELPERS =====

    private Stage createDialog(String title) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(title);
        dialog.setResizable(false);
        return dialog;
    }

    private TextField createTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(inputStyle());
        return tf;
    }

    private ComboBox<String> createKategoriCombo() {
        ComboBox<String> cmb = new ComboBox<>();
        cmb.setItems(FXCollections.observableArrayList("makanan", "minuman", "snack"));
        cmb.setPromptText("Pilih kategori");
        cmb.setMaxWidth(Double.MAX_VALUE);
        cmb.setStyle(inputStyle());
        return cmb;
    }

    private Button createBtnBatal(Stage dialog) {
        Button btn = new Button("Batal");
        btn.setStyle(
                "-fx-background-color: #F3F4F6; -fx-text-fill: #374151;" +
                        "-fx-background-radius: 8; -fx-cursor: hand;" +
                        "-fx-pref-height: 40; -fx-pref-width: 100;");
        btn.setOnAction(e -> dialog.close());
        return btn;
    }

    private VBox buildDialogLayout(String title, TextField txtNama,
                                   ComboBox<String> cmbKategori, TextField txtHarga,
                                   Button btnBatal, Button btnSimpan) {
        VBox root = new VBox(12);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: white;");

        Label lblTitle = new Label(title);
        lblTitle.setStyle(
                "-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        HBox btnBox = new HBox(10, btnBatal, btnSimpan);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(
                lblTitle,
                fieldLabel("Nama Menu"), txtNama,
                fieldLabel("Kategori"), cmbKategori,
                fieldLabel("Harga (Rp)"), txtHarga,
                btnBox
        );
        return root;
    }

    private Label fieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle(
                "-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        return lbl;
    }

    private String inputStyle() {
        return "-fx-background-color: #F3F4F6; -fx-background-radius: 8;" +
                "-fx-border-color: transparent; -fx-pref-height: 40;" +
                "-fx-font-size: 13; -fx-padding: 0 12 0 12;";
    }

    private String btnPrimaryStyle() {
        return "-fx-background-color: #1A1A2E; -fx-text-fill: white;" +
                "-fx-background-radius: 8; -fx-cursor: hand;" +
                "-fx-pref-height: 40; -fx-pref-width: 100;";
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}