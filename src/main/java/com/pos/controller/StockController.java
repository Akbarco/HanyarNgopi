package com.pos.controller;

import com.pos.dao.MenuDAO;
import com.pos.model.Menu;
import com.pos.model.Stock;
import com.pos.service.StockService;
import com.pos.util.AlertUtil;
import com.pos.util.ToastUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class StockController implements Initializable {

    private static final ObservableList<String> SATUAN_OPTIONS = FXCollections.observableArrayList(
            "kg", "g", "liter", "ml", "pcs", "box", "sachet", "botol"
    );

    @FXML private TableView<Stock> tableStock;
    @FXML private TableColumn<Stock, String> colNama;
    @FXML private TableColumn<Stock, Integer> colJumlah;
    @FXML private TableColumn<Stock, String> colSatuan;
    @FXML private TableColumn<Stock, Integer> colMinimum;
    @FXML private TableColumn<Stock, String> colStatus;
    @FXML private TableColumn<Stock, Void> colAksi;
    @FXML private Label lblTotalProduk;
    @FXML private Label lblStokAman;
    @FXML private Label lblStokMenipis;
    @FXML private Label lblStokHabis;
    @FXML private TextField txtSearchStock;
    @FXML private ComboBox<String> cmbFilterStatus;
    @FXML private ComboBox<String> cmbFilterSatuan;

    private final StockService stockService = new StockService();
    private final MenuDAO menuDAO = new MenuDAO();
    private final Locale localeId = new Locale("id", "ID");
    private final ObservableList<Stock> stockList = FXCollections.observableArrayList();
    private final FilteredList<Stock> filteredStockList = new FilteredList<>(stockList, stock -> true);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        setupSearch();
        loadData();
    }

    private void setupSearch() {
        if (tableStock != null) {
            tableStock.setItems(filteredStockList);
        }
        if (txtSearchStock != null) {
            txtSearchStock.textProperty().addListener((obs, oldValue, newValue) -> applySearchFilter());
        }
        if (cmbFilterStatus != null) {
            cmbFilterStatus.setItems(FXCollections.observableArrayList(
                    "Semua status", "Stok Aman", "Stok Menipis", "Stok Habis"
            ));
            cmbFilterStatus.setValue("Semua status");
            cmbFilterStatus.valueProperty().addListener((obs, oldValue, newValue) -> applySearchFilter());
        }
        if (cmbFilterSatuan != null) {
            cmbFilterSatuan.setItems(FXCollections.observableArrayList("Semua satuan"));
            cmbFilterSatuan.getItems().addAll(SATUAN_OPTIONS);
            cmbFilterSatuan.setValue("Semua satuan");
            cmbFilterSatuan.valueProperty().addListener((obs, oldValue, newValue) -> applySearchFilter());
        }
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
                        loadData();
                        ToastUtil.showSuccess(btnHapus, "Stok berhasil dihapus.");
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
        refreshSatuanFilterOptions();
        applySearchFilter();
        updateSummaryCards();
    }

    private void applySearchFilter() {
        String keyword = txtSearchStock == null ? "" : txtSearchStock.getText();
        String query = keyword == null ? "" : keyword.trim().toLowerCase();
        String status = cmbFilterStatus == null ? "Semua status" : cmbFilterStatus.getValue();
        String satuan = cmbFilterSatuan == null ? "Semua satuan" : cmbFilterSatuan.getValue();

        filteredStockList.setPredicate(stock -> {
            boolean matchesSearch = query.isBlank()
                    || contains(stock.getNamaMenu(), query)
                    || contains(stock.getSatuan(), query)
                    || contains(stock.getStatus(), query)
                    || String.valueOf(stock.getJumlahStok()).contains(query)
                    || String.valueOf(stock.getStokMinimum()).contains(query);
            boolean matchesStatus = status == null
                    || "Semua status".equals(status)
                    || matchesStockStatus(stock, status);
            boolean matchesSatuan = satuan == null
                    || "Semua satuan".equals(satuan)
                    || satuan.equalsIgnoreCase(stock.getSatuan());

            return matchesSearch && matchesStatus && matchesSatuan;
        });
    }

    private void refreshSatuanFilterOptions() {
        if (cmbFilterSatuan == null) {
            return;
        }
        String current = cmbFilterSatuan.getValue() == null ? "Semua satuan" : cmbFilterSatuan.getValue();
        ObservableList<String> options = FXCollections.observableArrayList("Semua satuan");
        for (String option : SATUAN_OPTIONS) {
            if (!options.contains(option)) {
                options.add(option);
            }
        }
        for (Stock stock : stockList) {
            String satuan = stock.getSatuan();
            if (satuan != null && !satuan.isBlank() && !options.contains(satuan)) {
                options.add(satuan);
            }
        }
        cmbFilterSatuan.setItems(options);
        cmbFilterSatuan.setValue(options.contains(current) ? current : "Semua satuan");
    }

    private boolean matchesStockStatus(Stock stock, String status) {
        return switch (status) {
            case "Stok Habis" -> stock.getJumlahStok() <= 0;
            case "Stok Menipis" -> stock.getJumlahStok() > 0
                    && stock.getJumlahStok() <= stock.getStokMinimum();
            case "Stok Aman" -> stock.getJumlahStok() > stock.getStokMinimum();
            default -> true;
        };
    }

    private void updateSummaryCards() {
        int total = stockList.size();
        long habis = stockList.stream()
                .filter(stock -> stock.getJumlahStok() <= 0)
                .count();
        long menipis = stockList.stream()
                .filter(stock -> stock.getJumlahStok() > 0 && stock.getJumlahStok() <= stock.getStokMinimum())
                .count();
        long aman = stockList.stream()
                .filter(stock -> stock.getJumlahStok() > stock.getStokMinimum())
                .count();

        setLabel(lblTotalProduk, String.valueOf(total));
        setLabel(lblStokAman, String.valueOf(aman));
        setLabel(lblStokMenipis, String.valueOf(menipis));
        setLabel(lblStokHabis, String.valueOf(habis));
    }

    private void setLabel(Label label, String value) {
        if (label != null) {
            label.setText(value);
        }
    }

    @FXML
    public void handleTambahStock() {
        showTambahDialog();
    }

    @FXML
    public void handleResetFilter() {
        if (txtSearchStock != null) txtSearchStock.clear();
        if (cmbFilterStatus != null) cmbFilterStatus.setValue("Semua status");
        if (cmbFilterSatuan != null) cmbFilterSatuan.setValue("Semua satuan");
        applySearchFilter();
    }

    private void showTambahDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (tableStock != null && tableStock.getScene() != null) {
            dialog.initOwner(tableStock.getScene().getWindow());
        }
        dialog.setTitle("Tambah Stok Baru");
        dialog.setResizable(false);

        VBox root = new VBox(16);
        root.setPadding(new Insets(32));
        root.getStyleClass().add("dialog-root");

        Label lblTitle = new Label("Tambah Stok Baru");
        lblTitle.getStyleClass().add("dialog-title");

        // Menu
        Label lblMenu = fieldLabel("Menu");
        ComboBox<Menu> cmbMenu = new ComboBox<>();
        cmbMenu.setItems(FXCollections.observableArrayList(menuDAO.findByActive(true)));
        cmbMenu.setPromptText("Pilih menu");
        cmbMenu.setMaxWidth(Double.MAX_VALUE);
        cmbMenu.getStyleClass().add("combo-input");
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
        enableMenuKeyboardJump(cmbMenu);

        // Jumlah & Satuan dalam 1 row
        Label lblJumlah = fieldLabel("Jumlah");
        Label lblSatuan = fieldLabel("Satuan");

        TextField txtJumlah = new TextField();
        txtJumlah.setPromptText("Jumlah stock");
        txtJumlah.getStyleClass().add("text-input");

        ComboBox<String> cmbSatuan = new ComboBox<>();
        cmbSatuan.setItems(SATUAN_OPTIONS);
        cmbSatuan.setPromptText("Pilih satuan");
        cmbSatuan.setMaxWidth(Double.MAX_VALUE);
        cmbSatuan.getStyleClass().add("combo-input");

        HBox lblJumlahSatuan = new HBox(12, wrapLabel(lblJumlah), wrapLabel(lblSatuan));
        HBox inputJumlahSatuan = new HBox(12, txtJumlah, cmbSatuan);
        HBox.setHgrow(txtJumlah, Priority.ALWAYS);
        HBox.setHgrow(cmbSatuan, Priority.ALWAYS);

        // Stok Minimum
        Label lblMin = fieldLabel("Stok Minimum");
        TextField txtMin = new TextField("0");
        txtMin.getStyleClass().add("text-input");

        // Buttons
        Button btnBatal = new Button("Batal");
        btnBatal.getStyleClass().add("secondary-button");
        btnBatal.setPrefWidth(128);
        btnBatal.setOnAction(e -> dialog.close());

        Button btnTambah = new Button("Tambah");
        btnTambah.getStyleClass().add("primary-button");
        btnTambah.setPrefWidth(150);
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
            dialog.close();
            loadData();
            ToastUtil.showSuccess(dialog, "Stok berhasil ditambahkan.");
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

        dialog.setScene(createDialogScene(root, 560, 460));
        dialog.showAndWait();
    }

    private void enableMenuKeyboardJump(ComboBox<Menu> comboBox) {
        ObservableList<Menu> allItems = FXCollections.observableArrayList(comboBox.getItems());
        comboBox.setOnHidden(event -> comboBox.setItems(allItems));
        comboBox.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            String key = event.getCharacter();
            if (key == null || key.isBlank() || allItems.isEmpty()) {
                return;
            }

            String query = key.trim().toLowerCase(localeId);
            ObservableList<Menu> matches = FXCollections.observableArrayList();
            for (Menu menu : allItems) {
                String name = menu.getNamaMenu();
                if (name != null && name.toLowerCase(localeId).startsWith(query)) {
                    matches.add(menu);
                }
            }
            if (matches.isEmpty()) {
                return;
            }
            comboBox.setItems(matches);
            comboBox.getSelectionModel().selectFirst();
            comboBox.show();
            event.consume();
        });
    }

    private void showEditDialog(Stock stock) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (tableStock != null && tableStock.getScene() != null) {
            dialog.initOwner(tableStock.getScene().getWindow());
        }
        dialog.setTitle("Edit Stok");
        dialog.setResizable(false);

        VBox root = new VBox(16);
        root.setPadding(new Insets(32));
        root.getStyleClass().add("dialog-root");

        Label lblTitle = new Label("Edit Stok — " + stock.getNamaMenu());
        lblTitle.getStyleClass().add("dialog-title");

        // Nama barang (readonly)
        Label lblNama = fieldLabel("Nama Barang");
        TextField txtNama = new TextField(stock.getNamaMenu());
        txtNama.setEditable(false);
        txtNama.getStyleClass().add("text-input");

        // Jumlah & Satuan
        Label lblJumlah = fieldLabel("Jumlah");
        Label lblSatuan = fieldLabel("Satuan");

        TextField txtJumlah = new TextField(String.valueOf(stock.getJumlahStok()));
        txtJumlah.getStyleClass().add("text-input");

        ComboBox<String> cmbSatuan = new ComboBox<>();
        cmbSatuan.setItems(SATUAN_OPTIONS);
        if (stock.getSatuan() != null && !SATUAN_OPTIONS.contains(stock.getSatuan())) {
            cmbSatuan.getItems().add(stock.getSatuan());
        }
        cmbSatuan.setValue(stock.getSatuan());
        cmbSatuan.setMaxWidth(Double.MAX_VALUE);
        cmbSatuan.getStyleClass().add("combo-input");

        HBox lblRow = new HBox(12, wrapLabel(lblJumlah), wrapLabel(lblSatuan));
        HBox inputRow = new HBox(12, txtJumlah, cmbSatuan);
        HBox.setHgrow(txtJumlah, Priority.ALWAYS);
        HBox.setHgrow(cmbSatuan, Priority.ALWAYS);

        // Stok Minimum
        Label lblMin = fieldLabel("Stok Minimum");
        TextField txtMin = new TextField(String.valueOf(stock.getStokMinimum()));
        txtMin.getStyleClass().add("text-input");

        // Buttons
        Button btnBatal = new Button("Batal");
        btnBatal.getStyleClass().add("secondary-button");
        btnBatal.setPrefWidth(128);
        btnBatal.setOnAction(e -> dialog.close());

        Button btnSimpan = new Button("Simpan");
        btnSimpan.getStyleClass().add("primary-button");
        btnSimpan.setPrefWidth(150);
        btnSimpan.setOnAction(e -> {
            String jumlahStr = txtJumlah.getText().trim();
            String satuan = cmbSatuan.getValue();
            String minStr = txtMin.getText().trim();

            if (jumlahStr.isEmpty() || satuan == null || satuan.isBlank()) {
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
            dialog.close();
            loadData();
            ToastUtil.showSuccess(dialog, "Stok berhasil diupdate.");
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

        dialog.setScene(createDialogScene(root, 560, 500));
        dialog.showAndWait();
    }

    // ===== HELPERS =====

    private Label fieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("form-label");
        return lbl;
    }

    private Scene createDialogScene(VBox root, double width, double height) {
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(getClass().getResource("/com/pos/view/css/menu.css").toExternalForm());
        return scene;
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

    private boolean contains(String source, String query) {
        return source != null && source.toLowerCase().contains(query);
    }
}
