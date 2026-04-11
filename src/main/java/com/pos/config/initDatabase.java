package com.pos.config;

import java.sql.Connection;
import java.sql.Statement;

public class initDatabase {

    public static void init () {
        try (Connection conn = koneksi.getConnection();
             Statement stmt = conn.createStatement()) {

            // USERS
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id_user INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(100),
                    password VARCHAR(255),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // MENUS
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS menus (
                    id_menu INT AUTO_INCREMENT PRIMARY KEY,
                    nama_menu VARCHAR(100),
                    harga INT,
                    kategori ENUM('makanan','minuman','stok'),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // STOCK
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS stock (
                    id_stok INT AUTO_INCREMENT PRIMARY KEY,
                    id_menu INT,
                    jumlah_stok INT,
                    satuan ENUM('kg','g','liter','ml','pcs','box','sachet','botol'),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (id_menu) REFERENCES menus(id_menu)
                )
            """);

            // TRANSACTIONS
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id_transaksi INT AUTO_INCREMENT PRIMARY KEY,
                    id_user INT,
                    tanggal DATETIME,
                    total INT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (id_user) REFERENCES users(id_user)
                )
            """);

            // TRANSACTION DETAIL
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS transaction_detail (
                    id_detail INT AUTO_INCREMENT PRIMARY KEY,
                    id_transaksi INT,
                    id_menu INT,
                    qty INT,
                    subtotal INT,
                    metode_pembayaran ENUM('cash','qris'),
                    FOREIGN KEY (id_transaksi) REFERENCES transactions(id_transaksi),
                    FOREIGN KEY (id_menu) REFERENCES menus(id_menu)
                )
            """);

            // DEBTS
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS debts (
                    id_debt INT AUTO_INCREMENT PRIMARY KEY,
                    nama VARCHAR(100),
                    tipe ENUM('hutang','piutang'),
                    nominal INT,
                    tanggal DATE,
                    status ENUM('lunas','belum'),
                    keterangan VARCHAR(255),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // PAYMENT
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS payment (
                    id_payment INT AUTO_INCREMENT PRIMARY KEY,
                    id_debt INT,
                    tanggal_bayar DATE,
                    jumlah_bayar INT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (id_debt) REFERENCES debts(id_debt)
                )
            """);

            System.out.println("🔥 Database + ENUM berhasil dibuat!");

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
}