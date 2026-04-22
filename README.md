<p align="center">
  <img src="src/main/resources/com/pos/view/image/logo.jpeg" alt="HanyarNgopi Logo" width="140" />
</p>

<h1 align="center">HanyarNgopi</h1>

<p align="center">
  Aplikasi desktop Point of Sale dan manajemen operasional sederhana untuk UMKM kopi.
</p>

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-22-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white">
  <img alt="JavaFX" src="https://img.shields.io/badge/JavaFX-21.0.6-2F74C0?style=for-the-badge">
  <img alt="SQLite" src="https://img.shields.io/badge/SQLite-3.50-003B57?style=for-the-badge&logo=sqlite&logoColor=white">
</p>

## Tentang Project

HanyarNgopi adalah aplikasi desktop berbasis JavaFX untuk membantu pengelolaan kasir, menu, stok, hutang/piutang, dan laporan bisnis pada skala UMKM. Aplikasi ini memakai SQLite sebagai database lokal sehingga dapat berjalan tanpa server eksternal.

Fokus utama project ini adalah membuat alur operasional harian menjadi cepat, rapi, dan mudah dipakai: transaksi tidak terhambat popup berulang, data tersimpan lokal, serta laporan dapat diekspor untuk arsip bisnis.

## Fitur Utama

- Login admin dan owner.
- Dashboard ringkasan penjualan, stok, transaksi terbaru, hutang, dan piutang.
- Manajemen menu aktif dan arsip.
- Manajemen stok dengan status stok aman atau menipis.
- Kasir/transaksi dengan validasi stok.
- Riwayat transaksi dan detail item.
- Manajemen hutang dan piutang.
- Konfirmasi untuk aksi berisiko seperti hapus, arsip, dan tandai lunas.
- Toast non-blocking untuk feedback sukses.
- Export laporan lengkap ke Excel dan PDF.
- Backup database SQLite.
- Seed menu awal HanyarNgopi yang berjalan satu kali saat database pertama dibuat.

## Tech Stack

- Java 22
- JavaFX 21.0.6
- SQLite JDBC
- Maven
- FXML + CSS

## Struktur Project

```text
src/main/java/com/pos
├── config      # Koneksi dan inisialisasi database
├── controller  # Controller JavaFX/FXML
├── dao         # Data access object untuk SQLite
├── model       # Model/domain object
├── service     # Business logic
├── util        # Helper alert dan toast
└── MainApp.java

src/main/resources/com/pos/view
├── css         # Styling JavaFX
├── image       # Logo aplikasi
└── *.fxml      # Layout halaman
```

## Cara Menjalankan

Pastikan Java 22 sudah terpasang.

```bash
./mvnw javafx:run
```

Untuk Windows:

```bash
mvnw.cmd javafx:run
```

## Akun Default

Saat aplikasi pertama kali berjalan, user default akan dibuat otomatis jika belum ada.

```text
Username: admin
Password: 123

Username: owner
Password: 123
```

## Build

Compile project:

```bash
mvnw.cmd -DskipTests compile
```

Clean compile:

```bash
mvnw.cmd -DskipTests clean compile
```

Catatan: jika `clean` gagal karena file di folder `target` sedang terkunci, tutup aplikasi JavaFX yang masih berjalan lalu ulangi perintah build.

## Database

Database menggunakan SQLite lokal dan dibuat otomatis saat aplikasi dijalankan. Struktur tabel, migrasi ringan, default user, dan seed menu awal dikelola dari kode inisialisasi database.

## Export dan Backup

Menu laporan menyediakan:

- Export Excel untuk laporan lengkap.
- Export PDF dengan desain laporan rapi.
- Backup database ke file `.db`.

Laporan lengkap mencakup penjualan, stok, hutang, dan piutang dalam satu file.

## Status Project

Project ini dikembangkan sebagai aplikasi desktop manajemen UMKM untuk HanyarNgopi, dengan fokus pada penggunaan lokal, data sederhana, dan workflow kasir yang cepat.
