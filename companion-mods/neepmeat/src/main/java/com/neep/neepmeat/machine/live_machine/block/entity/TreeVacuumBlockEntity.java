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

import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Faces the trunk. {@code progress} fills until {@link TreeVacuumProcess} breaks the tree. */
public class TreeVacuumBlockEntity extends BlockEntity implements LivingMachineComponent {
    public static final int MAX_PROGRESS = 5;
    public float progress;
    public boolean extended;
    public int animationTicks;

    public void syncAnimation(boolean playSound) {
        extended = playSound;
        animationTicks = playSound ? 10 : animationTicks;
        if (level instanceof net.minecraft.server.level.ServerLevel server) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingChunk(server, new net.minecraft.world.level.ChunkPos(worldPosition),
                    new com.neep.neepmeat.network.TreeVacuumAnimationPacket(worldPosition, playSound));
        }
    }

    public void startAnimation(boolean playSound) {
        animationTicks = 10;
        extended = playSound;
    }

    public void clientTick() {
        animationTicks = Math.max(0, animationTicks - 1);
    }

    public TreeVacuumBlockEntity(BlockPos pos, BlockState state) {
        super(LivingMachines.TREE_VACUUM_BE.get(), pos, state);
    }

    @Override
    public boolean componentRemoved() {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType() {
        return LivingMachineComponents.TREE_VACUUM;
    }
}
