package com.storemate.util;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Helper tampilan agar seluruh form seragam. */
public final class UI {
    public static final Color PRIMARY = new Color(0x2563EB), DARK = new Color(0x0F172A),
            DANGER = new Color(0xDC2626), OK = new Color(0x16A34A),
            WARN = new Color(0xF59E0B), BG = new Color(0xF1F5F9),
            SURFACE = Color.WHITE, TEXT_MUTED = new Color(0x64748B),
            BORDER = new Color(0xD7E3F8);
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

    /** Kolom isian dengan garis tepi tipis dan jarak dalam yang lega. */
    public static JTextField textField() {
        JTextField f = new JTextField();
        styleInput(f);
        return f;
    }

    public static JPasswordField passwordField() {
        JPasswordField f = new JPasswordField();
        styleInput(f);
        return f;
    }

    private static void styleInput(JTextField f) {
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(9, 10, 9, 10)));
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
