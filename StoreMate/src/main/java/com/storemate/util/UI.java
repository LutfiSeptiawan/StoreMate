package com.storemate.util;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Helper tampilan agar seluruh form seragam. */
public final class UI {
    public static final Color PRIMARY = new Color(0x1E88E5), DARK = new Color(0x263238),
            DANGER = new Color(0xE53935), OK = new Color(0x43A047),
            WARN = new Color(0xF9A825), BG = new Color(0xF5F7FA);
    public static final Font BOLD = new Font("SansSerif", Font.BOLD, 13);

    private UI() { }

    public static JButton button(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE); b.setOpaque(true);
        b.setFocusPainted(false); b.setBorderPainted(false); b.setFont(BOLD);
        b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JLabel title(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("SansSerif", Font.BOLD, 22));
        l.setForeground(DARK);
        return l;
    }

    public static DefaultTableModel model(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    public static JTable table(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setRowHeight(26);
        t.getTableHeader().setFont(BOLD);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return t;
    }

    public static void error(Component c, String m) { JOptionPane.showMessageDialog(c, m, "Kesalahan", JOptionPane.ERROR_MESSAGE); }
    public static void info(Component c, String m) { JOptionPane.showMessageDialog(c, m, "Informasi", JOptionPane.INFORMATION_MESSAGE); }
    public static void warn(Component c, String m) { JOptionPane.showMessageDialog(c, m, "Peringatan", JOptionPane.WARNING_MESSAGE); }
    public static boolean confirm(Component c, String m) {
        return JOptionPane.showConfirmDialog(c, m, "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
