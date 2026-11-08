package com.storemate.dao;

import com.storemate.model.Product;
import com.storemate.util.JpaUtil;
import com.storemate.util.Session;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.List;

/**
 * CRUD barang memakai ORM (EntityManager + JPQL).
 * persist() = INSERT, merge() = UPDATE, remove() = DELETE; Hibernate membuat SQL-nya.
 * Operasi tulis (tambah/ubah/hapus) hanya untuk Admin.
 */
public class ProductDAO {

    /** Cari berdasarkan kode/nama/kategori; keyword kosong = semua barang. */
    public List<Product> findAll(String keyword) throws SQLException {
        String k = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                    "SELECT p FROM Product p WHERE p.code LIKE :k OR p.name LIKE :k OR p.category LIKE :k ORDER BY p.name",
                    Product.class)
                    .setParameter("k", k)
                    .getResultList();
        } catch (PersistenceException e) {
            throw JpaUtil.toSql(e);
        } finally {
            em.close();
        }
    }

    /** Barang yang stoknya <= batas minimum (dasar alert stok menipis). */
    public List<Product> findLowStock() throws SQLException {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery("SELECT p FROM Product p WHERE p.stock <= p.minStock ORDER BY p.stock", Product.class)
                    .getResultList();
        } catch (PersistenceException e) {
            throw JpaUtil.toSql(e);
        } finally {
            em.close();
        }
    }

    public void insert(Product p) throws SQLException {
        Session.requireAdmin();
        inTransaction(em -> em.persist(p));
    }

    public void update(Product p) throws SQLException {
        Session.requireAdmin();
        inTransaction(em -> em.merge(p));
    }

    public void delete(int id) throws SQLException {
        Session.requireAdmin();
        inTransaction(em -> {
            Product ref = em.find(Product.class, id);
            if (ref != null) em.remove(ref);
        });
    }

    // ----------------------------------------------------------------------
    /** Aksi di dalam satu transaksi: begin -> aksi -> commit, rollback bila gagal. */
    private interface Action { void run(EntityManager em); }

    private void inTransaction(Action action) throws SQLException {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            action.run(em);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw JpaUtil.toSql(e);
        } finally {
            em.close();
        }
    }
}
