-- =====================================================================
--  StoreMate - Skrip Database MySQL / MariaDB
--  Cara pakai: phpMyAdmin -> tab "SQL" -> tempel seluruh isi -> Go
--  Aman dijalankan ulang (tabel tidak dihapus). Untuk mengulang dari nol:
--  jalankan  DROP DATABASE storemate;  lalu jalankan skrip ini lagi.
-- =====================================================================
CREATE DATABASE IF NOT EXISTS storemate
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE storemate;

-- 1. Pengguna & role (RBAC): ADMIN atau KASIR
CREATE TABLE IF NOT EXISTS users (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  username      VARCHAR(50)  NOT NULL UNIQUE,
  password_hash CHAR(64)     NOT NULL,              -- SHA-256 (hex)
  full_name     VARCHAR(100) NOT NULL,
  role          ENUM('ADMIN','KASIR') NOT NULL,
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Produk / inventaris
CREATE TABLE IF NOT EXISTS products (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  code       VARCHAR(30)  NOT NULL UNIQUE,
  name       VARCHAR(120) NOT NULL,
  category   VARCHAR(60)  NOT NULL,
  buy_price  DECIMAL(15,2) NOT NULL DEFAULT 0,
  sell_price DECIMAL(15,2) NOT NULL DEFAULT 0,
  stock      INT NOT NULL DEFAULT 0,
  min_stock  INT NOT NULL DEFAULT 5,
  image_path VARCHAR(255) NULL,                     -- lokasi file foto, mis. images/MI-001_1730000000000.jpg
  CONSTRAINT chk_stock_nonneg CHECK (stock >= 0)    -- stok tidak boleh minus
) ENGINE=InnoDB;

-- 3. Supplier
CREATE TABLE IF NOT EXISTS suppliers (
  id      INT AUTO_INCREMENT PRIMARY KEY,
  name    VARCHAR(120) NOT NULL,
  phone   VARCHAR(30),
  address VARCHAR(255)
) ENGINE=InnoDB;

-- 4. Transaksi penjualan (header) & detail
CREATE TABLE IF NOT EXISTS sales (
  id             INT AUTO_INCREMENT PRIMARY KEY,
  invoice_no     VARCHAR(30) NOT NULL UNIQUE,
  user_id        INT NOT NULL,
  total          DECIMAL(15,2) NOT NULL DEFAULT 0,
  paid           DECIMAL(15,2) NOT NULL DEFAULT 0,
  change_amount  DECIMAL(15,2) NOT NULL DEFAULT 0,
  payment_method ENUM('TUNAI','TRANSFER') NOT NULL DEFAULT 'TUNAI',
  created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sale_items (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  sale_id    INT NOT NULL,
  product_id INT NOT NULL,
  qty        INT NOT NULL,
  buy_price  DECIMAL(15,2) NOT NULL,   -- snapshot harga beli (untuk hitung laba)
  sell_price DECIMAL(15,2) NOT NULL,   -- snapshot harga jual
  subtotal   DECIMAL(15,2) NOT NULL,
  FOREIGN KEY (sale_id)    REFERENCES sales(id)    ON DELETE CASCADE,
  FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB;

-- 5. Pembelian / Purchase Order (header) & detail
CREATE TABLE IF NOT EXISTS purchases (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  po_no       VARCHAR(30) NOT NULL UNIQUE,
  supplier_id INT NOT NULL,
  user_id     INT NOT NULL,
  total       DECIMAL(15,2) NOT NULL DEFAULT 0,
  created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
  FOREIGN KEY (user_id)     REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS purchase_items (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  purchase_id INT NOT NULL,
  product_id  INT NOT NULL,
  qty         INT NOT NULL,
  buy_price   DECIMAL(15,2) NOT NULL,
  subtotal    DECIMAL(15,2) NOT NULL,
  FOREIGN KEY (purchase_id) REFERENCES purchases(id) ON DELETE CASCADE,
  FOREIGN KEY (product_id)  REFERENCES products(id)
) ENGINE=InnoDB;

-- ---------------------- DATA AWAL (SEED) -----------------------------
-- Login:  admin / admin123   |   kasir / kasir123
INSERT IGNORE INTO users (username, password_hash, full_name, role) VALUES
 ('admin', SHA2('admin123',256), 'Administrator', 'ADMIN'),
 ('kasir', SHA2('kasir123',256), 'Kasir Toko',    'KASIR');

INSERT INTO suppliers (name, phone, address)
SELECT * FROM (SELECT 'PT Sumber Pangan Jaya' AS n, '021-5551234' AS p, 'Jl. Raya Margonda No. 10, Depok' AS a
               UNION ALL
               SELECT 'CV Minuman Segar', '021-5559876', 'Jl. Kelapa Dua No. 5, Jakarta') AS s
WHERE NOT EXISTS (SELECT 1 FROM suppliers);

INSERT IGNORE INTO products (code, name, category, buy_price, sell_price, stock, min_stock) VALUES
 ('MI-001','Indomie Goreng',        'Mi Instan',       2800,  3500, 100, 20),
 ('MI-002','Mie Sedaap Soto',       'Mi Instan',       2700,  3400,  60, 20),
 ('SN-001','Chitato Sapi Panggang', 'Makanan Ringan',  9000, 11000,  30, 10),
 ('SN-002','Oreo Original',         'Makanan Ringan',  7000,  8500,  12, 10),
 ('MN-001','Aqua 600ml',            'Minuman Kemasan', 2500,  3500,  80, 24),
 ('MN-002','Teh Botol Sosro 450ml', 'Minuman Kemasan', 3800,  5000,   6, 12),
 ('BM-001','Minyak Goreng 1L',      'Bumbu Dapur',    16000, 19000,  25, 10),
 ('BM-002','Gula Pasir 1kg',        'Bumbu Dapur',    13500, 16000,   4,  8);
