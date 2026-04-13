package com.pos.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Debt {
    private int idDebt;
    private String nama;
    private String tipe;
    private double nominal;
    private LocalDate tanggal;
    private String status;
    private String keterangan;
    private LocalDateTime createdAt;

    public Debt() {}

    public Debt(int idDebt, String nama, String tipe, double nominal,
                LocalDate tanggal, String status, String keterangan) {
        this.idDebt = idDebt;
        this.nama = nama;
        this.tipe = tipe;
        this.nominal = nominal;
        this.tanggal = tanggal;
        this.status = status;
        this.keterangan = keterangan;
    }

    public int getIdDebt() {
        return idDebt;
    }

    public void setIdDebt(int idDebt) {
        this.idDebt = idDebt;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getTipe() {
        return tipe;
    }

    public void setTipe(String tipe) {
        this.tipe = tipe;
    }

    public double getNominal() {
        return nominal;
    }

    public void setNominal(double nominal) {
        this.nominal = nominal;
    }

    public LocalDate getTanggal() {
        return tanggal;
    }

    public void setTanggal(LocalDate tanggal) {
        this.tanggal = tanggal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isBelumLunas() {
        return "belum".equalsIgnoreCase(status);
    }
}
