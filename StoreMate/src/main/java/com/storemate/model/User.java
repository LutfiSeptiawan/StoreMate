package com.storemate.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * MODEL + ENTITY ORM: satu objek User = satu baris tabel "users".
 * Pemetaan dilakukan lewat anotasi JPA, sehingga SQL tidak perlu ditulis manual.
 */
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // AUTO_INCREMENT di MySQL
    private int id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 64)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "role", nullable = false)
    private String role;

    /** Wajib ada: dipakai Hibernate untuk membuat objek dari hasil query. */
    public User() { }

    public User(int id, String username, String fullName, String role) {
        this.id = id; this.username = username; this.fullName = fullName; this.role = role;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isAdmin() { return "ADMIN".equals(role); }
}
