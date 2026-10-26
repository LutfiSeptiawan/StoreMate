package com.storemate.view;

import com.storemate.dao.UserDAO;
import com.storemate.model.User;
import com.storemate.util.Session;
import com.storemate.util.UI;
import java.awt.*;
import javax.swing.*;

/** Halaman login. Role (Admin/Kasir) ditentukan dari data user di database. */
public class LoginFrame extends JFrame {
    private final JTextField txtUser = new JTextField(18);
    private final JPasswordField txtPass = new JPasswordField(18);

    public LoginFrame() {
        super("StoreMate - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UI.DARK);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 0, 6, 0); g.gridx = 0; g.fill = GridBagConstraints.HORIZONTAL;

        JLabel brand = new JLabel("StoreMate", SwingConstants.CENTER);
        brand.setFont(new Font("SansSerif", Font.BOLD, 30)); brand.setForeground(UI.PRIMARY);
        JLabel sub = new JLabel("Manajemen Toko Makanan", SwingConstants.CENTER);
        sub.setForeground(Color.GRAY);

        JButton btn = UI.button("MASUK", UI.PRIMARY);
        btn.addActionListener(e -> doLogin());
        getRootPane().setDefaultButton(btn);                 // tombol Enter = Masuk

        g.gridy = 0; card.add(brand, g);
        g.gridy = 1; card.add(sub, g);
        g.gridy = 2; card.add(new JLabel("Username"), g);
        g.gridy = 3; card.add(txtUser, g);
        g.gridy = 4; card.add(new JLabel("Password"), g);
        g.gridy = 5; card.add(txtPass, g);
        g.gridy = 6; g.insets = new Insets(16, 0, 0, 0); card.add(btn, g);

        root.add(card);
        setContentPane(root);
        setSize(480, 460);
        setLocationRelativeTo(null);
    }

    private void doLogin() {
        String u = txtUser.getText().trim();
        String p = new String(txtPass.getPassword());
        if (u.isEmpty() || p.isEmpty()) { UI.warn(this, "Username dan password wajib diisi."); return; }
        try {
            User user = new UserDAO().login(u, p);
            if (user == null) { UI.error(this, "Username atau password salah."); return; }
            Session.set(user);
            new MainFrame(user).setVisible(true);
            dispose();
        } catch (Exception ex) {
            UI.error(this, "Tidak dapat terhubung ke database.\nPastikan MySQL aktif & skrip storemate.sql sudah dijalankan.\n\n" + ex.getMessage());
        }
    }
}
