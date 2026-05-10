package com.pos.service;

import com.pos.dao.UserDAO;
import com.pos.model.User;

/**
 * Service autentikasi.
 * Menjadi penghubung antara layar login dan tabel users melalui UserDAO.
 */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();
    private static User loggedInUser = null;

    /**
     * Membuat akun default demo/awal jika belum ada di database.
     */
    public void initDefaultUsers() {
        userDAO.insertIfNotExists("admin", "123");
        userDAO.insertIfNotExists("owner", "123");
    }

    /**
     * Memvalidasi username/password lalu menyimpan user aktif jika login berhasil.
     */
    public User login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        String normalizedUsername = username.trim();
        if (normalizedUsername.isEmpty() || password.isBlank()) {
            return null;
        }

        User user = userDAO.findByUsername(normalizedUsername);
        if (user != null && user.getPassword().equals(password)) {
            loggedInUser = user;
            return user;
        }
        return null;
    }

    /**
     * Mengambil user yang sedang login untuk dipakai transaksi dan tampilan dashboard.
     */
    public static User getLoggedInUser() { return loggedInUser; }

    /**
     * Menghapus sesi login saat pengguna keluar aplikasi.
     */
    public static void logout() { loggedInUser = null; }
}
