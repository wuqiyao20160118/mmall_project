package com.mmall.util;

import java.math.BigDecimal;

public class BigDecimalUtil {

    private BigDecimalUtil() {

    }

    public static BigDecimal add(double val1, double val2) {
        BigDecimal b1 = new BigDecimal(Double.toString(val1));
        BigDecimal b2 = new BigDecimal(Double.toString(val2));
        return b1.add(b2);
    }

    public static BigDecimal sub(double val1, double val2) {
        BigDecimal b1 = new BigDecimal(Double.toString(val1));
        BigDecimal b2 = new BigDecimal(Double.toString(val2));
        return b1.subtract(b2);
    }

    public static BigDecimal mul(double val1, double val2) {
        BigDecimal b1 = new BigDecimal(Double.toString(val1));
        BigDecimal b2 = new BigDecimal(Double.toString(val2));
        return b1.multiply(b2);
    }

    public static BigDecimal div(double val1, double val2) {
        BigDecimal b1 = new BigDecimal(Double.toString(val1));
        BigDecimal b2 = new BigDecimal(Double.toString(val2));
        return b1.divide(b2, 2, BigDecimal.ROUND_HALF_UP);
    }
}
