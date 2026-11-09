package com.storemate.dao;

import com.storemate.model.Supplier;
import com.storemate.util.Session;
import java.sql.SQLException;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * CRUD supplier (khusus Admin) memakai Spring JDBC.
 * JdbcTemplate membuka/menutup koneksi dan statement sendiri, sehingga tidak ada try-with-resources manual.
 */
@Repository
public class SupplierDAO {
    private final JdbcTemplate jdbc;

    /** Constructor injection: JdbcTemplate dibuat otomatis oleh Spring Boot. */
    public SupplierDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** Memetakan satu baris hasil query menjadi objek Supplier. */
    private static final RowMapper<Supplier> MAPPER = (rs, rowNum) -> {
        Supplier s = new Supplier();
        s.id = rs.getInt("id");
        s.name = rs.getString("name");
        s.phone = rs.getString("phone");
        s.address = rs.getString("address");
        return s;
    };

    public List<Supplier> findAll() throws SQLException {
        Session.requireAdmin();
        return jdbc.query("SELECT * FROM suppliers ORDER BY name", MAPPER);
    }

    public void insert(Supplier s) throws SQLException {
        Session.requireAdmin();
        jdbc.update("INSERT INTO suppliers(name,phone,address) VALUES(?,?,?)", s.name, s.phone, s.address);
    }

    public void update(Supplier s) throws SQLException {
        Session.requireAdmin();
        jdbc.update("UPDATE suppliers SET name=?,phone=?,address=? WHERE id=?", s.name, s.phone, s.address, s.id);
    }

    public void delete(int id) throws SQLException {
        Session.requireAdmin();
        try {
            jdbc.update("DELETE FROM suppliers WHERE id=?", id);
        } catch (DataAccessException e) {
            throw SqlErrors.toSql(e);       // supplier sudah dipakai PO -> SQLIntegrityConstraintViolationException
        }
    }
}
