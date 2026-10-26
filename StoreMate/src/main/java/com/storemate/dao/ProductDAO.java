package com.storemate.dao;

import com.storemate.config.DBConnection;
import com.storemate.model.Product;
import com.storemate.util.Session;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CRUD barang. Operasi tulis (tambah/ubah/hapus) hanya untuk Admin. */
public class ProductDAO {

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.id = rs.getInt("id");
        p.code = rs.getString("code");
        p.name = rs.getString("name");
        p.category = rs.getString("category");
        p.buyPrice = rs.getDouble("buy_price");
        p.sellPrice = rs.getDouble("sell_price");
        p.stock = rs.getInt("stock");
        p.minStock = rs.getInt("min_stock");
        p.imagePath = rs.getString("image_path");
        return p;
    }

    /** Cari berdasarkan kode/nama/kategori; keyword kosong = semua barang. */
    public List<Product> findAll(String keyword) throws SQLException {
        String sql = "SELECT * FROM products WHERE code LIKE ? OR name LIKE ? OR category LIKE ? ORDER BY name";
        List<Product> list = new ArrayList<>();
        String k = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, k); ps.setString(2, k); ps.setString(3, k);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** Barang yang stoknya <= batas minimum (dasar alert stok menipis). */
    public List<Product> findLowStock() throws SQLException {
        List<Product> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM products WHERE stock <= min_stock ORDER BY stock")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void insert(Product p) throws SQLException {
        Session.requireAdmin();
        String sql = "INSERT INTO products(code,name,category,buy_price,sell_price,stock,min_stock,image_path) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            ps.executeUpdate();
        }
    }

    public void update(Product p) throws SQLException {
        Session.requireAdmin();
        String sql = "UPDATE products SET code=?,name=?,category=?,buy_price=?,sell_price=?,stock=?,min_stock=?,image_path=? WHERE id=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            ps.setInt(9, p.id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        Session.requireAdmin();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM products WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Product p) throws SQLException {
        ps.setString(1, p.code); ps.setString(2, p.name); ps.setString(3, p.category);
        ps.setDouble(4, p.buyPrice); ps.setDouble(5, p.sellPrice);
        ps.setInt(6, p.stock); ps.setInt(7, p.minStock);
        ps.setString(8, p.imagePath);                        // null -> NULL di database
    }
}
