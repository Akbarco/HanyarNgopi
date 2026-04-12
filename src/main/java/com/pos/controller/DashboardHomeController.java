package com.pos.controller;

import com.pos.config.koneksi;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class DashboardHomeController implements Initializable {

    @FXML private Label lblTotalPenjualan;
    @FXML private Label lblJumlahTransaksi;
    @FXML private Label lblHutang;
    @FXML private Label lblPiutang;
    @FXML private Label lblStokWarning;
    @FXML private Label lblTransaksiBaru;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadStats();
    }

    private void loadStats() {
        try (Connection conn = koneksi.getConnection()) {

            // Total penjualan
            PreparedStatement ps1 = conn.prepareStatement(
                    "SELECT COALESCE(SUM(total), 0) FROM transactions");
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next())
                lblTotalPenjualan.setText(
                        "Rp " + String.format("%,.0f", rs1.getDouble(1)));

            // Jumlah transaksi
            PreparedStatement ps2 = conn.prepareStatement(
                    "SELECT COUNT(*) FROM transactions");
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next())
                lblJumlahTransaksi.setText(String.valueOf(rs2.getInt(1)));

            // Hutang belum lunas
            PreparedStatement ps3 = conn.prepareStatement(
                    "SELECT COALESCE(SUM(nominal), 0) FROM debts " +
                            "WHERE tipe='hutang' AND status='belum'");
            ResultSet rs3 = ps3.executeQuery();
            if (rs3.next())
                lblHutang.setText(
                        "Rp " + String.format("%,.0f", rs3.getDouble(1)));

            // Piutang belum lunas
            PreparedStatement ps4 = conn.prepareStatement(
                    "SELECT COALESCE(SUM(nominal), 0) FROM debts " +
                            "WHERE tipe='piutang' AND status='belum'");
            ResultSet rs4 = ps4.executeQuery();
            if (rs4.next())
                lblPiutang.setText(
                        "Rp " + String.format("%,.0f", rs4.getDouble(1)));

            // Stok menipis
            PreparedStatement ps5 = conn.prepareStatement(
                    "SELECT m.nama_menu, s.jumlah_stok, s.satuan " +
                            "FROM stock s JOIN menus m ON s.id_menu = m.id_menu " +
                            "WHERE s.jumlah_stok <= 5 ORDER BY s.jumlah_stok ASC LIMIT 3");
            ResultSet rs5 = ps5.executeQuery();
            StringBuilder stokWarn = new StringBuilder();
            while (rs5.next()) {
                stokWarn.append("• ")
                        .append(rs5.getString("nama_menu"))
                        .append(" — Sisa: ")
                        .append(rs5.getInt("jumlah_stok"))
                        .append(" ")
                        .append(rs5.getString("satuan"))
                        .append("\n");
            }
            if (stokWarn.length() > 0) {
                lblStokWarning.setText(stokWarn.toString().trim());
                lblStokWarning.setStyle(
                        "-fx-text-fill: #92400E; -fx-font-size: 12;");
            }

            // Transaksi terbaru
            PreparedStatement ps6 = conn.prepareStatement(
                    "SELECT t.tanggal, t.total FROM transactions t " +
                            "ORDER BY t.tanggal DESC LIMIT 3");
            ResultSet rs6 = ps6.executeQuery();
            StringBuilder trans = new StringBuilder();
            while (rs6.next()) {
                trans.append("• Rp ")
                        .append(String.format("%,.0f", rs6.getDouble("total")))
                        .append(" — ")
                        .append(rs6.getTimestamp("tanggal")
                                .toLocalDateTime()
                                .format(java.time.format.DateTimeFormatter
                                        .ofPattern("dd MMM yyyy, HH:mm")))
                        .append("\n");
            }
            if (trans.length() > 0) {
                lblTransaksiBaru.setText(trans.toString().trim());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}