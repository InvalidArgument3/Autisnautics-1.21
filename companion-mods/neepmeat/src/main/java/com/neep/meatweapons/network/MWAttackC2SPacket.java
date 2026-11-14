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

package com.neep.meatweapons.network;

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.network.GlobalChannelManager;
import com.neep.meatweapons.interfaces.MWPlayerEntity;
import com.neep.meatweapons.item.GunItem;
import com.neep.neepmeat.NeepMeat;
import net.neoforged.api.distmarker.Dist;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MWAttackC2SPacket
{
    public static final GlobalChannelManager<SyncAttack> CHANNEL = GlobalChannelManager.create(
            ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "sync_attack"), ChannelFormat.builder(SyncAttack.class)
                            .param(ParamCodec.INT)
                            .param(ParamCodec.DOUBLE)
                            .param(ParamCodec.DOUBLE)
                            .param(ParamCodec.ofEnum(HandType.class))
                            .param(ParamCodec.ofEnum(ActionType.class))
                    .build());

    public interface SyncAttack
    {
        void apply(int trigger, double pitch, double yaw, HandType hand, ActionType actionType);
    }

    public static final int TRIGGER_PRIMARY = 1;
    public static final int TRIGGER_SECONDARY = 2;

    public static void init()
    {

    }

    public static FriendlyByteBuf create(int trigger, double pitch, double yaw, int hand, ActionType actionType)
    {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(trigger);
        buf.writeDouble(pitch);
        buf.writeDouble(yaw);
        buf.writeByte(hand);
        buf.writeByte(actionType.ordinal());

        return buf;
    }

    static
    {
        CHANNEL.receiverHandler(Dist.DEDICATED_SERVER, (player, buf, responseSender) -> (trigger, pitch, yaw, hand, actionType) ->
                apply(player, trigger, pitch, yaw, hand, actionType));
    }

    private static void apply(Player player, int triggerId, double pitch, double yaw, HandType handType, ActionType actionType)
    {
        ItemStack mainStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offStack = player.getItemInHand(InteractionHand.OFF_HAND);

        // Slight jank here. Needs a refactor. Some day. Probably.
        switch (actionType)
        {
            case PRESS ->
            {
                if (handType.mainHand() && mainStack.getItem() instanceof GunItem gunItem)
                {
                    gunItem.trigger(player.level(), player, mainStack, triggerId, pitch, yaw, handType, InteractionHand.MAIN_HAND);
                }

                if (handType.offHand() && offStack.getItem() instanceof GunItem gunItem)
                {
                    gunItem.trigger(player.level(), player, offStack, triggerId, pitch, yaw, handType, InteractionHand.OFF_HAND);
                }
            }
            case RELEASE ->
            {
                if (handType.mainHand() && mainStack.getItem() instanceof GunItem gunItem)
                {
                    gunItem.release(player.level(), player, mainStack, triggerId, pitch, yaw, handType, InteractionHand.MAIN_HAND);
                }

                if (handType.offHand() && offStack.getItem() instanceof GunItem gunItem)
                {
                    gunItem.release(player.level(), player, offStack, triggerId, pitch, yaw, handType, InteractionHand.OFF_HAND);
                }
            }
        }

        ((MWPlayerEntity) player).meatweapons$getWeaponManager().updateStatus(handType, triggerId, actionType);
    }

    public enum HandType
    {
        NONE(),
        MAIN(),
        OFF(),
        BOTH();

        public static HandType of(int binary)
        {
            return values()[binary];
        }

        public static HandType of(boolean main, boolean off)
        {
            return main && off ? BOTH
                    : !main && !off ? NONE
                    : main ? MAIN
                    : OFF;
        }

        public InteractionHand oneOrTheOther()
        {
            return this == OFF ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        }

        public boolean mainHand()
        {
            return this == MAIN || this == BOTH;
        }

        public boolean offHand()
        {
            return this == OFF || this == BOTH;
        }
    }

    public enum ActionType
    {
        PRESS,
        RELEASE;

        public boolean pressed()
        {
            return this == PRESS;
        }

    }
}
