package com.storemate.util;

import com.storemate.model.User;

/** Menyimpan pengguna yang sedang login + penjaga hak akses (RBAC). */
public final class Session {
    private static User current;
    private Session() { }

    public static void set(User u) { current = u; }
    public static User get() { return current; }
    public static boolean isAdmin() { return current != null && current.isAdmin(); }

    /** Dipanggil di lapisan DAO: kasir tidak boleh menjalankan operasi khusus Admin. */
    public static void requireAdmin() {
        if (!isAdmin()) throw new SecurityException("Akses ditolak: fitur ini khusus Admin.");
    }
}
