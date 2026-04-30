package com.pos.controller;

import com.pos.dao.DebtDAO;
import com.pos.model.Debt;
import com.pos.util.AlertUtil;
import com.pos.util.CurrencyFormatUtil;
import com.pos.util.ToastUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class DebtController implements Initializable {

    @FXML private Label lblTotalHutang;
    @FXML private Label lblCountHutang;
    @FXML private Label lblTotalPiutang;
    @FXML private Label lblCountPiutang;
    @FXML private Label lblSectionTitle;
    @FXML private Button btnTabHutang;
    @FXML private Button btnTabPiutang;
    @FXML private VBox containerDaftar;
    @FXML private ScrollPane scrollDaftar;
    @FXML private TextField txtSearchDebt;
    @FXML private ComboBox<String> cmbFilterStatus;
    @FXML private ComboBox<String> cmbSortNominal;

    private final DebtDAO debtDAO = new DebtDAO();
    private final Locale localeId = new Locale("id", "ID");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private List<Debt> hutangList = Collections.emptyList();
    private List<Debt> piutangList = Collections.emptyList();
    private String activeType = "hutang";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (txtSearchDebt != null) {
            txtSearchDebt.textProperty().addListener((obs, oldValue, newValue) -> refreshListSection());
        }
        if (cmbFilterStatus != null) {
            cmbFilterStatus.getItems().setAll("Semua status", "Belum Lunas", "Lunas");
            cmbFilterStatus.setValue("Semua status");
            cmbFilterStatus.valueProperty().addListener((obs, oldValue, newValue) -> refreshListSection());
        }
        if (cmbSortNominal != null) {
            cmbSortNominal.getItems().setAll("Urutan default", "Nominal tertinggi", "Nominal terendah");
            cmbSortNominal.setValue("Urutan default");
            cmbSortNominal.valueProperty().addListener((obs, oldValue, newValue) -> refreshListSection());
        }
        refreshData();
    }

    @FXML
    public void openTabHutang() {
        activeType = "hutang";
        refreshListSection();
    }

    @FXML
    public void openTabPiutang() {
        activeType = "piutang";
        refreshListSection();
    }

    @FXML
    public void handleTambahHutang() {
        showDebtDialog("hutang");
    }

    @FXML
    public void handleTambahPiutang() {
        showDebtDialog("piutang");
    }

    @FXML
    public void handleResetFilter() {
        if (txtSearchDebt != null) txtSearchDebt.clear();
        if (cmbFilterStatus != null) cmbFilterStatus.setValue("Semua status");
        if (cmbSortNominal != null) cmbSortNominal.setValue("Urutan default");
        refreshListSection();
    }

    private void refreshData() {
        hutangList = debtDAO.findAllByType("hutang");
        piutangList = debtDAO.findAllByType("piutang");

        lblTotalHutang.setText(formatCurrency(debtDAO.getOutstandingTotal("hutang")));
        lblTotalPiutang.setText(formatCurrency(debtDAO.getOutstandingTotal("piutang")));

        lblCountHutang.setText(formatOutstandingCount(debtDAO.getOutstandingCount("hutang")));
        lblCountPiutang.setText(formatOutstandingCount(debtDAO.getOutstandingCount("piutang")));

        btnTabHutang.setText("Hutang (" + hutangList.size() + ")");
        btnTabPiutang.setText("Piutang (" + piutangList.size() + ")");

        refreshListSection();
    }

    private void refreshListSection() {
        styleTabButtons();

        List<Debt> activeList = getFilteredActiveList();
        String activeLabel = capitalize(activeType);
        lblSectionTitle.setText("Daftar " + activeLabel);

        containerDaftar.getChildren().clear();

        if (activeList.isEmpty()) {
            containerDaftar.getChildren().add(buildEmptyState(activeLabel));
            if (scrollDaftar != null) {
                scrollDaftar.setVvalue(0);
            }
            return;
        }

        containerDaftar.getChildren().add(buildHeaderRow());
        for (Debt debt : activeList) {
            containerDaftar.getChildren().add(buildDataRow(debt));
        }

        if (scrollDaftar != null) {
            scrollDaftar.setVvalue(0);
        }
    }

    private List<Debt> getActiveList() {
        return "hutang".equals(activeType) ? hutangList : piutangList;
    }

    private List<Debt> getFilteredActiveList() {
        List<Debt> source = getActiveList();
        String keyword = txtSearchDebt == null ? "" : txtSearchDebt.getText();
        String query = keyword == null ? "" : keyword.trim().toLowerCase(localeId);
        String status = cmbFilterStatus == null ? "Semua status" : cmbFilterStatus.getValue();

        return source.stream()
                .filter(debt -> matchesSearch(debt, query))
                .filter(debt -> matchesStatus(debt, status))
                .sorted(debtComparator())
                .toList();
    }

    private Comparator<Debt> debtComparator() {
        String sort = cmbSortNominal == null ? "Urutan default" : cmbSortNominal.getValue();
        if ("Nominal tertinggi".equals(sort)) {
            return Comparator.comparingDouble(Debt::getNominal).reversed();
        }
        if ("Nominal terendah".equals(sort)) {
            return Comparator.comparingDouble(Debt::getNominal);
        }
        return Comparator.comparing(Debt::getTanggal).reversed()
                .thenComparing(Debt::getNama, String.CASE_INSENSITIVE_ORDER);
    }

    private boolean matchesSearch(Debt debt, String query) {
        if (query.isBlank()) {
            return true;
        }
        return contains(debt.getNama(), query)
                || contains(debt.getTipe(), query)
                || contains(debt.getStatus(), query)
                || contains(debt.getKeterangan(), query)
                || contains(debt.getTanggal().format(dateFormatter), query)
                || contains(formatCurrency(debt.getNominal()), query);
    }

    private boolean matchesStatus(Debt debt, String status) {
        if (status == null || "Semua status".equals(status)) {
            return true;
        }
        return "Belum Lunas".equals(status) ? debt.isBelumLunas() : !debt.isBelumLunas();
    }

    private VBox buildEmptyState(String activeLabel) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(48, 24, 48, 24));
        box.setStyle(
                "-fx-background-color: #F9FAFB;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: #E5E7EB;" +
                        "-fx-border-radius: 14;" +
                        "-fx-border-width: 1;"
        );

        Label title = new Label("Belum ada data " + activeLabel.toLowerCase(localeId));
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label subtitle = new Label("Klik tombol tambah untuk mulai mencatat data.");
        subtitle.setStyle("-fx-font-size: 12; -fx-text-fill: #6B7280;");

        box.getChildren().addAll(title, subtitle);
        return box;
    }

    private GridPane buildHeaderRow() {
        GridPane row = createBaseRow();
        row.setStyle(
                "-fx-background-color: #F9FAFB;" +
                        "-fx-background-radius: 12 12 0 0;" +
                        "-fx-border-color: transparent transparent #E5E7EB transparent;" +
                        "-fx-border-width: 0 0 1 0;"
        );

        row.add(createHeaderLabel("Nama"), 0, 0);
        row.add(createHeaderLabel("Nominal"), 1, 0);
        row.add(createHeaderLabel("Tanggal"), 2, 0);
        row.add(createHeaderLabel("Keterangan"), 3, 0);
        row.add(createHeaderLabel("Status"), 4, 0);
        row.add(createHeaderLabel("Aksi"), 5, 0);

        GridPane.setHalignment(row.getChildren().get(5), HPos.RIGHT);
        return row;
    }

    private GridPane buildDataRow(Debt debt) {
        GridPane row = createBaseRow();
        row.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: transparent transparent #F3F4F6 transparent;" +
                        "-fx-border-width: 0 0 1 0;"
        );

        Label lblNama = createCellLabel(debt.getNama(), true);
        Label lblNominal = createCellLabel(formatCurrency(debt.getNominal()), true);
        lblNominal.setStyle(lblNominal.getStyle() + "-fx-text-fill: #7C2D12;");
        Label lblTanggal = createCellLabel(debt.getTanggal().format(dateFormatter), false);
        Label lblKeterangan = createCellLabel(
                debt.getKeterangan() == null || debt.getKeterangan().isBlank() ? "-" : debt.getKeterangan(),
                false
        );
        lblKeterangan.setWrapText(true);

        Label badge = buildStatusBadge(debt);
        HBox badgeBox = new HBox(badge);
        badgeBox.setAlignment(Pos.CENTER_LEFT);

        HBox actionBox = new HBox(8);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setMinWidth(210);

        if (debt.isBelumLunas()) {
            Button btnPaid = new Button("Tandai Lunas");
            btnPaid.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-text-fill: #111827;" +
                            "-fx-border-color: #D1D5DB;" +
                            "-fx-border-radius: 10;" +
                            "-fx-background-radius: 10;" +
                            "-fx-font-size: 14;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;" +
                            "-fx-pref-height: 38;" +
                            "-fx-padding: 0 14 0 14;"
            );
            btnPaid.setMinWidth(126);
            btnPaid.setPrefWidth(132);
            btnPaid.setOnAction(event -> handleMarkAsPaid(debt));
            actionBox.getChildren().add(btnPaid);
        }

        Button btnDelete = new Button("Hapus");
        btnDelete.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #EF4444;" +
                        "-fx-font-size: 14;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
        );
        btnDelete.setMinWidth(62);
        btnDelete.setPrefWidth(66);
        btnDelete.setOnAction(event -> handleDelete(debt));
        actionBox.getChildren().add(btnDelete);

        row.add(lblNama, 0, 0);
        row.add(lblNominal, 1, 0);
        row.add(lblTanggal, 2, 0);
        row.add(lblKeterangan, 3, 0);
        row.add(badgeBox, 4, 0);
        row.add(actionBox, 5, 0);

        GridPane.setHgrow(lblKeterangan, Priority.ALWAYS);
        GridPane.setHalignment(actionBox, HPos.RIGHT);
        return row;
    }

    private GridPane createBaseRow() {
        GridPane row = new GridPane();
        row.setHgap(12);
        row.setVgap(4);
        row.setPadding(new Insets(18, 12, 18, 12));
        row.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints c1 = percentColumn(16);
        ColumnConstraints c2 = percentColumn(14);
        ColumnConstraints c3 = percentColumn(12);
        ColumnConstraints c4 = percentColumn(22);
        ColumnConstraints c5 = percentColumn(12);
        ColumnConstraints c6 = percentColumn(24);
        row.getColumnConstraints().addAll(c1, c2, c3, c4, c5, c6);

        return row;
    }

    private ColumnConstraints percentColumn(double percentWidth) {
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setPercentWidth(percentWidth);
        constraints.setHgrow(Priority.ALWAYS);
        return constraints;
    }

    private Label createHeaderLabel(String text) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-font-size: 16;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #6B7280;"
        );
        return label;
    }

    private Label createCellLabel(String text, boolean bold) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle(
                "-fx-font-size: 16;" +
                        "-fx-font-weight: " + (bold ? "bold" : "normal") + ";" +
                        "-fx-text-fill: #111827;"
        );
        return label;
    }

    private Label buildStatusBadge(Debt debt) {
        Label badge = new Label();
        badge.setStyle(
                "-fx-background-radius: 999;" +
                        "-fx-padding: 6 14 6 14;" +
                        "-fx-font-size: 12;" +
                        "-fx-font-weight: bold;"
        );

        if (debt.isBelumLunas()) {
            badge.setText("Belum Lunas");
            if ("hutang".equalsIgnoreCase(debt.getTipe())) {
                badge.setStyle(badge.getStyle() +
                        "-fx-background-color: #FEE2E2;" +
                        "-fx-text-fill: #BE123C;");
            } else {
                badge.setStyle(badge.getStyle() +
                        "-fx-background-color: #FEF3C7;" +
                        "-fx-text-fill: #B45309;");
            }
        } else {
            badge.setText("Lunas");
            badge.setStyle(badge.getStyle() +
                    "-fx-background-color: #DCFCE7;" +
                    "-fx-text-fill: #15803D;");
        }

        return badge;
    }

    private void handleDelete(Debt debt) {
        String label = capitalize(debt.getTipe());
        if (!AlertUtil.showConfirm("Hapus " + label,
                "Yakin hapus data " + label.toLowerCase(localeId) + " atas nama \"" + debt.getNama() + "\"?")) {
            return;
        }

        debtDAO.delete(debt.getIdDebt());
        refreshData();
        ToastUtil.showSuccess(containerDaftar, label + " berhasil dihapus.");
    }

    private void handleMarkAsPaid(Debt debt) {
        String label = capitalize(debt.getTipe());
        if (!AlertUtil.showConfirm("Tandai Lunas",
                "Tandai " + label.toLowerCase(localeId) + " \"" + debt.getNama() + "\" sebagai lunas?")) {
            return;
        }

        debtDAO.markAsPaid(debt);
        refreshData();
        ToastUtil.showSuccess(containerDaftar, label + " berhasil ditandai lunas.");
    }

    private void showDebtDialog(String tipe) {
        boolean isHutang = "hutang".equalsIgnoreCase(tipe);
        String title = isHutang ? "Tambah Hutang Baru" : "Tambah Piutang Baru";
        String namaLabel = isHutang ? "Nama Supplier" : "Nama Customer";
        String namaPrompt = isHutang ? "Nama supplier" : "Nama customer";

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (containerDaftar != null && containerDaftar.getScene() != null) {
            dialog.initOwner(containerDaftar.getScene().getWindow());
        }
        dialog.setTitle(title);
        dialog.setResizable(false);

        VBox root = new VBox(20);
        root.setPadding(new Insets(28, 30, 30, 30));
        root.getStyleClass().add("dialog-root");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("dialog-title");
        header.getChildren().add(lblTitle);

        TextField txtNama = createTextField(namaPrompt);
        TextField txtNominal = createTextField("Contoh: 500000");
        configureCurrencyField(txtNominal);
        DatePicker datePicker = createDatePicker();
        datePicker.setValue(LocalDate.now());

        TextArea txtKeterangan = new TextArea();
        txtKeterangan.setPromptText("Keterangan singkat");
        txtKeterangan.setWrapText(true);
        txtKeterangan.getStyleClass().add("text-area");

        Button btnBatal = new Button("Batal");
        btnBatal.getStyleClass().add("secondary-button");
        btnBatal.setPrefWidth(96);
        btnBatal.setOnAction(event -> dialog.close());

        Button btnSubmit = new Button("Tambah");
        btnSubmit.getStyleClass().add("primary-button");
        btnSubmit.setPrefWidth(130);
        btnSubmit.setOnAction(event -> {
            String nama = txtNama.getText().trim();
            String nominalText = txtNominal.getText().trim();
            LocalDate tanggal = datePicker.getValue();
            String keterangan = txtKeterangan.getText().trim();

            if (nama.isEmpty() || nominalText.isEmpty() || tanggal == null) {
                AlertUtil.showError("Validasi", "Nama, nominal, dan tanggal wajib diisi.");
                return;
            }

            double nominal;
            try {
                nominal = parseNominal(nominalText);
                if (nominal <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                AlertUtil.showError("Validasi", "Nominal harus berupa angka positif.");
                return;
            }

            Debt debt = new Debt();
            debt.setNama(nama);
            debt.setTipe(tipe);
            debt.setNominal(nominal);
            debt.setTanggal(tanggal);
            debt.setStatus("belum");
            debt.setKeterangan(keterangan.isBlank() ? "-" : keterangan);

            debtDAO.insert(debt);
            activeType = tipe;
            dialog.close();
            refreshData();
            ToastUtil.showSuccess(dialog,
                    capitalize(tipe) + " baru berhasil ditambahkan.");
        });

        HBox buttonBox = new HBox(10, btnBatal, btnSubmit);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(
                header,
                fieldGroup(namaLabel, txtNama),
                fieldGroup("Nominal (Rp)", txtNominal),
                fieldGroup("Tanggal", datePicker),
                fieldGroup("Keterangan", txtKeterangan),
                buttonBox
        );

        Scene scene = new Scene(root, 680, 560);
        scene.getStylesheets().add(getClass().getResource("/com/pos/view/css/menu.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private Label fieldLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-label");
        return label;
    }

    private VBox fieldGroup(String labelText, Region input) {
        VBox group = new VBox(8, fieldLabel(labelText), input);
        input.setMaxWidth(Double.MAX_VALUE);
        return group;
    }

    private TextField createTextField(String promptText) {
        TextField textField = new TextField();
        textField.setPromptText(promptText);
        textField.getStyleClass().add("text-input");
        return textField;
    }

    private DatePicker createDatePicker() {
        DatePicker datePicker = new DatePicker();
        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.getStyleClass().add("dialog-date-input");
        datePicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : date.format(dateFormatter);
            }

            @Override
            public LocalDate fromString(String string) {
                if (string == null || string.isBlank()) {
                    return null;
                }
                try {
                    return LocalDate.parse(string, dateFormatter);
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        });
        return datePicker;
    }

    private void styleTabButtons() {
        String activeStyle = """
                -fx-background-color: white;
                -fx-text-fill: #111827;
                -fx-font-size: 14;
                -fx-font-weight: bold;
                -fx-background-radius: 14;
                -fx-cursor: hand;
                -fx-pref-height: 58;
                """;

        String inactiveStyle = """
                -fx-background-color: transparent;
                -fx-text-fill: #6B7280;
                -fx-font-size: 14;
                -fx-font-weight: bold;
                -fx-background-radius: 14;
                -fx-cursor: hand;
                -fx-pref-height: 58;
                """;

        btnTabHutang.setStyle("hutang".equals(activeType) ? activeStyle : inactiveStyle);
        btnTabPiutang.setStyle("piutang".equals(activeType) ? activeStyle : inactiveStyle);
    }

    private double parseNominal(String value) {
        if (containsNegativeSign(value)) {
            throw new NumberFormatException();
        }

        String cleaned = normalizeLeadingZeros(extractDigits(value));
        if (cleaned.isBlank()) {
            throw new NumberFormatException();
        }
        return Double.parseDouble(cleaned);
    }

    private double parseOptionalNominal(String value, double fallback) {
        String cleaned = normalizeLeadingZeros(extractDigits(value));
        if (cleaned.isBlank()) {
            return fallback;
        }
        return Double.parseDouble(cleaned);
    }

    private void configureCurrencyField(TextField field) {
        final boolean[] updating = {false};
        field.textProperty().addListener((obs, oldValue, newValue) -> {
            if (updating[0]) {
                return;
            }

            if (containsNegativeSign(newValue)) {
                updating[0] = true;
                field.setText(oldValue == null ? "" : oldValue);
                field.positionCaret(field.getText().length());
                updating[0] = false;
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

    private boolean containsNegativeSign(String value) {
        return value != null && value.matches(".*[-\\u2212\\u2013\\u2014].*");
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

    private String formatCurrency(double amount) {
        return CurrencyFormatUtil.formatRupiah(amount);
    }

    private String formatOutstandingCount(int count) {
        return count + " item belum lunas";
    }

    private String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase(localeId) + value.substring(1);
    }

    private boolean contains(String source, String query) {
        return source != null && source.toLowerCase(localeId).contains(query);
    }
}
