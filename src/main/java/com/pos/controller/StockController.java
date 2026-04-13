package com.pos.controller;

import com.pos.dao.MenuDAO;
import com.pos.model.Menu;
import com.pos.model.Stock;
import com.pos.service.StockService;
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

public class StockController implements Initializable {

    @FXML private TableView<Stock> tableStock;
    @FXML private TableColumn<Stock, String> colNama;
    @FXML private TableColumn<Stock, Integer> colJumlah;
    @FXML private TableColumn<Stock, String> colSatuan;
    @FXML private TableColumn<Stock, Integer> colMinimum;
    @FXML private TableColumn<Stock, String> colStatus;
    @FXML private TableColumn<Stock, Void> colAksi;

    private final StockService stockService = new StockService();
    private final MenuDAO menuDAO = new MenuDAO();
    private final ObservableList<Stock> stockList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        loadData();
    }

    private void setupColumns() {
        colNama.setPrefWidth(240);
        colNama.setMinWidth(200);

        colSatuan.setPrefWidth(120);
        colSatuan.setMinWidth(100);

        colJumlah.setPrefWidth(120);
        colJumlah.setMinWidth(100);

        colMinimum.setPrefWidth(140);
        colMinimum.setMinWidth(120);

        colStatus.setPrefWidth(170);
        colStatus.setMinWidth(150);

        colAksi.setPrefWidth(190);
        colAksi.setMinWidth(190);
        colAksi.setMaxWidth(220);
        colAksi.setStyle("-fx-alignment: CENTER_RIGHT;");

        colNama.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getNamaMenu()));

        colJumlah.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        data.getValue().getJumlahStok()));

        colSatuan.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getSatuan()));

        colSatuan.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(item);
                badge.setStyle(
                        "-fx-background-color: #F3F4F6;" +
                                "-fx-text-fill: #374151;" +
                                "-fx-background-radius: 20;" +
                                "-fx-padding: 3 12 3 12;" +
                                "-fx-font-size: 11;" +
                                "-fx-font-weight: bold;"
                );
                setGraphic(badge);
            }
        });

        colMinimum.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        data.getValue().getStokMinimum()));

        colStatus.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getStatus()));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label();
                if (item.equals("Stok Aman")) {
                    badge.setText("✓ Stok Aman");
                    badge.setStyle(
                            "-fx-background-color: #DCFCE7;" +
                                    "-fx-text-fill: #16A34A;" +
                                    "-fx-background-radius: 20;" +
                                    "-fx-padding: 4 14 4 14;" +
                                    "-fx-font-size: 11;" +
                                    "-fx-font-weight: bold;"
                    );
                } else {
                    badge.setText("⚠ Stok Menipis");
                    badge.setStyle(
                            "-fx-background-color: #FEF3C7;" +
                                    "-fx-text-fill: #D97706;" +
                                    "-fx-background-radius: 20;" +
                                    "-fx-padding: 4 14 4 14;" +
                                    "-fx-font-size: 11;" +
                                    "-fx-font-weight: bold;"
                    );
                }
                setGraphic(badge);
            }
        });

        colAksi.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnHapus = new Button("Hapus");

            {
                btnEdit.getStyleClass().addAll("table-action-button", "table-action-button-edit");
                btnHapus.getStyleClass().addAll("table-action-button", "table-action-button-delete");

                btnEdit.setOnAction(e -> {
                    Stock stock = getTableView().getItems().get(getIndex());
                    showEditDialog(stock);
                });

                btnHapus.setOnAction(e -> {
                    Stock stock = getTableView().getItems().get(getIndex());
                    if (AlertUtil.showConfirm("Hapus Stock",
                            "Yakin hapus stock \"" + stock.getNamaMenu() + "\"?")) {
                        stockService.deleteStock(stock.getIdStok());
                        AlertUtil.showInfo("Sukses", "Stock berhasil dihapus!");
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
                    box.setMaxWidth(Double.MAX_VALUE);
                    setGraphic(box);
                    setText(null);
                    setAlignment(Pos.CENTER_RIGHT);
                }
            }
        });
    }

    private void loadData() {
        stockList.setAll(stockService.getAllStock());
        tableStock.setItems(stockList);
    }

    @FXML
    public void handleTambahStock() {
        showTambahDialog();
    }

    private void showTambahDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Tambah Stok Baru");
        dialog.setResizable(false);

        VBox root = new VBox(12);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: white;");

        Label lblTitle = new Label("Tambah Stok Baru");
        lblTitle.setStyle(
                "-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #111827;");

        // Menu
        Label lblMenu = fieldLabel("Menu");
        ComboBox<Menu> cmbMenu = new ComboBox<>();
        cmbMenu.setItems(FXCollections.observableArrayList(menuDAO.findAll()));
        cmbMenu.setPromptText("Pilih menu");
        cmbMenu.setMaxWidth(Double.MAX_VALUE);
        cmbMenu.setStyle(inputStyle());
        cmbMenu.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Menu item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNamaMenu());
            }
        });
        cmbMenu.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Menu item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNamaMenu());
            }
        });

        // Jumlah & Satuan dalam 1 row
        Label lblJumlah = fieldLabel("Jumlah");
        Label lblSatuan = fieldLabel("Satuan");

        TextField txtJumlah = new TextField();
        txtJumlah.setPromptText("Jumlah stock");
        txtJumlah.setStyle(inputStyle());

        ComboBox<String> cmbSatuan = new ComboBox<>();
        cmbSatuan.setItems(FXCollections.observableArrayList(
                "kg", "g", "liter", "ml", "pcs", "box", "sachet", "botol"));
        cmbSatuan.setPromptText("Pilih satuan");
        cmbSatuan.setMaxWidth(Double.MAX_VALUE);
        cmbSatuan.setStyle(inputStyle());

        HBox lblJumlahSatuan = new HBox(12, wrapLabel(lblJumlah), wrapLabel(lblSatuan));
        HBox inputJumlahSatuan = new HBox(12, txtJumlah, cmbSatuan);
        HBox.setHgrow(txtJumlah, Priority.ALWAYS);
        HBox.setHgrow(cmbSatuan, Priority.ALWAYS);

        // Stok Minimum
        Label lblMin = fieldLabel("Stok Minimum");
        TextField txtMin = new TextField("0");
        txtMin.setStyle(inputStyle());

        // Buttons
        Button btnBatal = new Button("Batal");
        btnBatal.setStyle(
                "-fx-background-color: #F3F4F6; -fx-text-fill: #374151;" +
                        "-fx-background-radius: 8; -fx-cursor: hand;" +
                        "-fx-pref-height: 40; -fx-pref-width: 100;");
        btnBatal.setOnAction(e -> dialog.close());

        Button btnTambah = new Button("Tambah");
        btnTambah.setStyle(
                "-fx-background-color: #111827; -fx-text-fill: white;" +
                        "-fx-background-radius: 8; -fx-cursor: hand;" +
                        "-fx-pref-height: 40; -fx-pref-width: 100;");
        btnTambah.setOnAction(e -> {
            Menu menu = cmbMenu.getValue();
            String jumlahStr = txtJumlah.getText().trim();
            String satuan = cmbSatuan.getValue();
            String minStr = txtMin.getText().trim();

            if (menu == null || jumlahStr.isEmpty() || satuan == null) {
                AlertUtil.showError("Validasi", "Semua field harus diisi!");
                return;
            }

            int jumlah, min;
            try {
                jumlah = Integer.parseInt(jumlahStr);
                min = minStr.isEmpty() ? 0 : Integer.parseInt(minStr);
                if (jumlah <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                AlertUtil.showError("Validasi", "Jumlah harus angka positif!");
                return;
            }

            Stock stock = new Stock(0, menu.getIdMenu(), jumlah, satuan, min);
            stockService.tambahStock(stock);
            AlertUtil.showInfo("Sukses", "Stock berhasil ditambahkan!");
            dialog.close();
            loadData();
        });

        HBox btnBox = new HBox(10, btnBatal, btnTambah);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(
                lblTitle,
                lblMenu, cmbMenu,
                lblJumlahSatuan, inputJumlahSatuan,
                lblMin, txtMin,
                btnBox
        );

        dialog.setScene(new Scene(root, 460, 400));
        dialog.showAndWait();
    }

    private void showEditDialog(Stock stock) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Stok");
        dialog.setResizable(false);

        VBox root = new VBox(12);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: white;");

        Label lblTitle = new Label("Edit Stok — " + stock.getNamaMenu());
        lblTitle.setStyle(
                "-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #111827;");

        // Nama barang (readonly)
        Label lblNama = fieldLabel("Nama Barang");
        TextField txtNama = new TextField(stock.getNamaMenu());
        txtNama.setEditable(false);
        txtNama.setStyle(inputStyle() +
                "-fx-background-color: #F9FAFB; -fx-text-fill: #6B7280;");

        // Jumlah & Satuan
        Label lblJumlah = fieldLabel("Jumlah");
        Label lblSatuan = fieldLabel("Satuan");

        TextField txtJumlah = new TextField(String.valueOf(stock.getJumlahStok()));
        txtJumlah.setStyle(inputStyle());

        TextField txtSatuan = new TextField(stock.getSatuan());
        txtSatuan.setStyle(inputStyle());

        HBox lblRow = new HBox(12, wrapLabel(lblJumlah), wrapLabel(lblSatuan));
        HBox inputRow = new HBox(12, txtJumlah, txtSatuan);
        HBox.setHgrow(txtJumlah, Priority.ALWAYS);
        HBox.setHgrow(txtSatuan, Priority.ALWAYS);

        // Stok Minimum
        Label lblMin = fieldLabel("Stok Minimum");
        TextField txtMin = new TextField(String.valueOf(stock.getStokMinimum()));
        txtMin.setStyle(inputStyle());

        // Buttons
        Button btnBatal = new Button("Batal");
        btnBatal.setStyle(
                "-fx-background-color: #F3F4F6; -fx-text-fill: #374151;" +
                        "-fx-background-radius: 8; -fx-cursor: hand;" +
                        "-fx-pref-height: 40; -fx-pref-width: 100;");
        btnBatal.setOnAction(e -> dialog.close());

        Button btnSimpan = new Button("Simpan");
        btnSimpan.setStyle(
                "-fx-background-color: #111827; -fx-text-fill: white;" +
                        "-fx-background-radius: 8; -fx-cursor: hand;" +
                        "-fx-pref-height: 40; -fx-pref-width: 100;");
        btnSimpan.setOnAction(e -> {
            String jumlahStr = txtJumlah.getText().trim();
            String satuan = txtSatuan.getText().trim();
            String minStr = txtMin.getText().trim();

            if (jumlahStr.isEmpty() || satuan.isEmpty()) {
                AlertUtil.showError("Validasi", "Jumlah dan satuan harus diisi!");
                return;
            }

            int jumlah, min;
            try {
                jumlah = Integer.parseInt(jumlahStr);
                min = minStr.isEmpty() ? 0 : Integer.parseInt(minStr);
                if (jumlah < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                AlertUtil.showError("Validasi", "Jumlah harus angka valid!");
                return;
            }

            stock.setJumlahStok(jumlah);
            stock.setSatuan(satuan);
            stock.setStokMinimum(min);
            stockService.updateStock(stock);
            AlertUtil.showInfo("Sukses", "Stock berhasil diupdate!");
            dialog.close();
            loadData();
        });

        HBox btnBox = new HBox(10, btnBatal, btnSimpan);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(
                lblTitle,
                lblNama, txtNama,
                lblRow, inputRow,
                lblMin, txtMin,
                btnBox
        );

        dialog.setScene(new Scene(root, 460, 420));
        dialog.showAndWait();
    }

    // ===== HELPERS =====

    private Label fieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle(
                "-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #374151;");
        return lbl;
    }

    private VBox wrapLabel(Label lbl) {
        VBox box = new VBox(lbl);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private String inputStyle() {
        return "-fx-background-color: #F3F4F6; -fx-background-radius: 8;" +
                "-fx-border-color: transparent; -fx-pref-height: 40;" +
                "-fx-font-size: 13; -fx-padding: 0 12 0 12;";
    }
}
