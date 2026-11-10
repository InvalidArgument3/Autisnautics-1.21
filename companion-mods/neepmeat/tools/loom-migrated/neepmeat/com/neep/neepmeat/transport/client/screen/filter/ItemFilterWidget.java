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

import com.mojang.blaze3d.platform.GlStateManager;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.TooltipHelper;
import com.neep.meatlib.client.screen.primitive.Point;
import com.neep.meatlib.client.screen.widget.AbstractClickableWidget;
import com.neep.meatlib.client.screen.widget.CheckboxWidget;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.item.filter.FilterList;
import com.neep.neepmeat.item.filter.ItemFilter;
import com.neep.neepmeat.transport.screen.FilterWidgetHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.function.Supplier;

public class ItemFilterWidget extends FilterEntryWidget<ItemFilter>
{
    private final Minecraft client = Minecraft.getInstance();
    private final Font textRenderer = client.font;

    private final Supplier<ItemStack> cursorStack;

    public ItemFilterWidget(int w, int index, FilterList.Entry entry, ItemFilter filter,
                            FilterWidgetHandler handler, Supplier<ItemStack> cursorStack)
    {
        super(w, 50, index, NeepMeat.translationKey("screen", "filter.item_filter"), entry, filter, handler);
        this.cursorStack = cursorStack;
    }

    @Override
    public void init()
    {
        super.init();

        int slotsY = y + textRenderer.lineHeight + 4;
        int slotsX = x + 2;

        int slotsW = 5;
        int slotsH = 2;
        for (int j = 0; j < slotsH; j++)
        {
            for (int i = 0; i < slotsW; i++)
            {
                addChild(new ItemSlotWidget(slotsX + i * 17, slotsY + 17 * j, i + j * slotsW));
            }
        }

        addChild(new CheckboxWidget(x + 17 * 5 + 4, y + textRenderer.lineHeight + 4, 50, 16, () -> filter.ignoreDamage(), Component.nullToEmpty("Use damage"), (b, t) ->
        {
            filter.setUseDamage(t);
            updateToServer();
        }));

        addChild(new CheckboxWidget(x + 17 * 5 + 4, y + textRenderer.lineHeight + 4 + 17, 50, 16, () -> filter.ignoreNbt(), Component.nullToEmpty("Use all NBT"), (b, t) ->
        {
            filter.setUseNbt(t);
            updateToServer();
        }));
    }

    public class ItemSlotWidget extends AbstractClickableWidget implements Point.Mutable
    {
        private final int slotIndex;

        public ItemSlotWidget(int x, int y, int slotIndex)
        {
            super(x, y, 18, 18, Component.empty());
            this.slotIndex = slotIndex;
        }

        @Override
        protected void renderButton(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            GUIUtil.renderBorderInner(context, x0(), y0(), 18, 18, PLCCols.BORDER.col, 0);
            GUIUtil.renderBorderInner(context, x0(), y0(), 18, 18, PLCCols.TRANSPARENT.col, -1);

            ItemStack stack = filter.getItem(slotIndex).toStack(1);
            context.renderItem(stack, x0() + 1, y0() + 1);
            context.renderItemDecorations(textRenderer, stack, x0() + 1, y0() + 1);

            if (isMouseOver(mouseX, mouseY) && !stack.isEmpty() && cursorStack.get().isEmpty())
            {
                GlStateManager._disableScissorTest();
                TooltipHelper.get().draw(context, mouseX, mouseY, stack.getTooltipLines(client.player, TooltipFlag.NORMAL));
                GlStateManager._enableScissorTest();
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY)
        {
            super.onClick(mouseX, mouseY);
            setStack(cursorStack.get());
        }

        public void setStack(ItemStack stack)
        {
            filter.setItem(slotIndex, ItemVariant.of(stack));
            updateToServer();
        }
    }
}
