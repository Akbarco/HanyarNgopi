package com.pos.config;

import java.sql.Connection;
import java.sql.Statement;

public class initDatabase {

    public static void init () {
        try (Connection conn = koneksi.getConnection()) {
            if (conn == null) {
                return;
            }
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        id_user INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT,
                        password TEXT,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                    )
                """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS menus (
                        id_menu INTEGER PRIMARY KEY AUTOINCREMENT,
                        nama_menu TEXT,
                        harga INTEGER,
                        kategori TEXT CHECK (kategori IN ('makanan','minuman','snack')),
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                    )
                """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS debts (
                        id_debt INTEGER PRIMARY KEY AUTOINCREMENT,
                        nama TEXT,
                        tipe TEXT CHECK (tipe IN ('hutang','piutang')),
                        nominal INTEGER,
                        tanggal TEXT,
                        status TEXT CHECK (status IN ('lunas','belum')),
                        keterangan TEXT,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                    )
                """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS transactions (
                        id_transaksi INTEGER PRIMARY KEY AUTOINCREMENT,
                        id_user INTEGER,
                        tanggal DATETIME,
                        total INTEGER,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (id_user) REFERENCES users(id_user) ON DELETE RESTRICT ON UPDATE RESTRICT
                    )
                """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS stock (
                        id_stok INTEGER PRIMARY KEY AUTOINCREMENT,
                        id_menu INTEGER,
                        jumlah_stok INTEGER,
                        satuan TEXT CHECK (satuan IN ('kg','g','liter','ml','pcs','box','sachet','botol')),
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        stok_minimum INTEGER DEFAULT 0,
                        FOREIGN KEY (id_menu) REFERENCES menus(id_menu) ON DELETE RESTRICT ON UPDATE RESTRICT
                    )
                """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS transaction_detail (
                        id_detail INTEGER PRIMARY KEY AUTOINCREMENT,
                        id_transaksi INTEGER,
                        id_menu INTEGER,
                        qty INTEGER,
                        subtotal INTEGER,
                        metode_pembayaran TEXT CHECK (metode_pembayaran IN ('cash','qris')),
                        FOREIGN KEY (id_transaksi) REFERENCES transactions(id_transaksi) ON DELETE RESTRICT ON UPDATE RESTRICT,
                        FOREIGN KEY (id_menu) REFERENCES menus(id_menu) ON DELETE RESTRICT ON UPDATE RESTRICT
                    )
                """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS payment (
                        id_payment INTEGER PRIMARY KEY AUTOINCREMENT,
                        id_debt INTEGER,
                        tanggal_bayar TEXT,
                        jumlah_bayar INTEGER,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (id_debt) REFERENCES debts(id_debt) ON DELETE RESTRICT ON UPDATE RESTRICT
                    )
                """);

                stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_stock_id_menu ON stock(id_menu)");
                stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_transactions_id_user ON transactions(id_user)");
                stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_transaction_detail_id_transaksi ON transaction_detail(id_transaksi)");
                stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_transaction_detail_id_menu ON transaction_detail(id_menu)");
                stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_payment_id_debt ON payment(id_debt)");

                System.out.println("Database SQLite berhasil dibuat di: " + koneksi.getDatabasePath());
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
