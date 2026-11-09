package com.storemate.controller;

import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;

import com.storemate.dao.ProductDAO;
import com.storemate.dao.PurchaseDAO;
import com.storemate.dao.SupplierDAO;
import com.storemate.model.Product;
import com.storemate.model.PurchaseLine;
import com.storemate.model.Supplier;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** CONTROLLER: Purchase Order. Menyimpan PO otomatis menambah stok (dilakukan di PurchaseDAO). */
@Component
@Scope("prototype")   // satu objek baru per pemanggilan (menyimpan state keranjang)
public class PurchaseController {
    private final PurchaseDAO purchaseDAO;
    private final SupplierDAO supplierDAO;
    private final ProductDAO productDAO;

    public PurchaseController(PurchaseDAO purchaseDAO, SupplierDAO supplierDAO, ProductDAO productDAO) {
        this.purchaseDAO = purchaseDAO; this.supplierDAO = supplierDAO; this.productDAO = productDAO;
    }
    private final List<PurchaseLine> lines = new ArrayList<>();

    public List<Supplier> suppliers() throws SQLException { return supplierDAO.findAll(); }
    public List<Product> products() throws SQLException { return productDAO.findAll(""); }
    public List<PurchaseLine> lines() { return Collections.unmodifiableList(lines); }

    public double total() {
        double t = 0;
        for (PurchaseLine l : lines) t += l.subtotal();
        return t;
    }

    /** @throws IllegalArgumentException input tidak valid */
    public void addLine(Product p, String qtyText, String priceText) {
        if (p == null) throw new IllegalArgumentException("Pilih barang terlebih dahulu.");
        int qty; double price;
        try {
            qty = Integer.parseInt(qtyText.trim());
            price = Double.parseDouble(priceText.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Qty dan harga beli harus berupa angka.");
        }
        if (qty <= 0 || price < 0) throw new IllegalArgumentException("Qty harus > 0 dan harga tidak boleh negatif.");
        lines.add(new PurchaseLine(p, qty, price));
    }

    public void removeLine(int index) { lines.remove(index); }

    /** @return nomor PO */
    public String save(Supplier s) throws SQLException {
        if (s == null) throw new IllegalArgumentException("Tambahkan data supplier terlebih dahulu.");
        if (lines.isEmpty()) throw new IllegalArgumentException("Daftar barang masih kosong.");
        String po = purchaseDAO.save(s.id, lines);
        lines.clear();
        return po;
    }
}
