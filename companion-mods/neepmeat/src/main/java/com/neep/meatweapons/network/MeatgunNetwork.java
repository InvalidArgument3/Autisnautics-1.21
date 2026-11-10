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
import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.init.MWComponents;
import com.neep.meatweapons.meatgun.module.HalberdModule;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class MeatgunNetwork
{
    public static final GlobalChannelManager<SendAnimation> SEND_ANIMATION = GlobalChannelManager.create(ResourceLocation.tryBuild(MeatWeapons.NAMESPACE, "meatgun/send_animation"),
            ChannelFormat.builder(SendAnimation.class).param(ParamCodec.STRING).param(ParamCodec.ofEnum(InteractionHand.class)).param(ParamCodec.BUF).build());

    public static final GlobalChannelManager<SyncModule> SYNC_MODULE = GlobalChannelManager.create(ResourceLocation.tryBuild(MeatWeapons.NAMESPACE, "meatgun/sync_module"),
            ChannelFormat.builder(SyncModule.class).param(ParamCodec.UUID).param(ParamCodec.NBT).param(ParamCodec.ofEnum(InteractionHand.class)).build());

    public static final GlobalChannelManager<SendRecoil> SEND_RECOIL = GlobalChannelManager.create(ResourceLocation.tryBuild(MeatWeapons.NAMESPACE, "meatgun/send_recoil"), SendRecoil.FORMAT);
    public static final GlobalChannelManager<SendShake> SEND_SHAKE = GlobalChannelManager.create(ResourceLocation.tryBuild(MeatWeapons.NAMESPACE, "meatgun/send_shake"), SendShake.FORMAT);

    public static void syncModule(Player player, HalberdModule module, InteractionHand hand)
    {
        UUID uuid = module.getUuid();
        CompoundTag nbt = module.writeNbt(new CompoundTag());
        SYNC_MODULE.emitter(player).apply(uuid, nbt, hand);
    }

    public static void sendRecoil(LivingEntity player, float amount, float horAmount, float returnSpeed, float horReturnSpeed, InteractionHand hand)
    {
        sendRecoil(player, amount, horAmount, returnSpeed, horReturnSpeed, 0, hand);
    }

    public static void sendRecoil(LivingEntity player, float amount, float horAmount, float returnSpeed, float horReturnSpeed, float cameraAmount, InteractionHand hand)
    {
        if (player instanceof ServerPlayer serverPlayerEntity)
        {
            serverPlayerEntity.getComponent(MWComponents.GUN_TICK_HANDLER).addRecoil(cameraAmount);
            SEND_RECOIL.emitter(serverPlayerEntity).apply(RecoilDirection.UP, amount, horAmount, returnSpeed, horReturnSpeed, cameraAmount, hand);
        }
    }

    public static void sendShake(LivingEntity player, float magnitude, float returnTicks, InteractionHand hand)
    {
        if (player instanceof ServerPlayer serverPlayerEntity)
        {
            float returnSpeed = magnitude / returnTicks;
            SEND_SHAKE.emitter(serverPlayerEntity).apply(magnitude, returnSpeed, hand);
        }
    }

    // TODO: remove
    public enum RecoilDirection
    {
        UP,
        DOWN,
    }

    public interface SendAnimation
    {
        void apply(String name, InteractionHand hand, FriendlyByteBuf buf);
    }

    public interface SyncModule
    {
        void apply(UUID uuid, CompoundTag nbt, InteractionHand hand);
    }

    public interface SendRecoil
    {
        ChannelFormat<SendRecoil> FORMAT = ChannelFormat.builder(SendRecoil.class)
                .param(ParamCodec.ofEnum(RecoilDirection.class))
                .param(ParamCodec.FLOAT)
                .param(ParamCodec.FLOAT)
                .param(ParamCodec.FLOAT)
                .param(ParamCodec.FLOAT)
                .param(ParamCodec.FLOAT)
                .param(ParamCodec.ofEnum(InteractionHand.class))
                .build();

        void apply(RecoilDirection direction, float amount, float horAmount, float returnSpeed, float horReturnSpeed, float cameraAmount, InteractionHand hand);
    }

    public interface SendShake
    {
        ChannelFormat<SendShake> FORMAT = ChannelFormat.builder(SendShake.class)
                .param(ParamCodec.FLOAT)
                .param(ParamCodec.FLOAT)
                .param(ParamCodec.ofEnum(InteractionHand.class))
                .build();

        void apply(float magnitude, float returnSpeed, InteractionHand hand);
    }
}
