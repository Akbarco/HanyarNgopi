package com.pos.controller;

import com.pos.dao.MenuDAO;
import com.pos.model.Menu;
import com.pos.util.AlertUtil;
import com.pos.util.CurrencyFormatUtil;
import com.pos.util.ToastUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.ResourceBundle;

public class MenuController implements Initializable {

    @FXML private TableView<Menu> tableMenu;
    @FXML private TableColumn<Menu, String> colNama;
    @FXML private TableColumn<Menu, String> colKategori;
    @FXML private TableColumn<Menu, Double> colHarga;
    @FXML private TableColumn<Menu, Void> colAksi;
    @FXML private Label lblTableTitle;
    @FXML private Label lblPlaceholder;
    @FXML private Button btnTabAktif;
    @FXML private Button btnTabArsip;
    @FXML private TextField txtSearchMenu;
    @FXML private ComboBox<String> cmbFilterKategori;
    @FXML private ComboBox<String> cmbSortHarga;

    private final MenuDAO menuDAO = new MenuDAO();
    private final ObservableList<Menu> menuList = FXCollections.observableArrayList();
    private final FilteredList<Menu> filteredMenuList = new FilteredList<>(menuList, menu -> true);
    private final SortedList<Menu> sortedMenuList = new SortedList<>(filteredMenuList);
    private final Locale localeId = new Locale("id", "ID");
    private boolean activeView = true;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        setupSearch();
        loadData();
    }

    private void setupSearch() {
        if (tableMenu != null) {
            tableMenu.setItems(sortedMenuList);
        }
        if (txtSearchMenu != null) {
            txtSearchMenu.textProperty().addListener((obs, oldValue, newValue) -> applySearchFilter());
        }
        if (cmbFilterKategori != null) {
            cmbFilterKategori.setItems(FXCollections.observableArrayList(
                    "Semua kategori", "makanan", "minuman", "snack"
            ));
            cmbFilterKategori.setValue("Semua kategori");
            cmbFilterKategori.valueProperty().addListener((obs, oldValue, newValue) -> applySearchFilter());
        }
        if (cmbSortHarga != null) {
            cmbSortHarga.setItems(FXCollections.observableArrayList(
                    "Urutan default", "Harga tertinggi", "Harga terendah"
            ));
            cmbSortHarga.setValue("Urutan default");
            cmbSortHarga.valueProperty().addListener((obs, oldValue, newValue) -> applySearchFilter());
        }
    }

    private void setupColumns() {
        colNama.setPrefWidth(260);
        colNama.setMinWidth(220);

        colKategori.setPrefWidth(160);
        colKategori.setMinWidth(140);

        colHarga.setPrefWidth(160);
        colHarga.setMinWidth(140);

        colAksi.setPrefWidth(190);
        colAksi.setMinWidth(190);
        colAksi.setMaxWidth(220);
        colAksi.setStyle("-fx-alignment: CENTER_RIGHT;");

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
                    setText("Rp " + formatPlainCurrency(item));
                    setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold; -fx-font-size: 16;");
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
                    case "makanan" -> { bg = "#FFF7ED"; fg = "#EA580C"; }
                    case "minuman" -> { bg = "#DBEAFE"; fg = "#2563EB"; }
                    default ->        { bg = "#F0FDF4"; fg = "#16A34A"; }
                }
                badge.setStyle(
                        "-fx-background-color: " + bg + ";" +
                                "-fx-text-fill: " + fg + ";" +
                                "-fx-background-radius: 20;" +
                                "-fx-padding: 6 16 6 16;" +
                                "-fx-font-size: 13;" +
                                "-fx-font-weight: bold;"
                );
                setGraphic(badge);
            }
        });

        // Kolom aksi
        colAksi.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnArchive = new Button("Arsipkan");
            private final Button btnRestore = new Button("Aktifkan");

            {
                btnEdit.getStyleClass().addAll("table-action-button", "table-action-button-edit");
                btnArchive.getStyleClass().addAll("table-action-button", "table-action-button-archive");
                btnRestore.getStyleClass().addAll("table-action-button", "table-action-button-restore");

                btnEdit.setOnAction(e -> {
                    Menu menu = getCurrentMenu();
                    if (menu == null) return;
                    showEditDialog(menu);
                });

                btnArchive.setOnAction(e -> {
                    Menu menu = getCurrentMenu();
                    if (menu == null) return;
                    if (AlertUtil.showConfirm("Arsipkan Menu",
                            "Arsipkan \"" + menu.getNamaMenu() + "\"?\nMenu disembunyikan dari daftar aktif.")) {
                        menuDAO.archive(menu.getIdMenu());
                        loadData();
                        ToastUtil.showSuccess(tableMenu, "Menu berhasil diarsipkan.");
                    }
                });

                btnRestore.setOnAction(e -> {
                    Menu menu = getCurrentMenu();
                    if (menu == null) return;
                    menuDAO.activate(menu.getIdMenu());
                    loadData();
                    ToastUtil.showSuccess(tableMenu, "Menu berhasil diaktifkan kembali.");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8);
                    Menu menu = getCurrentMenu();
                    if (menu != null && menu.isActive()) {
                        box.getChildren().addAll(btnEdit, btnArchive);
                    } else {
                        box.getChildren().add(btnRestore);
                    }
                    box.setAlignment(Pos.CENTER_RIGHT);
                    box.setMaxWidth(Double.MAX_VALUE);
                    setGraphic(box);
                    setText(null);
                    setAlignment(Pos.CENTER_RIGHT);
                }
            }

            private Menu getCurrentMenu() {
                int index = getIndex();
                if (index < 0 || index >= getTableView().getItems().size()) {
                    return null;
                }
                return getTableView().getItems().get(index);
            }
        });
    }

    private void loadData() {
        menuList.setAll(activeView ? menuDAO.findByActive(true) : menuDAO.findArchived());
        applySearchFilter();
        updateMenuTabs();
    }

    private void applySearchFilter() {
        String keyword = txtSearchMenu == null ? "" : txtSearchMenu.getText();
        String query = keyword == null ? "" : keyword.trim().toLowerCase(localeId);
        String kategori = cmbFilterKategori == null ? "Semua kategori" : cmbFilterKategori.getValue();

        filteredMenuList.setPredicate(menu -> {
            boolean matchesSearch = query.isBlank()
                    || contains(menu.getNamaMenu(), query)
                    || contains(menu.getKategori(), query)
                    || contains(formatPlainCurrency(menu.getHarga()), query)
                    || contains(formatCurrencyForSearch(menu.getHarga()), query);
            boolean matchesCategory = kategori == null
                    || "Semua kategori".equals(kategori)
                    || kategori.equalsIgnoreCase(menu.getKategori());
            return matchesSearch && matchesCategory;
        });
        applySort();
    }

    private void applySort() {
        String sort = cmbSortHarga == null ? "Urutan default" : cmbSortHarga.getValue();
        if ("Harga tertinggi".equals(sort)) {
            sortedMenuList.setComparator(Comparator.comparingDouble(Menu::getHarga).reversed());
        } else if ("Harga terendah".equals(sort)) {
            sortedMenuList.setComparator(Comparator.comparingDouble(Menu::getHarga));
        } else {
            sortedMenuList.setComparator(Comparator.comparing(Menu::getKategori, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Menu::getNamaMenu, String.CASE_INSENSITIVE_ORDER));
        }
    }

    @FXML
    public void handleTambahMenu() {
        showTambahDialog();
    }

    @FXML
    public void openTabAktif() {
        activeView = true;
        loadData();
    }

    @FXML
    public void openTabArsip() {
        activeView = false;
        loadData();
    }

    @FXML
    public void handleResetFilter() {
        if (txtSearchMenu != null) txtSearchMenu.clear();
        if (cmbFilterKategori != null) cmbFilterKategori.setValue("Semua kategori");
        if (cmbSortHarga != null) cmbSortHarga.setValue("Urutan default");
        applySearchFilter();
    }

    private void updateMenuTabs() {
        if (lblTableTitle != null) {
            lblTableTitle.setText(activeView ? "Daftar Menu Aktif" : "Menu Diarsipkan");
        }
        if (lblPlaceholder != null) {
            lblPlaceholder.setText(activeView
                    ? "Belum ada menu aktif"
                    : "Belum ada menu yang diarsipkan");
        }
        if (btnTabAktif != null && btnTabArsip != null) {
            setTabStyle(btnTabAktif, activeView);
            setTabStyle(btnTabArsip, !activeView);
        }
    }

    private void setTabStyle(Button button, boolean active) {
        button.getStyleClass().removeAll("menu-segment", "menu-segment-active");
        button.getStyleClass().add(active ? "menu-segment-active" : "menu-segment");
    }

    private void showTambahDialog() {
        Stage dialog = createDialog("Tambah Menu Baru");

        TextField txtNama = createTextField("Nama menu");
        ComboBox<String> cmbKategori = createKategoriCombo();
        TextField txtHarga = createTextField("Harga (Rp)");
        configureCurrencyField(txtHarga);

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
        dialog.setScene(createDialogScene(root, 520, 430));
        dialog.showAndWait();
    }

    private void showEditDialog(Menu menu) {
        Stage dialog = createDialog("Edit Menu");

        TextField txtNama = createTextField("Nama menu");
        txtNama.setText(menu.getNamaMenu());

        ComboBox<String> cmbKategori = createKategoriCombo();
        cmbKategori.setValue(menu.getKategori());

        TextField txtHarga = createTextField("Harga (Rp)");
        configureCurrencyField(txtHarga);
        txtHarga.setText(formatPlainCurrency(menu.getHarga()));

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
        dialog.setScene(createDialogScene(root, 520, 430));
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
            harga = parseCurrency(hargaStr);
            if (harga <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            AlertUtil.showError("Validasi", "Harga harus berupa angka positif!");
            return false;
        }

        if (existing == null) {
            menuDAO.insert(new Menu(0, nama, harga, kategori));
            ToastUtil.showSuccess(txtNama, "Menu berhasil ditambahkan.");
        } else {
            existing.setNamaMenu(nama);
            existing.setHarga(harga);
            existing.setKategori(kategori);
            menuDAO.update(existing);
            ToastUtil.showSuccess(txtNama, "Menu berhasil diupdate.");
        }
        return true;
    }

    // ===== HELPERS =====

    private Stage createDialog(String title) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (tableMenu != null && tableMenu.getScene() != null) {
            dialog.initOwner(tableMenu.getScene().getWindow());
        }
        dialog.setTitle(title);
        dialog.setResizable(false);
        return dialog;
    }

    private TextField createTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("text-input");
        return tf;
    }

    private ComboBox<String> createKategoriCombo() {
        ComboBox<String> cmb = new ComboBox<>();
        cmb.setItems(FXCollections.observableArrayList("makanan", "minuman", "snack"));
        cmb.setPromptText("Pilih kategori");
        cmb.setMaxWidth(Double.MAX_VALUE);
        cmb.getStyleClass().add("combo-input");
        return cmb;
    }

    private Button createBtnBatal(Stage dialog) {
        Button btn = new Button("Batal");
        btn.getStyleClass().add("secondary-button");
        btn.setPrefWidth(118);
        btn.setOnAction(e -> dialog.close());
        return btn;
    }

    private VBox buildDialogLayout(String title, TextField txtNama,
                                   ComboBox<String> cmbKategori, TextField txtHarga,
                                   Button btnBatal, Button btnSimpan) {
        VBox root = new VBox(16);
        root.setPadding(new Insets(32));
        root.getStyleClass().add("dialog-root");

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("dialog-title");

        HBox btnBox = new HBox(10, btnBatal, btnSimpan);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        btnBox.setPadding(new Insets(10, 0, 0, 0));

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
        lbl.getStyleClass().add("form-label");
        return lbl;
    }

    private Scene createDialogScene(VBox root, double width, double height) {
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(getClass().getResource("/com/pos/view/css/menu.css").toExternalForm());
        return scene;
    }

    private String inputStyle() {
        return "-fx-background-color: #F3F4F6; -fx-background-radius: 8;" +
                "-fx-border-color: transparent; -fx-pref-height: 40;" +
                "-fx-font-size: 13; -fx-padding: 0 12 0 12;";
    }

    private String btnPrimaryStyle() {
        return "-fx-background-color: linear-gradient(to right, #5B4BFF, #4F46E5); -fx-text-fill: white;" +
                "-fx-background-radius: 12; -fx-cursor: hand;" +
                "-fx-pref-height: 48; -fx-pref-width: 118; -fx-font-weight: bold;";
    }

    private void configureCurrencyField(TextField field) {
        final boolean[] updating = {false};
        field.textProperty().addListener((obs, oldValue, newValue) -> {
            if (updating[0]) {
                return;
            }

            String digits = extractDigits(newValue);
            if (digits.isEmpty()) {
                if (!newValue.isEmpty()) {
                    updating[0] = true;
                    field.clear();
                    updating[0] = false;
                }
                return;
            }

            String normalized = normalizeLeadingZeros(digits);
            String formatted = formatDigits(normalized);
            if (!formatted.equals(newValue)) {
                updating[0] = true;
                field.setText(formatted);
                field.positionCaret(formatted.length());
                updating[0] = false;
            }
        });
    }

    private double parseCurrency(String value) {
        String digits = extractDigits(value);
        if (digits.isBlank()) {
            throw new NumberFormatException();
        }
        return Double.parseDouble(normalizeLeadingZeros(digits));
    }

    private double parseOptionalCurrency(String value, double fallback) {
        String digits = extractDigits(value);
        if (digits.isBlank()) {
            return fallback;
        }
        return Double.parseDouble(normalizeLeadingZeros(digits));
    }

    private String extractDigits(String value) {
        return value == null ? "" : value.replaceAll("[^0-9]", "");
    }

    private String normalizeLeadingZeros(String digits) {
        if (digits == null || digits.isBlank()) {
            return "";
        }
        return digits.replaceFirst("^0+(?!$)", "");
    }

    private String formatDigits(String digits) {
        if (digits == null || digits.isBlank()) {
            return "";
        }
        return CurrencyFormatUtil.formatNumber(Long.parseLong(digits));
    }

    private String formatPlainCurrency(double value) {
        return formatDigits(String.valueOf((long) value));
    }

    private String formatCurrencyForSearch(double value) {
        return ("Rp " + formatPlainCurrency(value)).toLowerCase(localeId);
    }

    private boolean contains(String source, String query) {
        return source != null && source.toLowerCase(localeId).contains(query);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
