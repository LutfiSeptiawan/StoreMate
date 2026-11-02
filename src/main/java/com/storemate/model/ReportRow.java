package com.storemate.model;

/** MODEL: satu baris laporan (per hari atau per bulan). */
public class ReportRow {
    public String date;                    // periode: yyyy-MM-dd atau yyyy-MM
    public int trx;
    public double revenue, cogs;           // pendapatan, harga pokok penjualan (HPP)
    public double profit() { return revenue - cogs; }
}
