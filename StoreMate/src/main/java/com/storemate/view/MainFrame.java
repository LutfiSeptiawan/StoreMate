package com.storemate.view;

import com.storemate.controller.LoginController;
import com.storemate.model.User;
import com.storemate.util.Refreshable;
import com.storemate.util.UI;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;

/**
 * VIEW jendela utama: sidebar navigasi + area konten (CardLayout).
 * RBAC: menu & panel Admin HANYA dibuat bila role = ADMIN.
 */
public class MainFrame extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, JPanel> panels = new LinkedHashMap<>();

    public MainFrame(User user) {
        super("StoreMate - " + user.getFullName() + " (" + user.getRole() + ")");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(UI.DARK);
        side.setBorder(BorderFactory.createEmptyBorder(18, 14, 18, 14));
        side.setPreferredSize(new Dimension(220, 0));

        JLabel brand = new JLabel("StoreMate");
        brand.setFont(new Font("SansSerif", Font.BOLD, 24));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Lencana role + nama pengguna
        JPanel userBadges = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        userBadges.setOpaque(false);
        userBadges.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel role = new JLabel(user.getRole());
        role.setOpaque(true);
        role.setBackground(new Color(0x1D4ED8));
        role.setForeground(Color.WHITE);
        role.setFont(new Font("SansSerif", Font.BOLD, 11));
        role.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        JLabel person = new JLabel(user.getFullName());
        person.setForeground(new Color(0xCBD5E1));
        person.setFont(new Font("SansSerif", Font.PLAIN, 12));
        userBadges.add(role);
        userBadges.add(Box.createHorizontalStrut(8));
        userBadges.add(person);

        side.add(brand);
        side.add(Box.createVerticalStrut(8));
        side.add(userBadges);
        side.add(Box.createVerticalStrut(20));

        if (user.isAdmin()) {
            addMenu(side, "dashboard", "Dashboard", new DashboardPanel());
            addMenu(side, "produk", "Data Barang", new ProductPanel(true));
            addMenu(side, "kasir", "Kasir (POS)", new PosPanel());
            addMenu(side, "supplier", "Supplier", new SupplierPanel());
            // setelah PO tersimpan, Dashboard & Data Barang diperbarui (stok bertambah)
            addMenu(side, "pembelian", "Pembelian (PO)", new PurchasePanel(() -> {
                refreshPanel("dashboard");
                refreshPanel("produk");
            }));
            addMenu(side, "laporan", "Laporan Laba Rugi", new ReportPanel());
        } else {                                   // KASIR: hanya POS + lihat barang (read-only)
            addMenu(side, "kasir", "Kasir (POS)", new PosPanel());
            addMenu(side, "produk", "Daftar Barang", new ProductPanel(false));
        }

        side.add(Box.createVerticalGlue());
        JButton out = UI.button("Logout", UI.DANGER);
        out.setAlignmentX(Component.LEFT_ALIGNMENT);
        out.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        out.addActionListener(e -> {
            if (UI.confirm(this, "Keluar dari aplikasi?")) {
                new LoginController().logout();    // hapus sesi + buka kembali form login
                dispose();
            }
        });
        side.add(out);

        content.setBackground(UI.BG);
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
        b.setBackground(new Color(0x111827));
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x1F2937), 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        b.setFocusPainted(false);
        b.setFont(UI.BOLD);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> show(key));
        side.add(b);
        side.add(Box.createVerticalStrut(6));
    }

    private void show(String key) {
        refreshPanel(key);
        cards.show(content, key);
    }

    private void refreshPanel(String key) {
        JPanel p = panels.get(key);
        if (p instanceof Refreshable) ((Refreshable) p).refresh();   // data selalu terbaru
    }
}
