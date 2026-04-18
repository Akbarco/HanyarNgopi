package com.pos.controller;

import com.pos.config.koneksi;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.ResourceBundle;

public class LaporanController implements Initializable {

    @FXML private DatePicker dpMulai;
    @FXML private DatePicker dpAkhir;
    @FXML private Label lblPeriode;

    @FXML private Button btnTabPenjualan;
    @FXML private Button btnTabStok;
    @FXML private Button btnTabHutang;

    @FXML private VBox panelPenjualan;
    @FXML private VBox panelStok;
    @FXML private VBox panelHutang;

    @FXML private Label lblTotalPenjualan;
    @FXML private Label lblJumlahTransaksi;

    @FXML private VBox containerPenjualanPerMenu;
    @FXML private VBox containerDetailTransaksi;

    @FXML private VBox containerStokMenipis;
    @FXML private VBox containerStokAman;
    @FXML private VBox containerDetailStok;

    @FXML private Label lblHutangBelum;
    @FXML private Label lblHutangLunas;
    @FXML private Label lblTotalHutang;
    @FXML private Label lblPiutangBelum;
    @FXML private Label lblPiutangLunas;
    @FXML private Label lblTotalPiutang;
    @FXML private VBox containerHutangBelum;
    @FXML private VBox containerPiutangBelum;

    private final Locale localeId = new Locale("id", "ID");
    private final DateTimeFormatter periodFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", localeId);
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", localeId);
    private String activeTab = "penjualan";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dpMulai.setValue(LocalDate.now().withDayOfMonth(1));
        dpAkhir.setValue(LocalDate.now());

        dpMulai.setOnAction(event -> refreshAll());
        dpAkhir.setOnAction(event -> refreshAll());

