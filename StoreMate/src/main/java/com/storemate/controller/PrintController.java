package com.storemate.controller;

import com.storemate.dao.ProductDAO;
import com.storemate.model.Product;
import com.storemate.util.Session;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;
import org.springframework.stereotype.Component;

/**
 * CONTROLLER pencetakan laporan (JasperReports).
 * Alurnya: ambil data dari DAO -> kirim ke template .jrxml -> tampilkan di JasperViewer.
 * Laporan tidak terhubung ke database sendiri; datanya disiapkan di sini.
 */
@Component
public class PrintController {
    /** Letak template di src/main/resources (ikut masuk ke classpath saat aplikasi dibuild). */
    private static final String TEMPLATE_DAFTAR_BARANG = "/Reports/DaftarBarang.jrxml";

    private final ProductDAO productDAO;
    private JasperReport daftarBarang;      // hasil kompilasi disimpan agar cetak berikutnya cepat

    /** Constructor injection. */
    public PrintController(ProductDAO productDAO) { this.productDAO = productDAO; }

    /** Isi template dengan data barang -> JasperPrint (dokumen laporan siap tampil/ekspor). */
    public JasperPrint daftarBarangPrint() throws SQLException, JRException {
        // Setiap baris = Map dengan kunci SAMA PERSIS dengan nama field di template
        List<Map<String, ?>> rows = new ArrayList<>();
        for (Product p : productDAO.findAll("")) {
            Map<String, Object> row = new HashMap<>();
            row.put("code", p.code);
            row.put("name", p.name);
            row.put("category", p.category);
            row.put("buy_price", p.buyPrice);       // Double
            row.put("sell_price", p.sellPrice);     // Double
            row.put("stock", p.stock);              // Integer
            row.put("min_stock", p.minStock);       // Integer
            rows.add(row);
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("PRINTED_BY", Session.get() == null ? "-" : Session.get().getFullName());

        return JasperFillManager.fillReport(compiledDaftarBarang(), parameters, new JRMapCollectionDataSource(rows));
    }

    /** Membuka jendela pratinjau laporan (di dalamnya ada tombol cetak dan simpan sebagai PDF). */
    public void showDaftarBarang() throws SQLException, JRException {
        JasperViewer.viewReport(daftarBarangPrint(), false);
    }

    /** Kompilasi .jrxml menjadi JasperReport (sekali saja, lalu disimpan). */
    private synchronized JasperReport compiledDaftarBarang() throws JRException {
        if (daftarBarang == null) {
            try (InputStream in = PrintController.class.getResourceAsStream(TEMPLATE_DAFTAR_BARANG)) {
                if (in == null) throw new JRException("Template laporan tidak ditemukan: " + TEMPLATE_DAFTAR_BARANG);
                daftarBarang = JasperCompileManager.compileReport(in);
            } catch (IOException e) {
                throw new JRException(e);
            }
        }
        return daftarBarang;
    }
}
