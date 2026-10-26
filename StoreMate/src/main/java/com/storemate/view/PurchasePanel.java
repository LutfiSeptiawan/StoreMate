package com.storemate.view;

import com.storemate.dao.ProductDAO;
import com.storemate.dao.PurchaseDAO;
import com.storemate.dao.SupplierDAO;
import com.storemate.model.Product;
import com.storemate.model.Supplier;
import com.storemate.util.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Purchase Order / barang masuk dari supplier. Menyimpan = stok barang otomatis bertambah. */
public class PurchasePanel extends JPanel implements Refreshable {
    private final PurchaseDAO purchaseDAO = new PurchaseDAO();
    private final JComboBox<Supplier> cbSupplier = new JComboBox<>();
    private final JComboBox<Product> cbProduct = new JComboBox<>();
    private final JTextField txtQty = new JTextField("1", 6), txtPrice = new JTextField(10);
    private final DefaultTableModel model = UI.model("Kode", "Barang", "Qty", "Harga Beli", "Subtotal");
    private final JTable table = UI.table(model);
    private final JLabel lblTotal = new JLabel("Rp 0");
    private final List<PurchaseDAO.Line> lines = new ArrayList<>();

    public PurchasePanel() {
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
            if (r >= 0) { lines.remove(r); reload(); }
        });
        save.addActionListener(e -> save());
        bottom.add(rem); bottom.add(new JLabel("Total:")); bottom.add(lblTotal); bottom.add(save);
        add(bottom, BorderLayout.SOUTH);
    }

    @Override public void refresh() {
        try {
            cbSupplier.removeAllItems();
            for (Supplier s : new SupplierDAO().findAll()) cbSupplier.addItem(s);
            cbProduct.removeAllItems();
            for (Product p : new ProductDAO().findAll("")) cbProduct.addItem(p);
        } catch (Exception e) { UI.error(this, "Gagal memuat data: " + e.getMessage()); }
    }

    private void addLine() {
        Product p = (Product) cbProduct.getSelectedItem();
        if (p == null) { UI.warn(this, "Pilih barang terlebih dahulu."); return; }
        int qty; double price;
        try {
            qty = Integer.parseInt(txtQty.getText().trim());
            price = Double.parseDouble(txtPrice.getText().trim());
        } catch (NumberFormatException e) { UI.warn(this, "Qty dan harga beli harus berupa angka."); return; }
        if (qty <= 0 || price < 0) { UI.warn(this, "Qty harus > 0 dan harga tidak boleh negatif."); return; }
        lines.add(new PurchaseDAO.Line(p, qty, price));
        reload();
    }

    private void reload() {
        model.setRowCount(0);
        double total = 0;
        for (PurchaseDAO.Line l : lines) {
            model.addRow(new Object[]{l.product.code, l.product.name, l.qty, Fmt.rp(l.price), Fmt.rp(l.subtotal())});
            total += l.subtotal();
        }
        lblTotal.setText(Fmt.rp(total));
    }

    private void save() {
        Supplier s = (Supplier) cbSupplier.getSelectedItem();
        if (s == null) { UI.warn(this, "Tambahkan data supplier terlebih dahulu."); return; }
        if (lines.isEmpty()) { UI.warn(this, "Daftar barang masih kosong."); return; }
        try {
            String po = purchaseDAO.save(s.id, lines);
            UI.info(this, "Pembelian tersimpan (" + po + ").\nStok barang telah bertambah otomatis.");
            lines.clear(); reload(); refresh();
        } catch (Exception e) { UI.error(this, "Gagal menyimpan pembelian: " + e.getMessage()); }
    }
}
