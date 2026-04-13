package com.pos.dao;

import com.pos.config.koneksi;
import com.pos.model.Stock;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockDAO {

    public List<Stock> findAll() {
        List<Stock> list = new ArrayList<>();
        String sql = """
            SELECT s.*, m.nama_menu FROM stock s
            JOIN menus m ON s.id_menu = m.id_menu
            ORDER BY m.nama_menu
            """;
        try (Connection conn = koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Stock findByIdMenu(int idMenu) {
        String sql = "SELECT * FROM stock WHERE id_menu = ?";
        try (Connection conn = koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMenu);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void insert(Stock stock) {
        String sql = "INSERT INTO stock (id_menu, jumlah_stok, satuan, stok_minimum) VALUES (?, ?, ?, ?)";
        try (Connection conn = koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stock.getIdMenu());
            ps.setInt(2, stock.getJumlahStok());
            ps.setString(3, stock.getSatuan());
            ps.setInt(4, stock.getStokMinimum());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Stock stock) {
        String sql = "UPDATE stock SET jumlah_stok=?, satuan=?, stok_minimum=? WHERE id_stok=?";
        try (Connection conn = koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stock.getJumlahStok());
            ps.setString(2, stock.getSatuan());
            ps.setInt(3, stock.getStokMinimum());
            ps.setInt(4, stock.getIdStok());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int idStok) {
        String sql = "DELETE FROM stock WHERE id_stok=?";
        try (Connection conn = koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idStok);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void decreaseStock(int idMenu, int qty) {
        String sql = "UPDATE stock SET jumlah_stok = jumlah_stok - ? WHERE id_menu = ?";
        try (Connection conn = koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setInt(2, idMenu);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Stock mapRow(ResultSet rs) throws SQLException {
        Stock s = new Stock();
        s.setIdStok(rs.getInt("id_stok"));
        s.setIdMenu(rs.getInt("id_menu"));
        s.setJumlahStok(rs.getInt("jumlah_stok"));
        s.setSatuan(rs.getString("satuan"));
        s.setStokMinimum(rs.getInt("stok_minimum"));
        try { s.setNamaMenu(rs.getString("nama_menu")); }
        catch (SQLException ignored) {}
        return s;
    }
}