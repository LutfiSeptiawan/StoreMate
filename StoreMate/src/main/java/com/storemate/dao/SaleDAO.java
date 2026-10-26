package com.storemate.dao;

import com.storemate.config.DBConnection;
import com.storemate.model.CartItem;
import com.storemate.util.Session;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/** Menyimpan transaksi POS dalam SATU transaksi database (atomik). */
public class SaleDAO {

    /** Hasil transaksi untuk dicetak di struk. */
    public static class Receipt {
        public String invoiceNo, dateTime;
        public double total, paid, change;
    }

    /**
     * Alur: insert header -> untuk tiap item kurangi stok (hanya jika stok cukup)
     * dan simpan detail dengan harga dari DB -> hitung total -> validasi pembayaran -> commit.
     * Gagal di langkah mana pun = rollback, sehingga stok tidak pernah setengah berubah.
     */
    public Receipt save(List<CartItem> cart, String method, double paid) throws SQLException {
        Date now = new Date();
        String invoice = "INV-" + new SimpleDateFormat("yyyyMMdd-HHmmssSSS").format(now);

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int saleId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO sales(invoice_no,user_id,payment_method) VALUES(?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, invoice);
                    ps.setInt(2, Session.get().id);
                    ps.setString(3, method);
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) { k.next(); saleId = k.getInt(1); }
                }

                for (CartItem it : cart) {
                    // Pengurangan stok atomik: gagal (0 baris) bila stok tidak mencukupi
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE products SET stock = stock - ? WHERE id=? AND stock >= ?")) {
                        ps.setInt(1, it.qty); ps.setInt(2, it.product.id); ps.setInt(3, it.qty);
                        if (ps.executeUpdate() == 0)
                            throw new SQLException("Stok tidak cukup untuk: " + it.product.name);
                    }
                    // Harga diambil dari DB (bukan dari layar) agar tidak bisa dimanipulasi/basi
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO sale_items(sale_id,product_id,qty,buy_price,sell_price,subtotal) "
                          + "SELECT ?, id, ?, buy_price, sell_price, sell_price * ? FROM products WHERE id=?")) {
                        ps.setInt(1, saleId); ps.setInt(2, it.qty); ps.setInt(3, it.qty); ps.setInt(4, it.product.id);
                        ps.executeUpdate();
                    }
                }

                double total;
                try (PreparedStatement ps = c.prepareStatement("SELECT SUM(subtotal) FROM sale_items WHERE sale_id=?")) {
                    ps.setInt(1, saleId);
                    try (ResultSet rs = ps.executeQuery()) { rs.next(); total = rs.getDouble(1); }
                }
                if (paid < total) throw new SQLException("Uang yang dibayarkan kurang dari total belanja.");

                double change = paid - total;
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE sales SET total=?, paid=?, change_amount=? WHERE id=?")) {
                    ps.setDouble(1, total); ps.setDouble(2, paid); ps.setDouble(3, change); ps.setInt(4, saleId);
                    ps.executeUpdate();
                }
                c.commit();

                Receipt r = new Receipt();
                r.invoiceNo = invoice; r.total = total; r.paid = paid; r.change = change;
                r.dateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(now);
                return r;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }
}
