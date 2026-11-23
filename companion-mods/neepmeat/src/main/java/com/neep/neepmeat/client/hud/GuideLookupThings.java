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

package com.neep.neepmeat.client.hud;

import com.mojang.blaze3d.platform.InputConstants;
import com.neep.meatlib.client.event.AppendTooltipEvent;
import com.neep.meatlib.item.MeatlibItemExtension;
import com.neep.neepmeat.api.theme.GuideCols;
import com.neep.neepmeat.client.screen.tablet.GuideMainScreen;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.guide.GuideNode;
import com.neep.neepmeat.guide.GuideReloadListener;
import com.neep.neepmeat.implant.entity.GuideImplant;
import com.neep.neepmeat.implant.entity.PlayerImplantManager;
import com.neep.neepmeat.item.ProjectorItem;
import me.shedaniel.autoconfig.annotation.Config;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class GuideLookupThings
{
    public static ItemStack NEXT_LOOKUP = ItemStack.EMPTY;

    public static void init()
    {
//        RenderItemGuiEvent.EVENT.register(GuideTooltipThings::onRender);

        ClientTickEvents.START_CLIENT_TICK.register(GuideLookupThings::onStartTick);
        AppendTooltipEvent.EVENT.register(GuideLookupThings::onAppendTooltip);
    }

    private static void onStartTick(Minecraft client)
    {
        if (!NEXT_LOOKUP.isEmpty())
        {
            Player playerEntity = client.player;
            var implants = PlayerImplantManager.get(playerEntity);
            if (!implants.isInstalled(GuideImplant.ID) && !playerEntity.getInventory().contains(NMTags.GUIDE_LOOKUP))
            {
                NEXT_LOOKUP = ItemStack.EMPTY;
                client.player.displayClientMessage(Component.literal("No Guide Projector in inventory"), true);
                return;
            }

            List<GuideNode> path = GuideReloadListener.getInstance().getPath(NEXT_LOOKUP);
            if (path != null && GuideReloadListener.getInstance().isValid())
            {
                GuideMainScreen screen = ProjectorItem.Client.openScreen();
                for (var node : path)
                {
                    node.visitScreen(screen);
                }
            }
            NEXT_LOOKUP = ItemStack.EMPTY;
        }
    }

    private static void onAppendTooltip(ItemStack stack, @Nullable Level world, TooltipFlag context, List<Component> toot)
    {
        if (((MeatlibItemExtension) stack.getItem()).meatlib$supportsGuideLookup()
                && GuideReloadListener.getInstance().isValidLookup(stack))
        {
            if (toot instanceof ArrayList<Component> mutable)
            {
                mutable.add(Component.literal("\uD83D\uDD6E CTRL+L").setStyle(Style.EMPTY.withColor(GuideCols.darkCol())));
            }

            if (Screen.hasControlDown() && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_L))
            {
                GuideLookupThings.NEXT_LOOKUP = stack.copy();
            }
        }
    }

//    private static void onRender(DrawContext context, TextRenderer textRenderer, ItemStack stack, int x, int y)
//    {
//        MinecraftClient client = MinecraftClient.getInstance();
//        if (client.player == null || client.player.currentScreenHandler == null)
//            return;
//
//        ItemStack cursorStack = client.player.currentScreenHandler.getCursorStack();
//
//        if (!stack.isOf(NMItems.PROJECTOR) || !cursorStack.equals(stack))
//            return;
//
////        client.player.currentScreenHandler.click
//        context.drawTooltip(textRenderer, Text.of("GROMMY"), x, y);
//    }
}
