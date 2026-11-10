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

package com.neep.neepmeat.api.big_block;

import com.neep.neepmeat.api.block.PlacementPreview;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public interface BlockPlacementPreview extends PlacementPreview
{
    BigBlockPattern getVolume(BlockState state);

    @Nullable
    default BlockState getPlacementStatePreview(BlockPlaceContext itemPlacementContext)
    {
        if (this instanceof Block block)
        {
            return block.getStateForPlacement(itemPlacementContext);
        }

        return null;
    }

    default boolean canPlaceAtPreview(BlockState placementState, ClientLevel world, BlockPos pos)
    {
        if (this instanceof Block block)
        {
            return block.canSurvive(placementState, world, pos);
        }

        return false;
    }

    default VoxelShape getBreakParticleShape(Level world, BlockPos pos, BlockState state)
    {
        return state.getShape(world, pos);
    }

    default void addBlockBreakParticles(Level world, BlockPos pos, BlockState state, SpawnParticle spawn)
    {
        VoxelShape voxelShape = getBreakParticleShape(world, pos, state);
        voxelShape.forAllBoxes(
                (minX, minY, minZ, maxX, maxY, maxZ) ->
                {
                    double dx = maxX - minX;
                    double dy = maxY - minY;
                    double dz = maxZ - minZ;
                    int i = Math.max(2, Mth.ceil(dx / 0.5));
                    int j = Math.max(2, Mth.ceil(dy / 0.5));
                    int k = Math.max(2, Mth.ceil(dz / 0.5));

                    for (int l = 0; l < i; l++)
                    {
                        for (int m = 0; m < j; m++)
                        {
                            for (int n = 0; n < k; n++)
                            {
                                double g = ((double) l + 0.5) / (double) i;
                                double h = ((double) m + 0.5) / (double) j;
                                double o = ((double) n + 0.5) / (double) k;
                                double p = g * dx + minX;
                                double q = h * dy + minY;
                                double r = o * dz + minZ;
                                spawn.apply(pos.getX() + p, (double) pos.getY() + q, (double) pos.getZ() + r, g - 0.5, h - 0.5, o - 0.5);
                            }
                        }
                    }
                }
        );
        
    }

    default BlockHitResult modifyHitPos(BlockHitResult hitResult)
    {
        return hitResult;
    }

    @FunctionalInterface
    interface SpawnParticle
    {
        void apply(double x, double y, double z, double vx, double vy, double vz);
    }
}
