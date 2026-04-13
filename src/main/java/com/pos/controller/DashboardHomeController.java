package com.pos.controller;

import com.pos.config.koneksi;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class DashboardHomeController implements Initializable {

    @FXML private Label lblTotalPenjualan;
    @FXML private Label lblJumlahTransaksi;
    @FXML private Label lblHutang;
    @FXML private Label lblPiutang;
    @FXML private VBox containerStokWarning;
    @FXML private VBox containerTransaksiBaru;

    private final Locale localeId = new Locale("id", "ID");
    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", new Locale("id", "ID"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadStats();
    }

    private void loadStats() {
        Connection conn = koneksi.getConnection();
        if (conn == null) {
            setFallbackStats();
            renderEmptyPanels();
            return;
        }

        try (conn) {
            setLabelText(lblTotalPenjualan, formatCurrency(queryDouble(conn,
                    "SELECT COALESCE(SUM(total), 0) FROM transactions")));
            setLabelText(lblJumlahTransaksi, String.valueOf(queryInt(conn,
                    "SELECT COUNT(*) FROM transactions")));
            setLabelText(lblHutang, formatCurrency(queryDouble(conn,
                    "SELECT COALESCE(SUM(nominal), 0) FROM debts WHERE tipe='hutang' AND status='belum'")));
            setLabelText(lblPiutang, formatCurrency(queryDouble(conn,
                    "SELECT COALESCE(SUM(nominal), 0) FROM debts WHERE tipe='piutang' AND status='belum'")));

            renderStockWarnings(conn);
            renderRecentTransactions(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            setFallbackStats();
            renderEmptyPanels();
        } catch (Exception e) {
            e.printStackTrace();
            setFallbackStats();
            renderEmptyPanels();
        }
    }

    private void renderStockWarnings(Connection conn) throws SQLException {
        if (containerStokWarning == null) {
            return;
        }

        containerStokWarning.getChildren().clear();

        String sql = """
                SELECT m.nama_menu, s.jumlah_stok, s.stok_minimum, s.satuan
                FROM stock s
                JOIN menus m ON s.id_menu = m.id_menu
                WHERE s.jumlah_stok <= s.stok_minimum
                ORDER BY s.jumlah_stok ASC, m.nama_menu ASC
                LIMIT 3
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean found = false;
            while (rs.next()) {
                found = true;
                containerStokWarning.getChildren().add(createStockWarningCard(
                        rs.getString("nama_menu"),
                        rs.getInt("jumlah_stok"),
                        rs.getInt("stok_minimum"),
                        rs.getString("satuan")
                ));
            }

            if (!found) {
                containerStokWarning.getChildren().add(createEmptyState("Tidak ada stok menipis"));
            }
        }
    }

    private void renderRecentTransactions(Connection conn) throws SQLException {
        if (containerTransaksiBaru == null) {
            return;
        }

        containerTransaksiBaru.getChildren().clear();

        String sql = """
                SELECT id_transaksi, tanggal, total
                FROM transactions
                ORDER BY tanggal DESC, id_transaksi DESC
                LIMIT 3
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean found = false;
            while (rs.next()) {
                found = true;
                int idTransaksi = rs.getInt("id_transaksi");
                String tanggal = rs.getTimestamp("tanggal")
                        .toLocalDateTime()
                        .format(dateFormatter);
                double total = rs.getDouble("total");
                TransactionSnapshot snapshot = loadTransactionSnapshot(conn, idTransaksi);
                containerTransaksiBaru.getChildren().add(createTransactionCard(
                        snapshot.title,
                        tanggal,
                        total,
                        snapshot.paymentMethod
                ));
            }

            if (!found) {
                containerTransaksiBaru.getChildren().add(createEmptyState("Belum ada transaksi"));
            }
        }
    }

    private TransactionSnapshot loadTransactionSnapshot(Connection conn, int idTransaksi) throws SQLException {
        String sql = """
                SELECT m.nama_menu, td.metode_pembayaran
                FROM transaction_detail td
                JOIN menus m ON td.id_menu = m.id_menu
                WHERE td.id_transaksi = ?
                ORDER BY td.id_detail ASC
                """;

        List<String> itemNames = new ArrayList<>();
        String paymentMethod = "-";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTransaksi);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    itemNames.add(rs.getString("nama_menu"));
                    if ("-".equals(paymentMethod)) {
                        paymentMethod = rs.getString("metode_pembayaran");
                    }
                }
            }
        }

        if (itemNames.isEmpty()) {
            itemNames.add("Transaksi #" + idTransaksi);
        }

        return new TransactionSnapshot(String.join(", ", itemNames), formatMetode(paymentMethod));
    }

    private VBox createStockWarningCard(String namaMenu, int stok, int minimum, String satuan) {
        VBox card = new VBox(8);
        card.getStyleClass().add("warning-item-card");
        card.setPadding(new Insets(14, 16, 14, 16));

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(namaMenu);
        name.getStyleClass().add("warning-item-name");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label pill = new Label("Sisa: " + stok + " " + satuan + " / Min: " + minimum);
        pill.getStyleClass().add("warning-item-pill");

        row.getChildren().addAll(name, spacer, pill);
        card.getChildren().add(row);
        return card;
    }

    private VBox createTransactionCard(String title, String tanggal, double total, String paymentMethod) {
        VBox card = new VBox(10);
        card.getStyleClass().add("transaction-row-card");
        card.setPadding(new Insets(16, 18, 16, 18));

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox left = new VBox(4);
        left.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(left, Priority.ALWAYS);

        Label name = new Label(title);
        name.getStyleClass().add("transaction-row-name");
        name.setWrapText(true);

        Label date = new Label(tanggal);
        date.getStyleClass().add("transaction-row-date");

        left.getChildren().addAll(name, date);

        VBox right = new VBox(4);
        right.setAlignment(Pos.CENTER_RIGHT);

        Label amount = new Label(formatCurrency(total));
        amount.getStyleClass().add("transaction-row-total");

        Label pill = new Label(paymentMethod);
        pill.getStyleClass().add("transaction-row-pill");

        right.getChildren().addAll(amount, pill);
        topRow.getChildren().addAll(left, right);

        card.getChildren().add(topRow);
        return card;
    }

    private VBox createEmptyState(String text) {
        VBox empty = new VBox();
        empty.setAlignment(Pos.CENTER);
        empty.setPadding(new Insets(24, 20, 24, 20));
        empty.getStyleClass().add("dashboard-empty-state");

        Label label = new Label(text);
        label.getStyleClass().add("dashboard-empty-text");
        empty.getChildren().add(label);
        return empty;
    }

    private double queryDouble(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    private int queryInt(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void renderEmptyPanels() {
        if (containerStokWarning != null) {
            containerStokWarning.getChildren().setAll(createEmptyState("Tidak ada stok menipis"));
        }
        if (containerTransaksiBaru != null) {
            containerTransaksiBaru.getChildren().setAll(createEmptyState("Belum ada transaksi"));
        }
    }

    private void setFallbackStats() {
        setLabelText(lblTotalPenjualan, "Rp 0");
        setLabelText(lblJumlahTransaksi, "0");
        setLabelText(lblHutang, "Rp 0");
        setLabelText(lblPiutang, "Rp 0");
    }

    private String formatCurrency(double amount) {
        return "Rp " + String.format(localeId, "%,.0f", amount);
    }

    private String formatMetode(String metode) {
        if (metode == null) {
            return "-";
        }
        return switch (metode.toLowerCase()) {
            case "cash" -> "Tunai";
            case "qris" -> "QRIS";
            default -> metode;
        };
    }

    private void setLabelText(Label label, String text) {
        if (label != null) {
            label.setText(text);
        }
    }

    private record TransactionSnapshot(String title, String paymentMethod) {}
}
