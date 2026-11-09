package com.storemate.controller;

import org.springframework.stereotype.Component;

import com.storemate.dao.UserDAO;
import com.storemate.model.User;
import com.storemate.util.Session;
import com.storemate.view.LoginFrame;
import com.storemate.view.MainFrame;
import java.sql.SQLException;

/** CONTROLLER: alur login/logout. View hanya meneruskan input & menampilkan hasil. */
@Component
public class LoginController {
    private final UserDAO dao;

    /** Constructor injection: Spring menyuntikkan UserDAO. */
    public LoginController(UserDAO dao) { this.dao = dao; }

    /**
     * @throws IllegalArgumentException input kosong atau username/password salah (pesan siap tampil)
     * @throws SQLException             database tidak dapat diakses
     */
    public User login(String username, String password) throws SQLException {
        if (username.trim().isEmpty() || password.isEmpty())
            throw new IllegalArgumentException("Username dan password wajib diisi.");
        User u = dao.login(username.trim(), password);
        if (u == null) throw new IllegalArgumentException("Username atau password salah.");
        Session.set(u);
        return u;
    }

    public void openMainForm(User user) { new MainFrame(user).setVisible(true); }

    public void logout() {
        Session.set(null);
        new LoginFrame().setVisible(true);
    }
}
