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

import com.neep.meatlib.client.screen.widget.AbstractClickableWidget;
import com.neep.meatlib.theme.PLCCols;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Contains behaviour that replicates TextFieldWidget
 */ 
public abstract class AbstractTextField extends AbstractClickableWidget
{
    protected final Font textRenderer;
    
    protected boolean drawFancyBackground = true;
    
    private String text;
    private int maxLength;
    private boolean focusUnlocked;
    private boolean editable;
    private boolean selecting;
    protected int firstCharacterIndex;
    protected int selectionStart;
    protected int selectionEnd;

    protected int focusedTicks;

    protected int editableColor;
    protected int uneditableColor;
    
    @Nullable private Consumer<String> changedListener;
    private Predicate<String> textPredicate = Objects::nonNull;
//    private Function<String, String> processor = s -> Objects.toString(s, "");

    public AbstractTextField(Font textRenderer, int x, int y, int width, int height, Component text)
    {
        super(x, y, width, height, text);
        this.textRenderer = textRenderer;
        this.text = "";
        this.maxLength = 32;
        this.focusUnlocked = true;
        this.editable = true;
        this.editableColor = PLCCols.TEXT.col;
        this.uneditableColor = PLCCols.INVALID.col;
    }

    public boolean visible()
    {
        return this.visible;
    }

    public void setResponder(@Nullable Consumer<String> changedListener)
    {
        this.changedListener = changedListener;
    }

    public void tick()
    {
        ++this.focusedTicks;
    }

    protected MutableComponent getNarrationMessage()
    {
        Component text = this.getMessage();
        return Component.translatable("gui.narrate.editBox", text, this.text);
    }

    public String text()
    {
        return this.text;
    }

    public void setText(String text)
    {
        if (this.textPredicate.test(text))
        {
            if (text.length() > this.maxLength)
            {
                this.text = text.substring(0, this.maxLength);
            }
            else
            {
                this.text = text;
            }

            this.moveCursorToEnd();
            this.setSelectionEnd(this.selectionStart);
            this.onValueChange(text);
        }
    }

    public String getSelectedText()
    {
        int i = Math.min(this.selectionStart, this.selectionEnd);
        int j = Math.max(this.selectionStart, this.selectionEnd);
        return this.text.substring(i, j);
    }

    public void setFilter(Predicate<String> textPredicate)
    {
        this.textPredicate = textPredicate;
    }

//    public void setTextProcessor(Function<String, String> processor)
//    {
//        this.processor = processor;
//    }

    public void write(String text)
    {
        int i = Math.min(this.selectionStart, this.selectionEnd);
        int j = Math.max(this.selectionStart, this.selectionEnd);
        int k = this.maxLength - this.text.length() - (i - j);
        String string = net.minecraft.util.StringUtil.filterText(text);
        int l = string.length();
        if (k < l)
        {
            string = string.substring(0, k);
            l = k;
        }

        String string2 = (new StringBuilder(this.text)).replace(i, j, string).toString();
        if (this.textPredicate.test(string2))
        {
            this.text = string2;
            this.setCursorPosition(i + l);
            this.setSelectionEnd(this.selectionStart);
            this.onValueChange(this.text);
        }
    }

    private void onValueChange(String newText)
    {
        if (this.changedListener != null)
        {
            this.changedListener.accept(newText);
        }

    }

    private void deleteText(int offset)
    {
        if (Screen.hasControlDown())
        {
            this.deleteWords(offset);
        }
        else
        {
            this.deleteChars(offset);
        }

    }

    public void deleteWords(int wordOffset)
    {
        if (!this.text.isEmpty())
        {
            if (this.selectionEnd != this.selectionStart)
            {
                this.write("");
            }
            else
            {
                this.deleteChars(this.getWordPosition(wordOffset) - this.selectionStart);
            }
        }
    }

    public void deleteChars(int characterOffset)
    {
        if (!this.text.isEmpty())
        {
            if (this.selectionEnd != this.selectionStart)
            {
                this.write("");
            }
            else
            {
                int i = this.getCursorPos(characterOffset);
                int j = Math.min(i, this.selectionStart);
                int k = Math.max(i, this.selectionStart);
                if (j != k)
                {
                    String string = (new StringBuilder(this.text)).delete(j, k).toString();
                    if (this.textPredicate.test(string))
                    {
                        this.text = string;
                        this.moveCursorTo(j);
                    }
                }
            }
        }
    }

