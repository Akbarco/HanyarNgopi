package com.pos.service;

import com.pos.dao.StockDAO;
import com.pos.model.Stock;

import java.util.List;

public class StockService {

    private final StockDAO stockDAO = new StockDAO();

    public List<Stock> getAllStock() {
        return stockDAO.findAll();
    }

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

    public void updateStock(Stock stock) {
        stockDAO.update(stock);
    }

    public void deleteStock(int idStok) {
        stockDAO.delete(idStok);
    }
}