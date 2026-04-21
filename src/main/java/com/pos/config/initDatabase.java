package com.pos.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
                        is_active INTEGER NOT NULL DEFAULT 1,
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
                        nama_menu_snapshot TEXT,
                        harga_satuan_snapshot INTEGER,
                        metode_pembayaran TEXT CHECK (metode_pembayaran IN ('cash','qris')),
                        FOREIGN KEY (id_transaksi) REFERENCES transactions(id_transaksi) ON DELETE RESTRICT ON UPDATE RESTRICT,
                        FOREIGN KEY (id_menu) REFERENCES menus(id_menu) ON DELETE RESTRICT ON UPDATE RESTRICT
                    )
                """);

                ensureColumn(stmt, conn, "menus", "is_active",
                        "ALTER TABLE menus ADD COLUMN is_active INTEGER NOT NULL DEFAULT 1");
                ensureColumn(stmt, conn, "transaction_detail", "nama_menu_snapshot",
                        "ALTER TABLE transaction_detail ADD COLUMN nama_menu_snapshot TEXT");
                ensureColumn(stmt, conn, "transaction_detail", "harga_satuan_snapshot",
                        "ALTER TABLE transaction_detail ADD COLUMN harga_satuan_snapshot INTEGER");

                stmt.executeUpdate("""
                    UPDATE transaction_detail
                    SET nama_menu_snapshot = (
                        SELECT m.nama_menu FROM menus m WHERE m.id_menu = transaction_detail.id_menu
                    )
                    WHERE nama_menu_snapshot IS NULL OR TRIM(nama_menu_snapshot) = ''
                """);

                stmt.executeUpdate("""
                    UPDATE transaction_detail
                    SET harga_satuan_snapshot = (
                        SELECT m.harga FROM menus m WHERE m.id_menu = transaction_detail.id_menu
                    )
                    WHERE harga_satuan_snapshot IS NULL
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

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS app_seed (
                        seed_key TEXT PRIMARY KEY,
                        executed_at DATETIME DEFAULT CURRENT_TIMESTAMP
                    )
                """);

                seedDefaultMenus(conn);

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

    private static void seedDefaultMenus(Connection conn) throws SQLException {
        String seedKey = "hanyarngopi_menu_2026_01";
        if (isSeedExecuted(conn, seedKey)) {
            return;
        }

        String[][] menus = {
                {"Teh Panas", "5000", "minuman"},
                {"Teh Dingin", "5000", "minuman"},
                {"Teh SKM Panas", "12000", "minuman"},
                {"Teh SKM Dingin", "12000", "minuman"},
                {"Teh Susu Panas", "20000", "minuman"},
                {"Teh Susu Dingin", "20000", "minuman"},
                {"Teh Susu Aren Panas", "20000", "minuman"},
                {"Teh Susu Aren Dingin", "20000", "minuman"},
                {"Thai Tea Panas", "20000", "minuman"},
                {"Thai Tea Dingin", "20000", "minuman"},
                {"Green Thai Tea Panas", "20000", "minuman"},
                {"Green Thai Tea Dingin", "20000", "minuman"},
                {"Populer Dingin", "20000", "minuman"},
                {"Susu Coklat Panas", "20000", "minuman"},
                {"Susu Coklat Dingin", "20000", "minuman"},
                {"Susu Cookies & Cream Dingin", "20000", "minuman"},
                {"Susu Regal Dingin", "20000", "minuman"},
                {"Susu Panas", "15000", "minuman"},
                {"Susu Dingin", "15000", "minuman"},
                {"Blue Ocean", "20000", "minuman"},
                {"Red Soda", "20000", "minuman"},
                {"Violet Soda", "20000", "minuman"},
                {"Es Campur", "15000", "minuman"},
                {"Es Sirup", "10000", "minuman"},
                {"Es Sirup SKM", "12000", "minuman"},
                {"Es Sirup Susu", "15000", "minuman"},
                {"Air Mineral", "5000", "minuman"},
                {"Air Es", "3000", "minuman"},
                {"Es Batu", "2000", "minuman"},

                {"Nasi Telur", "15000", "makanan"},
                {"Nasi Nuget", "18000", "makanan"},
                {"Nasi Sosis", "18000", "makanan"},
                {"Nasi Ayam", "22000", "makanan"},
                {"Nasi Belly", "25000", "makanan"},
                {"Aneka Kerupuk", "1000", "snack"},
                {"Baso Ikan", "3000", "snack"},
                {"Baso Ikan Salmon", "3000", "snack"},
                {"Chikuwa Mini", "3000", "snack"},
                {"Dumpling Keju Ayam", "3000", "snack"},
                {"Sosis Ayam", "3000", "snack"},
                {"Otak-Otak Ikan", "3000", "snack"},
                {"Telur Ayam", "3000", "makanan"},
                {"Mie", "3000", "makanan"},
                {"Bakso", "15000", "makanan"},
                {"Indomie Goreng", "15000", "makanan"},
                {"Indomie Soto Mie", "15000", "makanan"},
                {"Burger Ayam", "18000", "makanan"},
                {"Burger Daging", "18000", "makanan"},
                {"Pisang Goreng", "15000", "snack"},
                {"Pisang Keju", "15000", "snack"},
                {"Kentang Goreng", "15000", "snack"},
                {"Nuget Goreng", "15000", "snack"},
                {"Sosis Goreng", "15000", "snack"},
                {"Samosa", "15000", "snack"},
                {"Roti Bakar Srikaya", "15000", "snack"},
                {"Roti Bakar Coklat", "15000", "snack"},
                {"Roti Bakar Stroberi", "15000", "snack"},
                {"Roti Bakar Nanas", "15000", "snack"},
                {"Roti Bakar Keju", "15000", "snack"}
        };

        try (PreparedStatement ps = conn.prepareStatement("""
                INSERT INTO menus (nama_menu, harga, kategori, is_active)
                SELECT ?, ?, ?, 1
                WHERE NOT EXISTS (
                    SELECT 1 FROM menus WHERE lower(nama_menu) = lower(?)
                )
                """)) {
            for (String[] menu : menus) {
                ps.setString(1, menu[0]);
                ps.setInt(2, Integer.parseInt(menu[1]));
                ps.setString(3, menu[2]);
                ps.setString(4, menu[0]);
                ps.addBatch();
            }
            ps.executeBatch();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO app_seed (seed_key) VALUES (?)"
        )) {
            ps.setString(1, seedKey);
            ps.executeUpdate();
        }
    }

    private static boolean isSeedExecuted(Connection conn, String seedKey) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM app_seed WHERE seed_key = ?"
        )) {
            ps.setString(1, seedKey);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void ensureColumn(Statement stmt, Connection conn,
                                     String tableName, String columnName,
                                     String alterSql) throws SQLException {
        if (!hasColumn(conn, tableName, columnName)) {
            stmt.executeUpdate(alterSql);
        }
    }

    private static boolean hasColumn(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet columns = metaData.getColumns(null, null, tableName, columnName)) {
            return columns.next();
        }
    }
}
