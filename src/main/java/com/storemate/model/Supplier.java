package com.storemate.model;

/** Data pemasok. */
public class Supplier {
    public int id;
    public String name, phone, address;
    @Override public String toString() { return name; } // untuk JComboBox
}
