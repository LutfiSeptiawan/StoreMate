package com.storemate.view;

import com.storemate.controller.PurchaseController;
import com.storemate.model.Product;
import com.storemate.model.PurchaseLine;
import com.storemate.model.Supplier;
import com.storemate.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** VIEW Pembelian / Purchase Order. Daftar barang PO dan penyimpanan dikelola PurchaseController. */
public class PurchasePanel extends JPanel implements Refreshable {
    private final PurchaseController controller = new PurchaseController();
    private final JComboBox<Supplier> cbSupplier = new JComboBox<>();
    private final JComboBox<Product> cbProduct = new JComboBox<>();
    private final JTextField txtQty = new JTextField("1", 6), txtPrice = new JTextField(10);
    private final DefaultTableModel model = UI.model("Kode", "Barang", "Qty", "Harga Beli", "Subtotal");
    private final JTable table = UI.table(model);
    private final JLabel lblTotal = new JLabel("Rp 0");
    private final Runnable onSaved;      // dipanggil setelah PO tersimpan (mis. memuat ulang Dashboard & Data Barang)

    public PurchasePanel(Runnable onSaved) {
        this.onSaved = onSaved;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(UI.BG);

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        top.add(UI.title("Pembelian Barang (Purchase Order)"), BorderLayout.NORTH);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.add(new JLabel("Supplier:")); row.add(cbSupplier);
        row.add(new JLabel("Barang:")); row.add(cbProduct);
        row.add(new JLabel("Qty:")); row.add(txtQty);
        row.add(new JLabel("Harga Beli:")); row.add(txtPrice);
        JButton add = UI.button("Tambah", UI.PRIMARY);
        add.addActionListener(e -> addLine());
        row.add(add);
        top.add(row, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        // pilih barang -> isi otomatis harga beli terakhir
        cbProduct.addActionListener(e -> {
            Product p = (Product) cbProduct.getSelectedItem();
            if (p != null) txtPrice.setText(String.valueOf((long) p.buyPrice));
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        bottom.setOpaque(false);
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 20));
        JButton rem = UI.button("Hapus Baris", UI.WARN), save = UI.button("Simpan Pembelian", UI.OK);
        rem.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) { controller.removeLine(r); reload(); }
        });
        save.addActionListener(e -> save());
        bottom.add(rem); bottom.add(new JLabel("Total:")); bottom.add(lblTotal); bottom.add(save);
        add(bottom, BorderLayout.SOUTH);
    }

    @Override public void refresh() {
        try {
            cbSupplier.removeAllItems();
            for (Supplier s : controller.suppliers()) cbSupplier.addItem(s);
            cbProduct.removeAllItems();
            for (Product p : controller.products()) cbProduct.addItem(p);
            reload();
        } catch (Exception e) { UI.error(this, "Gagal memuat data: " + e.getMessage()); }
    }

    private void addLine() {
        try {
            controller.addLine((Product) cbProduct.getSelectedItem(), txtQty.getText(), txtPrice.getText());
            reload();
        } catch (IllegalArgumentException e) { UI.warn(this, e.getMessage()); }
    }

    private void reload() {
        model.setRowCount(0);
        for (PurchaseLine l : controller.lines())
            model.addRow(new Object[]{l.product.code, l.product.name, l.qty, Fmt.rp(l.price), Fmt.rp(l.subtotal())});
        lblTotal.setText(Fmt.rp(controller.total()));
    }

    private void save() {
        try {
            String po = controller.save((Supplier) cbSupplier.getSelectedItem());
            UI.info(this, "Pembelian tersimpan (" + po + ").\nStok barang telah bertambah otomatis.");
            refresh();
            if (onSaved != null) onSaved.run();
        } catch (IllegalArgumentException e) { UI.warn(this, e.getMessage());
        } catch (Exception e) { UI.error(this, "Gagal menyimpan pembelian: " + e.getMessage()); }
    }
}
