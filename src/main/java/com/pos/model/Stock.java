package com.pos.model;

public class Stock {
    private int idStok;
    private int idMenu;
    private int jumlahStok;
    private String satuan;
    private int stokMinimum;
    private String namaMenu;

    public Stock() {}

    public Stock(int idStok, int idMenu, int jumlahStok, String satuan, int stokMinimum) {
        this.idStok = idStok;
        this.idMenu = idMenu;
        this.jumlahStok = jumlahStok;
        this.satuan = satuan;
        this.stokMinimum = stokMinimum;
    }

    public int getIdStok() { return idStok; }
    public void setIdStok(int idStok) { this.idStok = idStok; }

    public int getIdMenu() { return idMenu; }
    public void setIdMenu(int idMenu) { this.idMenu = idMenu; }

    public int getJumlahStok() { return jumlahStok; }
    public void setJumlahStok(int jumlahStok) { this.jumlahStok = jumlahStok; }

    public String getSatuan() { return satuan; }
    public void setSatuan(String satuan) { this.satuan = satuan; }

    public int getStokMinimum() { return stokMinimum; }
    public void setStokMinimum(int stokMinimum) { this.stokMinimum = stokMinimum; }

    public String getNamaMenu() { return namaMenu; }
    public void setNamaMenu(String namaMenu) { this.namaMenu = namaMenu; }

    public String getStatus() {
        return jumlahStok <= stokMinimum ? "Stok Menipis" : "Stok Aman";
    }
}