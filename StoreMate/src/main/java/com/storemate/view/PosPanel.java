package com.storemate.view;

import com.storemate.dao.ProductDAO;
import com.storemate.dao.SaleDAO;
import com.storemate.model.CartItem;
import com.storemate.model.Product;
import com.storemate.util.*;
import java.awt.*;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/** Kasir (POS): grid produk di kiri, keranjang & pembayaran di kanan. */
public class PosPanel extends JPanel implements Refreshable {
    private final ProductDAO productDAO = new ProductDAO();
    private final SaleDAO saleDAO = new SaleDAO();
    private final List<CartItem> cart = new ArrayList<>();

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
        wrap.add(grid, BorderLayout.NORTH);                 // agar tombol tidak melar vertikal
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
        clr.addActionListener(e -> { cart.clear(); refreshCart(); });
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
            for (Product p : productDAO.findAll(txtSearch.getText())) {
                // Tombol produk: FOTO di atas, lalu NAMA, HARGA, dan stok kecil
                JButton b = new JButton("<html><center><b>" + p.name + "</b><br>" + Fmt.rp(p.sellPrice)
                        + "<br><small>Stok: " + p.stock + (p.isLowStock() ? " (menipis)" : "") + "</small></center></html>",
                        ImageUtil.iconPath(p.imagePath, 72));
                b.setVerticalTextPosition(SwingConstants.BOTTOM);
                b.setHorizontalTextPosition(SwingConstants.CENTER);
                b.setPreferredSize(new Dimension(150, 150));
                b.setBackground(p.stock <= 0 ? Color.LIGHT_GRAY : p.isLowStock() ? new Color(0xFFCDD2) : Color.WHITE);
                b.setOpaque(true);
                b.setEnabled(p.stock > 0);                  // stok habis tidak bisa dijual
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
        for (CartItem it : cart) {
            if (it.product.id == p.id) {
                if (it.qty + 1 > p.stock) { UI.warn(this, "Stok " + p.name + " hanya " + p.stock + "."); return; }
                it.qty++; refreshCart(); return;
            }
        }
        cart.add(new CartItem(p, 1));
        refreshCart();
    }

    private void changeQty(int d) {
        int row = cartTable.getSelectedRow();
        if (row < 0) return;
        CartItem it = cart.get(row);
        int nq = it.qty + d;
        if (nq <= 0) { cart.remove(row); }
        else if (nq > it.product.stock) { UI.warn(this, "Stok " + it.product.name + " hanya " + it.product.stock + "."); return; }
        else it.qty = nq;
        refreshCart();
        if (row < cart.size()) cartTable.setRowSelectionInterval(row, row);
    }

    private void removeSelected() {
        int row = cartTable.getSelectedRow();
        if (row >= 0) { cart.remove(row); refreshCart(); }
    }

    private double total() {
        double t = 0;
        for (CartItem it : cart) t += it.subtotal();
        return t;
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        for (CartItem it : cart)
            cartModel.addRow(new Object[]{it.product.name, Fmt.rp(it.product.sellPrice), it.qty, Fmt.rp(it.subtotal())});
        lblTotal.setText(Fmt.rp(total()));     // total dihitung otomatis
        onMethodChanged();
    }

    private void onMethodChanged() {
        boolean transfer = "TRANSFER".equals(cbMethod.getSelectedItem());
        txtPaid.setEnabled(!transfer);
        if (transfer) txtPaid.setText(String.valueOf((long) total()));   // transfer = pas
        updateChange();
    }

    private void updateChange() {
        double c = Fmt.parse(txtPaid.getText()) - total();
        lblChange.setText(c >= 0 ? Fmt.rp(c) : "Kurang " + Fmt.rp(-c));
        lblChange.setForeground(c >= 0 ? UI.OK : UI.DANGER);
    }

    // ------------------------------------------------------------ bayar
    private void pay() {
        if (cart.isEmpty()) { UI.warn(this, "Keranjang masih kosong."); return; }
        String method = String.valueOf(cbMethod.getSelectedItem());
        double paid = "TRANSFER".equals(method) ? total() : Fmt.parse(txtPaid.getText());
        if (paid < total()) { UI.warn(this, "Uang yang diterima kurang dari total belanja."); return; }

        try {
            List<CartItem> snapshot = new ArrayList<>(cart);
            SaleDAO.Receipt r = saleDAO.save(cart, method, paid);   // simpan + kurangi stok (atomik)
            showReceipt(r, snapshot, method);
            cart.clear(); txtPaid.setText("");
            refresh();
            warnLowStock(snapshot);
        } catch (Exception e) {
            UI.error(this, "Transaksi gagal: " + e.getMessage());
            loadProducts();                                          // sinkronkan stok terbaru
        }
    }

    /** Setelah penjualan, beri tahu bila ada barang terjual yang kini menipis. */
    private void warnLowStock(List<CartItem> sold) {
        try {
            StringBuilder sb = new StringBuilder();
            for (Product low : productDAO.findLowStock())
                for (CartItem it : sold)
                    if (it.product.id == low.id) sb.append("- ").append(low.name).append(" (sisa ").append(low.stock).append(")\n");
            if (sb.length() > 0) UI.warn(this, "STOK MENIPIS! Segera lapor Admin untuk pembelian:\n" + sb);
        } catch (Exception ignored) { }
    }

    private void showReceipt(SaleDAO.Receipt r, List<CartItem> items, String method) {
        StringBuilder sb = new StringBuilder();
        sb.append("        STOREMATE\n     Struk Penjualan\n");
        sb.append("--------------------------------\n");
        sb.append("No   : ").append(r.invoiceNo).append('\n');
        sb.append("Tgl  : ").append(r.dateTime).append('\n');
        sb.append("Kasir: ").append(Session.get().fullName).append('\n');
        sb.append("--------------------------------\n");
        for (CartItem it : items) {
            sb.append(it.product.name).append('\n');
            sb.append(String.format("  %d x %-9s %12s%n", it.qty, Fmt.num(it.product.sellPrice), Fmt.num(it.subtotal())));
        }
        sb.append("--------------------------------\n");
        sb.append(String.format("%-14s %17s%n", "TOTAL", Fmt.rp(r.total)));
        sb.append(String.format("%-14s %17s%n", "Bayar (" + method + ")", Fmt.rp(r.paid)));
        sb.append(String.format("%-14s %17s%n", "Kembali", Fmt.rp(r.change)));
        sb.append("--------------------------------\n   Terima kasih atas kunjungan\n");

        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setEditable(false);
        Object[] opts = {"Cetak", "Tutup"};
        int pick = JOptionPane.showOptionDialog(this, new JScrollPane(area), "Struk " + r.invoiceNo,
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opts, opts[1]);
        if (pick == 0) {
            try { area.print(); } catch (PrinterException e) { UI.error(this, "Gagal mencetak: " + e.getMessage()); }
        }
    }

    /** Pembantu: DocumentListener satu aksi. */
    private static DocumentListener simple(Runnable r) {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { r.run(); }
            public void removeUpdate(DocumentEvent e) { r.run(); }
            public void changedUpdate(DocumentEvent e) { r.run(); }
        };
    }
}
