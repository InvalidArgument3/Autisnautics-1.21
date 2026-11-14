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

package com.neep.neepmeat.client.screen.living_machine;

import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.client.screen.slot.VanillaInventoryWidget;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.client.screen.button.TextToggleButtonWidget;
import com.neep.neepmeat.client.screen.util.BeigeSlotBlockWidget;
import com.neep.neepmeat.screen_handler.ItemOutputScreenHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ItemOutputPortScreen extends BetterHandledScreen<ItemOutputScreenHandler>
{
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/gui/machine_item_output.png");
    private final VanillaInventoryWidget vanillaInventory;

    public ItemOutputPortScreen(ItemOutputScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
        vanillaInventory = new VanillaInventoryWidget(0, 0, handler.slots.subList(9, 9 + 4 * 9), this);
    }

    @Override
    protected void init()
    {
        imageWidth = 176;
        imageHeight = 178;
        super.init();

        int buttonX = leftPos + 80;
        int buttonY = topPos + 14;

        addRenderableWidget(new TextToggleButtonWidget(buttonX, buttonY, 70, 20, () -> menu.getProperty(0) > 0, Component.nullToEmpty("Auto eject"), (b, t) ->
        {
            menu.setEject(t);
        }));

        addSlotBlock(vanillaInventory);
        vanillaInventory.setPos(leftPos, topPos + 88);
        var slots = addSlotBlock(new BeigeSlotBlockWidget(leftPos + 16, topPos + 14, 3, 3, menu.slots.subList(0, 9), this));
        addRenderableOnly(slots);
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY)
    {
        super.renderBackground(context, mouseX, mouseY, delta);
        context.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, 87);

//        GUIUtil.drawInventoryBackground(context, x, y + 88);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);
    }
}
