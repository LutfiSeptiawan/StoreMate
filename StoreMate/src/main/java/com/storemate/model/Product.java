package com.storemate.model;

/** Data barang/inventaris. */
public class Product {
    public int id, stock, minStock;
    public String code, name, category;
    public double buyPrice, sellPrice;
    public String imagePath;        // path relatif file foto (null = belum ada foto)

    /** Logika peringatan: stok menipis bila stok <= batas minimum. */
    public boolean isLowStock() { return stock <= minStock; }

    @Override public String toString() { return code + " - " + name; } // untuk JComboBox
}
