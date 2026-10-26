package com.storemate.util;

import java.text.NumberFormat;
import java.util.Locale;

/** Format & parsing angka rupiah. */
public final class Fmt {
    private static final NumberFormat NF = NumberFormat.getInstance(new Locale("id", "ID"));
    private Fmt() { }

    public static String rp(double v) { return "Rp " + NF.format(Math.round(v)); }
    public static String num(double v) { return NF.format(Math.round(v)); }

    /** Ambil hanya digit dari teks ("Rp 12.500" -> 12500). Kosong -> 0. */
    public static long parse(String s) {
        String d = s == null ? "" : s.replaceAll("[^0-9]", "");
        return d.isEmpty() ? 0 : Long.parseLong(d);
    }
}
