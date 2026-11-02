package com.storemate.dao;

import com.storemate.model.User;
import com.storemate.util.Hash;
import com.storemate.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.List;

/** Autentikasi pengguna memakai ORM (JPQL). Tidak ada SQL manual. */
public class UserDAO {

    /** @return User bila username+password cocok, selain itu null. */
    public User login(String username, String password) throws SQLException {
        EntityManager em = JpaUtil.em();
        try {
            // JPQL menulis nama kelas & atribut Java (User, u.username), bukan nama tabel/kolom
            List<User> hasil = em.createQuery(
                    "SELECT u FROM User u WHERE u.username = :un AND u.passwordHash = :ph", User.class)
                    .setParameter("un", username)
                    .setParameter("ph", Hash.sha256(password))
                    .setMaxResults(1)
                    .getResultList();
            return hasil.isEmpty() ? null : hasil.get(0);
        } catch (PersistenceException e) {
            throw JpaUtil.toSql(e);
        } finally {
            em.close();
        }
    }
}
