package com.storemate.controller;

import com.storemate.dao.ProductDAO;
import com.storemate.dao.SaleDAO;
import com.storemate.model.CartItem;
import com.storemate.model.Receipt;
import com.storemate.model.Product;
import com.storemate.util.Fmt;
import com.storemate.util.Session;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** CONTROLLER: keranjang belanja & proses pembayaran POS. View hanya menampilkan isi keranjang. */
public class PosController {
    private final ProductDAO productDAO = new ProductDAO();
    private final SaleDAO saleDAO = new SaleDAO();
    private final List<CartItem> cart = new ArrayList<>();
    private List<CartItem> lastSold = new ArrayList<>();
    private String lastMethod = "TUNAI";

    public List<Product> products(String keyword) throws SQLException { return productDAO.findAll(keyword); }
    public List<CartItem> cart() { return Collections.unmodifiableList(cart); }
    public boolean isEmpty() { return cart.isEmpty(); }

    public double total() {
        double t = 0;
        for (CartItem it : cart) t += it.subtotal();
        return t;
    }

    /** @throws IllegalStateException bila stok tidak mencukupi */
    public void add(Product p) {
        for (CartItem it : cart) {
            if (it.product.id == p.id) {
                if (it.qty + 1 > p.stock) throw new IllegalStateException("Stok " + p.name + " hanya " + p.stock + ".");
                it.qty++;
                return;
            }
        }
        if (p.stock <= 0) throw new IllegalStateException("Stok " + p.name + " habis.");
        cart.add(new CartItem(p, 1));
    }

    /** Ubah qty baris ke-index sebesar delta; qty <= 0 menghapus baris. */
    public void changeQty(int index, int delta) {
        CartItem it = cart.get(index);
        int nq = it.qty + delta;
        if (nq <= 0) { cart.remove(index); return; }
        if (nq > it.product.stock) throw new IllegalStateException("Stok " + it.product.name + " hanya " + it.product.stock + ".");
        it.qty = nq;
    }

    public void remove(int index) { cart.remove(index); }
    public void clear() { cart.clear(); }

    /**
     * Proses pembayaran. TRANSFER = uang pas. Stok dikurangi di database secara atomik.
     * @throws IllegalArgumentException keranjang kosong / uang kurang
     */
    public Receipt checkout(String method, double paid) throws SQLException {
        if (cart.isEmpty()) throw new IllegalArgumentException("Keranjang masih kosong.");
        if ("TRANSFER".equals(method)) paid = total();
        if (paid < total()) throw new IllegalArgumentException("Uang yang diterima kurang dari total belanja.");
        List<CartItem> snapshot = new ArrayList<>(cart);
        Receipt r = saleDAO.save(cart, method, paid);
        lastSold = snapshot; lastMethod = method;
        cart.clear();
        return r;
    }

    /** Nama barang yang baru terjual dan kini stoknya menipis (untuk peringatan ke kasir). */
    public List<String> lowStockAfterSale() throws SQLException {
        List<String> names = new ArrayList<>();
        for (Product low : productDAO.findLowStock())
            for (CartItem it : lastSold)
                if (it.product.id == low.id) names.add(low.name + " (sisa " + low.stock + ")");
        return names;
    }

    /** Teks struk siap cetak. */
    public String receiptText(Receipt r) {
        StringBuilder sb = new StringBuilder();
        sb.append("        STOREMATE\n     Struk Penjualan\n");
        sb.append("--------------------------------\n");
        sb.append("No   : ").append(r.invoiceNo).append('\n');
        sb.append("Tgl  : ").append(r.dateTime).append('\n');
        sb.append("Kasir: ").append(Session.get().getFullName()).append('\n');
        sb.append("--------------------------------\n");
        for (CartItem it : lastSold) {
            sb.append(it.product.name).append('\n');
            sb.append(String.format("  %d x %-9s %12s%n", it.qty, Fmt.num(it.product.sellPrice), Fmt.num(it.subtotal())));
        }
        sb.append("--------------------------------\n");
        sb.append(String.format("%-14s %17s%n", "TOTAL", Fmt.rp(r.total)));
        sb.append(String.format("%-14s %17s%n", "Bayar (" + lastMethod + ")", Fmt.rp(r.paid)));
        sb.append(String.format("%-14s %17s%n", "Kembali", Fmt.rp(r.change)));
        sb.append("--------------------------------\n   Terima kasih atas kunjungan\n");
        return sb.toString();
    }
}
