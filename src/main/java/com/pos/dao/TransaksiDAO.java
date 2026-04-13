package com.pos.dao;

import com.pos.config.koneksi;
import com.pos.model.Transaksi;
import com.pos.model.TransaksiDetail;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransaksiDAO {

    public int insertTransaksi(Transaksi t) {
        try (Connection conn = koneksi.getConnection()) {
            return insertTransaksi(conn, t);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int insertTransaksi(Connection conn, Transaksi t) throws SQLException {
        String sql = "INSERT INTO transactions (id_user, tanggal, total) VALUES (?, NOW(), ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getIdUser());
            ps.setInt(2, (int) Math.round(t.getTotal()));
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    public void insertDetail(TransaksiDetail detail) {
        try (Connection conn = koneksi.getConnection()) {
            insertDetail(conn, detail);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertDetail(Connection conn, TransaksiDetail detail) throws SQLException {
        String sql = """
            INSERT INTO transaction_detail
            (id_transaksi, id_menu, qty, subtotal, metode_pembayaran)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, detail.getIdTransaksi());
            ps.setInt(2, detail.getIdMenu());
            ps.setInt(3, detail.getQty());
            ps.setInt(4, (int) Math.round(detail.getSubtotal()));
            ps.setString(5, detail.getMetodePembayaran());
            ps.executeUpdate();
        }
    }

    public List<Transaksi> findAll() {
        List<Transaksi> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY tanggal DESC, id_transaksi DESC";
        Connection conn = koneksi.getConnection();
        if (conn == null) {
            return list;
        }
        try (conn;
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Transaksi t = new Transaksi();
                t.setIdTransaksi(rs.getInt("id_transaksi"));
                t.setIdUser(rs.getInt("id_user"));
                t.setTanggal(rs.getTimestamp("tanggal").toLocalDateTime());
                t.setTotal(rs.getDouble("total"));
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<TransaksiDetail> findDetailByTransaksiId(int idTransaksi) {
        List<TransaksiDetail> list = new ArrayList<>();
        String sql = """
            SELECT td.*, m.nama_menu, m.harga FROM transaction_detail td
            JOIN menus m ON td.id_menu = m.id_menu
            WHERE td.id_transaksi = ?
            """;
        Connection conn = koneksi.getConnection();
        if (conn == null) {
            return list;
        }
        try (conn;
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTransaksi);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                TransaksiDetail td = new TransaksiDetail();
                td.setIdDetail(rs.getInt("id_detail"));
                td.setIdTransaksi(rs.getInt("id_transaksi"));
                td.setIdMenu(rs.getInt("id_menu"));
                td.setQty(rs.getInt("qty"));
                td.setSubtotal(rs.getDouble("subtotal"));
                td.setMetodePembayaran(rs.getString("metode_pembayaran"));
                td.setNamaMenu(rs.getString("nama_menu"));
                td.setHarga(rs.getDouble("harga"));
                list.add(td);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
