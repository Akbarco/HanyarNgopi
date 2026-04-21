package com.pos.model;

public class Menu {
    private int idMenu;
    private String namaMenu;
    private double harga;
    private String kategori;
    private boolean active = true;

    public Menu() {}

    public Menu(int idMenu, String namaMenu, double harga, String kategori) {
        this(idMenu, namaMenu, harga, kategori, true);
    }

    public Menu(int idMenu, String namaMenu, double harga, String kategori, boolean active) {
        this.idMenu = idMenu;
        this.namaMenu = namaMenu;
        this.harga = harga;
        this.kategori = kategori;
        this.active = active;
    }

    public int getIdMenu() { return idMenu; }
    public void setIdMenu(int idMenu) { this.idMenu = idMenu; }

    public String getNamaMenu() { return namaMenu; }
    public void setNamaMenu(String namaMenu) { this.namaMenu = namaMenu; }

    public double getHarga() { return harga; }
    public void setHarga(double harga) { this.harga = harga; }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() { return namaMenu; }
}
