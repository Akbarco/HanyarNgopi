package com.pos.service;

import com.pos.dao.StockDAO;
import com.pos.model.Stock;

import java.util.List;

/**
 * Service stok.
 * Menampung aturan bisnis ringan sebelum data stok dikirim ke StockDAO.
 */
public class StockService {

    private final StockDAO stockDAO = new StockDAO();

    /**
     * Mengambil seluruh stok aktif untuk layar Kelola Stok.
     */
    public List<Stock> getAllStock() {
        return stockDAO.findAll();
    }

    /**
     * Menambah stok baru, atau menambahkan jumlah ke stok yang sudah ada untuk menu yang sama.
     */
    public void tambahStock(Stock stock) {
        Stock existing = stockDAO.findByIdMenu(stock.getIdMenu());
        if (existing != null) {
            existing.setJumlahStok(existing.getJumlahStok() + stock.getJumlahStok());
            existing.setSatuan(stock.getSatuan());
            existing.setStokMinimum(stock.getStokMinimum());
            stockDAO.update(existing);
        } else {
            stockDAO.insert(stock);
        }
    }

    /**
     * Memperbarui jumlah, satuan, dan batas minimum stok.
     */
    public void updateStock(Stock stock) {
        stockDAO.update(stock);
    }

    /**
     * Menghapus data stok berdasarkan ID stok.
     */
    public void deleteStock(int idStok) {
        stockDAO.delete(idStok);
    }
}
