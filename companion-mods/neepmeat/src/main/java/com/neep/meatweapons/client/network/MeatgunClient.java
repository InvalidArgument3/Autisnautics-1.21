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

package com.neep.meatweapons.client.network;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatweapons.component.MeatgunComponent;
import com.neep.meatweapons.init.MWComponents;
import com.neep.meatweapons.item.meatgun.Meatgun;
import com.neep.meatweapons.item.meatgun.MeatgunAnimationManager;
import com.neep.meatweapons.meatgun.AmmunitionType;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.meatgun.module.AmmunitionStoringModule;
import com.neep.meatweapons.meatgun.module.MeatgunModule;
import com.neep.meatweapons.network.MeatgunNetwork;
import com.neep.neepmeat.NeepMeat;
import com.neep.meatlib.theme.PLCCols;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class MeatgunClient
{
    public static void init()
    {
        MeatgunNetwork.SEND_RECOIL.receiver(Dist.CLIENT, (direction, amount, horAmount, returnSpeed, horReturnSpeed, cameraAmount,  hand) ->
        {
            Minecraft client = Minecraft.getInstance();

            MeatgunComponent component;
            if (hand == InteractionHand.MAIN_HAND)
                component = MWComponents.MEATGUN.getNullable(client.player.getMainHandItem());
            else
                component = MWComponents.MEATGUN.getNullable(client.player.getOffhandItem());

            if (component != null)
                component.getRecoil().set(direction, amount, horAmount, returnSpeed, horReturnSpeed);

            if (cameraAmount > 0)
            {
                client.player.getComponent(MWComponents.GUN_TICK_HANDLER).addRecoil(cameraAmount);
            }
        });

        MeatgunNetwork.SEND_SHAKE.receiver(Dist.CLIENT, (magnitude, returnSpeed, hand) ->
        {
            Minecraft client = Minecraft.getInstance();

            MeatgunComponent component;
            if (hand == InteractionHand.MAIN_HAND)
                component = MWComponents.MEATGUN.getNullable(client.player.getMainHandItem());
            else
                component = MWComponents.MEATGUN.getNullable(client.player.getOffhandItem());

            if (component != null)
                component.getRecoil().setShake(magnitude, returnSpeed);
        });

        MeatgunNetwork.SEND_ANIMATION.receiver(Dist.CLIENT, MeatgunClient::receiveAnimation);

        MeatgunNetwork.SYNC_MODULE.receiverHandler(Dist.CLIENT, MeatgunClient::receiveSync);

        HudRenderCallback.EVENT.register((drawContext, tickDelta) ->
        {
            if (NeepMeat.CONFIG.meatgunAmmoOnModel.getRealValue())
                return;

            Minecraft client = Minecraft.getInstance();
            LocalPlayer player = client.player;
            if (player.getMainHandItem().getItem() instanceof Meatgun)
            {
                MeatgunComponent component1 = MWComponents.MEATGUN.getNullable(player.getMainHandItem());
                MeatgunComponent component2 = MWComponents.MEATGUN.getNullable(player.getOffhandItem());

                drawContext.pose().pushPose();
                drawContext.pose().translate(client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight(), 0);
                if (component1 != null)
                {
                    renderHud(drawContext, tickDelta, component1.getRootHolder(), true);
                }
                else if (component2 != null)
                {
                    renderHud(drawContext, tickDelta, component2.getRootHolder(), true);
                }

                drawContext.pose().popPose();
            }
        });
    }

    private static MeatgunNetwork.SyncModule receiveSync(Player player, FriendlyByteBuf buf, PacketSender packetSender)
    {
        return (uuid, nbt, hand) ->
        {
            ItemStack stack = player.getItemInHand(hand);
            MeatgunComponent component = MWComponents.MEATGUN.getNullable(stack);
            if (component != null)
            {
                MeatgunModule module = component.getRootHolder().moduleCache().findModule(uuid);
                if (module != null)
                {
                    module.readNbt(nbt, null);
                }
            }
        };
    }

    public static void renderHud(GuiGraphics drawContext, float tickDelta, RootModuleHolder holder, boolean bottom)
    {
        Minecraft client = Minecraft.getInstance();
        Font textRenderer = client.font;
        Map<AmmunitionType, List<AmmunitionStoringModule>> map = holder.moduleCache().getAmmo();

        int lines = 0;
        int stride = textRenderer.lineHeight + 1;
        for (var value : map.values())
        {
            if (!value.isEmpty())
                lines++;
        }

        if (map.containsKey(AmmunitionType.METABOLIC))
            lines--;

        if (lines == 0)
            return;

        int col = PLCCols.BORDER.col & 0x88FFFFFF;

        int width = 60;
        int height = lines * stride + 2;
        PoseStack matrices = drawContext.pose();
        if (bottom)
        {
            matrices.translate(-width, -height, 0);
        }
        matrices.translate(0, 0, -0.01);
        drawContext.fill(-1, -1, width + 1, height + 1, 0x30000000);
        matrices.translate(0, 0, 0.01);
        GUIUtil.renderBorderInner(drawContext, 0, 0, width, height, col, 0);

        int textY = 2;
        matrices.pushPose();
        matrices.scale(1, 1, 0.1f); // Squish down the text and shadow layers in Z
        for (var entry : map.entrySet())
        {
            if (entry.getValue().isEmpty() || entry.getKey() == AmmunitionType.METABOLIC)
                continue;

            int amount = entry.getValue().stream().mapToInt(AmmunitionStoringModule::amount).sum();

            int col2 = col;
            if (amount == 0)
                col2 = 0x88FF0000;

            GUIUtil.drawText(drawContext, textRenderer, entry.getKey().shortName() + " " + amount, 2, textY, col2, true);
            textY += stride;
        }
        matrices.popPose();
    }

    private static void receiveAnimation(String name, InteractionHand hand, FriendlyByteBuf buf)
    {
        Minecraft client = Minecraft.getInstance();
        MeatgunComponent component1 = MWComponents.MEATGUN.getNullable(client.player.getItemInHand(hand));
        if (component1 != null)
        {
            MeatgunAnimationManager animationManager = component1.getAnimationManager();
            if (animationManager != null)
                animationManager.queue(name, buf);
        }
    }

    public static float modifyFov(float prevValue)
    {
        var client = Minecraft.getInstance();

        float p = client.player.getComponent(MWComponents.GUN_TICK_HANDLER).getAds().getProgress(client.getTimer().getRealtimeDeltaTicks());

        return prevValue - p * 0.1f;
    }
}
