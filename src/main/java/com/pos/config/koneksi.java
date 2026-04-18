package com.pos.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class koneksi {
    private static final String DB_FILE_NAME = "hanyarngopi.db";
    private static final Path DB_PATH = resolveDatabasePath();
    private static final String URL = "jdbc:sqlite:" + DB_PATH.toAbsolutePath();

    private static Path resolveDatabasePath() {
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null && !localAppData.isBlank()) {
            return Paths.get(localAppData, "HanyarNgopi", "data", DB_FILE_NAME);
        }
        return Paths.get(System.getProperty("user.home"), ".hanyarngopi", "data", DB_FILE_NAME);
    }

    private static void ensureDatabaseDirectory() throws Exception {
        Path parent = DB_PATH.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    public static Connection getConnection() {
        try {
            ensureDatabaseDirectory();
            Connection conn = DriverManager.getConnection(URL);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
                stmt.execute("PRAGMA busy_timeout = 5000");
            }
            System.out.println("✅ Koneksi berhasil!");
            return conn;
        } catch (Exception e) {
            System.out.println("❌ Koneksi gagal: " + e.getMessage());
            return null;
        }
    }

    public static Path getDatabasePath() {
        return DB_PATH;
    }
}
