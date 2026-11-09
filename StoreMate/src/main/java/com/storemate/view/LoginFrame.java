package com.storemate.view;

import com.storemate.controller.LoginController;
import com.storemate.model.User;
import com.storemate.util.AppContext;
import com.storemate.util.UI;
import java.awt.*;
import javax.swing.*;

/**
 * VIEW halaman login. Tidak menyentuh database: input diteruskan ke LoginController,
 * dan hasilnya (sukses / pesan kesalahan) hanya ditampilkan.
 */
public class LoginFrame extends JFrame {
    private final JTextField txtUser = UI.textField();
    private final JPasswordField txtPass = UI.passwordField();
    private final LoginController controller = AppContext.get(LoginController.class);   // VIEW -> CONTROLLER (bean Spring)

    public LoginFrame() {
        super("StoreMate - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(0xEAF2FF));
        root.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UI.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UI.BORDER, 1, true),
                BorderFactory.createEmptyBorder(34, 38, 34, 38)));

        // Logo "S"
        JPanel badge = new JPanel(new GridBagLayout());
        badge.setOpaque(false);
        JLabel logo = new JLabel("S", SwingConstants.CENTER);
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("SansSerif", Font.BOLD, 30));
        logo.setOpaque(true);
        logo.setBackground(UI.PRIMARY);
        logo.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        logo.setPreferredSize(new Dimension(64, 64));
        badge.add(logo);

        JLabel brand = new JLabel("StoreMate", SwingConstants.CENTER);
        brand.setFont(new Font("SansSerif", Font.BOLD, 30));
        brand.setForeground(UI.DARK);

        JLabel sub = new JLabel("Manajemen Toko Makanan", SwingConstants.CENTER);
        sub.setForeground(UI.TEXT_MUTED);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JLabel userLbl = new JLabel("Username");
        userLbl.setForeground(UI.DARK); userLbl.setFont(UI.BOLD);
        JLabel passLbl = new JLabel("Password");
        passLbl.setForeground(UI.DARK); passLbl.setFont(UI.BOLD);

        JButton btn = UI.button("MASUK", UI.PRIMARY);
        btn.setPreferredSize(new Dimension(240, 42));
        btn.addActionListener(e -> doLogin());
        getRootPane().setDefaultButton(btn);                 // tombol Enter = Masuk

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1.0;
        g.insets = new Insets(0, 0, 12, 0);

        g.gridy = 0; card.add(badge, g);
        g.gridy = 1; card.add(brand, g);
        g.gridy = 2; card.add(sub, g);
        g.gridy = 3; g.insets = new Insets(18, 0, 6, 0); card.add(userLbl, g);
        g.gridy = 4; g.insets = new Insets(0, 0, 10, 0); card.add(txtUser, g);
        g.gridy = 5; g.insets = new Insets(0, 0, 6, 0); card.add(passLbl, g);
        g.gridy = 6; g.insets = new Insets(0, 0, 18, 0); card.add(txtPass, g);
        g.gridy = 7; g.insets = new Insets(4, 0, 0, 0); card.add(btn, g);

        root.add(card, BorderLayout.CENTER);
        setContentPane(root);
        setSize(440, 520);
        setLocationRelativeTo(null);
    }

    private void doLogin() {
        String u = txtUser.getText().trim();
        String p = new String(txtPass.getPassword());
        try {
            User user = controller.login(u, p);       // validasi + cek DB dilakukan Controller
            controller.openMainForm(user);
            dispose();
        } catch (IllegalArgumentException ex) {       // input kosong / kredensial salah
            UI.warn(this, ex.getMessage());
            txtPass.setText("");
        } catch (Exception ex) {
            UI.error(this, "Gagal terhubung ke database:\n" + ex.getMessage()
                    + "\n\nPastikan MySQL di XAMPP sudah Start dan skrip storemate.sql sudah dijalankan.");
        }
    }
}
