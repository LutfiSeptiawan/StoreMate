package com.storemate.dao;

import com.storemate.config.DBConnection;
import com.storemate.model.User;
import com.storemate.util.Hash;
import java.sql.*;

/** Autentikasi pengguna. */
public class UserDAO {

    /** @return User bila username+password cocok, selain itu null. */
    public User login(String username, String password) throws SQLException {
        String sql = "SELECT id, username, full_name, role FROM users WHERE username=? AND password_hash=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, Hash.sha256(password));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("id"), rs.getString("username"),
                            rs.getString("full_name"), rs.getString("role"));
                }
            }
        }
        return null;
    }
}
