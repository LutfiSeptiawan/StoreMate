package com.storemate.controller;

import org.springframework.stereotype.Component;

import com.storemate.dao.ProductDAO;
import com.storemate.model.Product;
import com.storemate.util.ImageUtil;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

/** CONTROLLER: validasi & penyimpanan data barang (termasuk file foto). */
@Component
public class ProductController {
    private final ProductDAO dao;

    public ProductController(ProductDAO dao) { this.dao = dao; }

    public List<Product> list(String keyword) throws SQLException { return dao.findAll(keyword); }
    public List<Product> lowStock() throws SQLException { return dao.findLowStock(); }

    /** Foto dikecilkan ke JPEG maks 300px; baru ditulis ke disk saat save(). */
    public byte[] prepareImage(File f) throws IOException { return ImageUtil.toJpegBytes(f, 300); }

    /**
     * Validasi isian form lalu bentuk objek Product.
     * @throws IllegalArgumentException berisi pesan yang siap ditampilkan ke pengguna
     */
    public Product validate(Product existing, String code, String name, String category,
                            String buy, String sell, String stock, String min) {
        if (code.trim().isEmpty() || name.trim().isEmpty())
            throw new IllegalArgumentException("Kode dan nama barang wajib diisi.");
        Product p = new Product();
        if (existing != null) { p.id = existing.id; p.imagePath = existing.imagePath; }
        try {
            p.buyPrice = Double.parseDouble(buy.trim());
            p.sellPrice = Double.parseDouble(sell.trim());
            p.stock = Integer.parseInt(stock.trim());
            p.minStock = Integer.parseInt(min.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Harga, stok, dan batas minimum harus berupa angka.");
        }
        if (p.buyPrice < 0 || p.sellPrice < 0 || p.stock < 0 || p.minStock < 0)
            throw new IllegalArgumentException("Nilai tidak boleh negatif.");
        p.code = code.trim();
        p.name = name.trim();
        p.category = category.trim().isEmpty() ? "Lainnya" : category.trim();
        return p;
    }

    /** Simpan (insert bila isNew, selain itu update) + kelola file foto. */
    public void save(Product p, boolean isNew, byte[] newImage, boolean removeImage) throws SQLException, IOException {
        String oldPath = p.imagePath, stored = null;
        try {
            if (newImage != null) { stored = ImageUtil.store(newImage, p.code); p.imagePath = stored; }
            else if (removeImage) p.imagePath = null;
            if (isNew) dao.insert(p); else dao.update(p);
            if (oldPath != null && !oldPath.equals(p.imagePath)) ImageUtil.delete(oldPath);
        } catch (SQLIntegrityConstraintViolationException e) {
            if (stored != null) ImageUtil.delete(stored);
            throw new IllegalArgumentException("Kode barang \"" + p.code + "\" sudah dipakai barang lain.");
        } catch (SQLException | IOException | RuntimeException e) {
            if (stored != null) ImageUtil.delete(stored);   // batalkan file yang terlanjur ditulis
            throw e;
        }
    }

    public void delete(Product p) throws SQLException {
        try {
            dao.delete(p.id);
            ImageUtil.delete(p.imagePath);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalStateException("Barang tidak dapat dihapus karena sudah memiliki riwayat transaksi.");
        }
    }
}
