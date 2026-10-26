package com.storemate.view;

import com.storemate.dao.ProductDAO;
import com.storemate.dao.ReportDAO;
import com.storemate.model.Product;
import com.storemate.util.*;
import java.awt.*;
import java.util.Calendar;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Dashboard Admin: ringkasan hari ini, grafik penjualan bulanan, alert stok menipis. */
public class DashboardPanel extends JPanel implements Refreshable {
    private final ReportDAO reportDAO = new ReportDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final JLabel lblSales = new JLabel("-"), lblTrx = new JLabel("-"), lblLow = new JLabel("-");
    private final JLabel lblAlert = new JLabel(" ");
    private final BarChart chart = new BarChart();
    private final DefaultTableModel lowModel = UI.model("Kode", "Nama Barang", "Stok", "Batas Min");

    public DashboardPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(UI.BG);

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(UI.title("Dashboard"), BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 0));
        stats.setOpaque(false);
        stats.add(card("Penjualan Hari Ini", lblSales, UI.PRIMARY));
        stats.add(card("Transaksi Hari Ini", lblTrx, UI.OK));
        stats.add(card("Barang Stok Menipis", lblLow, UI.DANGER));
        top.add(stats, BorderLayout.CENTER);

        // Alert stok: warna mencolok
        lblAlert.setOpaque(true);
        lblAlert.setFont(UI.BOLD);
        lblAlert.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        top.add(lblAlert, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        JTable lowTable = UI.table(lowModel);
        lowTable.setForeground(UI.DANGER);
        JScrollPane sp = new JScrollPane(lowTable);
        sp.setBorder(BorderFactory.createTitledBorder("Barang yang Perlu Segera Dibeli"));
        sp.setPreferredSize(new Dimension(380, 0));

        add(chart, BorderLayout.CENTER);
        add(sp, BorderLayout.EAST);
    }

    private JPanel card(String title, JLabel value, Color color) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(color);
        p.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        JLabel t = new JLabel(title); t.setForeground(Color.WHITE);
        value.setForeground(Color.WHITE); value.setFont(new Font("SansSerif", Font.BOLD, 24));
        p.add(t, BorderLayout.NORTH); p.add(value, BorderLayout.CENTER);
        return p;
    }

    @Override public void refresh() {
        try {
            lblSales.setText(Fmt.rp(reportDAO.todaySales()));
            lblTrx.setText(String.valueOf(reportDAO.todayTransactions()));
            chart.data = reportDAO.monthlySales(Calendar.getInstance().get(Calendar.YEAR));
            chart.repaint();

            List<Product> low = productDAO.findLowStock();
            lowModel.setRowCount(0);
            for (Product p : low) lowModel.addRow(new Object[]{p.code, p.name, p.stock, p.minStock});
            lblLow.setText(String.valueOf(low.size()));
            if (low.isEmpty()) {
                lblAlert.setBackground(new Color(0xC8E6C9)); lblAlert.setForeground(new Color(0x1B5E20));
                lblAlert.setText("Semua stok barang dalam kondisi aman.");
            } else {
                lblAlert.setBackground(UI.DANGER); lblAlert.setForeground(Color.WHITE);
                lblAlert.setText("PERINGATAN: " + low.size() + " barang stoknya menipis / habis. Segera buat Pembelian (PO).");
            }
        } catch (Exception e) {
            UI.error(this, "Gagal memuat dashboard: " + e.getMessage());
        }
    }

    /** Grafik batang sederhana (digambar manual, tanpa library tambahan). */
    private static class BarChart extends JPanel {
        double[] data = new double[12];
        private static final String[] MONTHS = {"Jan","Feb","Mar","Apr","Mei","Jun","Jul","Agu","Sep","Okt","Nov","Des"};

        BarChart() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createTitledBorder("Grafik Penjualan Bulanan (tahun berjalan)"));
        }

        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), padL = 30, padR = 20, padT = 50, padB = 40;
            double max = 1;
            for (double d : data) max = Math.max(max, d);
            int slot = (w - padL - padR) / 12, barW = Math.max(10, slot - 14);
            int base = h - padB, chartH = h - padT - padB;

            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(padL, base, w - padR, base);
            for (int i = 0; i < 12; i++) {
                int bh = (int) (data[i] / max * chartH);
                int x = padL + i * slot + (slot - barW) / 2;
                g.setColor(UI.PRIMARY);
                g.fillRect(x, base - bh, barW, bh);
                g.setColor(Color.DARK_GRAY);
                g.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g.drawString(MONTHS[i], x, base + 16);
                if (data[i] > 0) {
                    String s = data[i] >= 1_000_000 ? String.format("%.1fjt", data[i] / 1_000_000) : Fmt.num(data[i] / 1000) + "rb";
                    g.drawString(s, x, base - bh - 4);
                }
            }
        }
    }
}
