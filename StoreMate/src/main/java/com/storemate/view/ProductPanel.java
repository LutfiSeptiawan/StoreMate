package com.storemate.view;

import com.storemate.dao.ProductDAO;
import com.storemate.model.Product;
import com.storemate.util.*;
import java.awt.*;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Daftar + CRUD barang. canEdit=false (Kasir) -> tombol tambah/edit/hapus tidak ditampilkan.
 * Baris dengan stok <= batas minimum diberi warna merah dan label "Stok Menipis".
 */
public class ProductPanel extends JPanel implements Refreshable {
    private final ProductDAO dao = new ProductDAO();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Foto", "Kode", "Nama Barang", "Kategori", "Harga Beli", "Harga Jual", "Stok", "Batas Min", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Class<?> getColumnClass(int c) { return c == 0 ? javax.swing.Icon.class : Object.class; }
    };
    private final JTable table = UI.table(model);
    private final JTextField txtSearch = new JTextField(22);
    private List<Product> current = new ArrayList<>();      // sejajar dengan baris tabel

    public ProductPanel(boolean canEdit) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(UI.BG);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.setOpaque(false);
        top.add(UI.title(canEdit ? "Data Barang & Inventaris" : "Daftar Barang"));
        top.add(Box.createHorizontalStrut(20));
        top.add(new JLabel("Cari:"));
        top.add(txtSearch);
        JButton find = UI.button("Cari", UI.PRIMARY);
        find.addActionListener(e -> refresh());
        txtSearch.addActionListener(e -> refresh());
        top.add(find);
        add(top, BorderLayout.NORTH);

        // Renderer: merah bila stok menipis
        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                boolean low = row < current.size() && current.get(t.convertRowIndexToModel(row)).isLowStock();
                if (!sel) { c.setBackground(low ? new Color(0xFFEBEE) : Color.WHITE); }
                c.setForeground(low ? UI.DANGER : Color.BLACK);
                setFont(low && col == 8 ? UI.BOLD : t.getFont());
                return c;
            }
        };
        table.setDefaultRenderer(Object.class, r);
        table.setRowHeight(52);                                   // ruang untuk thumbnail
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        add(new JScrollPane(table), BorderLayout.CENTER);

        if (canEdit) {
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            bottom.setOpaque(false);
            JButton add = UI.button("+ Tambah", UI.OK), edit = UI.button("Edit", UI.PRIMARY), del = UI.button("Hapus", UI.DANGER);
            add.addActionListener(e -> form(null));
            edit.addActionListener(e -> { Product p = selected(); if (p != null) form(p); });
            del.addActionListener(e -> delete());
            bottom.add(add); bottom.add(edit); bottom.add(del);
            add(bottom, BorderLayout.SOUTH);
        }
    }

    private Product selected() {
        int row = table.getSelectedRow();
        if (row < 0) { UI.warn(this, "Pilih barang pada tabel terlebih dahulu."); return null; }
        return current.get(row);
    }

    @Override public void refresh() {
        try {
            current = dao.findAll(txtSearch.getText());
            model.setRowCount(0);
            for (Product p : current) {
                model.addRow(new Object[]{ImageUtil.iconPath(p.imagePath, 46), p.code, p.name, p.category, Fmt.rp(p.buyPrice), Fmt.rp(p.sellPrice),
                        p.stock, p.minStock, p.isLowStock() ? "Stok Menipis" : "Aman"});
            }
        } catch (Exception e) {
            UI.error(this, "Gagal memuat barang: " + e.getMessage());
        }
    }

    private void delete() {
        Product p = selected();
        if (p == null) return;
        if (!UI.confirm(this, "Hapus barang \"" + p.name + "\"?")) return;       // konfirmasi hapus
        try {
            dao.delete(p.id);
            ImageUtil.delete(p.imagePath);                    // bersihkan file foto
            refresh();
        } catch (SQLIntegrityConstraintViolationException e) {
            UI.error(this, "Barang tidak dapat dihapus karena sudah memiliki riwayat transaksi.");
        } catch (Exception e) {
            UI.error(this, "Gagal menghapus: " + e.getMessage());
        }
    }

    /** Form tambah (existing == null) atau edit. */
    private void form(Product existing) {
        JTextField code = new JTextField(existing == null ? "" : existing.code, 18);
        JTextField name = new JTextField(existing == null ? "" : existing.name);
        JComboBox<String> cat = new JComboBox<>(new String[]{"Makanan Ringan", "Minuman Kemasan", "Mi Instan", "Bumbu Dapur", "Lainnya"});
        cat.setEditable(true);
        if (existing != null) cat.setSelectedItem(existing.category);
        JTextField buy = new JTextField(existing == null ? "" : String.valueOf((long) existing.buyPrice));
        JTextField sell = new JTextField(existing == null ? "" : String.valueOf((long) existing.sellPrice));
        JTextField stock = new JTextField(existing == null ? "0" : String.valueOf(existing.stock));
        JTextField min = new JTextField(existing == null ? "5" : String.valueOf(existing.minStock));

        // ---- Foto produk (disimpan ke database sebagai BLOB) ----
        // Foto baru ditahan di memori dulu; baru ditulis ke folder images/ saat tombol OK dan data tersimpan
        final byte[][] pending = {null};
        final boolean[] removed = {false};
        JLabel preview = new JLabel(ImageUtil.iconPath(existing == null ? null : existing.imagePath, 110), SwingConstants.CENTER);
        preview.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        preview.setPreferredSize(new Dimension(120, 120));
        JButton pick = new JButton("Pilih Foto..."), noPic = new JButton("Hapus Foto");
        pick.addActionListener(ev -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Gambar (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif"));
            if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
            try {
                pending[0] = ImageUtil.toJpegBytes(fc.getSelectedFile(), 300);   // dikecilkan otomatis
                removed[0] = false;
                preview.setIcon(ImageUtil.iconBytes(pending[0], 110));
            } catch (java.io.IOException ex) { UI.error(this, ex.getMessage()); }
        });
        noPic.addActionListener(ev -> { pending[0] = null; removed[0] = true; preview.setIcon(ImageUtil.iconPath(null, 110)); });
        JPanel photo = new JPanel(new BorderLayout(0, 4));
        JPanel pb = new JPanel(new GridLayout(0, 1, 0, 4));
        pb.add(pick); pb.add(noPic);
        photo.add(preview, BorderLayout.CENTER); photo.add(pb, BorderLayout.EAST);

        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.add(new JLabel("Foto Produk")); p.add(photo);
        p.add(new JLabel("Kode Barang")); p.add(code);
        p.add(new JLabel("Nama Barang")); p.add(name);
        p.add(new JLabel("Kategori")); p.add(cat);
        p.add(new JLabel("Harga Beli")); p.add(buy);
        p.add(new JLabel("Harga Jual")); p.add(sell);
        p.add(new JLabel("Stok")); p.add(stock);
        p.add(new JLabel("Batas Minimum Stok")); p.add(min);

        while (true) {
            int ok = JOptionPane.showConfirmDialog(this, p, existing == null ? "Tambah Barang" : "Edit Barang",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (ok != JOptionPane.OK_OPTION) return;

            // ---- Validasi input ----
            if (code.getText().trim().isEmpty() || name.getText().trim().isEmpty()) {
                UI.warn(this, "Kode dan nama barang wajib diisi."); continue;
            }
            Product np = existing == null ? new Product() : existing;
            try {
                np.buyPrice = Double.parseDouble(buy.getText().trim());
                np.sellPrice = Double.parseDouble(sell.getText().trim());
                np.stock = Integer.parseInt(stock.getText().trim());
                np.minStock = Integer.parseInt(min.getText().trim());
            } catch (NumberFormatException ex) {
                UI.warn(this, "Harga, stok, dan batas minimum harus berupa angka."); continue;
            }
            if (np.buyPrice < 0 || np.sellPrice < 0 || np.stock < 0 || np.minStock < 0) {
                UI.warn(this, "Nilai tidak boleh negatif."); continue;
            }
            if (np.sellPrice < np.buyPrice && !UI.confirm(this, "Harga jual lebih rendah dari harga beli (rugi). Lanjutkan?")) continue;

            np.code = code.getText().trim();
            np.name = name.getText().trim();
            np.category = String.valueOf(cat.getSelectedItem()).trim();
            String oldPath = existing == null ? null : existing.imagePath;
            String stored = null;
            try {
                np.imagePath = oldPath;
                if (pending[0] != null) { stored = ImageUtil.store(pending[0], np.code); np.imagePath = stored; }
                else if (removed[0]) np.imagePath = null;
                if (existing == null) dao.insert(np); else dao.update(np);
                if (oldPath != null && !oldPath.equals(np.imagePath)) ImageUtil.delete(oldPath);  // buang foto lama
                refresh();
                return;
            } catch (SQLIntegrityConstraintViolationException ex) {
                if (stored != null) ImageUtil.delete(stored);       // batalkan file yang sudah terlanjur ditulis
                np.imagePath = oldPath;
                UI.warn(this, "Kode barang \"" + np.code + "\" sudah dipakai barang lain.");
            } catch (Exception ex) {
                if (stored != null) ImageUtil.delete(stored);
                np.imagePath = oldPath;
                UI.error(this, "Gagal menyimpan: " + ex.getMessage()); return;
            }
        }
    }
}