        refreshAll();
    }

    @FXML
    private void showPenjualan() {
        activeTab = "penjualan";
        refreshAll();
    }

    @FXML
    private void showStok() {
        activeTab = "stok";
        refreshAll();
    }

    @FXML
    private void showHutang() {
        activeTab = "hutang";
        refreshAll();
    }

    private void refreshAll() {
        normalizeDates();
        updatePeriodLabel();
        updateActivePanel();

        if ("penjualan".equals(activeTab)) {
            loadPenjualanReport();
        } else if ("stok".equals(activeTab)) {
            loadStokReport();
        } else {
            loadHutangReport();
        }
    }

    private void normalizeDates() {
        LocalDate mulai = dpMulai.getValue();
        LocalDate akhir = dpAkhir.getValue();

        if (mulai == null) {
            mulai = LocalDate.now().withDayOfMonth(1);
            dpMulai.setValue(mulai);
        }

        if (akhir == null) {
            akhir = LocalDate.now();
            dpAkhir.setValue(akhir);
        }

        if (akhir.isBefore(mulai)) {
            dpAkhir.setValue(mulai);
        }
    }

    private void updatePeriodLabel() {
        lblPeriode.setText(dpMulai.getValue().format(periodFormatter) + " - " +
                dpAkhir.getValue().format(periodFormatter));
    }

    private void updateActivePanel() {
        setVisible(panelPenjualan, "penjualan".equals(activeTab));
        setVisible(panelStok, "stok".equals(activeTab));
        setVisible(panelHutang, "hutang".equals(activeTab));

        btnTabPenjualan.getStyleClass().removeAll("report-tab-active");
        btnTabStok.getStyleClass().removeAll("report-tab-active");
        btnTabHutang.getStyleClass().removeAll("report-tab-active");

        if (!btnTabPenjualan.getStyleClass().contains("report-tab")) btnTabPenjualan.getStyleClass().add("report-tab");
        if (!btnTabStok.getStyleClass().contains("report-tab")) btnTabStok.getStyleClass().add("report-tab");
        if (!btnTabHutang.getStyleClass().contains("report-tab")) btnTabHutang.getStyleClass().add("report-tab");

        Button activeButton = switch (activeTab) {
            case "stok" -> btnTabStok;
            case "hutang" -> btnTabHutang;
            default -> btnTabPenjualan;
        };

        activeButton.getStyleClass().remove("report-tab");
        if (!activeButton.getStyleClass().contains("report-tab-active")) {
            activeButton.getStyleClass().add("report-tab-active");
        }
    }

    private void loadPenjualanReport() {
        clear(containerPenjualanPerMenu);
        clear(containerDetailTransaksi);
        lblTotalPenjualan.setText("Rp 0");
        lblJumlahTransaksi.setText("0");

        try (Connection conn = koneksi.getConnection()) {
            if (conn == null) {
                showEmpty(containerPenjualanPerMenu, "Koneksi database tidak tersedia");
                showEmpty(containerDetailTransaksi, "Koneksi database tidak tersedia");
                return;
            }

            loadPenjualanSummary(conn);
            loadPenjualanPerMenu(conn);
            loadDetailTransaksi(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            showEmpty(containerPenjualanPerMenu, "Gagal memuat data penjualan");
            showEmpty(containerDetailTransaksi, "Gagal memuat detail transaksi");
        }
    }

    private void loadPenjualanSummary(Connection conn) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(total), 0) AS total_penjualan,
                       COUNT(*) AS jumlah_transaksi
                FROM transactions
                WHERE date(tanggal) BETWEEN ? AND ?
                """;

        try (PreparedStatement ps = preparePeriod(conn, sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                lblTotalPenjualan.setText(formatCurrency(rs.getDouble("total_penjualan")));
                lblJumlahTransaksi.setText(String.valueOf(rs.getInt("jumlah_transaksi")));
            }
        }
    }

    private void loadPenjualanPerMenu(Connection conn) throws SQLException {
        String sql = """
                SELECT m.nama_menu,
                       SUM(td.qty) AS total_qty,
                       COALESCE(SUM(td.subtotal), 0) AS total_nominal
                FROM transaction_detail td
                JOIN transactions t ON td.id_transaksi = t.id_transaksi
                JOIN menus m ON td.id_menu = m.id_menu
                WHERE date(t.tanggal) BETWEEN ? AND ?
                GROUP BY m.id_menu, m.nama_menu
                ORDER BY total_nominal DESC, total_qty DESC
                """;

        try (PreparedStatement ps = preparePeriod(conn, sql);
             ResultSet rs = ps.executeQuery()) {
            boolean found = false;
            while (rs.next()) {
                found = true;
                containerPenjualanPerMenu.getChildren().add(createSalesMenuRow(
                        rs.getString("nama_menu"),
                        rs.getInt("total_qty"),
                        rs.getDouble("total_nominal")
                ));
            }

            if (!found) {
                showEmpty(containerPenjualanPerMenu, "Tidak ada data penjualan");
            }
        }
    }

    private void loadDetailTransaksi(Connection conn) throws SQLException {
        String sql = """
                SELECT t.id_transaksi,
                       t.tanggal,
                       t.total
                FROM transactions t
                WHERE date(t.tanggal) BETWEEN ? AND ?
                ORDER BY t.tanggal DESC, t.id_transaksi DESC
                LIMIT 20
                """;

        try (PreparedStatement ps = preparePeriod(conn, sql);
             ResultSet rs = ps.executeQuery()) {
            boolean found = false;
            while (rs.next()) {
                found = true;
                TransactionSummary summary = loadTransactionSummary(conn, rs.getInt("id_transaksi"));
                LocalDateTime tanggal = readDateTime(rs, "tanggal");
                containerDetailTransaksi.getChildren().add(createTransactionRow(
                        summary.items(),
                        tanggal == null ? "-" : tanggal.format(dateFormatter),
                        formatMetode(summary.metode()),
                        rs.getDouble("total")
                ));
            }

            if (!found) {
                showEmpty(containerDetailTransaksi, "Tidak ada transaksi");
            }
        }
    }

    private void loadStokReport() {
        clear(containerStokMenipis);
        clear(containerStokAman);
        clear(containerDetailStok);

        try (Connection conn = koneksi.getConnection()) {
            if (conn == null) {
                showEmpty(containerStokMenipis, "Koneksi database tidak tersedia");
                showEmpty(containerStokAman, "Koneksi database tidak tersedia");
                showEmpty(containerDetailStok, "Koneksi database tidak tersedia");
                return;
            }

            loadStockCards(conn, true);
            loadStockCards(conn, false);
            loadStockDetail(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            showEmpty(containerDetailStok, "Gagal memuat data stok");
        }
    }

    private void loadStockCards(Connection conn, boolean menipis) throws SQLException {
        String sql = """
                SELECT m.nama_menu, s.jumlah_stok, s.satuan, s.stok_minimum
                FROM stock s
                JOIN menus m ON s.id_menu = m.id_menu
                WHERE %s
                ORDER BY m.nama_menu ASC
                LIMIT 6
                """.formatted(menipis ? "s.jumlah_stok <= s.stok_minimum" : "s.jumlah_stok > s.stok_minimum");

        VBox target = menipis ? containerStokMenipis : containerStokAman;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean found = false;
            while (rs.next()) {
                found = true;
                target.getChildren().add(createStockMiniRow(
                        rs.getString("nama_menu"),
                        rs.getInt("jumlah_stok") + " " + rs.getString("satuan"),
                        menipis
                ));
            }

            if (!found) {
                showEmpty(target, menipis ? "Tidak ada stok menipis" : "Belum ada stok aman");
            }
        }
    }

    private void loadStockDetail(Connection conn) throws SQLException {
        String sql = """
                SELECT m.nama_menu, s.jumlah_stok, s.satuan, s.stok_minimum
                FROM stock s
                JOIN menus m ON s.id_menu = m.id_menu
                ORDER BY m.nama_menu ASC
                """;

        containerDetailStok.getChildren().add(createStockHeader());

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean found = false;
            while (rs.next()) {
                found = true;
                int jumlah = rs.getInt("jumlah_stok");
                int minimum = rs.getInt("stok_minimum");
                containerDetailStok.getChildren().add(createStockDetailRow(
                        rs.getString("nama_menu"),
                        rs.getString("satuan"),
                        jumlah,
                        minimum,
                        jumlah <= minimum ? "Menipis" : "Aman"
                ));
            }

            if (!found) {
                clear(containerDetailStok);
                showEmpty(containerDetailStok, "Belum ada data stok");
            }
        }
    }

    private void loadHutangReport() {
        clear(containerHutangBelum);
        clear(containerPiutangBelum);

        try (Connection conn = koneksi.getConnection()) {
            if (conn == null) {
                setDebtFallback();
                showEmpty(containerHutangBelum, "Koneksi database tidak tersedia");
                showEmpty(containerPiutangBelum, "Koneksi database tidak tersedia");
                return;
            }

            lblHutangBelum.setText(formatCurrency(sumDebt(conn, "hutang", "belum")));
            lblHutangLunas.setText(formatCurrency(sumDebt(conn, "hutang", "lunas")));
            lblTotalHutang.setText(formatCurrency(sumDebtTotal(conn, "hutang")));

            lblPiutangBelum.setText(formatCurrency(sumDebt(conn, "piutang", "belum")));
            lblPiutangLunas.setText(formatCurrency(sumDebt(conn, "piutang", "lunas")));
            lblTotalPiutang.setText(formatCurrency(sumDebtTotal(conn, "piutang")));

            loadDebtList(conn, "hutang", containerHutangBelum);
            loadDebtList(conn, "piutang", containerPiutangBelum);
        } catch (SQLException e) {
            e.printStackTrace();
            setDebtFallback();
        }
    }

    private double sumDebt(Connection conn, String tipe, String status) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(nominal), 0)
                FROM debts
                WHERE tipe = ? AND status = ? AND tanggal BETWEEN ? AND ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipe);
            ps.setString(2, status);
            ps.setString(3, dpMulai.getValue().toString());
            ps.setString(4, dpAkhir.getValue().toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }

    private double sumDebtTotal(Connection conn, String tipe) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(nominal), 0)
                FROM debts
                WHERE tipe = ? AND tanggal BETWEEN ? AND ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipe);
            ps.setString(2, dpMulai.getValue().toString());
            ps.setString(3, dpAkhir.getValue().toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }

    private void loadDebtList(Connection conn, String tipe, VBox target) throws SQLException {
        String sql = """
                SELECT nama, nominal, tanggal, keterangan
                FROM debts
                WHERE tipe = ? AND status = 'belum' AND tanggal BETWEEN ? AND ?
                ORDER BY tanggal DESC, id_debt DESC
                """;

        target.getChildren().add(createDebtHeader());

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipe);
            ps.setString(2, dpMulai.getValue().toString());
            ps.setString(3, dpAkhir.getValue().toString());

            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    target.getChildren().add(createDebtRow(
                            rs.getString("nama"),
                            rs.getDouble("nominal"),
                            readDate(rs, "tanggal").format(DateTimeFormatter.ofPattern("dd MMM", localeId)),
                            rs.getString("keterangan")
                    ));
                }

                if (!found) {
                    clear(target);
                    showEmpty(target, "Tidak ada data " + tipe + " belum lunas");
                }
            }
        }
    }

    private PreparedStatement preparePeriod(Connection conn, String sql) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, dpMulai.getValue().toString());
        ps.setString(2, dpAkhir.getValue().toString());
        return ps;
    }

    private HBox createSalesMenuRow(String nama, int qty, double total) {
        HBox row = createReportRow();
        row.getChildren().addAll(label(nama, "report-row-title", true),
                label("Terjual " + qty, "report-row-muted", false),
                spacer(),
                label(formatCurrency(total), "report-row-money", false));
        return row;
    }

    private HBox createTransactionRow(String items, String tanggal, String metode, double total) {
        HBox row = createReportRow();

        VBox left = new VBox(4);
        HBox.setHgrow(left, Priority.ALWAYS);
        left.getChildren().addAll(label(items, "report-row-title", true),
                label(tanggal + " - " + metode, "report-row-muted", false));

        row.getChildren().addAll(left, label(formatCurrency(total), "report-row-money", false));
        return row;
    }

    private HBox createStockMiniRow(String nama, String jumlah, boolean warning) {
        HBox row = createReportRow();
        row.getStyleClass().add(warning ? "report-stock-warning-row" : "report-stock-safe-row");
        row.getChildren().addAll(label(nama, "report-row-title", true),
                spacer(),
                label(jumlah, warning ? "report-row-danger" : "report-row-success", false));
        return row;
    }

    private HBox createStockHeader() {
        HBox row = createReportHeader();
        row.getChildren().addAll(
                columnLabel("Nama Barang", 2),
                columnLabel("Satuan", 1),
                columnLabel("Jumlah", 1),
                columnLabel("Min", 1),
                columnLabel("Status", 1)
        );
        return row;
    }

    private HBox createStockDetailRow(String nama, String satuan, int jumlah, int min, String status) {
        HBox row = createReportTableRow();
        row.getChildren().addAll(
                columnValue(nama, 2),
                columnValue(satuan, 1),
                columnValue(String.valueOf(jumlah), 1),
                columnValue(String.valueOf(min), 1),
                statusBadge(status)
        );
        return row;
    }

    private HBox createDebtHeader() {
        HBox row = createReportHeader();
        row.getChildren().addAll(
                columnLabel("Nama", 2),
                columnLabel("Nominal", 1),
                columnLabel("Tanggal", 1),
                columnLabel("Keterangan", 2)
        );
        return row;
    }

    private HBox createDebtRow(String nama, double nominal, String tanggal, String keterangan) {
        HBox row = createReportTableRow();
        row.getChildren().addAll(
                columnValue(nama, 2),
                columnMoney(formatCurrency(nominal), 1),
                columnValue(tanggal, 1),
                columnValue(keterangan == null || keterangan.isBlank() ? "-" : keterangan, 2)
        );
        return row;
    }

    private HBox createReportRow() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 14, 12, 14));
        row.getStyleClass().add("report-list-row");
        return row;
    }

    private HBox createReportHeader() {
        HBox row = new HBox(12);
        row.setPadding(new Insets(0, 8, 10, 8));
        row.getStyleClass().add("report-table-header");
        return row;
    }

    private HBox createReportTableRow() {
        HBox row = new HBox(12);
        row.setPadding(new Insets(10, 8, 10, 8));
        row.getStyleClass().add("report-table-row");
        return row;
    }

    private Label columnLabel(String text, int grow) {
        Label label = label(text, "report-column-title", true);
        HBox.setHgrow(label, Priority.ALWAYS);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setPrefWidth(120 * grow);
        return label;
    }

    private Label columnValue(String text, int grow) {
        Label label = label(text, "report-column-text", false);
        HBox.setHgrow(label, Priority.ALWAYS);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setPrefWidth(120 * grow);
        return label;
    }

    private Label columnMoney(String text, int grow) {
        Label label = columnValue(text, grow);
        label.getStyleClass().add("report-row-danger");
        return label;
    }

    private Label statusBadge(String status) {
        Label label = new Label(status);
        label.getStyleClass().add("report-status-badge");
        if ("Aman".equals(status)) {
            label.getStyleClass().add("report-status-safe");
        } else {
            label.getStyleClass().add("report-status-warning");
        }
        label.setPrefWidth(120);
        return label;
    }

    private Label label(String text, String styleClass, boolean wrap) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setWrapText(wrap);
        return label;
    }

    private Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private void showEmpty(VBox target, String message) {
        target.getChildren().add(createEmptyState(message));
    }

    private VBox createEmptyState(String text) {
        VBox empty = new VBox();
        empty.setAlignment(Pos.CENTER);
        empty.setPadding(new Insets(48, 20, 48, 20));
        empty.getStyleClass().add("report-empty-state");

        Label label = new Label(text);
        label.getStyleClass().add("report-empty-text");
        empty.getChildren().add(label);
        return empty;
    }

    private void setDebtFallback() {
        lblHutangBelum.setText("Rp 0");
        lblHutangLunas.setText("Rp 0");
        lblTotalHutang.setText("Rp 0");
        lblPiutangBelum.setText("Rp 0");
        lblPiutangLunas.setText("Rp 0");
        lblTotalPiutang.setText("Rp 0");
    }

    private void clear(VBox container) {
        if (container != null) {
            container.getChildren().clear();
        }
    }

    private void setVisible(VBox panel, boolean visible) {
        panel.setVisible(visible);
        panel.setManaged(visible);
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(localeId);
        formatter.setMaximumFractionDigits(0);
        return "Rp " + formatter.format(amount);
    }

    private TransactionSummary loadTransactionSummary(Connection conn, int idTransaksi) throws SQLException {
        String sql = """
                SELECT m.nama_menu, td.metode_pembayaran
                FROM transaction_detail td
                JOIN menus m ON td.id_menu = m.id_menu
                WHERE td.id_transaksi = ?
                ORDER BY td.id_detail ASC
                """;

        StringBuilder items = new StringBuilder();
        String metode = "-";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTransaksi);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!items.isEmpty()) {
                        items.append(", ");
                    }
                    items.append(rs.getString("nama_menu"));
                    if ("-".equals(metode)) {
                        metode = rs.getString("metode_pembayaran");
                    }
                }
            }
        }

        if (items.isEmpty()) {
            items.append("Transaksi #").append(idTransaksi);
        }

        return new TransactionSummary(items.toString(), metode);
    }

    private LocalDate readDate(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }

    private LocalDateTime readDateTime(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(value.replace(" ", "T"));
        } catch (DateTimeParseException ignored) {
        }

        java.sql.Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private String formatMetode(String metode) {
        if (metode == null) return "-";
        return switch (metode.toLowerCase()) {
            case "cash" -> "Tunai";
            case "qris" -> "QRIS";
            default -> metode;
        };
    }

    private record TransactionSummary(String items, String metode) {}
}
