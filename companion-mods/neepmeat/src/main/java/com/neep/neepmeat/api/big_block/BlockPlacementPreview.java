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

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public interface BlockPlacementPreview {
    BigBlockPattern getVolume(BlockState state);

    @Nullable
    default BlockState getPlacementStatePreview(BlockPlaceContext context) {
        if (this instanceof Block block) {
            return block.getStateForPlacement(context);
        }
        return null;
    }

    default boolean canPlaceAtPreview(BlockState placementState, ClientLevel world, BlockPos pos) {
        return world.getBlockState(pos).canBeReplaced();
    }

    default VoxelShape getBreakParticleShape(Level world, BlockPos pos, BlockState state) {
        return state.getShape(world, pos);
    }

    default void addBlockBreakParticles(Level world, BlockPos pos, BlockState state) {
        VoxelShape shape = getBreakParticleShape(world, pos, state);
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double dx = maxX - minX;
            double dy = maxY - minY;
            double dz = maxZ - minZ;
            int xSteps = Math.max(2, Mth.ceil(dx / 0.5));
            int ySteps = Math.max(2, Mth.ceil(dy / 0.5));
            int zSteps = Math.max(2, Mth.ceil(dz / 0.5));
            for (int x = 0; x < xSteps; x++) {
                for (int y = 0; y < ySteps; y++) {
                    for (int z = 0; z < zSteps; z++) {
                        double gx = (x + 0.5) / xSteps;
                        double gy = (y + 0.5) / ySteps;
                        double gz = (z + 0.5) / zSteps;
                        world.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state),
                                pos.getX() + gx * dx + minX,
                                pos.getY() + gy * dy + minY,
                                pos.getZ() + gz * dz + minZ,
                                gx - 0.5, gy - 0.5, gz - 0.5);
                    }
                }
            }
        });
    }

    default BlockHitResult modifyHitPos(BlockHitResult hitResult) {
        return hitResult;
    }
}
