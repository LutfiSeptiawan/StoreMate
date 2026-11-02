package com.storemate.model;

/** MODEL: satu baris barang pada Purchase Order. */
public class PurchaseLine {
    public final Product product;
    public int qty;
    public double price;

    public PurchaseLine(Product p, int qty, double price) { this.product = p; this.qty = qty; this.price = price; }
    public double subtotal() { return qty * price; }
}
