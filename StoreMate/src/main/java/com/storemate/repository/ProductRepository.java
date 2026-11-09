package com.storemate.repository;

import com.storemate.model.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA untuk barang. Metode CRUD (save, findById, deleteById, ...) sudah disediakan
 * oleh JpaRepository; di sini hanya query khusus yang ditambahkan.
 */
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p WHERE p.code LIKE :k OR p.name LIKE :k OR p.category LIKE :k ORDER BY p.name")
    List<Product> search(@Param("k") String keyword);

    /** Barang yang stoknya <= batas minimum (dasar alert stok menipis). */
    @Query("SELECT p FROM Product p WHERE p.stock <= p.minStock ORDER BY p.stock")
    List<Product> findLowStock();
}
