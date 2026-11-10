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

import com.neep.meatlib.theme.PLCCols;
import com.neep.meatlib.client.screen.NMTextField;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public abstract class AbstractConfigTextField extends NMTextField implements ConfigTextField
{
    protected boolean ignoreChange;
    public boolean dirty;

    protected String prevText;
    private final Component prefix;

    protected static final ChatFormatting[] DIRTY = new ChatFormatting[]{ChatFormatting.ITALIC};

    public AbstractConfigTextField(Font textRenderer, int x, int y, int width, int height, Component prefix)
    {
        super(textRenderer, x, y, width, height, Component.empty());
        this.prefix = prefix;

        setResponder(this::onValueChange);
    }

    @Override
    public void setDirty(boolean dirty)
    {
        this.dirty = dirty;
    }

    protected abstract void update();

    protected abstract boolean isValidString(String text);

    @Override
    public String getPlayerPrefix()
    {
        String s = prefix.getString();
//        return dirty ? s + "*" : s;
        return s;
    }

    @Override
    protected int getTextStart()
    {
        return prefix.getString().isEmpty() ? 0 : nw() / 2;
    }

    @Override
    protected int prefixCol()
    {
        return PLCCols.TEXT.col;
//        return dirty ? PLCCols.TEXT.col : PLCCols.INVALID.col;
    }

    protected ChatFormatting[] getPrefixStyle()
    {
        return dirty ? DIRTY : CLEAN;
//        return super.getPrefixStyle();
    }

    // Can't be bothered to add another AW entry and restart the IDE and generate sources again.
    private void onValueChange(String text)
    {
        if (!ignoreChange && !Objects.equals(prevText, text))
            setDirty(true);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (keyCode == GLFW.GLFW_KEY_ENTER)
        {
            update();
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void setFocused(boolean focused)
    {
        super.setFocused(focused);
        if (!focused)
            update();
    }

    @Override
    public void write(String text)
    {
        if (!isValidString(text))
            return;

        super.write(text);
    }
}
