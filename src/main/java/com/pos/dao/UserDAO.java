package com.pos.dao;

import com.pos.config.koneksi;
import com.pos.model.User;

import java.sql.*;

public class UserDAO {

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setIdUser(rs.getInt("id_user"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void insertIfNotExists(String username, String password) {
        String check = "SELECT COUNT(*) FROM users WHERE username = ?";
        String insert = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = koneksi.getConnection();
             PreparedStatement psCheck = conn.prepareStatement(check)) {
            psCheck.setString(1, username);
            ResultSet rs = psCheck.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement psInsert = conn.prepareStatement(insert)) {
                    psInsert.setString(1, username);
                    psInsert.setString(2, password);
                    psInsert.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}