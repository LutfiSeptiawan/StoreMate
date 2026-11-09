package com.storemate.dao;

import com.storemate.model.ReportRow;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/** Query ringkasan untuk Dashboard dan Laporan Laba Rugi (Spring JDBC). */
@Repository
public class ReportDAO {
    private final JdbcTemplate jdbc;

    public ReportDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private static final RowMapper<ReportRow> ROW_MAPPER = (rs, rowNum) -> {
        ReportRow r = new ReportRow();
        r.date = rs.getString("d");
        r.trx = rs.getInt("trx");
        r.revenue = rs.getDouble("rev");
        r.cogs = rs.getDouble("cogs");
        return r;
    };

    public double todaySales() throws SQLException {
        Double v = jdbc.queryForObject("SELECT COALESCE(SUM(total),0) FROM sales WHERE DATE(created_at)=CURDATE()", Double.class);
        return v == null ? 0 : v;
    }

    public int todayTransactions() throws SQLException {
        Integer v = jdbc.queryForObject("SELECT COUNT(*) FROM sales WHERE DATE(created_at)=CURDATE()", Integer.class);
        return v == null ? 0 : v;
    }

    /** Total penjualan per bulan (indeks 0 = Januari) pada tahun tertentu. */
    public double[] monthlySales(int year) throws SQLException {
        double[] m = new double[12];
        jdbc.query("SELECT MONTH(created_at) mth, SUM(total) t FROM sales WHERE YEAR(created_at)=? GROUP BY mth",
                (RowCallbackHandler) rs -> m[rs.getInt("mth") - 1] = rs.getDouble("t"), year);
        return m;
    }

    /** Rekap per hari dalam rentang tanggal (inklusif). Laba = penjualan - HPP. */
    public List<ReportRow> daily(java.sql.Date from, java.sql.Date to) throws SQLException {
        return grouped("DATE(s.created_at)", from, to);
    }

    /** Rekap per bulan (format yyyy-MM) dalam rentang tanggal. */
    public List<ReportRow> monthly(java.sql.Date from, java.sql.Date to) throws SQLException {
        return grouped("DATE_FORMAT(s.created_at, '%Y-%m')", from, to);
    }

    private List<ReportRow> grouped(String periodExpr, java.sql.Date from, java.sql.Date to) {
        String sql = "SELECT " + periodExpr + " d, COUNT(DISTINCT s.id) trx, "
                   + "SUM(si.subtotal) rev, SUM(si.qty * si.buy_price) cogs "
                   + "FROM sales s JOIN sale_items si ON si.sale_id = s.id "
                   + "WHERE DATE(s.created_at) BETWEEN ? AND ? GROUP BY d ORDER BY d";
        return jdbc.query(sql, ROW_MAPPER, from, to);
    }
}
