package com.storemate.dao;

import com.storemate.config.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Query ringkasan untuk Dashboard dan Laporan Laba Rugi. */
public class ReportDAO {

    /** Satu baris laporan harian. */
    public static class Row {
        public String date;
        public int trx;
        public double revenue, cogs;                 // pendapatan, HPP (harga pokok)
        public double profit() { return revenue - cogs; }
    }

    public double todaySales() throws SQLException {
        return scalar("SELECT COALESCE(SUM(total),0) FROM sales WHERE DATE(created_at)=CURDATE()");
    }

    public int todayTransactions() throws SQLException {
        return (int) scalar("SELECT COUNT(*) FROM sales WHERE DATE(created_at)=CURDATE()");
    }

    /** Total penjualan per bulan (indeks 0 = Januari) pada tahun tertentu. */
    public double[] monthlySales(int year) throws SQLException {
        double[] m = new double[12];
        String sql = "SELECT MONTH(created_at) mth, SUM(total) t FROM sales WHERE YEAR(created_at)=? GROUP BY mth";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) m[rs.getInt("mth") - 1] = rs.getDouble("t");
            }
        }
        return m;
    }

    /** Rekap harian dalam rentang tanggal (inklusif). Laba = penjualan - HPP. */
    public List<Row> daily(java.sql.Date from, java.sql.Date to) throws SQLException {
        String sql = "SELECT DATE(s.created_at) d, COUNT(DISTINCT s.id) trx, "
                   + "SUM(si.subtotal) rev, SUM(si.qty * si.buy_price) cogs "
                   + "FROM sales s JOIN sale_items si ON si.sale_id = s.id "
                   + "WHERE DATE(s.created_at) BETWEEN ? AND ? GROUP BY d ORDER BY d";
        List<Row> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, from); ps.setDate(2, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Row r = new Row();
                    r.date = rs.getString("d"); r.trx = rs.getInt("trx");
                    r.revenue = rs.getDouble("rev"); r.cogs = rs.getDouble("cogs");
                    list.add(r);
                }
            }
        }
        return list;
    }

    private double scalar(String sql) throws SQLException {
        try (Connection c = DBConnection.getConnection(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }
}
