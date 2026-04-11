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
        colNama.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNamaMenu()));
        colKategori.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        capitalize(data.getValue().getKategori())));
        colHarga.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getHarga()));

        // Format harga kolom
        colHarga.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Rp " + String.format("%,.0f", item));
                    setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold;");
                }
            }
        });

        // Format kategori badge
        colKategori.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-background-color: #EEF2FF; -fx-text-fill: #4F46E5; " +
                            "-fx-background-radius: 20; -fx-padding: 2 10 2 10; -fx-font-weight: bold;");
                }
            }
        });

        // Kolom Aksi
        colAksi.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnHapus = new Button("Hapus");

            {
                btnEdit.setStyle("-fx-background-color: transparent; -fx-text-fill: #4F46E5; " +
                        "-fx-cursor: hand; -fx-font-weight: bold;");
                btnHapus.setStyle("-fx-background-color: transparent; -fx-text-fill: #EF4444; " +
                        "-fx-cursor: hand; -fx-font-weight: bold;");

                btnEdit.setOnAction(e -> {
                    Menu menu = getTableView().getItems().get(getIndex());
                    showEditDialog(menu);
                });

                btnHapus.setOnAction(e -> {
                    Menu menu = getTableView().getItems().get(getIndex());
                    if (AlertUtil.showConfirm("Hapus Menu",
                            "Yakin hapus menu \"" + menu.getNamaMenu() + "\"?")) {
                        menuDAO.delete(menu.getIdMenu());
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
                    HBox box = new HBox(8, btnEdit, btnHapus);
                    box.setAlignment(Pos.CENTER_RIGHT);
                    setGraphic(box);
                }
            }
        });

        loadData();
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
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Tambah Menu Baru");

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: white;");

        Label title = new Label("Tambah Menu Baru");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        TextField txtNama = new TextField();
        txtNama.setPromptText("Nama menu");
        txtNama.setStyle(inputStyle());

        ComboBox<String> cmbKategori = new ComboBox<>();
        cmbKategori.setItems(FXCollections.observableArrayList("makanan", "minuman", "stok"));
        cmbKategori.setPromptText("Pilih kategori");
        cmbKategori.setMaxWidth(Double.MAX_VALUE);
        cmbKategori.setStyle(inputStyle());

        TextField txtHarga = new TextField();
        txtHarga.setPromptText("Harga (Rp)");
        txtHarga.setStyle(inputStyle());

        Button btnBatal = new Button("Batal");
        btnBatal.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-pref-height: 40; -fx-pref-width: 100;");
        btnBatal.setOnAction(e -> dialog.close());

        Button btnTambah = new Button("Tambah");
        btnTambah.setStyle("-fx-background-color: #1A1A2E; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-pref-height: 40; -fx-pref-width: 100;");
        btnTambah.setOnAction(e -> {
            if (validateAndSave(txtNama, cmbKategori, txtHarga, null)) {
                dialog.close();
                loadData();
            }
        });

        HBox btnBox = new HBox(10, btnBatal, btnTambah);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(
                title,
                new Label("Nama Menu"), txtNama,
                new Label("Kategori"), cmbKategori,
                new Label("Harga (Rp)"), txtHarga,
                btnBox
        );

        styleLabels(root);

        dialog.setScene(new Scene(root, 480, 380));
        dialog.showAndWait();
    }

    private void showEditDialog(Menu menu) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Menu");

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: white;");

        Label title = new Label("Edit Menu");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        TextField txtNama = new TextField(menu.getNamaMenu());
        txtNama.setStyle(inputStyle());

        ComboBox<String> cmbKategori = new ComboBox<>();
        cmbKategori.setItems(FXCollections.observableArrayList("makanan", "minuman", "stok"));
        cmbKategori.setValue(menu.getKategori());
        cmbKategori.setMaxWidth(Double.MAX_VALUE);
        cmbKategori.setStyle(inputStyle());

        TextField txtHarga = new TextField(String.valueOf((int) menu.getHarga()));
        txtHarga.setStyle(inputStyle());

        Button btnBatal = new Button("Batal");
        btnBatal.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-pref-height: 40; -fx-pref-width: 100;");
        btnBatal.setOnAction(e -> dialog.close());

        Button btnSimpan = new Button("Simpan");
        btnSimpan.setStyle("-fx-background-color: #1A1A2E; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-pref-height: 40; -fx-pref-width: 100;");
        btnSimpan.setOnAction(e -> {
            if (validateAndSave(txtNama, cmbKategori, txtHarga, menu)) {
                dialog.close();
                loadData();
            }
        });

        HBox btnBox = new HBox(10, btnBatal, btnSimpan);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(
                title,
                new Label("Nama Menu"), txtNama,
                new Label("Kategori"), cmbKategori,
                new Label("Harga (Rp)"), txtHarga,
                btnBox
        );

        styleLabels(root);

        dialog.setScene(new Scene(root, 480, 380));
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

    private String inputStyle() {
        return "-fx-background-color: #F3F4F6; -fx-background-radius: 8; " +
                "-fx-border-color: transparent; -fx-pref-height: 40; -fx-font-size: 13; -fx-padding: 0 12 0 12;";
    }

    private void styleLabels(VBox root) {
        root.getChildren().stream()
                .filter(n -> n instanceof Label && !((Label) n).getStyle().contains("font-size: 18"))
                .forEach(n -> ((Label) n).setStyle(
                        "-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;"));
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}