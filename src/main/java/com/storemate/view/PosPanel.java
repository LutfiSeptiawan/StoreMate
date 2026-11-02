package com.storemate.view;

import com.storemate.controller.PosController;
import com.storemate.model.CartItem;
import com.storemate.model.Product;
import com.storemate.model.Receipt;
import com.storemate.util.*;
import java.awt.*;
import java.awt.print.PrinterException;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/** VIEW Kasir (POS). Keranjang & pembayaran dikelola PosController; panel ini hanya menampilkan. */
public class PosPanel extends JPanel implements Refreshable {
    private final PosController controller = new PosController();

    private final JTextField txtSearch = new JTextField(20);
    private final JPanel grid = new JPanel(new GridLayout(0, 3, 8, 8));
    private final DefaultTableModel cartModel = UI.model("Barang", "Harga", "Qty", "Subtotal");
    private final JTable cartTable = UI.table(cartModel);
    private final JLabel lblTotal = new JLabel("Rp 0"), lblChange = new JLabel("Rp 0");
    private final JComboBox<String> cbMethod = new JComboBox<>(new String[]{"TUNAI", "TRANSFER"});
    private final JTextField txtPaid = new JTextField();

    public PosPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(UI.BG);

        // ---------- KIRI: pencarian + grid produk ----------
        JPanel left = new JPanel(new BorderLayout(0, 8));
        left.setOpaque(false);
        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        search.setOpaque(false);
        search.add(UI.title("Kasir"));
        search.add(Box.createHorizontalStrut(16));
        search.add(new JLabel("Cari:")); search.add(txtSearch);
        txtSearch.addActionListener(e -> loadProducts());
        txtSearch.getDocument().addDocumentListener(simple(this::loadProducts));
        left.add(search, BorderLayout.NORTH);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(grid, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(wrap);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        left.add(sp, BorderLayout.CENTER);

        // ---------- KANAN: keranjang ----------
        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.setPreferredSize(new Dimension(430, 0));
        right.setBorder(BorderFactory.createTitledBorder("Keranjang Belanja"));
        right.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        JPanel qtyBtns = new JPanel(new GridLayout(1, 4, 6, 0));
        JButton plus = UI.button("+", UI.PRIMARY), minus = UI.button("-", UI.PRIMARY),
                rem = UI.button("Hapus", UI.WARN), clr = UI.button("Kosongkan", UI.DANGER);
        plus.addActionListener(e -> changeQty(1));
        minus.addActionListener(e -> changeQty(-1));
        rem.addActionListener(e -> removeSelected());
        clr.addActionListener(e -> { controller.clear(); refreshCart(); });
        qtyBtns.add(plus); qtyBtns.add(minus); qtyBtns.add(rem); qtyBtns.add(clr);

        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 28)); lblTotal.setForeground(UI.PRIMARY);
        lblChange.setFont(new Font("SansSerif", Font.BOLD, 16));

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("TOTAL")); form.add(lblTotal);
        form.add(new JLabel("Metode Bayar")); form.add(cbMethod);
        form.add(new JLabel("Uang Diterima")); form.add(txtPaid);
        form.add(new JLabel("Kembalian")); form.add(lblChange);

        cbMethod.addActionListener(e -> onMethodChanged());
        txtPaid.getDocument().addDocumentListener(simple(this::updateChange));

        JButton pay = UI.button("BAYAR & CETAK STRUK", UI.OK);
        pay.setFont(new Font("SansSerif", Font.BOLD, 15));
        pay.addActionListener(e -> pay());

        bottom.add(qtyBtns); bottom.add(Box.createVerticalStrut(8));
        bottom.add(form); bottom.add(Box.createVerticalStrut(8));
        bottom.add(pay);
        right.add(bottom, BorderLayout.SOUTH);

        add(left, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
    }

    @Override public void refresh() { loadProducts(); refreshCart(); }

    // ------------------------------------------------------------ produk
    private void loadProducts() {
        try {
            grid.removeAll();
            for (Product p : controller.products(txtSearch.getText())) {
                // Tombol produk: FOTO di atas, lalu NAMA, HARGA, dan stok kecil
                JButton b = new JButton("<html><center><b>" + p.name + "</b><br>" + Fmt.rp(p.sellPrice)
                        + "<br><small>Stok: " + p.stock + (p.isLowStock() ? " (menipis)" : "") + "</small></center></html>",
                        ImageUtil.iconPath(p.imagePath, 72));
                b.setVerticalTextPosition(SwingConstants.BOTTOM);
                b.setHorizontalTextPosition(SwingConstants.CENTER);
                b.setPreferredSize(new Dimension(150, 150));
                b.setBackground(p.stock <= 0 ? Color.LIGHT_GRAY : p.isLowStock() ? new Color(0xFFCDD2) : Color.WHITE);
                b.setOpaque(true);
                b.setEnabled(p.stock > 0);
                b.addActionListener(e -> addToCart(p));
                grid.add(b);
            }
            grid.revalidate(); grid.repaint();
        } catch (Exception e) {
            UI.error(this, "Gagal memuat produk: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------- keranjang
    private void addToCart(Product p) {
        try { controller.add(p); refreshCart(); }
        catch (IllegalStateException e) { UI.warn(this, e.getMessage()); }
    }

    private void changeQty(int d) {
        int row = cartTable.getSelectedRow();
        if (row < 0) return;
        try { controller.changeQty(row, d); }
        catch (IllegalStateException e) { UI.warn(this, e.getMessage()); return; }
        refreshCart();
        if (row < controller.cart().size()) cartTable.setRowSelectionInterval(row, row);
    }

    private void removeSelected() {
        int row = cartTable.getSelectedRow();
        if (row >= 0) { controller.remove(row); refreshCart(); }
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        for (CartItem it : controller.cart())
            cartModel.addRow(new Object[]{it.product.name, Fmt.rp(it.product.sellPrice), it.qty, Fmt.rp(it.subtotal())});
        lblTotal.setText(Fmt.rp(controller.total()));
        onMethodChanged();
    }

    private void onMethodChanged() {
        boolean transfer = "TRANSFER".equals(cbMethod.getSelectedItem());
        txtPaid.setEnabled(!transfer);
        if (transfer) txtPaid.setText(String.valueOf((long) controller.total()));   // transfer = uang pas
        updateChange();
    }

    private void updateChange() {
        double c = Fmt.parse(txtPaid.getText()) - controller.total();
        lblChange.setText(c >= 0 ? Fmt.rp(c) : "Kurang " + Fmt.rp(-c));
        lblChange.setForeground(c >= 0 ? UI.OK : UI.DANGER);
    }

    // ------------------------------------------------------------ bayar
    private void pay() {
        try {
            Receipt r = controller.checkout(String.valueOf(cbMethod.getSelectedItem()), Fmt.parse(txtPaid.getText()));
            showReceipt(r);
            txtPaid.setText("");
            refresh();
            List<String> low = controller.lowStockAfterSale();
            if (!low.isEmpty()) {
                StringBuilder sb = new StringBuilder("STOK MENIPIS! Segera lapor Admin untuk pembelian:\n");
                for (String n : low) sb.append("- ").append(n).append('\n');
                UI.warn(this, sb.toString());
            }
        } catch (IllegalArgumentException e) {
            UI.warn(this, e.getMessage());
        } catch (Exception e) {
            UI.error(this, "Transaksi gagal: " + e.getMessage());
            loadProducts();
        }
    }

    private void showReceipt(Receipt r) {
        JTextArea area = new JTextArea(controller.receiptText(r));
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setEditable(false);
        Object[] opts = {"Cetak", "Tutup"};
        int pick = JOptionPane.showOptionDialog(this, new JScrollPane(area), "Struk " + r.invoiceNo,
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opts, opts[1]);
        if (pick == 0) {
            try { area.print(); } catch (PrinterException e) { UI.error(this, "Gagal mencetak: " + e.getMessage()); }
        }
    }

    private static DocumentListener simple(Runnable r) {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { r.run(); }
            public void removeUpdate(DocumentEvent e) { r.run(); }
            public void changedUpdate(DocumentEvent e) { r.run(); }
        };
    }
}
