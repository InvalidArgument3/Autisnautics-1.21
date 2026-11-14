/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat.util;

public class Easing
{
    public static double easeInCubic(double x)
    {
        return x * x * x;
    }

    public static double easeOutCubic(double x)
    {
        return 1 - Math.pow(1 - x, 3);
    }

    public static double easeInOutCubic(double x)
    {
        return x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2;
    }

    public static double dEaseInOutCubic(double x)
    {
        return x < 0.5 ? 12 * x * x : 12 * (x - 1) * (x - 1);
    }

    public static double easeInOutBack(double x)
    {
        var c1 = 1.70158;
        var c2 = c1 * 1.525;

        return x < 0.5
                ? (Math.pow(2 * x, 2) * ((c2 + 1) * 2 * x - c2)) / 2
                : (Math.pow(2 * x - 2, 2) * ((c2 + 1) * (x * 2 - 2) + c2) + 2) / 2;
    }

    public static double easeOutBack(double x)
    {
        var c1 = 1.70158;
        var c3 = c1 + 1;

        return 1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2);
    }

    public static double easeInBack(double x)
    {
        var c1 = 1.70158;
        var c3 = c1 + 1;

        return c3 * x * x * x - c1 * x * x;

    }

    public static double easeOutExp(double x)
    {
        return x == 1 ? 1 : 1 - Math.pow(2, -10 * x);
    }

    public static double easeInExp(double x)
    {
        return x == 0 ? 0 : Math.pow(2, 10 * x - 10);
    }

    public static double easeOutQuad(float x)
    {
        return 1 - (1 - x) * (1 - x);
    }

    @FunctionalInterface
    public interface Curve
    {
        double apply(double x);
    }
}
