package com.pos.service;

import com.pos.dao.StockDAO;
import com.pos.dao.TransaksiDAO;
import com.pos.model.Transaksi;
import com.pos.model.TransaksiDetail;

import java.util.List;

public class KasirService {

    private final TransaksiDAO transaksiDAO = new TransaksiDAO();
    private final StockDAO stockDAO = new StockDAO();

    public boolean simpanTransaksi(Transaksi transaksi,
                                   List<TransaksiDetail> details,
                                   String metodePembayaran) {
        try {
            int idTransaksi = transaksiDAO.insertTransaksi(transaksi);
            if (idTransaksi == -1) return false;

            for (TransaksiDetail detail : details) {
                detail.setIdTransaksi(idTransaksi);
                detail.setMetodePembayaran(metodePembayaran);
                transaksiDAO.insertDetail(detail);
                stockDAO.decreaseStock(detail.getIdMenu(), detail.getQty());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public double hitungTotal(List<TransaksiDetail> details) {
        return details.stream().mapToDouble(TransaksiDetail::getSubtotal).sum();
    }
}