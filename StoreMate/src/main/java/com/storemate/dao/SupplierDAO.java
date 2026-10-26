package com.storemate.dao;

import com.storemate.config.DBConnection;
import com.storemate.model.Supplier;
import com.storemate.util.Session;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CRUD supplier (khusus Admin). */
public class SupplierDAO {

    public List<Supplier> findAll() throws SQLException {
        Session.requireAdmin();
        List<Supplier> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM suppliers ORDER BY name")) {
            while (rs.next()) {
                Supplier s = new Supplier();
                s.id = rs.getInt("id"); s.name = rs.getString("name");
                s.phone = rs.getString("phone"); s.address = rs.getString("address");
                list.add(s);
            }
        }
        return list;
    }

    public void insert(Supplier s) throws SQLException {
        Session.requireAdmin();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO suppliers(name,phone,address) VALUES(?,?,?)")) {
            ps.setString(1, s.name); ps.setString(2, s.phone); ps.setString(3, s.address);
            ps.executeUpdate();
        }
    }

    public void update(Supplier s) throws SQLException {
        Session.requireAdmin();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE suppliers SET name=?,phone=?,address=? WHERE id=?")) {
            ps.setString(1, s.name); ps.setString(2, s.phone); ps.setString(3, s.address); ps.setInt(4, s.id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        Session.requireAdmin();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM suppliers WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
