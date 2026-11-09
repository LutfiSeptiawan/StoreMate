package com.storemate.repository;

import com.storemate.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA: cukup mendeklarasikan interface, implementasinya dibuat Spring saat aplikasi berjalan.
 * Nama metode findFirstByUsernameAndPasswordHash otomatis diterjemahkan menjadi query
 * "WHERE username = ? AND password_hash = ? LIMIT 1".
 */
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findFirstByUsernameAndPasswordHash(String username, String passwordHash);
}
