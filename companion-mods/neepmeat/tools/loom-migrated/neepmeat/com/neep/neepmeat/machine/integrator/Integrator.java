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

package com.neep.neepmeat.machine.integrator;

import com.neep.meatweapons.particle.MWGraphicsEffects;
import com.neep.neepmeat.api.DataResource;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface Integrator
{
    @Nullable
    static Integrator findIntegrator(Level world, BlockPos pos, int maxDist, long required)
    {
        Queue<BlockPos> queue = new LinkedList<>();
        List<BlockPos> visited = new ArrayList<>();
        queue.add(pos);
        while (!queue.isEmpty())
        {
            BlockPos current = queue.poll();
            for (Direction direction : Direction.values())
            {
                BlockPos offset = current.relative(direction);

                if (pos.distManhattan(offset) > maxDist || visited.contains(offset)) continue;

                BlockState offsetState = world.getBlockState(offset);

                // instanceof DataCable feel more correct, but integrators and neepbus blocks also implement this.
                if (offsetState.is(NMBlocks.DATA_CABLE) || offsetState.is(NMBlocks.ENCASED_DATA_CABLE))
                {
                    queue.add(offset);
                    visited.add(offset);
                }
                else if (world.getBlockEntity(offset) instanceof Integrator integrator && integrator.getData(DataResource.NORMAL) >= required)
                {
                    return integrator;
                }
            }
        }
        return null;
    }

    BlockPos getBlockPos();

    boolean canEnlighten();

//    static boolean canEnlighten(Set<Integrator> set)
//    {
//        for (var integrator : set)
//        {
//            if (integrator.canEnlighten())
//        }
//    }

    void setLookPos(BlockPos pos);

    void spawnBeam(Level world, BlockPos pos);

    static void spawnBeam(ServerLevel world, BlockPos startPos, BlockPos endPos)
    {
        Vec3 start = Vec3.upFromBottomCenterOf(startPos, 0.9);
        Vec3 end = Vec3.upFromBottomCenterOf(endPos, 0.8f);
        for (ServerPlayer player : PlayerLookup.around(world, start, 32d))
        {
            MWGraphicsEffects.syncBeamEffect(player, MWGraphicsEffects.BEAM, world, start, end, new Vec3(0, 0, 0), 0.5f, 50);
        }
    }

    long getData(DataResource variant);

    float extract(DataResource variant, long amount, TransactionContext transaction);
}
