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

import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.meatlib.theme.PLCCols;
import com.neep.meatlib.client.screen.AbstractTextField;
import com.neep.meatlib.client.screen.button.NMButtonWidget;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public class NMTextField extends AbstractTextField implements Rectangle.Mutable
{
    protected static final ChatFormatting[] CLEAN = new ChatFormatting[]{};
    private final BiFunction<String, Integer, FormattedCharSequence> renderTextProvider = (string, firstCharacterIndex) -> FormattedCharSequence.forward(
            string, Style.EMPTY.applyFormats(getPrefixStyle())
    );

    public NMTextField(Font textRenderer, int x, int y, int width, int height, @Nullable NMTextField copyFrom, Component text)
    {
        super(textRenderer, x, y, width, height, text);

        if (copyFrom != null)
            this.setText(copyFrom.text());
    }

    public NMTextField(Font textRenderer, int x, int y, int width, int height, Component text)
    {
        this(textRenderer, x, y, width, height, null, text);
        setDrawsBackground(false);
    }

    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    public void appendClickableNarrations(NarrationElementOutput builder)
    {
        builder.add(NarratedElementType.TITLE, this.getNarrationMessage());
    }

    public NMTextField drawFancyBackground(boolean draw)
    {
        this.drawFancyBackground = draw;
        return this;
    }

    protected void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        if (drawFancyBackground)
        {
            GUIUtil.drawNineSlicedTexture(context, NMButtonWidget.NM_WIDGETS_TEXTURE, x0(), this.y0(), nw(), nh(), 4, 200, 20, 0, 90);
        }
    }

    protected int getTextY()
    {
        return this.y0() + (this.height - 8) / 2;
    }

    @Override
    public void renderButton(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        renderBackground(context, mouseX, mouseY, delta);

        int col = isEditable() ? editableColor : uneditableColor;
        int j = selectionStart - firstCharacterIndex;
        int k = selectionEnd - firstCharacterIndex;

        String string = this.textRenderer.plainSubstrByWidth(text().substring(firstCharacterIndex), this.getInnerWidth());
        boolean selectionWithin = j >= 0 && j <= string.length();
        boolean drawCursor = this.isFocused() && focusedTicks / 6 % 2 == 0 && selectionWithin;

        String prefix = getPrefix();
        int prefixStart = x0() + (drawFancyBackground ? 4 : 2);
        int textStart = getTextStart(prefixStart);
        int m = getTextY();
        int n = textStart;

        if (k > string.length())
        {
            k = string.length();
        }

        if (!prefix.isEmpty())
        {
            GUIUtil.drawText(context, this.textRenderer, Component.literal(prefix).withStyle(getPrefixStyle()), prefixStart, m, prefixCol(), false);
        }

        if (!string.isEmpty())
        {
            n = renderUnselectedText(context, string, selectionWithin, textStart, m, col, j);
        }

        boolean cursorNotAtEnd = selectionStart < text().length() || text().length() >= getMaxLength();
        int o = n;
        if (!selectionWithin)
        {
            o = j > 0 ? textStart + this.width : textStart;
        }
        else if (cursorNotAtEnd)
        {
            o = n - 1;
            --n;
        }

        if (!string.isEmpty() && selectionWithin && j < string.length())
        {
            GUIUtil.drawText(context, this.textRenderer, this.renderTextProvider.apply(string.substring(j), selectionStart), n, m, col, true);
        }

        if (drawCursor)
        {
            if (cursorNotAtEnd)
            {
                context.fill(RenderType.guiOverlay(), o, m - 1, o + 1, m + 1 + 9, -3092272);
            }
            else
            {
                GUIUtil.drawText(context, this.textRenderer, "_", o, m, col, true);
            }
        }

        if (k != j)
        {
            int p = textStart + this.textRenderer.width(string.substring(0, k));
            drawSelectionHighlight(context, o, m - 1, p - 1, m + 1 + 9);
        }
    }

    protected int getTextStart(int prefixStart)
    {
        return prefixStart + Math.max(textRenderer.width(getPrefix()), getTextStart());
    }

    protected ChatFormatting[] getPrefixStyle()
    {
        return CLEAN;
    }

    protected int renderUnselectedText(GuiGraphics context, String string, boolean selectionWithin, int textStart, int m, int col, int j)
    {
        String string2 = selectionWithin ? string.substring(0, j) : string;
        return GUIUtil.drawText(context, this.textRenderer, this.renderTextProvider.apply(string2, firstCharacterIndex), textStart, m, col, true);
    }

    protected String getPrefix()
    {
        return "";
    }

    protected int getTextStart()
    {
        return 0;
    }

    protected int prefixCol()
    {
        return PLCCols.INVALID.col;
    }

    @Override
    public void setDims(int x, int y, int w, int h)
    {
        setPos(x, y);
        this.width = w;
        this.height = h;
    }
}
