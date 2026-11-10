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
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.neepmeat.client.sound.LoopingEntityTrackingSoundInstance;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.enlightenment.EnlightenmentManager;
import com.neep.neepmeat.enlightenment.PlayerEnlightenmentManager;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.init.NMItems;
import com.neep.neepmeat.init.NMSounds;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;

public class DosimeterItem extends BaseItem
{
    public DosimeterItem(RegistrationContext registryName, TooltipSupplier tooltipSupplier, Properties settings)
    {
        super(tooltipSupplier, settings);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Client
    {
        private static final DecimalFormat DF = new DecimalFormat("###.#");
        private static final TagKey<Item> DOSIMETER = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "dosimeter"));
        @Nullable static private SoundInstance alarmSound;

        public static void init()
        {
            HudRenderCallback.EVENT.register(Client::render);
//            ClientTickEvents.START_CLIENT_TICK.
        }

        public static void onTick(Minecraft client, Player player, PlayerEnlightenmentManager manager)
        {
            boolean hasDosimeter = player.getInventory().contains(stack -> stack.is(DOSIMETER) || stack.is(NMItems.DOSIMETER));
            boolean danger = manager.lastDose() > 1;

            if (hasDosimeter && danger)
            {
                if (alarmSound == null)
                {
                    alarmSound = new LoopingEntityTrackingSoundInstance(NMSounds.DOSIMETER_ALARM.get(), SoundSource.PLAYERS, 0.5f, 1, client.player, 0);
                    client.getSoundManager().play(alarmSound);
                }
            }
            else
            {
                if (alarmSound != null)
                {
                    client.getSoundManager().stop(alarmSound);
                    alarmSound = null;
                }
            }
        }

        public static void render(GuiGraphics matrices, float tickDelta) {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) {
                return;
            }

            ItemStack stack = null;
            if (client.player.getMainHandItem().is(NMItems.DOSIMETER))
                stack = client.player.getMainHandItem();
            else if (client.player.getOffhandItem().is(NMItems.DOSIMETER))
                stack = client.player.getOffhandItem();

            if (stack != null)
            {
                PlayerEnlightenmentManager manager = new PlayerEnlightenmentManager(client.player);
                var attachment = NMComponents.ENLIGHTENMENT_MANAGER.attachmentType();
                if (attachment != null) {
                    manager.readFromNbt(client.player.getData(attachment));
                }
                int width = client.getWindow().getGuiScaledWidth();
                int height = client.getWindow().getGuiScaledHeight();

                Component text = Component.nullToEmpty("Dose per tick: " + DF.format(manager.lastDose())).copy().withStyle(ChatFormatting.YELLOW);
                int textWidth = client.font.width(text);

                GUIUtil.drawText(matrices, client.font, text,
                        (float) (width - textWidth) / 2, height - 50, 0xFFFFFFFF, true);
            }
        }
    }
}
