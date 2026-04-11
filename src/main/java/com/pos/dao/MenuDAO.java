package com.pos.dao;

import com.pos.config.koneksi;
import com.pos.model.Menu;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {

    public List<Menu> findAll() {
        List<Menu> list = new ArrayList<>();
        String sql = "SELECT * FROM menus ORDER BY kategori, nama_menu";
        try (Connection conn = koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void insert(Menu menu) {
        String sql = "INSERT INTO menus (nama_menu, harga, kategori) VALUES (?, ?, ?)";
        try (Connection conn = koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, menu.getNamaMenu());
            ps.setDouble(2, menu.getHarga());
            ps.setString(3, menu.getKategori());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Menu menu) {
        String sql = "UPDATE menus SET nama_menu=?, harga=?, kategori=? WHERE id_menu=?";
        try (Connection conn = koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, menu.getNamaMenu());
            ps.setDouble(2, menu.getHarga());
            ps.setString(3, menu.getKategori());
            ps.setInt(4, menu.getIdMenu());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int idMenu) {
        String sql = "DELETE FROM menus WHERE id_menu=?";
        try (Connection conn = koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMenu);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Menu mapRow(ResultSet rs) throws SQLException {
        Menu m = new Menu();
        m.setIdMenu(rs.getInt("id_menu"));
        m.setNamaMenu(rs.getString("nama_menu"));
        m.setHarga(rs.getDouble("harga"));
        m.setKategori(rs.getString("kategori"));
        return m;
    }
}