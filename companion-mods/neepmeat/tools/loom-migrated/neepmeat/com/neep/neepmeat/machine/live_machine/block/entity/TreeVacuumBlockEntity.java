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

package com.neep.neepmeat.machine.live_machine.block.entity;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.init.NMSounds;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TreeVacuumBlockEntity extends SyncableBlockEntity implements LivingMachineComponent
{
    public static final ResourceLocation CHANNEL_ID = new ResourceLocation("tree_vacuum_anim");

    public float progress;
    public static final int maxProgress = 5;

    // Client-only jank
    public int animationTicks;

    public TreeVacuumBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public boolean componentRemoved()
    {
        return isRemoved();
    }

    public void syncAnimation(boolean playSound)
    {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(getBlockPos());
        buf.writeBoolean(playSound);
        for (var player : PlayerLookup.tracking(this))
        {
            ServerPlayNetworking.send(player, CHANNEL_ID, buf);
        }
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType()
    {
        return LivingMachineComponents.TREE_VACUUM;
    }

    public static void clientTick(Level world, BlockPos pos, BlockState state, TreeVacuumBlockEntity be)
    {
        be.animationTicks = Math.max(0, be.animationTicks - 1);
    }

    public void startAnimation(boolean playSound, Player player)
    {
        animationTicks = 10;

        if (playSound && getBlockPos().closerToCenterThan(player.position(), 16))
            level.playLocalSound(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), NMSounds.TREE_VACUUM_SUCK, SoundSource.BLOCKS, 0.5f, 1, true);
    }
}
