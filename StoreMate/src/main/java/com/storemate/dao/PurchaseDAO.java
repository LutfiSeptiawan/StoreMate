package com.storemate.dao;

import com.storemate.config.DBConnection;
import com.storemate.model.Product;
import com.storemate.util.Session;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/** Purchase Order / barang masuk. Khusus Admin; otomatis menambah stok. */
public class PurchaseDAO {

    /** Satu baris barang di PO. */
    public static class Line {
        public final Product product;
        public int qty;
        public double price;
        public Line(Product p, int qty, double price) { this.product = p; this.qty = qty; this.price = price; }
        public double subtotal() { return qty * price; }
    }

    /** @return nomor PO yang tersimpan. */
    public String save(int supplierId, List<Line> lines) throws SQLException {
        Session.requireAdmin();                                   // RBAC: kasir ditolak
        String po = "PO-" + new SimpleDateFormat("yyyyMMdd-HHmmssSSS").format(new Date());
        double total = 0;
        for (Line l : lines) total += l.subtotal();

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO purchases(po_no,supplier_id,user_id,total) VALUES(?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, po); ps.setInt(2, supplierId);
                    ps.setInt(3, Session.get().id); ps.setDouble(4, total);
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) { k.next(); id = k.getInt(1); }
                }
                for (Line l : lines) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO purchase_items(purchase_id,product_id,qty,buy_price,subtotal) VALUES(?,?,?,?,?)")) {
                        ps.setInt(1, id); ps.setInt(2, l.product.id); ps.setInt(3, l.qty);
                        ps.setDouble(4, l.price); ps.setDouble(5, l.subtotal());
                        ps.executeUpdate();
                    }
                    // Tambah stok + perbarui harga beli terakhir
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE products SET stock = stock + ?, buy_price = ? WHERE id=?")) {
                        ps.setInt(1, l.qty); ps.setDouble(2, l.price); ps.setInt(3, l.product.id);
                        ps.executeUpdate();
                    }
                }
                c.commit();
                return po;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }
}
