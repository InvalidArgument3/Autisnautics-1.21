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

package com.neep.meatlib.client.screen.slot;

import com.mojang.datafixers.util.Pair;
import com.neep.meatlib.client.screen.primitive.Point;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class MobileSlot implements Rectangle, Point.Mutable
{
    private final Slot slot;
    private int x;
    private int y;

    private final Minecraft client = Minecraft.getInstance();
    private final Font textRenderer = client.font;

    public MobileSlot(Slot slot)
    {
        this.slot = slot;
    }

    @Override
    public void setPos(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    @Override
    public int x0()
    {
        return x;
    }

    @Override
    public int y0()
    {
        return y;
    }

    @Override
    public int nw()
    {
        return 16;
    }

    @Override
    public int nh()
    {
        return 16;
    }

    public Slot slot()
    {
        return slot;
    }

    public void render(GuiGraphics context, ItemStack itemStack, @Nullable String countOverride, boolean bl, boolean bl2)
    {
        if (itemStack.isEmpty() && slot.isActive())
        {
            Pair<ResourceLocation, ResourceLocation> pair = slot.getNoItemIcon();
            if (pair != null)
            {
                TextureAtlasSprite sprite = client.getTextureAtlas(pair.getFirst()).apply(pair.getSecond());
                context.blit(x, y, 0, 16, 16, sprite);
                bl2 = true;
            }
        }

        if (!bl2)
        {
            if (bl)
            {
                context.fill(x, y, x + 16, y + 16, -2130706433);
            }

            context.renderItem(itemStack, x, y, x + y);
            context.renderItemDecorations(textRenderer, itemStack, x, y, countOverride);
        }
    }
}
