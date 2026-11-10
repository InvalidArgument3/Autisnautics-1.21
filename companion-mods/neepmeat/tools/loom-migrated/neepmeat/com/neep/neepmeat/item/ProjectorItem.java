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

package com.neep.neepmeat.item;

import com.neep.meatlib.item.BaseItem;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.theme.GuideCols;
import com.neep.neepmeat.client.screen.tablet.GuideMainScreen;
import com.neep.neepmeat.guide.GuideReloadListener;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.List;

public class ProjectorItem extends BaseItem
{
    public ProjectorItem(Properties settings)
    {
        super(ProjectorItem::applyTooltip, settings);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand)
    {
        if (world.isClientSide())
        {
            Client.openScreen();
        }
        return InteractionResultHolder.success(user.getItemInHand(hand));
    }

//    @Override
//    public Text getDisplayName()
//    {
//        return Text.translatable(NeepMeat.NAMESPACE + "screen.guide");
//    }

    public static void applyTooltip(ItemStack item, List<Component> tooltip)
    {
        TooltipSupplier.wrapLines(tooltip, Component.translatable(item.getDescriptionId() + ".lore_0").withStyle(ChatFormatting.YELLOW));
        TooltipSupplier.wrapLines(tooltip, Component.translatable(item.getDescriptionId() + ".lore_1").setStyle(Style.EMPTY.withColor(GuideCols.darkCol())));
    }

//    @Nullable
//    @Override
//    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player)
//    {
//        return new GuideScreenHandler(syncId, inv);
//    }


    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction clickType, Player player)
    {
        return super.overrideStackedOnOther(stack, slot, clickType, player);
//        return true;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference)
    {
        return super.overrideOtherStackedOnMe(stack, otherStack, slot, clickType, player, cursorStackReference);
//        return true;
    }

    @Environment(EnvType.CLIENT)
    public static class Client
    {
        public static GuideMainScreen openScreen()
        {
            Minecraft client = Minecraft.getInstance();

            GuideReloadListener listener = GuideReloadListener.getInstance();
            if (!listener.isValid())
            {
                NeepMeat.LOGGER.error("Error opening NEEPMeat guide.");
                return null;
            }

            // Nothing to do, the screen is already open
            if (client.screen instanceof GuideMainScreen mainScreen)
                return mainScreen;

            GuideMainScreen screen = new GuideMainScreen();

            if (client.player != null)
                client.player.closeContainer();

            client.setScreen(screen);

            if (listener.getPersistentArticle() != null)
                screen.openArticle(listener.getPersistentArticle());

            return screen;
        }
    }
}
