# StoreMate - Aplikasi Manajemen Toko Makanan

Aplikasi desktop (Java Swing) untuk kasir (POS), inventaris, supplier, pembelian, dan laporan laba rugi.
Dibangun bertahap sesuai materi praktikum: OOP, MVC, ORM (Hibernate/JPA), Spring Boot, dan JasperReports.

## Teknologi
Java 21 | Spring Boot 3.2.5 (Spring Data JPA, Spring JDBC) | Hibernate | MySQL/MariaDB | JasperReports 6.21 | Maven

## Fitur
| Peran | Fitur |
|---|---|
| Admin | Dashboard (ringkasan harian, grafik bulanan, alert stok menipis), Data Barang (CRUD + foto), Kasir, Supplier, Pembelian (stok otomatis bertambah), Laporan Laba Rugi (harian/bulanan, ekspor CSV), Cetak Laporan Daftar Barang (JasperReports) |
| Kasir | Kasir (POS: tunai/transfer, struk, stok berkurang otomatis) dan Daftar Barang (hanya lihat) |

## Cara menjalankan
1. Nyalakan MySQL (XAMPP), lalu jalankan `database/storemate.sql` di phpMyAdmin (tab SQL).
2. Buka project di Apache NetBeans (Maven), tunggu dependensi terunduh (butuh internet).
3. Sesuaikan user/password database di `src/main/resources/application.properties` bila perlu.
4. **Clean and Build**, lalu **Run** (kelas utama `com.storemate.Main`).

Akun awal: `admin` / `admin123` dan `kasir` / `kasir123`.

## Struktur (MVC)
```
src/main/java/com/storemate/
├── Main.java          titik masuk (@SpringBootApplication)
├── model/             Entity & model data
├── repository/        Spring Data JPA
├── dao/               akses data (Spring Data JPA & Spring JDBC)
├── controller/        logika aplikasi (Spring bean)
├── view/              tampilan Swing
└── util/              pembantu (Session/RBAC, Hash, format, gambar)
src/main/resources/
├── application.properties
└── Reports/DaftarBarang.jrxml     template JasperReports
images/                            foto produk (dibuat otomatis)
```
