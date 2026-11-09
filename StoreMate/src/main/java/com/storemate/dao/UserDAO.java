package com.storemate.dao;

import com.storemate.model.User;
import com.storemate.repository.UserRepository;
import com.storemate.util.Hash;
import java.sql.SQLException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

/** Autentikasi pengguna. UserRepository (Spring Data JPA) disuntikkan oleh Spring. */
@Repository
public class UserDAO {
    private final UserRepository repository;

    /** Constructor injection. */
    public UserDAO(UserRepository repository) { this.repository = repository; }

    /** @return User bila username+password cocok, selain itu null. */
    public User login(String username, String password) throws SQLException {
        try {
            return repository.findFirstByUsernameAndPasswordHash(username, Hash.sha256(password)).orElse(null);
        } catch (DataAccessException e) {
            throw new SQLException(e.getMessage(), e);
        }
    }
}
