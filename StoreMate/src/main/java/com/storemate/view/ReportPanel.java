package com.storemate.view;

import com.storemate.dao.ReportDAO;
import com.storemate.util.*;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Laporan penjualan & laba rugi per rentang tanggal.
 * Laba bersih = SUM(harga jual x qty) - SUM(harga beli x qty), memakai harga saat transaksi.
 */
public class ReportPanel extends JPanel implements Refreshable {
    private final ReportDAO dao = new ReportDAO();
    private final DefaultTableModel model = UI.model("Tanggal", "Jml Transaksi", "Penjualan", "Harga Pokok (HPP)", "Laba / Rugi");
    private final JTextField txtFrom = new JTextField(10), txtTo = new JTextField(10);
    private final JLabel lblSum = new JLabel(" ");
    private List<ReportDAO.Row> rows;

    public ReportPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(UI.BG);

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        top.add(UI.title("Laporan Keuangan & Laba Rugi"), BorderLayout.NORTH);
        JPanel f = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        f.setOpaque(false);
        f.add(new JLabel("Dari (yyyy-MM-dd):")); f.add(txtFrom);
        f.add(new JLabel("Sampai:")); f.add(txtTo);
        JButton show = UI.button("Tampilkan", UI.PRIMARY), today = UI.button("Hari Ini", UI.PRIMARY),
                month = UI.button("Bulan Ini", UI.PRIMARY), exp = UI.button("Ekspor CSV", UI.OK);
        show.addActionListener(e -> load());
        today.addActionListener(e -> { setRange(LocalDate.now(), LocalDate.now()); load(); });
        month.addActionListener(e -> { setRange(LocalDate.now().withDayOfMonth(1), LocalDate.now()); load(); });
        exp.addActionListener(e -> export());
        f.add(show); f.add(today); f.add(month); f.add(exp);
        top.add(f, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        add(new JScrollPane(UI.table(model)), BorderLayout.CENTER);
        lblSum.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(lblSum, BorderLayout.SOUTH);
    }

    private void setRange(LocalDate a, LocalDate b) { txtFrom.setText(a.toString()); txtTo.setText(b.toString()); }

    @Override public void refresh() {
        if (txtFrom.getText().isEmpty()) setRange(LocalDate.now().withDayOfMonth(1), LocalDate.now());
        load();
    }

    private void load() {
        java.sql.Date from, to;
        try {
            from = java.sql.Date.valueOf(txtFrom.getText().trim());
            to = java.sql.Date.valueOf(txtTo.getText().trim());
        } catch (IllegalArgumentException e) { UI.warn(this, "Format tanggal harus yyyy-MM-dd, contoh 2026-10-25."); return; }
        if (from.after(to)) { UI.warn(this, "Tanggal awal tidak boleh melewati tanggal akhir."); return; }

        try {
            rows = dao.daily(from, to);
            model.setRowCount(0);
            double rev = 0, cogs = 0; int trx = 0;
            for (ReportDAO.Row r : rows) {
                model.addRow(new Object[]{r.date, r.trx, Fmt.rp(r.revenue), Fmt.rp(r.cogs), Fmt.rp(r.profit())});
                rev += r.revenue; cogs += r.cogs; trx += r.trx;
            }
            double profit = rev - cogs;
            lblSum.setText(String.format("Transaksi: %d   |   Penjualan: %s   |   %s: %s",
                    trx, Fmt.rp(rev), profit >= 0 ? "LABA BERSIH" : "RUGI", Fmt.rp(Math.abs(profit))));
            lblSum.setForeground(profit >= 0 ? UI.OK : UI.DANGER);
        } catch (Exception e) { UI.error(this, "Gagal memuat laporan: " + e.getMessage()); }
    }

    private void export() {
        if (rows == null || rows.isEmpty()) { UI.warn(this, "Tidak ada data untuk diekspor."); return; }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("laporan_" + txtFrom.getText() + "_" + txtTo.getText() + ".csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (PrintWriter w = new PrintWriter(fc.getSelectedFile(), "UTF-8")) {
            w.println("Tanggal,Jumlah Transaksi,Penjualan,HPP,Laba");
            for (ReportDAO.Row r : rows)
                w.println(r.date + "," + r.trx + "," + (long) r.revenue + "," + (long) r.cogs + "," + (long) r.profit());
            UI.info(this, "Laporan berhasil diekspor.");
        } catch (Exception e) { UI.error(this, "Gagal ekspor: " + e.getMessage()); }
    }
}
