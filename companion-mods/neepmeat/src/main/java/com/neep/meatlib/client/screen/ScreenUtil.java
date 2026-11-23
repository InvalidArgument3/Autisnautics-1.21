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

package com.neep.meatlib.client.screen;

import org.lwjgl.glfw.GLFW;

import java.util.function.IntConsumer;

public class ScreenUtil
{

    public static boolean keyScroll(int chr, int modifiers, IntConsumer scroll)
    {
        if (chr == GLFW.GLFW_KEY_K || chr == GLFW.GLFW_KEY_UP)
        {
            scroll.accept(1);
            return true;
        }
        else if (chr == GLFW.GLFW_KEY_J || chr == GLFW.GLFW_KEY_DOWN)
        {
            scroll.accept(-1);
            return true;
        }

        if (chr == GLFW.GLFW_KEY_PAGE_UP)
        {
            scroll.accept(10);
            return true;
        }
        else if (chr == GLFW.GLFW_KEY_PAGE_DOWN)
        {
            scroll.accept(-10);
            return true;
        }

        if (chr == GLFW.GLFW_KEY_HOME)
        {
            scroll.accept(Integer.MAX_VALUE);
            return true;
        }
        else if (chr == GLFW.GLFW_KEY_END)
        {
            scroll.accept(Integer.MIN_VALUE);
            return true;
        }

        return false;
    }
}
