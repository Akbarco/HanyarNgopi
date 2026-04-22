package com.pos.controller;

import com.pos.config.koneksi;
import com.pos.util.AlertUtil;
import com.pos.util.ToastUtil;
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
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

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
    private final DateTimeFormatter fileTimestampFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
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

    @FXML
    private void handleBackupData() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Simpan Backup Database");
        chooser.setInitialFileName("backup-hanyarngopi-" +
                LocalDateTime.now().format(fileTimestampFormatter) + ".db");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("SQLite Database (*.db)", "*.db")
        );

        File target = chooser.showSaveDialog(dpMulai.getScene().getWindow());
        if (target == null) {
            return;
        }

        try {
            Path source = koneksi.getDatabasePath();
            if (!Files.exists(source)) {
                AlertUtil.showError("Backup Gagal", "File database tidak ditemukan.");
                return;
            }

            Files.copy(source, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            ToastUtil.showSuccess(dpMulai, "Backup data berhasil dibuat.");
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Backup Gagal", "Gagal membuat backup data.");
        }
    }

    @FXML
    private void handleExportExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Laporan Excel");
        chooser.setInitialFileName("laporan-lengkap-" +
                LocalDateTime.now().format(fileTimestampFormatter) + ".xlsx");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Workbook (*.xlsx)", "*.xlsx")
        );

        File target = chooser.showSaveDialog(dpMulai.getScene().getWindow());
        if (target == null) {
            return;
        }

        try (Connection conn = koneksi.getConnection()) {
            if (conn == null) {
                AlertUtil.showError("Export Gagal", "Koneksi database tidak tersedia.");
                return;
            }

            ReportData report = buildFullReportData(conn);
            writeExcelReport(target.toPath(), report);
            ToastUtil.showSuccess(dpMulai, "Laporan Excel berhasil dibuat.");
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            AlertUtil.showError("Export Gagal", "Gagal export Excel.");
        }
    }

    @FXML
    private void handleExportPdf() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Laporan PDF");
        chooser.setInitialFileName("laporan-lengkap-" +
                LocalDateTime.now().format(fileTimestampFormatter) + ".pdf");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf")
        );

        File target = chooser.showSaveDialog(dpMulai.getScene().getWindow());
        if (target == null) {
            return;
        }

        try (Connection conn = koneksi.getConnection()) {
            if (conn == null) {
                AlertUtil.showError("Export Gagal", "Koneksi database tidak tersedia.");
                return;
            }

            ReportData report = buildFullReportData(conn);
            writePdfReport(target.toPath(), report);
            ToastUtil.showSuccess(dpMulai, "Laporan PDF berhasil dibuat.");
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            AlertUtil.showError("Export Gagal", "Gagal export PDF.");
        }
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
                SELECT COALESCE(td.nama_menu_snapshot, m.nama_menu) AS nama_menu,
                       SUM(td.qty) AS total_qty,
                       COALESCE(SUM(td.subtotal), 0) AS total_nominal
                FROM transaction_detail td
                JOIN transactions t ON td.id_transaksi = t.id_transaksi
                LEFT JOIN menus m ON td.id_menu = m.id_menu
                WHERE date(t.tanggal) BETWEEN ? AND ?
                GROUP BY COALESCE(td.nama_menu_snapshot, m.nama_menu)
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
                WHERE m.is_active = 1 AND %s
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
                WHERE m.is_active = 1
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

    private String buildExportCsv(Connection conn) throws SQLException {
        return switch (activeTab) {
            case "stok" -> buildStockCsv(conn);
            case "hutang" -> buildDebtCsv(conn);
            default -> buildSalesCsv(conn);
        };
    }

    private ReportData buildReportData(Connection conn) throws SQLException {
        String title = switch (activeTab) {
            case "stok" -> "Laporan Stok";
            case "hutang" -> "Laporan Hutang Piutang";
            default -> "Laporan Penjualan";
        };
        String period = dpMulai.getValue().toString() + " - " + dpAkhir.getValue().toString();
        return new ReportData(title, period, parseCsv(buildExportCsv(conn)));
    }

    private ReportData buildFullReportData(Connection conn) throws SQLException {
        String period = dpMulai.getValue().toString() + " - " + dpAkhir.getValue().toString();
        return new ReportData("Laporan Lengkap", period, parseCsv(buildFullExportCsv(conn)));
    }

    private String buildFullExportCsv(Connection conn) throws SQLException {
        StringBuilder csv = new StringBuilder();
        appendReportTitle(csv, "Laporan Lengkap");

        appendCsvContentWithoutHeader(csv, buildSalesCsv(conn));
        csv.append(System.lineSeparator());

        appendCsvRow(csv, "Detail Stok");
        appendCsvContentWithoutHeader(csv, buildStockCsv(conn));
        csv.append(System.lineSeparator());

        appendCsvContentWithoutHeader(csv, buildDebtCsv(conn));
        return csv.toString();
    }

    private void appendCsvContentWithoutHeader(StringBuilder target, String csv) {
        List<String[]> rows = parseCsv(csv);
        int index = 2;
        while (index < rows.size() && isBlankPdfRow(rows.get(index))) {
            index++;
        }

        for (int i = index; i < rows.size(); i++) {
            appendCsvRow(target, rows.get(i));
        }
    }

    private void writeExcelReport(Path target, ReportData report) throws IOException {
        List<String[]> rows = report.rows();
        int maxColumns = rows.stream().mapToInt(row -> Math.max(1, row.length)).max().orElse(1);

        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(target))) {
            zipEntry(zip, "[Content_Types].xml", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                      <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                      <Default Extension="xml" ContentType="application/xml"/>
                      <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                      <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                      <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
                    </Types>
                    """);
            zipEntry(zip, "_rels/.rels", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                      <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
                    </Relationships>
                    """);
            zipEntry(zip, "xl/_rels/workbook.xml.rels", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                      <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
                      <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
                    </Relationships>
                    """);
            zipEntry(zip, "xl/workbook.xml", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                      <sheets>
                        <sheet name="Laporan" sheetId="1" r:id="rId1"/>
                      </sheets>
                    </workbook>
                    """);
            zipEntry(zip, "xl/styles.xml", excelStylesXml());
            zipEntry(zip, "xl/worksheets/sheet1.xml", excelSheetXml(rows, maxColumns));
        }
    }

    private String excelSheetXml(List<String[]> rows, int maxColumns) {
        StringBuilder xml = new StringBuilder();
        List<String> mergedCells = new ArrayList<>();
        xml.append("""
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                  <sheetViews><sheetView workbookViewId="0"><pane ySplit="3" topLeftCell="A4" activePane="bottomLeft" state="frozen"/></sheetView></sheetViews>
                  <cols>
                """);
        for (int i = 1; i <= maxColumns; i++) {
            double width = switch (i) {
                case 1 -> 28;
                case 2 -> 20;
                case 3 -> 26;
                case 4 -> 18;
                default -> 22;
            };
            xml.append("<col min=\"").append(i).append("\" max=\"").append(i)
                    .append("\" width=\"").append(width).append("\" customWidth=\"1\"/>");
        }
        xml.append("</cols><sheetData>");

        for (int r = 0; r < rows.size(); r++) {
            String[] row = rows.get(r);
            int excelRow = r + 1;
            int style = excelStyleForRow(row, r);
            int span = excelRowSpan(rows, r, maxColumns);
            if ((r == 0 || r == 1 || (row.length == 1 && !row[0].isBlank())) && span > 1) {
                mergedCells.add("A" + excelRow + ":" + columnName(span) + excelRow);
            }
            xml.append("<row r=\"").append(excelRow).append("\" ht=\"")
                    .append(excelRowHeight(style)).append("\" customHeight=\"1\">");
            for (int c = 0; c < span; c++) {
                String cellRef = columnName(c + 1) + excelRow;
                xml.append("<c r=\"").append(cellRef).append("\" t=\"inlineStr\" s=\"")
                        .append(style).append("\"><is><t>")
                        .append(xmlEscape(excelCellDisplay(row, r, c)))
                        .append("</t></is></c>");
            }
            xml.append("</row>");
        }

        xml.append("</sheetData>");
        if (!mergedCells.isEmpty()) {
            xml.append("<mergeCells count=\"").append(mergedCells.size()).append("\">");
            for (String mergedCell : mergedCells) {
                xml.append("<mergeCell ref=\"").append(mergedCell).append("\"/>");
            }
            xml.append("</mergeCells>");
        }
        xml.append("""
                  <pageMargins left="0.5" right="0.5" top="0.7" bottom="0.7" header="0.3" footer="0.3"/>
                </worksheet>
                """);
        return xml.toString();
    }

    private int excelStyleForRow(String[] row, int index) {
        if (row.length == 0 || (row.length == 1 && row[0].isBlank())) {
            return 0;
        }
        if (index == 0) {
            return 1;
        }
        if (index == 1) {
            return 5;
        }
        if (row.length == 1 && !row[0].isBlank()) {
            return 2;
        }
        if (isTableHeader(row)) {
            return 3;
        }
        return 4;
    }

    private int excelRowHeight(int style) {
        return switch (style) {
            case 1 -> 32;
            case 2 -> 26;
            case 3 -> 24;
            case 5 -> 22;
            default -> 22;
        };
    }

    private int excelRowSpan(List<String[]> rows, int index, int maxColumns) {
        String[] row = rows.get(index);
        if (index == 0 || index == 1) {
            return maxColumns;
        }
        if (row.length == 0 || (row.length == 1 && row[0].isBlank())) {
            return 1;
        }
        if (row.length == 1) {
            for (int i = index + 1; i < rows.size(); i++) {
                String[] next = rows.get(i);
                if (next.length == 0 || (next.length == 1 && next[0].isBlank())) {
                    continue;
                }
                return Math.max(1, next.length);
            }
        }
        return Math.max(1, row.length);
    }

    private String excelCellDisplay(String[] row, int rowIndex, int columnIndex) {
        if (rowIndex == 1 && columnIndex == 0 && row.length > 1) {
            return "Periode: " + row[1];
        }
        if (columnIndex >= row.length || (rowIndex == 1 && columnIndex > 0)) {
            return "";
        }
        if (isTableHeader(row) || row.length == 1) {
            return row[columnIndex];
        }
        return formatPdfCell(row, columnIndex);
    }

    private boolean isTableHeader(String[] row) {
        if (row.length < 2) {
            return false;
        }
        String first = row[0].toLowerCase(localeId);
        return first.equals("menu") || first.equals("id") || first.equals("nama barang")
                || first.equals("jenis") || first.equals("nama");
    }

    private String excelStylesXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                  <fonts count="5">
                    <font><sz val="11"/><color rgb="FF111827"/><name val="Calibri"/></font>
                    <font><b/><sz val="20"/><color rgb="FF111827"/><name val="Calibri"/></font>
                    <font><b/><sz val="12"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font>
                    <font><b/><sz val="12"/><color rgb="FF1E1B4B"/><name val="Calibri"/></font>
                    <font><sz val="10"/><color rgb="FF64748B"/><name val="Calibri"/></font>
                  </fonts>
                  <fills count="6">
                    <fill><patternFill patternType="none"/></fill>
                    <fill><patternFill patternType="gray125"/></fill>
                    <fill><patternFill patternType="solid"><fgColor rgb="FFEEF2FF"/></patternFill></fill>
                    <fill><patternFill patternType="solid"><fgColor rgb="FF4F46E5"/></patternFill></fill>
                    <fill><patternFill patternType="solid"><fgColor rgb="FFF8FAFC"/></patternFill></fill>
                    <fill><patternFill patternType="solid"><fgColor rgb="FFFFFFFF"/></patternFill></fill>
                  </fills>
                  <borders count="2">
                    <border><left/><right/><top/><bottom/><diagonal/></border>
                    <border><left style="thin"><color rgb="FFE5E7EB"/></left><right style="thin"><color rgb="FFE5E7EB"/></right><top style="thin"><color rgb="FFE5E7EB"/></top><bottom style="thin"><color rgb="FFE5E7EB"/></bottom><diagonal/></border>
                  </borders>
                  <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
                  <cellXfs count="6">
                    <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
                    <xf numFmtId="0" fontId="1" fillId="5" borderId="0" xfId="0" applyFill="1" applyFont="1"/>
                    <xf numFmtId="0" fontId="3" fillId="2" borderId="1" xfId="0" applyFill="1" applyFont="1" applyBorder="1"/>
                    <xf numFmtId="0" fontId="2" fillId="3" borderId="1" xfId="0" applyFill="1" applyFont="1" applyBorder="1"/>
                    <xf numFmtId="0" fontId="0" fillId="4" borderId="1" xfId="0" applyFill="1" applyBorder="1"/>
                    <xf numFmtId="0" fontId="4" fillId="5" borderId="0" xfId="0" applyFill="1" applyFont="1"/>
                  </cellXfs>
                  <cellStyles count="1"><cellStyle name="Normal" xfId="0" builtinId="0"/></cellStyles>
                </styleSheet>
                """;
    }

    private void writePdfReport(Path target, ReportData report) throws IOException {
        PdfDocument pdf = new PdfDocument(loadPdfLogo());
        PageCursor cursor = newPdfPage(pdf, report);

        for (PdfBlock block : collectPdfBlocks(report.rows())) {
            double blockHeight = estimateRenderedPdfBlockHeight(block);
            if (blockHeight <= 430 && cursor.y() - blockHeight < 48) {
                cursor = newPdfPage(pdf, report);
            }

            cursor = block.summary()
                    ? renderPdfSummaryBlock(cursor, block)
                    : renderPdfTableBlock(pdf, report, cursor, block);
        }

        Files.write(target, pdf.toBytes());
    }

    private PageCursor newPdfPage(PdfDocument pdf, ReportData report) {
        PdfPage page = pdf.newPage();
        return new PageCursor(page, drawPdfHeader(page, report, pdf.logo()));
    }

    private double drawPdfHeader(PdfPage page, ReportData report, PdfLogo logo) {
        page.roundedRect(42, 502, 758, 58, 10, "111827");
        page.rect(42, 496, 758, 5, "4F46E5");
        page.text(64, 536, report.title(), 18, true, "FFFFFF");
        page.text(64, 516, "Periode: " + report.period(), 9, false, "CBD5E1");
        page.image(logo, 650, 515, 30);
        page.text(688, 536, "HanyarNgopi", 11, true, "FFFFFF");
        page.text(688, 518, "Business Report", 8, false, "CBD5E1");
        return 474;
    }

    private List<PdfBlock> collectPdfBlocks(List<String[]> rows) {
        List<PdfBlock> blocks = new ArrayList<>();
        List<String[]> summaryRows = new ArrayList<>();
        int index = 2;

        while (index < rows.size()) {
            String[] row = rows.get(index);
            if (isBlankPdfRow(row)) {
                index++;
                continue;
            }

            if (row.length == 1) {
                String title = row[0];
                index++;
                String[] header = null;
                if (index < rows.size() && isTableHeader(rows.get(index))) {
                    header = rows.get(index);
                    index++;
                }

                List<String[]> dataRows = new ArrayList<>();
                while (index < rows.size() && !isBlankPdfRow(rows.get(index)) && rows.get(index).length != 1) {
                    dataRows.add(rows.get(index));
                    index++;
                }
                blocks.add(new PdfBlock(title, header, dataRows, false));
                continue;
            }

            if (isTableHeader(row)) {
                String[] header = row;
                List<String[]> dataRows = new ArrayList<>();
                index++;
                while (index < rows.size() && !isBlankPdfRow(rows.get(index)) && rows.get(index).length != 1) {
                    dataRows.add(rows.get(index));
                    index++;
                }
                blocks.add(new PdfBlock(defaultPdfSectionTitle(header), header, dataRows, false));
                continue;
            }

            summaryRows.add(row);
            index++;
        }

        if (!summaryRows.isEmpty()) {
            blocks.add(0, new PdfBlock("Ringkasan", null, summaryRows, true));
        }

        return blocks;
    }

    private PageCursor renderPdfSummaryBlock(PageCursor cursor, PdfBlock block) {
        PdfPage page = cursor.page();
        double y = cursor.y();
        double cardWidth = 360;
        double cardHeight = 46;
        double gap = 18;

        page.text(54, y, block.title(), 13, true, "0F172A");
        y -= 14;

        for (int i = 0; i < block.rows().size(); i++) {
            String[] row = block.rows().get(i);
            double x = 50 + (i % 2) * (cardWidth + gap);
            if (i > 0 && i % 2 == 0) {
                y -= cardHeight + 12;
            }

            page.roundedRect(x, y - cardHeight, cardWidth, cardHeight, 10, "FFFFFF");
            page.strokeRect(x, y - cardHeight, cardWidth, cardHeight, "E2E8F0");
            page.text(x + 18, y - 17, row.length > 0 ? row[0] : "-", 8, true, "64748B");
            page.text(x + 18, y - 35, row.length > 1 ? formatPdfCell(row, 1) : "-", 14, true, "111827");
        }

        return new PageCursor(page, y - cardHeight - 18);
    }

    private PageCursor renderPdfTableBlock(PdfDocument pdf, ReportData report, PageCursor cursor, PdfBlock block) {
        PdfPage page = cursor.page();
        double y = cursor.y();
        String[] header = block.header();
        List<String[]> rows = block.rows();
        int columns = header != null ? header.length : rows.stream().mapToInt(row -> row.length).max().orElse(1);
        int rowIndex = 0;
        boolean empty = rows.isEmpty();

        while (rowIndex < rows.size() || empty) {
            double titleHeight = 20;
            double headerHeight = header == null ? 0 : 20;
            double rowHeight = 19;
            int remainingRows = empty ? 1 : rows.size() - rowIndex;
            int availableRows = (int) Math.floor((y - 52 - titleHeight - headerHeight) / rowHeight);

            if (availableRows < 1) {
                cursor = newPdfPage(pdf, report);
                page = cursor.page();
                y = cursor.y();
                continue;
            }

            int chunkSize = Math.min(remainingRows, availableRows);
            double tableHeight = headerHeight + (chunkSize * rowHeight);
            double sectionHeight = titleHeight + tableHeight;
            double totalHeight = sectionHeight + 12;

            if (totalHeight <= 430 && y - totalHeight < 48) {
                cursor = newPdfPage(pdf, report);
                page = cursor.page();
                y = cursor.y();
                continue;
            }

            page.roundedRect(50, y - sectionHeight, 742, sectionHeight, 8, "FFFFFF");
            page.strokeRect(50, y - sectionHeight, 742, sectionHeight, "CBD5E1");
            page.rect(50, y - titleHeight, 742, titleHeight, "EEF2FF");
            page.text(66, y - 13, rowIndex == 0 ? block.title() : block.title() + " (lanjutan)",
                    11, true, "1E1B4B");
            y -= titleHeight;

            if (header != null) {
                page.rect(50, y - headerHeight, 742, headerHeight, "4F46E5");
                drawPdfRowText(page, header, y - 13, columns, 8, true, "FFFFFF");
                y -= headerHeight;
            }

            for (int i = 0; i < chunkSize; i++) {
                String[] row = empty ? new String[]{"Tidak ada data"} : rows.get(rowIndex + i);
                String fill = i % 2 == 0 ? "FFFFFF" : "F8FAFC";
                page.rect(50, y - rowHeight, 742, rowHeight, fill);
                page.rect(50, y - rowHeight, 742, 0.6, "E2E8F0");
                drawPdfRowText(page, row, y - 13, columns, 7, false, "111827");
                y -= rowHeight;
            }

            rowIndex += chunkSize;
            empty = false;
            y -= 12;

            if (rowIndex < rows.size()) {
                cursor = newPdfPage(pdf, report);
                page = cursor.page();
                y = cursor.y();
            }
        }

        return new PageCursor(page, y);
    }

    private void drawPdfRowText(PdfPage page, String[] row, double baseline, int columns,
                                int fontSize, boolean bold, String color) {
        double[] widths = pdfColumnWidths(columns);
        double x = 66;
        for (int column = 0; column < columns; column++) {
            String text = column < row.length ? formatPdfCell(row, column) : "";
            int maxLength = Math.max(8, (int) (widths[column] / 5.6));
            page.text(x, baseline, truncate(text, maxLength), fontSize, bold, color);
            x += widths[column];
        }
    }

    private double estimateRenderedPdfBlockHeight(PdfBlock block) {
        if (block.summary()) {
            int rows = Math.max(1, (int) Math.ceil(block.rows().size() / 2.0));
            return 14 + (rows * 58) + 18;
        }

        int dataRows = Math.max(1, block.rows().size());
        return 20 + (block.header() == null ? 0 : 20) + (dataRows * 19) + 12;
    }

    private String defaultPdfSectionTitle(String[] header) {
        String first = header.length == 0 ? "" : header[0].toLowerCase(localeId);
        if (first.equals("nama barang")) {
            return "Detail Stok";
        }
        if (first.equals("id")) {
            return "Detail Transaksi";
        }
        if (first.equals("jenis")) {
            return "Ringkasan";
        }
        return "Detail Laporan";
    }

    private boolean isBlankPdfRow(String[] row) {
        if (row == null || row.length == 0) {
            return true;
        }
        return row.length == 1 && row[0].isBlank();
    }

    private PdfLogo loadPdfLogo() {
        try (InputStream stream = getClass().getResourceAsStream("/com/pos/view/image/logo.jpeg")) {
            if (stream == null) {
                return null;
            }
            byte[] bytes = stream.readAllBytes();
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                return null;
            }
            return new PdfLogo(bytes, image.getWidth(), image.getHeight());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private double[] pdfColumnWidths(int columnCount) {
        if (columnCount == 2) {
            return new double[]{357, 357};
        }
        if (columnCount == 3) {
            return new double[]{300, 160, 254};
        }
        if (columnCount == 4) {
            return new double[]{230, 150, 150, 184};
        }
        if (columnCount == 5) {
            return new double[]{84, 145, 215, 130, 140};
        }

        double[] widths = new double[columnCount];
        double width = 714.0 / columnCount;
        for (int i = 0; i < columnCount; i++) {
            widths[i] = width;
        }
        return widths;
    }

    private String formatPdfCell(String[] row, int columnIndex) {
        if (columnIndex >= row.length) {
            return "";
        }
        String value = row[columnIndex];
        String header = row.length > columnIndex ? row[columnIndex] : "";
        String first = row.length > 0 ? row[0].toLowerCase(localeId) : "";

        if (row.length == 2 && first.contains("penjualan") && columnIndex == 1) {
            return formatRupiahValue(value);
        }
        if (row.length == 3 && columnIndex == 2) {
            return formatRupiahValue(value);
        }
        if (row.length == 4 && (first.equals("hutang") || first.equals("piutang")) && columnIndex > 0) {
            return formatRupiahValue(value);
        }
        if (row.length == 4 && columnIndex == 1 && isNumeric(value)) {
            return formatRupiahValue(value);
        }
        if (row.length == 5 && columnIndex == 4 && isNumeric(value)) {
            return formatRupiahValue(value);
        }
        if (header.toLowerCase(localeId).contains("nominal") && isNumeric(value)) {
            return formatRupiahValue(value);
        }
        return value;
    }

    private boolean isNumeric(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            Long.parseLong(value.replaceAll("[^0-9-]", ""));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String formatRupiahValue(String value) {
        if (value == null || value.isBlank()) {
            return "Rp 0";
        }
        try {
            String digits = value.replaceAll("[^0-9-]", "");
            long amount = Long.parseLong(digits);
            NumberFormat formatter = NumberFormat.getNumberInstance(localeId);
            formatter.setMaximumFractionDigits(0);
            formatter.setMinimumFractionDigits(0);
            return "Rp " + formatter.format(amount);
        } catch (NumberFormatException e) {
            return value;
        }
    }

    private List<String[]> parseCsv(String csv) {
        List<String[]> rows = new ArrayList<>();
        for (String line : csv.split("\\R", -1)) {
            rows.add(parseCsvLine(line));
        }
        return rows;
    }

    private String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (quoted) {
                if (ch == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else if (ch == '"') {
                    quoted = false;
                } else {
                    current.append(ch);
                }
            } else if (ch == '"') {
                quoted = true;
            } else if (ch == ',') {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        values.add(current.toString());
        return values.toArray(String[]::new);
    }

    private void zipEntry(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private String columnName(int column) {
        StringBuilder name = new StringBuilder();
        while (column > 0) {
            int rem = (column - 1) % 26;
            name.insert(0, (char) ('A' + rem));
            column = (column - 1) / 26;
        }
        return name.toString();
    }

    private String xmlEscape(String value) {
        return value == null ? "" : value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, Math.max(0, max - 1)) + "...";
    }

    private record PdfBlock(String title, String[] header, List<String[]> rows, boolean summary) {}

    private record PageCursor(PdfPage page, double y) {}

    private record PdfLogo(byte[] bytes, int width, int height) {}

    private static final class PdfDocument {
        private final List<PdfPage> pages = new ArrayList<>();
        private final PdfLogo logo;

        private PdfDocument(PdfLogo logo) {
            this.logo = logo;
        }

        private PdfPage newPage() {
            PdfPage page = new PdfPage();
            page.rect(0, 0, 842, 595, "F8FAFC");
            pages.add(page);
            return page;
        }

        private PdfLogo logo() {
            return logo;
        }

        private byte[] toBytes() throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            List<byte[]> objects = new ArrayList<>();
            int pageCount = pages.size();
            int regularFontObject = 3;
            int boldFontObject = 4;
            int extStateObject = 5;
            int logoObject = logo == null ? -1 : 6;
            int firstPageObject = logo == null ? 6 : 7;
            int firstContentObject = firstPageObject + pageCount;

            objects.add("""
                    << /Type /Catalog /Pages 2 0 R >>
                    """.getBytes(StandardCharsets.UTF_8));

            StringBuilder kids = new StringBuilder();
            for (int i = 0; i < pageCount; i++) {
                kids.append(firstPageObject + i).append(" 0 R ");
            }
            objects.add(("<< /Type /Pages /Kids [" + kids + "] /Count " + pageCount + " >>\n")
                    .getBytes(StandardCharsets.UTF_8));

            objects.add(("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\n")
                    .getBytes(StandardCharsets.UTF_8));

            objects.add(("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\n")
                    .getBytes(StandardCharsets.UTF_8));

            objects.add(("<< /Type /ExtGState /ca 0.30 /CA 0.30 >>\n")
                    .getBytes(StandardCharsets.UTF_8));

            if (logo != null) {
                ByteArrayOutputStream imageObject = new ByteArrayOutputStream();
                String header = "<< /Type /XObject /Subtype /Image /Width " + logo.width() +
                        " /Height " + logo.height() +
                        " /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length " +
                        logo.bytes().length + " >>\nstream\n";
                imageObject.write(header.getBytes(StandardCharsets.UTF_8));
                imageObject.write(logo.bytes());
                imageObject.write("\nendstream\n".getBytes(StandardCharsets.UTF_8));
                objects.add(imageObject.toByteArray());
            }

            for (int i = 0; i < pageCount; i++) {
                int contentObject = firstContentObject + i;
                String xObject = logo == null ? "" : " /XObject << /Im1 " + logoObject + " 0 R >>";
                String pageObject = "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 842 595] " +
                        "/Resources << /Font << /F1 " + regularFontObject + " 0 R /F2 " + boldFontObject + " 0 R >> " +
                        "/ExtGState << /GS1 " + extStateObject + " 0 R >>" + xObject + " >> " +
                        "/Contents " + contentObject + " 0 R >>\n";
                objects.add(pageObject.getBytes(StandardCharsets.UTF_8));
            }

            for (PdfPage page : pages) {
                byte[] content = page.content().getBytes(StandardCharsets.UTF_8);
                String header = "<< /Length " + content.length + " >>\nstream\n";
                String footer = "\nendstream\n";
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                stream.write(header.getBytes(StandardCharsets.UTF_8));
                stream.write(content);
                stream.write(footer.getBytes(StandardCharsets.UTF_8));
                objects.add(stream.toByteArray());
            }

            out.write("%PDF-1.4\n".getBytes(StandardCharsets.UTF_8));
            List<Integer> offsets = new ArrayList<>();
            offsets.add(0);
            for (int i = 0; i < objects.size(); i++) {
                offsets.add(out.size());
                out.write((i + 1 + " 0 obj\n").getBytes(StandardCharsets.UTF_8));
                out.write(objects.get(i));
                out.write("endobj\n".getBytes(StandardCharsets.UTF_8));
            }

            int xrefOffset = out.size();
            out.write(("xref\n0 " + (objects.size() + 1) + "\n").getBytes(StandardCharsets.UTF_8));
            out.write("0000000000 65535 f \n".getBytes(StandardCharsets.UTF_8));
            for (int i = 1; i < offsets.size(); i++) {
                out.write(String.format("%010d 00000 n \n", offsets.get(i)).getBytes(StandardCharsets.UTF_8));
            }
            out.write(("trailer\n<< /Size " + (objects.size() + 1) + " /Root 1 0 R >>\nstartxref\n" +
                    xrefOffset + "\n%%EOF").getBytes(StandardCharsets.UTF_8));
            return out.toByteArray();
        }
    }

    private static final class PdfPage {
        private final StringBuilder content = new StringBuilder();

        private void text(double x, double y, String value, int size, boolean bold) {
            text(x, y, value, size, bold, "000000");
        }

        private void text(double x, double y, String value, int size, boolean bold, String color) {
            double[] rgb = rgb(color);
            content.append("BT /").append(bold ? "F2" : "F1").append(' ').append(size)
                    .append(" Tf ").append(format(rgb[0])).append(' ').append(format(rgb[1]))
                    .append(' ').append(format(rgb[2])).append(" rg ")
                    .append(format(x)).append(' ').append(format(y)).append(" Td (")
                    .append(pdfEscape(value)).append(") Tj ET\n");
        }

        private void rect(double x, double y, double width, double height, String color) {
            double[] rgb = rgb(color);
            content.append(format(rgb[0])).append(' ').append(format(rgb[1])).append(' ')
                    .append(format(rgb[2])).append(" rg ")
                    .append(format(x)).append(' ').append(format(y)).append(' ')
                    .append(format(width)).append(' ').append(format(height)).append(" re f\n");
        }

        private void roundedRect(double x, double y, double width, double height, double radius, String color) {
            // Keep the PDF generator compact: rectangle fill with the same spacing/color language.
            rect(x, y, width, height, color);
        }

        private void strokeRect(double x, double y, double width, double height, String color) {
            double[] rgb = rgb(color);
            content.append(format(rgb[0])).append(' ').append(format(rgb[1])).append(' ')
                    .append(format(rgb[2])).append(" RG 0.8 w ")
                    .append(format(x)).append(' ').append(format(y)).append(' ')
                    .append(format(width)).append(' ').append(format(height)).append(" re S\n");
        }

        private void image(PdfLogo logo, double x, double y, double width) {
            if (logo == null) {
                return;
            }
            double height = width * logo.height() / logo.width();
            content.append("q ")
                    .append(format(width)).append(" 0 0 ").append(format(height)).append(' ')
                    .append(format(x)).append(' ').append(format(y)).append(" cm /Im1 Do Q\n");
        }

        private String content() {
            return content.toString();
        }

        private static double[] rgb(String hex) {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return new double[]{r / 255.0, g / 255.0, b / 255.0};
        }

        private static String pdfEscape(String value) {
            if (value == null) {
                return "";
            }
            return value
                    .replace("\\", "\\\\")
                    .replace("(", "\\(")
                    .replace(")", "\\)")
                    .replace("\r", " ")
                    .replace("\n", " ");
        }

        private static String format(double value) {
            return String.format(Locale.US, "%.2f", value);
        }
    }

    private String buildSalesCsv(Connection conn) throws SQLException {
        StringBuilder csv = new StringBuilder();
        appendReportTitle(csv, "Laporan Penjualan");

        String summarySql = """
                SELECT COALESCE(SUM(total), 0) AS total_penjualan,
                       COUNT(*) AS jumlah_transaksi
                FROM transactions
                WHERE date(tanggal) BETWEEN ? AND ?
                """;
        try (PreparedStatement ps = preparePeriod(conn, summarySql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                appendCsvRow(csv, "Total Penjualan", String.valueOf((int) Math.round(rs.getDouble("total_penjualan"))));
                appendCsvRow(csv, "Jumlah Transaksi", String.valueOf(rs.getInt("jumlah_transaksi")));
                csv.append(System.lineSeparator());
            }
        }

        appendCsvRow(csv, "Penjualan per Menu");
        appendCsvRow(csv, "Menu", "Total Qty", "Total Nominal");
        String perMenuSql = """
                SELECT COALESCE(td.nama_menu_snapshot, m.nama_menu) AS nama_menu,
                       SUM(td.qty) AS total_qty,
                       COALESCE(SUM(td.subtotal), 0) AS total_nominal
                FROM transaction_detail td
                JOIN transactions t ON td.id_transaksi = t.id_transaksi
                LEFT JOIN menus m ON td.id_menu = m.id_menu
                WHERE date(t.tanggal) BETWEEN ? AND ?
                GROUP BY COALESCE(td.nama_menu_snapshot, m.nama_menu)
                ORDER BY total_nominal DESC, total_qty DESC
                """;
        try (PreparedStatement ps = preparePeriod(conn, perMenuSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                appendCsvRow(csv,
                        rs.getString("nama_menu"),
                        String.valueOf(rs.getInt("total_qty")),
                        String.valueOf((int) Math.round(rs.getDouble("total_nominal"))));
            }
        }

        csv.append(System.lineSeparator());
        appendCsvRow(csv, "Detail Transaksi");
        appendCsvRow(csv, "ID", "Tanggal", "Item", "Metode", "Total");
        String detailSql = """
                SELECT t.id_transaksi, t.tanggal, t.total
                FROM transactions t
                WHERE date(t.tanggal) BETWEEN ? AND ?
                ORDER BY t.tanggal DESC, t.id_transaksi DESC
                """;
        try (PreparedStatement ps = preparePeriod(conn, detailSql);
             ResultSet rs = ps.executeQuery()) {
            int displayId = 1;
            while (rs.next()) {
                TransactionSummary summary = loadTransactionSummary(conn, rs.getInt("id_transaksi"));
                LocalDateTime tanggal = readDateTime(rs, "tanggal");
                appendCsvRow(csv,
                        String.valueOf(displayId++),
                        tanggal == null ? "-" : tanggal.format(dateFormatter),
                        summary.items(),
                        formatMetode(summary.metode()),
                        String.valueOf((int) Math.round(rs.getDouble("total"))));
            }
        }

        return csv.toString();
    }

    private String buildStockCsv(Connection conn) throws SQLException {
        StringBuilder csv = new StringBuilder();
        appendReportTitle(csv, "Laporan Stok");
        appendCsvRow(csv, "Nama Barang", "Satuan", "Jumlah", "Minimum", "Status");

        String sql = """
                SELECT m.nama_menu, s.satuan, s.jumlah_stok, s.stok_minimum
                FROM stock s
                JOIN menus m ON s.id_menu = m.id_menu
                WHERE m.is_active = 1
                ORDER BY m.nama_menu ASC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int jumlah = rs.getInt("jumlah_stok");
                int minimum = rs.getInt("stok_minimum");
                appendCsvRow(csv,
                        rs.getString("nama_menu"),
                        rs.getString("satuan"),
                        String.valueOf(jumlah),
                        String.valueOf(minimum),
                        jumlah <= minimum ? "Menipis" : "Aman");
            }
        }

        return csv.toString();
    }

    private String buildDebtCsv(Connection conn) throws SQLException {
        StringBuilder csv = new StringBuilder();
        appendReportTitle(csv, "Laporan Hutang Piutang");

        appendCsvRow(csv, "Ringkasan Hutang/Piutang");
        appendCsvRow(csv, "Jenis", "Belum Lunas", "Sudah Lunas", "Total");
        appendCsvRow(csv, "Hutang",
                String.valueOf((int) Math.round(sumDebt(conn, "hutang", "belum"))),
                String.valueOf((int) Math.round(sumDebt(conn, "hutang", "lunas"))),
                String.valueOf((int) Math.round(sumDebtTotal(conn, "hutang"))));
        appendCsvRow(csv, "Piutang",
                String.valueOf((int) Math.round(sumDebt(conn, "piutang", "belum"))),
                String.valueOf((int) Math.round(sumDebt(conn, "piutang", "lunas"))),
                String.valueOf((int) Math.round(sumDebtTotal(conn, "piutang"))));
        csv.append(System.lineSeparator());

        appendDebtRows(csv, conn, "hutang");
        csv.append(System.lineSeparator());
        appendDebtRows(csv, conn, "piutang");

        return csv.toString();
    }

    private void appendDebtRows(StringBuilder csv, Connection conn, String tipe) throws SQLException {
        appendCsvRow(csv, capitalize(tipe) + " Belum Lunas");
        appendCsvRow(csv, "Nama", "Nominal", "Tanggal", "Keterangan");

        String sql = """
                SELECT nama, nominal, tanggal, keterangan
                FROM debts
                WHERE tipe = ? AND status = 'belum' AND tanggal BETWEEN ? AND ?
                ORDER BY tanggal DESC, id_debt DESC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipe);
            ps.setString(2, dpMulai.getValue().toString());
            ps.setString(3, dpAkhir.getValue().toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    appendCsvRow(csv,
                            rs.getString("nama"),
                            String.valueOf((int) Math.round(rs.getDouble("nominal"))),
                            rs.getString("tanggal"),
                            rs.getString("keterangan"));
                }
            }
        }
    }

    private void appendReportTitle(StringBuilder csv, String title) {
        appendCsvRow(csv, title);
        appendCsvRow(csv, "Periode", dpMulai.getValue().toString() + " - " + dpAkhir.getValue().toString());
        csv.append(System.lineSeparator());
    }

    private void appendCsvRow(StringBuilder csv, String... values) {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                csv.append(',');
            }
            csv.append(escapeCsv(values[i]));
        }
        csv.append(System.lineSeparator());
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
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
                SELECT COALESCE(td.nama_menu_snapshot, m.nama_menu) AS nama_menu,
                       td.metode_pembayaran
                FROM transaction_detail td
                LEFT JOIN menus m ON td.id_menu = m.id_menu
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

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.substring(0, 1).toUpperCase(localeId) + value.substring(1);
    }

    private record ReportData(String title, String period, List<String[]> rows) {}

    private record TransactionSummary(String items, String metode) {}
}
