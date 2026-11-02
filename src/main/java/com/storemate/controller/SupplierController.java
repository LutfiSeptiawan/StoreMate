package com.storemate.controller;

import com.storemate.dao.SupplierDAO;
import com.storemate.model.Supplier;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

/** CONTROLLER: pengelolaan data supplier. */
public class SupplierController {
    private final SupplierDAO dao = new SupplierDAO();

    public List<Supplier> list() throws SQLException { return dao.findAll(); }

    /** existing == null -> tambah baru, selain itu ubah. */
    public void save(Supplier existing, String name, String phone, String address) throws SQLException {
        if (name.trim().isEmpty()) throw new IllegalArgumentException("Nama supplier wajib diisi.");
        Supplier s = existing == null ? new Supplier() : existing;
        s.name = name.trim(); s.phone = phone.trim(); s.address = address.trim();
        if (existing == null) dao.insert(s); else dao.update(s);
    }

    public void delete(Supplier s) throws SQLException {
        try {
            dao.delete(s.id);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalStateException("Supplier tidak dapat dihapus karena sudah memiliki riwayat pembelian.");
        }
    }
}
