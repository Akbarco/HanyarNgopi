package com.pos.service;

import com.pos.config.koneksi;
import com.pos.dao.StockDAO;
import com.pos.dao.TransaksiDAO;
import com.pos.model.Stock;
import com.pos.model.Transaksi;
import com.pos.model.TransaksiDetail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service kasir.
 * Mengatur proses transaksi agar penjualan dan pengurangan stok berjalan dalam satu alur.
 */
public class KasirService {

    private final TransaksiDAO transaksiDAO = new TransaksiDAO();
    private final StockDAO stockDAO = new StockDAO();

    /**
     * Mengambil stok tersedia untuk validasi saat kasir memilih menu.
     */
    public int getAvailableStock(int idMenu) {
        Stock stock = stockDAO.findByIdMenu(idMenu);
        return stock == null ? 0 : stock.getJumlahStok();
    }

    /**
     * Mengecek apakah semua item di keranjang masih memiliki stok cukup.
     * Mengembalikan pesan error jika ada item yang tidak valid.
     */
    public String validateStockAvailability(List<TransaksiDetail> details) {
        if (details == null || details.isEmpty()) {
            return "Keranjang masih kosong.";
        }

        Map<Integer, Integer> requestedByMenu = new LinkedHashMap<>();
        Map<Integer, String> nameByMenu = new LinkedHashMap<>();
        for (TransaksiDetail detail : details) {
            requestedByMenu.merge(detail.getIdMenu(), detail.getQty(), Integer::sum);
            if (detail.getNamaMenu() != null && !detail.getNamaMenu().isBlank()) {
                nameByMenu.putIfAbsent(detail.getIdMenu(), detail.getNamaMenu());
            }
        }

        for (Map.Entry<Integer, Integer> entry : requestedByMenu.entrySet()) {
            int idMenu = entry.getKey();
            int requestedQty = entry.getValue();
            Stock stock = stockDAO.findByIdMenu(idMenu);
            String namaMenu = nameByMenu.getOrDefault(idMenu, "menu #" + idMenu);

            if (requestedQty <= 0) {
                return "Jumlah pesanan untuk \"" + namaMenu + "\" tidak valid.";
            }
            if (stock == null) {
                return "Stok untuk \"" + namaMenu + "\" belum tersedia.";
            }
            if (stock.getJumlahStok() < requestedQty) {
                return "Stok \"" + namaMenu + "\" tidak cukup. Sisa: " + stock.getJumlahStok();
            }
        }

        return null;
    }

    /**
     * Menyimpan transaksi lengkap: header transaksi, detail item, dan pengurangan stok.
     * Jika salah satu langkah gagal, semua perubahan dibatalkan agar data tetap konsisten.
     */
    public boolean simpanTransaksi(Transaksi transaksi,
                                   List<TransaksiDetail> details,
                                   String metodePembayaran) {
        if (transaksi == null || details == null || details.isEmpty()) {
            return false;
        }

        try (Connection conn = koneksi.getConnection()) {
            if (conn == null) {
                return false;
            }
            conn.setAutoCommit(false);
            try {
                String validationError = validateStockAvailability(conn, details);
                if (validationError != null) {
                    throw new IllegalStateException(validationError);
                }

                int idTransaksi = transaksiDAO.insertTransaksi(conn, transaksi);
                if (idTransaksi == -1) {
                    throw new IllegalStateException("Gagal membuat transaksi.");
                }

                for (TransaksiDetail detail : details) {
                    detail.setIdTransaksi(idTransaksi);
                    detail.setMetodePembayaran(metodePembayaran);
                    transaksiDAO.insertDetail(conn, detail);
                    boolean updated = stockDAO.decreaseStock(conn, detail.getIdMenu(), detail.getQty());
                    if (!updated) {
                        throw new IllegalStateException("Stok tidak cukup untuk menyelesaikan transaksi.");
                    }
                }

                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Versi validasi stok yang memakai koneksi transaksi database yang sama.
     */
    private String validateStockAvailability(Connection conn, List<TransaksiDetail> details) throws SQLException {
        if (details == null || details.isEmpty()) {
            return "Keranjang masih kosong.";
        }

        Map<Integer, Integer> requestedByMenu = new LinkedHashMap<>();
        Map<Integer, String> nameByMenu = new LinkedHashMap<>();
        for (TransaksiDetail detail : details) {
            requestedByMenu.merge(detail.getIdMenu(), detail.getQty(), Integer::sum);
            if (detail.getNamaMenu() != null && !detail.getNamaMenu().isBlank()) {
                nameByMenu.putIfAbsent(detail.getIdMenu(), detail.getNamaMenu());
            }
        }

        for (Map.Entry<Integer, Integer> entry : requestedByMenu.entrySet()) {
            int idMenu = entry.getKey();
            int requestedQty = entry.getValue();
            Stock stock = stockDAO.findByIdMenu(conn, idMenu);
            String namaMenu = nameByMenu.getOrDefault(idMenu, "menu #" + idMenu);

            if (requestedQty <= 0) {
                return "Jumlah pesanan untuk \"" + namaMenu + "\" tidak valid.";
            }
            if (stock == null) {
                return "Stok untuk \"" + namaMenu + "\" belum tersedia.";
            }
            if (stock.getJumlahStok() < requestedQty) {
                return "Stok \"" + namaMenu + "\" tidak cukup. Sisa: " + stock.getJumlahStok();
            }
        }

        return null;
    }

    /**
     * Menghitung total pembayaran dari seluruh item di keranjang.
     */
    public double hitungTotal(List<TransaksiDetail> details) {
        return details.stream().mapToDouble(TransaksiDetail::getSubtotal).sum();
    }
}