    public int getWordPosition(int wordOffset)
    {
        return this.getWordPosition(wordOffset, this.getCursor());
    }

    private int getWordPosition(int wordOffset, int cursorPosition)
    {
        return this.getWordPosition(wordOffset, cursorPosition, true);
    }

    private int getWordPosition(int wordOffset, int cursorPosition, boolean skipOverSpaces)
    {
        int i = cursorPosition;
        boolean bl = wordOffset < 0;
        int j = Math.abs(wordOffset);

        for (int k = 0; k < j; ++k)
        {
            if (!bl)
            {
                int l = this.text.length();
                i = this.text.indexOf(32, i);
                if (i == -1)
                {
                    i = l;
                }
                else
                {
                    while (skipOverSpaces && i < l && this.text.charAt(i) == ' ')
                    {
                        ++i;
                    }
                }
            }
            else
            {
                while (skipOverSpaces && i > 0 && this.text.charAt(i - 1) == ' ')
                {
                    --i;
                }

                while (i > 0 && this.text.charAt(i - 1) != ' ')
                {
                    --i;
                }
            }
        }

        return i;
    }

    public void moveCursor(int offset)
    {
        this.moveCursorTo(this.getCursorPos(offset));
    }

    private int getCursorPos(int offset)
    {
        return moveByCodePoints(this.text, this.selectionStart, offset);
    }

    public void setCursorPosition(int cursor)
    {
        this.selectionStart = Mth.clamp(cursor, 0, this.text.length());
    }

    public void moveCursorToStart()
    {
        this.moveCursorTo(0);
    }

