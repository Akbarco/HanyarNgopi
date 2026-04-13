package com.pos.model;

public class TransaksiDetail {
    private int idDetail;
    private int idTransaksi;
    private int idMenu;
    private int qty;
    private double subtotal;
    private String metodePembayaran;
    private String namaMenu;
    private double harga;

    public TransaksiDetail() {}

    public TransaksiDetail(int idTransaksi, int idMenu, int qty,
                           double subtotal, String metodePembayaran) {
        this.idTransaksi = idTransaksi;
        this.idMenu = idMenu;
        this.qty = qty;
        this.subtotal = subtotal;
        this.metodePembayaran = metodePembayaran;
    }

    public int getIdDetail() { return idDetail; }
    public void setIdDetail(int idDetail) { this.idDetail = idDetail; }

    public int getIdTransaksi() { return idTransaksi; }
    public void setIdTransaksi(int idTransaksi) { this.idTransaksi = idTransaksi; }

    public int getIdMenu() { return idMenu; }
    public void setIdMenu(int idMenu) { this.idMenu = idMenu; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public String getMetodePembayaran() { return metodePembayaran; }
    public void setMetodePembayaran(String m) { this.metodePembayaran = m; }

    public String getNamaMenu() { return namaMenu; }
    public void setNamaMenu(String namaMenu) { this.namaMenu = namaMenu; }

    public double getHarga() { return harga; }
    public void setHarga(double harga) { this.harga = harga; }
}