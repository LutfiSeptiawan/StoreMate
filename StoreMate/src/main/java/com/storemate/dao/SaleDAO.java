package com.storemate.dao;

import com.storemate.model.CartItem;
import com.storemate.model.Receipt;
import com.storemate.util.Session;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Menyimpan transaksi POS dalam SATU transaksi database.
 * Transaksi dikelola Spring lewat @Transactional: commit otomatis bila metode selesai,
 * rollback otomatis bila terjadi exception. Tidak ada setAutoCommit/commit/rollback manual lagi.
 */
@Repository
public class SaleDAO {
    private final JdbcTemplate jdbc;

    public SaleDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /**
     * Alur: insert header -> untuk tiap item kurangi stok (hanya jika stok cukup)
     * dan simpan detail dengan harga dari DB -> hitung total -> validasi pembayaran.
     * rollbackFor = Exception.class: SQLException (checked) juga membatalkan seluruh transaksi,
     * sehingga stok tidak pernah setengah berubah.
     */
    @Transactional(rollbackFor = Exception.class)
    public Receipt save(List<CartItem> cart, String method, double paid) throws SQLException {
        Date now = new Date();
        String invoice = "INV-" + new SimpleDateFormat("yyyyMMdd-HHmmssSSS").format(now);
        int userId = Session.get().getId();

        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO sales(invoice_no,user_id,payment_method) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, invoice);
            ps.setInt(2, userId);
            ps.setString(3, method);
            return ps;
        }, keys);
        int saleId = keys.getKey().intValue();

        for (CartItem it : cart) {
            // Pengurangan stok atomik: gagal (0 baris) bila stok tidak mencukupi
            int changed = jdbc.update("UPDATE products SET stock = stock - ? WHERE id=? AND stock >= ?",
                    it.qty, it.product.id, it.qty);
            if (changed == 0) throw new SQLException("Stok tidak cukup untuk: " + it.product.name);

            // Harga diambil dari DB (bukan dari layar) agar tidak bisa dimanipulasi/basi
            jdbc.update("INSERT INTO sale_items(sale_id,product_id,qty,buy_price,sell_price,subtotal) "
                      + "SELECT ?, id, ?, buy_price, sell_price, sell_price * ? FROM products WHERE id=?",
                    saleId, it.qty, it.qty, it.product.id);
        }

        Double total = jdbc.queryForObject("SELECT COALESCE(SUM(subtotal),0) FROM sale_items WHERE sale_id=?",
                Double.class, saleId);
        double sum = total == null ? 0 : total;
        if (paid < sum) throw new SQLException("Uang yang dibayarkan kurang dari total belanja.");

        double change = paid - sum;
        jdbc.update("UPDATE sales SET total=?, paid=?, change_amount=? WHERE id=?", sum, paid, change, saleId);

        Receipt r = new Receipt();
        r.invoiceNo = invoice; r.total = sum; r.paid = paid; r.change = change;
        r.dateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(now);
        return r;
    }
}
