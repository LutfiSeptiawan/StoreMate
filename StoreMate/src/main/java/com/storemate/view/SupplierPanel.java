package com.storemate.view;

import com.storemate.dao.SupplierDAO;
import com.storemate.model.Supplier;
import com.storemate.util.*;
import java.awt.*;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Pencatatan data supplier (khusus Admin). */
public class SupplierPanel extends JPanel implements Refreshable {
    private final SupplierDAO dao = new SupplierDAO();
    private final DefaultTableModel model = UI.model("Nama Supplier", "Telepon", "Alamat");
    private final JTable table = UI.table(model);
    private final JTextField txtName = new JTextField(), txtPhone = new JTextField(), txtAddr = new JTextField();
    private List<Supplier> current = new ArrayList<>();
    private Supplier editing;                                // null = mode tambah

    public SupplierPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(UI.BG);
        add(UI.title("Data Supplier"), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Formulir Supplier"));
        form.add(new JLabel("Nama Supplier")); form.add(txtName);
        form.add(new JLabel("Telepon")); form.add(txtPhone);
        form.add(new JLabel("Alamat")); form.add(txtAddr);
        JButton save = UI.button("Simpan", UI.OK), clear = UI.button("Bersihkan", UI.PRIMARY), del = UI.button("Hapus", UI.DANGER);
        save.addActionListener(e -> save());
        clear.addActionListener(e -> clearForm());
        del.addActionListener(e -> delete());
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.add(save); btns.add(clear); btns.add(del);
        form.add(btns);

        JPanel east = new JPanel(new BorderLayout());
        east.setPreferredSize(new Dimension(380, 0));
        east.add(form, BorderLayout.NORTH);
        add(east, BorderLayout.EAST);

        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            if (!e.getValueIsAdjusting() && r >= 0 && r < current.size()) {
                editing = current.get(r);
                txtName.setText(editing.name); txtPhone.setText(editing.phone); txtAddr.setText(editing.address);
            }
        });
    }

    @Override public void refresh() {
        try {
            current = dao.findAll();
            model.setRowCount(0);
            for (Supplier s : current) model.addRow(new Object[]{s.name, s.phone, s.address});
        } catch (Exception e) { UI.error(this, "Gagal memuat supplier: " + e.getMessage()); }
    }

    private void clearForm() {
        editing = null; table.clearSelection();
        txtName.setText(""); txtPhone.setText(""); txtAddr.setText("");
    }

    private void save() {
        if (txtName.getText().trim().isEmpty()) { UI.warn(this, "Nama supplier wajib diisi."); return; }
        Supplier s = editing == null ? new Supplier() : editing;
        s.name = txtName.getText().trim(); s.phone = txtPhone.getText().trim(); s.address = txtAddr.getText().trim();
        try {
            if (editing == null) dao.insert(s); else dao.update(s);
            clearForm(); refresh();
        } catch (Exception e) { UI.error(this, "Gagal menyimpan: " + e.getMessage()); }
    }

    private void delete() {
        if (editing == null) { UI.warn(this, "Pilih supplier pada tabel."); return; }
        if (!UI.confirm(this, "Hapus supplier \"" + editing.name + "\"?")) return;
        try {
            dao.delete(editing.id); clearForm(); refresh();
        } catch (SQLIntegrityConstraintViolationException e) {
            UI.error(this, "Supplier tidak dapat dihapus karena sudah memiliki riwayat pembelian.");
        } catch (Exception e) { UI.error(this, "Gagal menghapus: " + e.getMessage()); }
    }
}
