/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Derivative NeoForge 1.21.1 port for the Autisnautics pack. Upstream source headers
 * release the work under the GNU General Public License, version 3 or later. The
 * LICENSE file shipped with NeepMeat is the GNU Lesser General Public License,
 * version 3. This port does not relicense the work.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat.building;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/**
 * Full-cube reinforced glass. Adjacent faces of the same block are hidden, matching the
 * upstream anonymous {@code BaseBuildingBlock} in {@code NMBlocks}.
 */
public final class ReinforcedGlassBlock extends Block {
    private final Supplier<Block> stairs;

    public ReinforcedGlassBlock(Properties properties, Supplier<Block> stairs) {
        super(properties);
        this.stairs = stairs;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacent, Direction side) {
        if (adjacent.is(this)) {
            return true;
        }
        Block stairBlock = stairs.get();
        if (adjacent.is(stairBlock) && adjacent.getValue(StairBlock.FACING) == side.getOpposite()) {
            return true;
        }
        return super.skipRendering(state, adjacent, side);
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
}
