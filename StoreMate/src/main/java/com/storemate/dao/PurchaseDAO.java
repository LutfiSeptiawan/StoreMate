package com.storemate.dao;

import com.storemate.model.PurchaseLine;
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

/** Purchase Order / barang masuk. Khusus Admin; otomatis menambah stok dalam satu transaksi Spring. */
@Repository
public class PurchaseDAO {
    private final JdbcTemplate jdbc;

    public PurchaseDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** @return nomor PO yang tersimpan. */
    @Transactional(rollbackFor = Exception.class)
    public String save(int supplierId, List<PurchaseLine> lines) throws SQLException {
        Session.requireAdmin();                                   // RBAC: kasir ditolak
        String po = "PO-" + new SimpleDateFormat("yyyyMMdd-HHmmssSSS").format(new Date());
        double total = 0;
        for (PurchaseLine l : lines) total += l.subtotal();
        final double poTotal = total;
        final int userId = Session.get().getId();

        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO purchases(po_no,supplier_id,user_id,total) VALUES(?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, po);
            ps.setInt(2, supplierId);
            ps.setInt(3, userId);
            ps.setDouble(4, poTotal);
            return ps;
        }, keys);
        int purchaseId = keys.getKey().intValue();

        for (PurchaseLine l : lines) {
            jdbc.update("INSERT INTO purchase_items(purchase_id,product_id,qty,buy_price,subtotal) VALUES(?,?,?,?,?)",
                    purchaseId, l.product.id, l.qty, l.price, l.subtotal());
            // Tambah stok + perbarui harga beli terakhir
            jdbc.update("UPDATE products SET stock = stock + ?, buy_price = ? WHERE id=?",
                    l.qty, l.price, l.product.id);
        }
        return po;
    }
}
