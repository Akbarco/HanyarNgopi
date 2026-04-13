package com.pos.model;

import java.time.LocalDateTime;

public class Transaksi {
    private int idTransaksi;
    private int idUser;
    private LocalDateTime tanggal;
    private double total;

    public Transaksi() {}

    public Transaksi(int idTransaksi, int idUser, LocalDateTime tanggal, double total) {
        this.idTransaksi = idTransaksi;
        this.idUser = idUser;
        this.tanggal = tanggal;
        this.total = total;
    }

    public int getIdTransaksi() { return idTransaksi; }
    public void setIdTransaksi(int idTransaksi) { this.idTransaksi = idTransaksi; }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public LocalDateTime getTanggal() { return tanggal; }
    public void setTanggal(LocalDateTime tanggal) { this.tanggal = tanggal; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}