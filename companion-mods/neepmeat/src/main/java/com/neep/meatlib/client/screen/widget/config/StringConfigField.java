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

package com.neep.meatlib.client.screen.widget.config;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class StringConfigField extends AbstractConfigTextField
{
    private final Consumer<StringConfigField> update;

    public StringConfigField(Font textRenderer, int x, int y, int width, int height, Component prefix, Consumer<StringConfigField> update)
    {
        super(textRenderer, x, y, width, height, prefix);
        this.update = update;
    }

    @Override
    protected void update()
    {
        if (dirty)
            this.update.accept(this);
    }

    @Override
    protected boolean isValidString(String text)
    {
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        // Consume E keys to stop the inventory from being closed
        if (keyCode == GLFW.GLFW_KEY_E)
            return true;

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public String parse()
    {
        return net.minecraft.ChatFormatting.stripFormatting(text());
    }

    public void setTextSilent(String text)
    {
        ignoreChange = true;
        setText(text);
        ignoreChange = false;
        prevText = text;
    }
}
