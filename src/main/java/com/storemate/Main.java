package com.storemate;

import com.storemate.util.JpaUtil;
import com.storemate.view.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Titik masuk aplikasi StoreMate. Jalankan class ini (Run Project). */
public class Main {
    public static void main(String[] args) {
        try {
            // Look & Feel lintas-platform agar warna tombol tampil konsisten
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) { }
        Runtime.getRuntime().addShutdownHook(new Thread(JpaUtil::close));   // tutup EntityManagerFactory saat keluar
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
