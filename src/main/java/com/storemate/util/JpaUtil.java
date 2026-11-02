package com.storemate.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.sql.SQLException;

/**
 * Pusat konfigurasi ORM (JPA/Hibernate).
 * EntityManagerFactory dibuat sekali (mahal) dari unit "storematePU" di META-INF/persistence.xml,
 * sedangkan EntityManager dibuat per operasi (murah) lalu ditutup.
 */
public final class JpaUtil {
    private static EntityManagerFactory emf;

    private JpaUtil() { }

    private static synchronized EntityManagerFactory factory() {
        if (emf == null) emf = Persistence.createEntityManagerFactory("storematePU");
        return emf;
    }

    /** Buka EntityManager baru. Wajib ditutup (em.close()) oleh pemanggil. */
    public static EntityManager em() { return factory().createEntityManager(); }

    /** Dipanggil saat aplikasi berhenti. */
    public static synchronized void close() {
        if (emf != null && emf.isOpen()) emf.close();
    }

    /**
     * Ubah exception JPA menjadi SQLException agar Controller/View tidak perlu tahu soal ORM.
     * Jika penyebabnya adalah SQLException asli (mis. SQLIntegrityConstraintViolationException
     * untuk kode barang ganda), SQLException itulah yang diteruskan.
     */
    public static SQLException toSql(RuntimeException e) {
        for (Throwable t = e; t != null; t = t.getCause())
            if (t instanceof SQLException) return (SQLException) t;
        return new SQLException(e.getMessage(), e);
    }
}
