package com.storemate.model;

/** Satu baris keranjang belanja di POS. */
public class CartItem {
    public final Product product;
    public int qty;

    public CartItem(Product product, int qty) { this.product = product; this.qty = qty; }
    public double subtotal() { return product.sellPrice * qty; }
}
