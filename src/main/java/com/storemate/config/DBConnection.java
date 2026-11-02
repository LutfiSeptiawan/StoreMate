package com.storemate.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Pusat koneksi JDBC ke MySQL.
 * Setiap pemanggil memakai try-with-resources sehingga koneksi selalu ditutup.
 * Sesuaikan USER/PASS bila konfigurasi MySQL Anda berbeda (XAMPP default: root & kosong).
 */
public final class DBConnection {

    private static final String URL  = "jdbc:mysql://localhost:3306/storemate"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta"
            + "&useUnicode=true&characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PASS = "";

    private DBConnection() { }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // MySQL Connector/J 8.x
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL tidak ditemukan. Tambahkan mysql-connector-j ke Libraries.", e);
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
