package com.pos.dao;

import com.pos.config.koneksi;
import com.pos.model.Debt;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

        try (Connection conn = koneksi.getConnection();
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
        String sql = """
            INSERT INTO debts (nama, tipe, nominal, tanggal, status, keterangan)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, debt.getNama());
            ps.setString(2, debt.getTipe());
            ps.setDouble(3, debt.getNominal());
            ps.setDate(4, Date.valueOf(debt.getTanggal()));
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
            VALUES (?, CURDATE(), ?)
            """;

        try (Connection conn = koneksi.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psDebt = conn.prepareStatement(updateDebtSql);
                 PreparedStatement psPayment = conn.prepareStatement(insertPaymentSql)) {
                psDebt.setInt(1, debt.getIdDebt());
                psDebt.executeUpdate();

                psPayment.setInt(1, debt.getIdDebt());
                psPayment.setDouble(2, debt.getNominal());
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

        try (Connection conn = koneksi.getConnection();
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

        try (Connection conn = koneksi.getConnection();
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
        debt.setTanggal(rs.getDate("tanggal").toLocalDate());
        debt.setStatus(rs.getString("status"));
        debt.setKeterangan(rs.getString("keterangan"));
        debt.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return debt;
    }
}
