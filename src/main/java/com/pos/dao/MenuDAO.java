package com.pos.dao;

import com.pos.config.koneksi;
import com.pos.model.Menu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {

    public List<Menu> findAll() {
        return findByActive(true);
    }

    public List<Menu> findArchived() {
        return findByActive(false);
    }

    public List<Menu> findByActive(boolean active) {
        List<Menu> list = new ArrayList<>();
        String sql = """
            SELECT * FROM menus
            WHERE is_active = ?
            ORDER BY kategori, nama_menu
            """;
        Connection conn = koneksi.getConnection();
        if (conn == null) {
            return list;
        }
        try (conn;
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, active ? 1 : 0);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Menu> findAvailableForTransaction() {
        List<Menu> list = new ArrayList<>();
        String sql = """
            SELECT DISTINCT m.*
            FROM menus m
            JOIN stock s ON s.id_menu = m.id_menu
            WHERE m.is_active = 1
              AND s.jumlah_stok > 0
            ORDER BY m.kategori, m.nama_menu
            """;
        Connection conn = koneksi.getConnection();
        if (conn == null) {
            return list;
        }
        try (conn;
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void insert(Menu menu) {
        String sql = "INSERT INTO menus (nama_menu, harga, kategori, is_active) VALUES (?, ?, ?, ?)";
        Connection conn = koneksi.getConnection();
        if (conn == null) {
            return;
        }
        try (conn;
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, menu.getNamaMenu());
            ps.setInt(2, (int) Math.round(menu.getHarga()));
            ps.setString(3, menu.getKategori());
            ps.setInt(4, menu.isActive() ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Menu menu) {
        String sql = "UPDATE menus SET nama_menu=?, harga=?, kategori=?, is_active=? WHERE id_menu=?";
        Connection conn = koneksi.getConnection();
        if (conn == null) {
            return;
        }
        try (conn;
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, menu.getNamaMenu());
            ps.setInt(2, (int) Math.round(menu.getHarga()));
            ps.setString(3, menu.getKategori());
            ps.setInt(4, menu.isActive() ? 1 : 0);
            ps.setInt(5, menu.getIdMenu());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void archive(int idMenu) {
        updateActiveState(idMenu, false);
    }

    public void activate(int idMenu) {
        updateActiveState(idMenu, true);
    }

    private void updateActiveState(int idMenu, boolean active) {
        String sql = "UPDATE menus SET is_active = ? WHERE id_menu = ?";
        Connection conn = koneksi.getConnection();
        if (conn == null) {
            return;
        }
        try (conn;
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, active ? 1 : 0);
            ps.setInt(2, idMenu);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String delete(int idMenu) {
        String sql = "DELETE FROM menus WHERE id_menu=?";
        Connection conn = koneksi.getConnection();
        if (conn == null) {
            return "Koneksi database tidak tersedia.";
        }
        try (conn;
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMenu);
            int affected = ps.executeUpdate();
            return affected > 0 ? null : "Menu tidak ditemukan atau sudah terhapus.";
        } catch (SQLException e) {
            e.printStackTrace();
            String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (message.contains("foreign key")) {
                return "Menu ini masih dipakai di data stok atau transaksi, jadi belum bisa dihapus.";
            }
            return "Gagal menghapus menu.";
        }
    }

    private Menu mapRow(ResultSet rs) throws SQLException {
        Menu menu = new Menu();
        menu.setIdMenu(rs.getInt("id_menu"));
        menu.setNamaMenu(rs.getString("nama_menu"));
        menu.setHarga(rs.getDouble("harga"));
        menu.setKategori(rs.getString("kategori"));
        menu.setActive(rs.getInt("is_active") == 1);
        return menu;
    }
}
