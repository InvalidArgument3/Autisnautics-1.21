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

package com.neep.neepmeat.transport.client.screen.filter;

import com.neep.meatlib.client.screen.TooltipHelper;
import com.neep.neepmeat.NeepMeat;
import com.neep.meatlib.theme.PLCCols;
import com.neep.meatlib.client.screen.NMTextField;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.neepmeat.item.filter.FilterList;
import com.neep.neepmeat.item.filter.TagFilter;
import com.neep.neepmeat.transport.screen.FilterWidgetHandler;
import com.neep.neepmeat.util.TagSuggestions;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class TagFilterWidget extends FilterEntryWidget<TagFilter>
{
    private final TooltipHelper belowTooltip;

    public TagFilterWidget(int w, int index, FilterList.Entry entry, TagFilter filter, FilterWidgetHandler handler)
    {
        super(w, 32, index, NeepMeat.translationKey("screen", "filter.tag_filter"), entry, filter, handler);
        this.belowTooltip = new TooltipHelper(false, PLCCols.TEXT.col, false);
    }

    @Override
    public void init()
    {
        super.init();

        addChild(new TagTextField(Minecraft.getInstance().font,
                this.x0() + 2, this.y0() + textRenderer.lineHeight + 4,
                w - 4, 16,
                this::update,
                filter.getTag() != null ? filter.getTag().location().toString() : ""));
    }

    private void update(String text)
    {
        ResourceLocation id = ResourceLocation.tryParse(text);
        if (TagSuggestions.INSTANCE.isValid(id))
        {
            filter.setTag(TagKey.create(BuiltInRegistries.ITEM.key(), id));
            handler.updateToServer.emitter().apply(index, filter.writeNbt(new CompoundTag()));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        boolean b = super.mouseClicked(mouseX, mouseY, button);

        return b;
    }

    private class TagTextField extends NMTextField
    {
        private final Consumer<String> update;
        private List<ResourceLocation> suggestions = List.of();

        public TagTextField(Font textRenderer, int x, int y, int width, int height, Consumer<String> update, String initial)
        {
            super(textRenderer, x, y, width, height, Component.empty());
            this.update = update;
            setText(initial);
            setChangedListener(this::onChanged);
        }

        @Override
        public void onClick(double mouseX, double mouseY)
        {
            super.onClick(mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button)
        {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers)
        {
            // Consume E key
            if (client.options.keyInventory.matches(keyCode, scanCode))
                return true;

            if (keyCode == GLFW.GLFW_KEY_TAB)
            {
                cycleSuggestion(Screen.hasShiftDown() ? 1 : -1);
                return true;
            }
            else if (keyCode == GLFW.GLFW_KEY_ENTER)
            {
                confirmSuggestion();
            }

            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char chr, int modifiers)
        {
            return super.charTyped(chr, modifiers);
        }

        @Override
        protected void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            int borderCol = isSelected() ? PLCCols.SELECTED.col : PLCCols.BORDER.col;
            GUIUtil.renderBorderInner(context, x0(), this.y0(), nw(), nh(), borderCol, 0);
        }

        public void renderButton(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            super.renderButton(context, mouseX, mouseY, delta);

            int maxSuggestions = 5;
            if (!suggestions.isEmpty() && isActive())
            {
                int suggestionY = y1();

                int end = Math.min(suggestions.size(), maxSuggestions);
                int stride = textRenderer.lineHeight + 2;
//                int yOffsetEnd = end * stride;
//                context.fill(x(), y2(), x() + w(), y2() + yOffsetEnd, 0xBB331111);

                List<Component> texts = suggestions.stream().limit(maxSuggestions).map(i -> Component.nullToEmpty(i.toString())).toList();
                belowTooltip.draw(context, x0(), suggestionY, texts);
            }
        }

        @Override
        protected int renderUnselectedText(GuiGraphics context, String string, boolean selectionWithin, int textStart, int m, int col, int j)
        {
            if (!suggestions.isEmpty())
            {
                GUIUtil.drawText(context, this.textRenderer, suggestions.get(0).toString(), textStart, m, PLCCols.INVALID.col, true);
            }

            return super.renderUnselectedText(context, string, selectionWithin, textStart, m, col, j);
        }

        public String getPrefix()
        {
            return "#";
        }

        private void onChanged(String current)
        {
            suggestions = TagSuggestions.INSTANCE.get(current);
            update.accept(current);
        }

        private void cycleSuggestion(int distance)
        {
            Collections.rotate(suggestions, distance);
        }

        private void confirmSuggestion()
        {
            if (!suggestions.isEmpty())
                setText(suggestions.get(0).toString());
        }

        @Override
        protected int prefixCol()
        {
            return PLCCols.LINE_NUMBER.col;
        }

        @Override
        public void setFocused(boolean focused)
        {
            super.setFocused(focused);
        }
    }
}
