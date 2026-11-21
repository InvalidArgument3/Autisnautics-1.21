/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 */

package com.neep.meatlib.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FormatUtil {
    public static String formatDouble(double value) {
        BigDecimal bigDecimal = new BigDecimal(value).setScale(3, RoundingMode.HALF_UP).stripTrailingZeros();
        return bigDecimal.toEngineeringString();
    }

    public static String formatFloat(float value, boolean strip) {
        BigDecimal bigDecimal = new BigDecimal(value).setScale(3, RoundingMode.HALF_UP);
        if (strip) {
            bigDecimal = bigDecimal.stripTrailingZeros();
        }
        return bigDecimal.toEngineeringString();
    }
}
