package com.storemate.view;

import com.storemate.controller.ReportController;
import com.storemate.model.ReportRow;
import com.storemate.util.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * VIEW Laporan Laba Rugi (rekap harian / bulanan + filter tanggal + ekspor CSV).
 * Laba bersih = penjualan - HPP, dihitung oleh ReportController/ReportDAO.
 */
public class ReportPanel extends JPanel implements Refreshable {
    private final ReportController controller = new ReportController();
    private final DefaultTableModel model = UI.model("Periode", "Jml Transaksi", "Penjualan", "Harga Pokok (HPP)", "Laba / Rugi");
    private final JTextField txtFrom = new JTextField(10), txtTo = new JTextField(10);
    private final JComboBox<String> cbMode = new JComboBox<>(new String[]{"Harian", "Bulanan"});
    private final JLabel lblSum = new JLabel(" ");
    private ReportController.Result result;

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
        f.add(new JLabel("Rekap:")); f.add(cbMode);
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
        try {
            result = controller.build(txtFrom.getText(), txtTo.getText(), cbMode.getSelectedIndex() == 1);
            model.setRowCount(0);
            for (ReportRow r : result.rows)
                model.addRow(new Object[]{r.date, r.trx, Fmt.rp(r.revenue), Fmt.rp(r.cogs), Fmt.rp(r.profit())});
            double profit = result.profit();
            lblSum.setText(String.format("Transaksi: %d   |   Penjualan: %s   |   %s: %s",
                    result.trx, Fmt.rp(result.revenue), profit >= 0 ? "LABA BERSIH" : "RUGI", Fmt.rp(Math.abs(profit))));
            lblSum.setForeground(profit >= 0 ? UI.OK : UI.DANGER);
        } catch (IllegalArgumentException e) { UI.warn(this, e.getMessage());
        } catch (Exception e) { UI.error(this, "Gagal memuat laporan: " + e.getMessage()); }
    }

    private void export() {
        if (result == null || result.rows.isEmpty()) { UI.warn(this, "Tidak ada data untuk diekspor."); return; }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("laporan_" + txtFrom.getText() + "_" + txtTo.getText() + ".csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            controller.exportCsv(result, fc.getSelectedFile());
            UI.info(this, "Laporan berhasil diekspor.");
        } catch (Exception e) { UI.error(this, "Gagal ekspor: " + e.getMessage()); }
    }
}
