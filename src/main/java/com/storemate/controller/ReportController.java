package com.storemate.controller;

import com.storemate.dao.ProductDAO;
import com.storemate.dao.ReportDAO;
import com.storemate.model.Product;
import com.storemate.model.ReportRow;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

/** CONTROLLER: data Dashboard dan Laporan Laba Rugi. */
public class ReportController {
    private final ReportDAO dao = new ReportDAO();
    private final ProductDAO productDAO = new ProductDAO();

    /** Hasil laporan + total keseluruhan. */
    public static class Result {
        public List<ReportRow> rows;
        public double revenue, cogs;
        public int trx;
        public double profit() { return revenue - cogs; }   // laba bersih (negatif = rugi)
    }

    // ----- Dashboard -----
    public double todaySales() throws SQLException { return dao.todaySales(); }
    public int todayTransactions() throws SQLException { return dao.todayTransactions(); }
    public double[] monthlySalesThisYear() throws SQLException { return dao.monthlySales(Calendar.getInstance().get(Calendar.YEAR)); }
    public List<Product> lowStock() throws SQLException { return productDAO.findLowStock(); }

    // ----- Laporan -----
    /**
     * @param monthly true = rekap per bulan, false = per hari
     * @throws IllegalArgumentException format/urutan tanggal salah
     */
    public Result build(String fromText, String toText, boolean monthly) throws SQLException {
        java.sql.Date from, to;
        try {
            from = java.sql.Date.valueOf(fromText.trim());
            to = java.sql.Date.valueOf(toText.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Format tanggal harus yyyy-MM-dd, contoh 2026-10-25.");
        }
        if (from.after(to)) throw new IllegalArgumentException("Tanggal awal tidak boleh melewati tanggal akhir.");

        Result r = new Result();
        r.rows = monthly ? dao.monthly(from, to) : dao.daily(from, to);
        for (ReportRow row : r.rows) { r.revenue += row.revenue; r.cogs += row.cogs; r.trx += row.trx; }
        return r;
    }

    public void exportCsv(Result r, File file) throws IOException {
        if (r == null || r.rows.isEmpty()) throw new IllegalStateException("Tidak ada data untuk diekspor.");
        try (PrintWriter w = new PrintWriter(file, "UTF-8")) {
            w.println("Periode,Jumlah Transaksi,Penjualan,HPP,Laba");
            for (ReportRow row : r.rows)
                w.println(row.date + "," + row.trx + "," + (long) row.revenue + "," + (long) row.cogs + "," + (long) row.profit());
        }
    }
}
