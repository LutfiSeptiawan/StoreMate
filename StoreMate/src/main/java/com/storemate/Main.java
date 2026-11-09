package com.storemate;

import com.storemate.view.LoginFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Titik masuk aplikasi StoreMate (desktop Swing) di atas Spring Boot.
 * @SpringBootApplication memindai package com.storemate dan membuat bean (@Component, @Repository),
 * menyiapkan koneksi database + JPA otomatis dari application.properties.
 */
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");        // aplikasi desktop: butuh jendela GUI
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) { }

        try {
            SpringApplication.run(Main.class, args);              // menyalakan container Spring
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null) root = root.getCause();
            JOptionPane.showMessageDialog(null,
                    "Aplikasi gagal dimulai.\nPastikan MySQL di XAMPP sudah Start dan database storemate sudah dibuat.\n\n" + root.getMessage(),
                    "Kesalahan", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
