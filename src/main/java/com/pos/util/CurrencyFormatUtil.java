package com.pos.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Helper format angka dan rupiah.
 * Dipakai di dashboard, kasir, laporan, dan hutang/piutang agar format uang konsisten.
 */
public final class CurrencyFormatUtil {

    private static final DecimalFormatSymbols SYMBOLS = createSymbols();

    private CurrencyFormatUtil() {
    }

    /**
     * Memformat angka bulat dengan pemisah ribuan Indonesia.
     */
    public static String formatNumber(long value) {
        DecimalFormat formatter = new DecimalFormat("#,##0", SYMBOLS);
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumFractionDigits(0);
        return formatter.format(value);
    }

    /**
     * Memformat angka desimal menjadi angka bulat tampilan.
     */
    public static String formatNumber(double value) {
        DecimalFormat formatter = new DecimalFormat("#,##0", SYMBOLS);
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumFractionDigits(0);
        return formatter.format(value);
    }

    /**
     * Memformat angka bulat menjadi teks rupiah.
     */
    public static String formatRupiah(long value) {
        return "Rp " + formatNumber(value);
    }

    /**
     * Memformat angka desimal menjadi teks rupiah.
     */
    public static String formatRupiah(double value) {
        return "Rp " + formatNumber(value);
    }

    /**
     * Mengatur simbol angka Indonesia: titik untuk ribuan dan koma untuk desimal.
     */
    private static DecimalFormatSymbols createSymbols() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        return symbols;
    }
}
