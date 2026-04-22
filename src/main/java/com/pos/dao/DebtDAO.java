package com.pos.dao;

import com.pos.config.koneksi;
import com.pos.model.Debt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class DebtDAO {

    public List<Debt> findAllByType(String tipe) {
        List<Debt> list = new ArrayList<>();
        String sql = """
            SELECT * FROM debts
            WHERE tipe = ?
            ORDER BY CASE WHEN status = 'belum' THEN 0 ELSE 1 END,
                     tanggal DESC,
                     created_at DESC
            """;

        Connection conn = koneksi.getConnection();
        if (conn == null) {
            return list;
        }

        try (conn;
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipe);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public void insert(Debt debt) {
        if (debt == null || debt.getNominal() <= 0) {
            throw new IllegalArgumentException("Nominal hutang/piutang harus positif.");
        }

        String sql = """
            INSERT INTO debts (nama, tipe, nominal, tanggal, status, keterangan)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        Connection conn = koneksi.getConnection();
        if (conn == null) {
            return;
        }

        try (conn;
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, debt.getNama());
            ps.setString(2, debt.getTipe());
            ps.setInt(3, (int) Math.round(debt.getNominal()));
            ps.setString(4, debt.getTanggal() == null ? null : debt.getTanggal().toString());
            ps.setString(5, debt.getStatus());
            ps.setString(6, debt.getKeterangan());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int idDebt) {
        String deletePaymentSql = "DELETE FROM payment WHERE id_debt = ?";
        String deleteDebtSql = "DELETE FROM debts WHERE id_debt = ?";

        try (Connection conn = koneksi.getConnection()) {
            if (conn == null) {
                return;
            }

            conn.setAutoCommit(false);
            try (PreparedStatement psPayment = conn.prepareStatement(deletePaymentSql);
                 PreparedStatement psDebt = conn.prepareStatement(deleteDebtSql)) {
                psPayment.setInt(1, idDebt);
                psPayment.executeUpdate();

                psDebt.setInt(1, idDebt);
                psDebt.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void markAsPaid(Debt debt) {
        String updateDebtSql = "UPDATE debts SET status = 'lunas' WHERE id_debt = ?";
        String insertPaymentSql = """
            INSERT INTO payment (id_debt, tanggal_bayar, jumlah_bayar)
            VALUES (?, ?, ?)
            """;

        try (Connection conn = koneksi.getConnection()) {
            if (conn == null) {
                return;
            }

            conn.setAutoCommit(false);
            try (PreparedStatement psDebt = conn.prepareStatement(updateDebtSql);
                 PreparedStatement psPayment = conn.prepareStatement(insertPaymentSql)) {
                psDebt.setInt(1, debt.getIdDebt());
                psDebt.executeUpdate();

                psPayment.setInt(1, debt.getIdDebt());
                psPayment.setString(2, LocalDate.now().toString());
                psPayment.setInt(3, (int) Math.round(debt.getNominal()));
                psPayment.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double getOutstandingTotal(String tipe) {
        String sql = """
            SELECT COALESCE(SUM(nominal), 0)
            FROM debts
            WHERE tipe = ? AND status = 'belum'
            """;

        Connection conn = koneksi.getConnection();
        if (conn == null) {
            return 0;
        }

        try (conn;
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipe);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int getOutstandingCount(String tipe) {
        String sql = """
            SELECT COUNT(*)
            FROM debts
            WHERE tipe = ? AND status = 'belum'
            """;

        Connection conn = koneksi.getConnection();
        if (conn == null) {
            return 0;
        }

        try (conn;
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipe);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private Debt mapRow(ResultSet rs) throws SQLException {
        Debt debt = new Debt();
        debt.setIdDebt(rs.getInt("id_debt"));
        debt.setNama(rs.getString("nama"));
        debt.setTipe(rs.getString("tipe"));
        debt.setNominal(rs.getDouble("nominal"));
        debt.setTanggal(readDate(rs, "tanggal"));
        debt.setStatus(rs.getString("status"));
        debt.setKeterangan(rs.getString("keterangan"));
        debt.setCreatedAt(readDateTime(rs, "created_at"));
        return debt;
    }

    private LocalDate readDate(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }

    private LocalDateTime readDateTime(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(value.replace(" ", "T"));
        } catch (DateTimeParseException ignored) {
        }

        Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
