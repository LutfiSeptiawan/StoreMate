package com.storemate.dao;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Spring membungkus semua kesalahan database menjadi DataAccessException (unchecked).
 * Kelas ini mengembalikannya ke SQLException agar Controller tidak perlu tahu detail Spring.
 */
final class SqlErrors {
    private SqlErrors() { }

    /** Kode ganda / data masih dipakai tabel lain -> SQLIntegrityConstraintViolationException. */
    static SQLException toSql(DataAccessException e) {
        if (e instanceof DataIntegrityViolationException)
            return new SQLIntegrityConstraintViolationException(e.getMessage(), e);
        return new SQLException(e.getMessage(), e);
    }
}