    public void moveCursorToEnd()
    {
        this.moveCursorTo(this.text.length());
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (!this.isActive())
        {
            return false;
        }
        else
        {
            this.selecting = Screen.hasShiftDown();
            if (Screen.isSelectAll(keyCode))
            {
                this.moveCursorToEnd();
                this.setSelectionEnd(0);
                return true;
            }
            else if (Screen.isCopy(keyCode))
            {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getSelectedText());
                return true;
            }
            else if (Screen.isPaste(keyCode))
            {
                if (this.editable)
                {
                    this.write(Minecraft.getInstance().keyboardHandler.getClipboard());
                }

                return true;
            }
            else if (Screen.isCut(keyCode))
            {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getSelectedText());
                if (this.editable)
                {
                    this.write("");
                }

                return true;
            }
            else
            {
                return switch (keyCode)
                {
                    case GLFW.GLFW_KEY_BACKSPACE ->
                    {
                        if (this.editable)
                        {
                            this.selecting = false;
                            this.deleteText(-1);
                            this.selecting = Screen.hasShiftDown();
                        }
                        yield true;
                    }
                    default -> false;
                    case GLFW.GLFW_KEY_DELETE ->
                    {
                        if (this.editable)
                        {
                            this.selecting = false;
                            this.deleteText(1);
                            this.selecting = Screen.hasShiftDown();
                        }
                        yield true;
                    }
                    case GLFW.GLFW_KEY_RIGHT ->
                    {
                        if (Screen.hasControlDown())
                        {
                            this.moveCursorTo(this.getWordPosition(1));
                        }
                        else
                        {
                            this.moveCursor(1);
                        }
                        yield true;
                    }
                    case GLFW.GLFW_KEY_LEFT ->
                    {
                        if (Screen.hasControlDown())
                        {
                            this.moveCursorTo(this.getWordPosition(-1));
                        }
                        else
                        {
                            this.moveCursor(-1);
                        }
                        yield true;
                    }
                    case GLFW.GLFW_KEY_HOME ->
                    {
                        this.moveCursorToStart();
                        yield true;
                    }
                    case GLFW.GLFW_KEY_END ->
                    {
                        this.moveCursorToEnd();
                        yield true;
                    }
                };
            }
        }
    }

    public boolean isActive()
    {
        return this.visible() && this.isFocused() && this.isEditable();
    }

    public boolean charTyped(char chr, int modifiers)
    {
        if (!this.isActive())
        {
            return false;
        }
        else if (net.minecraft.util.StringUtil.isAllowedChatCharacter(chr))
        {
            if (this.editable)
            {
                this.write(Character.toString(chr));
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    public void onClick(double mouseX, double mouseY)
    {
        int i = Mth.floor(mouseX) - x0();
        if (this.drawFancyBackground)
        {
            i -= 4;
        }

        String string = this.textRenderer.plainSubstrByWidth(this.text.substring(this.firstCharacterIndex), this.getInnerWidth());
        this.moveCursorTo(this.textRenderer.plainSubstrByWidth(string, i).length() + this.firstCharacterIndex);
    }
    
    @Override
    public void playDownSound(SoundManager soundManager)
    {
    }
    
    protected void drawSelectionHighlight(GuiGraphics context, int x1, int y1, int x2, int y2)
    {
        int i;
        if (x1 < x2)
        {
            i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2)
        {
            i = y1;
            y1 = y2;
            y2 = i;
        }

        if (x2 > x0() + this.width)
        {
            x2 = x0() + this.width;
        }

        if (x1 > x0() + this.width)
        {
            x1 = x0() + this.width;
        }

        context.fill(RenderType.guiTextHighlight(), x1, y1, x2, y2, -16776961);
    }

    public int getMaxLength()
    {
        return this.maxLength;
    }

    public void setMaxLength(int maxLength)
    {
        this.maxLength = maxLength;
        if (this.text.length() > maxLength)
        {
            this.text = this.text.substring(0, maxLength);
            this.onValueChange(this.text);
        }

    }

    public int getCursor()
    {
        return this.selectionStart;
    }

    public void moveCursorTo(int cursor)
    {
        this.setCursorPosition(cursor);
        if (!this.selecting)
        {
            setSelectionEnd(this.selectionStart);
        }

        this.onValueChange(this.text);
    }

    public int getScreenX(int index)
    {
        return index > this.text.length() ? x0() : x0() + this.textRenderer.width(this.text.substring(0, index));
    }

    public void setCanLoseFocus(boolean focusUnlocked)
    {
        this.focusUnlocked = focusUnlocked;
    }

    public void setBordered(boolean drawsBackground)
    {
        this.drawFancyBackground = drawsBackground;
    }

    public void setTextColor(int editableColor)
    {
        this.editableColor = editableColor;
    }

    public void setTextColorUneditable(int uneditableColor)
    {
        this.uneditableColor = uneditableColor;
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent navigation)
    {
        return this.visible && this.editable ? super.nextFocusPath(navigation) : null;
    }

    private static int moveByCodePoints(String text, int cursor, int delta)
    {
        if (delta == 0 || text.isEmpty())
            return cursor;
        try
        {
            return text.offsetByCodePoints(cursor, delta);
        }
        catch (IndexOutOfBoundsException ignored)
        {
            return delta > 0 ? text.length() : 0;
        }
    }

    public void setFocused(boolean focused)
    {
        if (this.focusUnlocked || focused)
        {
            super.setFocused(focused);
            if (focused)
            {
                this.focusedTicks = 0;
            }

        }
    }

    protected boolean isEditable()
    {
        return this.editable;
    }

    public void setEditable(boolean editable)
    {
        this.editable = editable;
    }

    public int getInnerWidth()
    {
        return drawFancyBackground ? this.width - 8 : this.width;
    }

    public void setSelectionEnd(int index)
    {
        int i = this.text.length();
        this.selectionEnd = Mth.clamp(index, 0, i);
        if (this.textRenderer != null)
        {
            if (this.firstCharacterIndex > i)
            {
                this.firstCharacterIndex = i;
            }

            int j = this.getInnerWidth();
            String string = this.textRenderer.plainSubstrByWidth(this.text.substring(this.firstCharacterIndex), j);
            int k = string.length() + this.firstCharacterIndex;
            if (this.selectionEnd == this.firstCharacterIndex)
            {
                this.firstCharacterIndex -= this.textRenderer.plainSubstrByWidth(this.text, j, true).length();
            }

            if (this.selectionEnd > k)
            {
                this.firstCharacterIndex += this.selectionEnd - k;
            }
            else if (this.selectionEnd <= this.firstCharacterIndex)
            {
                this.firstCharacterIndex -= this.firstCharacterIndex - this.selectionEnd;
            }

            this.firstCharacterIndex = Mth.clamp(this.firstCharacterIndex, 0, i);
        }
    }
}
