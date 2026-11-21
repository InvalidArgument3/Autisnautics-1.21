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
 *
 * The search walks any data cable, including a distributor or advanced-integrator structure.
 * An integrator is tested before it is treated as another cable.
 */

package com.neep.neepmeat.machine.integrator;

import com.neep.meatlib.transfer.TransactionContext;
import com.neep.neepmeat.api.DataResource;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public interface Integrator {
    @Nullable
    static Integrator findIntegrator(Level world, BlockPos pos, int maxDist, long required) {
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(pos);
        visited.add(pos);
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos offset = current.relative(direction);
                if (pos.distManhattan(offset) > maxDist || !visited.add(offset)) {
                    continue;
                }
                BlockState state = world.getBlockState(offset);
                if (world.getBlockEntity(offset) instanceof Integrator integrator
                        && integrator.getData(DataResource.NORMAL) >= required) {
                    return integrator;
                }
                if (state.getBlock() instanceof DataCable cable) {
                    boolean fromCable = world.getBlockState(current).getBlock() instanceof DataCable;
                    if (!fromCable || cable.isConnected(world, offset, state, direction.getOpposite(), false)) {
                        queue.add(offset);
                    }
                }
            }
        }
        return null;
    }

    BlockPos getBlockPos();

    boolean canEnlighten();

    void setLookPos(BlockPos pos);

    void spawnBeam(Level world, BlockPos pos);

    long getData(DataResource variant);

    float extract(DataResource variant, long amount, TransactionContext transaction);
}
