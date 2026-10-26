package com.storemate.model;

/** Pengguna aplikasi beserta role-nya (ADMIN / KASIR). */
public class User {
    public final int id;
    public final String username, fullName, role;

    public User(int id, String username, String fullName, String role) {
        this.id = id; this.username = username; this.fullName = fullName; this.role = role;
    }
    public boolean isAdmin() { return "ADMIN".equals(role); }
}
