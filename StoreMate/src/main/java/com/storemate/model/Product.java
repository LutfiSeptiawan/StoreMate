package com.storemate.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** MODEL + ENTITY ORM: satu objek Product = satu baris tabel "products". */
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;

    @Column(name = "code", nullable = false, unique = true, length = 30)
    public String code;

    @Column(name = "name", nullable = false, length = 120)
    public String name;

    @Column(name = "category", nullable = false, length = 60)
    public String category;

    @Column(name = "buy_price", nullable = false)
    public double buyPrice;

    @Column(name = "sell_price", nullable = false)
    public double sellPrice;

    @Column(name = "stock", nullable = false)
    public int stock;

    @Column(name = "min_stock", nullable = false)
    public int minStock;

    @Column(name = "image_path")
    public String imagePath;        // path relatif file foto (null = belum ada foto)

    /** Logika peringatan: stok menipis bila stok <= batas minimum. */
    public boolean isLowStock() { return stock <= minStock; }

    @Override public String toString() { return code + " - " + name; } // untuk JComboBox
}