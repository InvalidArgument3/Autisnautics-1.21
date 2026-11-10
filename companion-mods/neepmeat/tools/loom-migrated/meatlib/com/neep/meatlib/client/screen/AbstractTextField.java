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
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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

    public void setChangedListener(@Nullable Consumer<String> changedListener)
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

            this.setCursorToEnd();
            this.setSelectionEnd(this.selectionStart);
            this.onChanged(text);
        }
    }

    public String getSelectedText()
    {
        int i = Math.min(this.selectionStart, this.selectionEnd);
        int j = Math.max(this.selectionStart, this.selectionEnd);
        return this.text.substring(i, j);
    }

    public void setTextPredicate(Predicate<String> textPredicate)
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
        String string = SharedConstants.filterText(text);
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
            this.setSelectionStart(i + l);
            this.setSelectionEnd(this.selectionStart);
            this.onChanged(this.text);
        }
    }

    private void onChanged(String newText)
    {
        if (this.changedListener != null)
        {
            this.changedListener.accept(newText);
        }

    }

    private void erase(int offset)
    {
        if (Screen.hasControlDown())
        {
            this.eraseWords(offset);
        }
        else
        {
            this.eraseCharacters(offset);
        }

    }

    public void eraseWords(int wordOffset)
    {
        if (!this.text.isEmpty())
        {
            if (this.selectionEnd != this.selectionStart)
            {
                this.write("");
            }
            else
            {
                this.eraseCharacters(this.getWordSkipPosition(wordOffset) - this.selectionStart);
            }
        }
    }

    public void eraseCharacters(int characterOffset)
    {
        if (!this.text.isEmpty())
        {
            if (this.selectionEnd != this.selectionStart)
            {
                this.write("");
            }
            else
            {
                int i = this.getCursorPosWithOffset(characterOffset);
                int j = Math.min(i, this.selectionStart);
                int k = Math.max(i, this.selectionStart);
                if (j != k)
                {
                    String string = (new StringBuilder(this.text)).delete(j, k).toString();
                    if (this.textPredicate.test(string))
                    {
                        this.text = string;
                        this.setCursor(j);
                    }
                }
            }
        }
    }

    public int getWordSkipPosition(int wordOffset)
    {
        return this.getWordSkipPosition(wordOffset, this.getCursor());
    }

    private int getWordSkipPosition(int wordOffset, int cursorPosition)
    {
        return this.getWordSkipPosition(wordOffset, cursorPosition, true);
    }

    private int getWordSkipPosition(int wordOffset, int cursorPosition, boolean skipOverSpaces)
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
        this.setCursor(this.getCursorPosWithOffset(offset));
    }

    private int getCursorPosWithOffset(int offset)
    {
        return Util.offsetByCodepoints(this.text, this.selectionStart, offset);
    }

    public void setSelectionStart(int cursor)
    {
        this.selectionStart = Mth.clamp(cursor, 0, this.text.length());
    }

    public void setCursorToStart()
    {
        this.setCursor(0);
    }

    public void setCursorToEnd()
    {
        this.setCursor(this.text.length());
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
                this.setCursorToEnd();
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
                            this.erase(-1);
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
                            this.erase(1);
                            this.selecting = Screen.hasShiftDown();
                        }
                        yield true;
                    }
                    case GLFW.GLFW_KEY_RIGHT ->
                    {
                        if (Screen.hasControlDown())
                        {
                            this.setCursor(this.getWordSkipPosition(1));
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
                            this.setCursor(this.getWordSkipPosition(-1));
                        }
                        else
                        {
                            this.moveCursor(-1);
                        }
                        yield true;
                    }
                    case GLFW.GLFW_KEY_HOME ->
                    {
                        this.setCursorToStart();
                        yield true;
                    }
                    case GLFW.GLFW_KEY_END ->
                    {
                        this.setCursorToEnd();
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
        else if (SharedConstants.isAllowedChatCharacter(chr))
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
        this.setCursor(this.textRenderer.plainSubstrByWidth(string, i).length() + this.firstCharacterIndex);
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
            this.onChanged(this.text);
        }

    }

    public int getCursor()
    {
        return this.selectionStart;
    }

    public void setCursor(int cursor)
    {
        this.setSelectionStart(cursor);
        if (!this.selecting)
        {
            setSelectionEnd(this.selectionStart);
        }

        this.onChanged(this.text);
    }

    public int getCharacterX(int index)
    {
        return index > this.text.length() ? x0() : x0() + this.textRenderer.width(this.text.substring(0, index));
    }

    public void setFocusUnlocked(boolean focusUnlocked)
    {
        this.focusUnlocked = focusUnlocked;
    }

    public void setDrawsBackground(boolean drawsBackground)
    {
        this.drawFancyBackground = drawsBackground;
    }

    public void setEditableColor(int editableColor)
    {
        this.editableColor = editableColor;
    }

    public void setUneditableColor(int uneditableColor)
    {
        this.uneditableColor = uneditableColor;
    }

    @Nullable
    public ComponentPath nextFocusPath(FocusNavigationEvent navigation)
    {
        return this.visible && this.editable ? super.nextFocusPath(navigation) : null;
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
