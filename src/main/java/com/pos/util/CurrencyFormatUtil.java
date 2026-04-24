package com.pos.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public final class CurrencyFormatUtil {

    private static final DecimalFormatSymbols SYMBOLS = createSymbols();

    private CurrencyFormatUtil() {
    }

    public static String formatNumber(long value) {
        DecimalFormat formatter = new DecimalFormat("#,##0", SYMBOLS);
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumFractionDigits(0);
        return formatter.format(value);
    }

    public static String formatNumber(double value) {
        DecimalFormat formatter = new DecimalFormat("#,##0", SYMBOLS);
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumFractionDigits(0);
        return formatter.format(value);
    }

    public static String formatRupiah(long value) {
        return "Rp " + formatNumber(value);
    }

    public static String formatRupiah(double value) {
        return "Rp " + formatNumber(value);
    }

    private static DecimalFormatSymbols createSymbols() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        return symbols;
    }
}
