package com.storemate.view;

import com.storemate.model.User;
import com.storemate.util.Refreshable;
import com.storemate.util.Session;
import com.storemate.util.UI;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;

/**
 * Jendela utama: menu navigasi di sisi kiri + area konten (CardLayout).
 * RBAC: menu & panel Admin HANYA dibuat bila role = ADMIN.
 */
public class MainFrame extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, JPanel> panels = new LinkedHashMap<>();

    public MainFrame(User user) {
        super("StoreMate - " + user.fullName + " (" + user.role + ")");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(UI.DARK);
        side.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));
        side.setPreferredSize(new Dimension(210, 0));

        JLabel brand = new JLabel("StoreMate");
        brand.setFont(new Font("SansSerif", Font.BOLD, 24)); brand.setForeground(Color.WHITE);
        JLabel who = new JLabel(user.fullName + " | " + user.role);
        who.setForeground(new Color(0xB0BEC5));
        side.add(brand); side.add(who); side.add(Box.createVerticalStrut(20));

        if (user.isAdmin()) {
            addMenu(side, "dashboard", "Dashboard", new DashboardPanel());
            addMenu(side, "produk", "Data Barang", new ProductPanel(true));
            addMenu(side, "kasir", "Kasir (POS)", new PosPanel());
            addMenu(side, "supplier", "Supplier", new SupplierPanel());
            addMenu(side, "pembelian", "Pembelian (PO)", new PurchasePanel());
            addMenu(side, "laporan", "Laporan Laba Rugi", new ReportPanel());
        } else {                                   // KASIR: hanya POS + lihat barang (read-only)
            addMenu(side, "kasir", "Kasir (POS)", new PosPanel());
            addMenu(side, "produk", "Daftar Barang", new ProductPanel(false));
        }

        side.add(Box.createVerticalGlue());
        JButton out = UI.button("Logout", UI.DANGER);
        out.setAlignmentX(Component.LEFT_ALIGNMENT);
        out.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        out.addActionListener(e -> {
            if (UI.confirm(this, "Keluar dari aplikasi?")) {
                Session.set(null);
                new LoginFrame().setVisible(true);
                dispose();
            }
        });
        side.add(out);

        setLayout(new BorderLayout());
        add(side, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
        setSize(1200, 720);
        setLocationRelativeTo(null);

        show(panels.keySet().iterator().next());   // halaman awal = menu pertama
    }

    private void addMenu(JPanel side, String key, String label, JPanel panel) {
        panels.put(key, panel);
        content.add(panel, key);
        JButton b = new JButton(label);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBackground(UI.DARK); b.setForeground(Color.WHITE); b.setOpaque(true);
        b.setBorderPainted(false); b.setFocusPainted(false); b.setFont(UI.BOLD);
        b.addActionListener(e -> show(key));
        side.add(b); side.add(Box.createVerticalStrut(4));
    }

    private void show(String key) {
        JPanel p = panels.get(key);
        if (p instanceof Refreshable) ((Refreshable) p).refresh();   // data selalu terbaru
        cards.show(content, key);
    }
}
