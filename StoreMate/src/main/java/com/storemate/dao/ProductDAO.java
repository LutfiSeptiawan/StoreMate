package com.storemate.dao;

import com.storemate.model.Product;
import com.storemate.repository.ProductRepository;
import com.storemate.util.Session;
import java.sql.SQLException;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

/**
 * Akses data barang lewat Spring Data JPA. save() = INSERT/UPDATE, deleteById() = DELETE.
 * Operasi tulis hanya untuk Admin. Exception Spring diubah kembali ke SQLException agar
 * Controller dan View tidak perlu diubah.
 */
@Repository
public class ProductDAO {
    private final ProductRepository repository;

    /** Constructor injection. */
    public ProductDAO(ProductRepository repository) { this.repository = repository; }

    /** Cari berdasarkan kode/nama/kategori; keyword kosong = semua barang. */
    public List<Product> findAll(String keyword) throws SQLException {
        String k = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        try {
            return repository.search(k);
        } catch (DataAccessException e) {
            throw SqlErrors.toSql(e);
        }
    }

    public List<Product> findLowStock() throws SQLException {
        try {
            return repository.findLowStock();
        } catch (DataAccessException e) {
            throw SqlErrors.toSql(e);
        }
    }

    public void insert(Product p) throws SQLException {
        Session.requireAdmin();
        save(p);
    }

    public void update(Product p) throws SQLException {
        Session.requireAdmin();
        save(p);
    }

    public void delete(int id) throws SQLException {
        Session.requireAdmin();
        try {
            repository.deleteById(id);
        } catch (DataAccessException e) {
            throw SqlErrors.toSql(e);
        }
    }

    private void save(Product p) throws SQLException {
        try {
            repository.save(p);              // id == 0 -> INSERT baru, id > 0 -> UPDATE
        } catch (DataAccessException e) {
            throw SqlErrors.toSql(e);
        }
    }
}
