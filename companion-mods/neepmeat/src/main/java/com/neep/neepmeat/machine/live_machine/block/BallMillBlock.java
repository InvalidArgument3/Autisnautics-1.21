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

package com.neep.neepmeat.machine.live_machine.block;

import com.mojang.serialization.MapCodec;
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.api.big_block_2.BigBlock2;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.live_machine.block.entity.BallMillBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BallMillBlock extends BigBlock2 {
    private static final BigBlockPattern X_PATTERN = BigBlockPattern.makeRange(0, -1, -1, 0, 1, 1, Blocks.AIR.defaultBlockState());
    private static final BigBlockPattern Z_PATTERN = BigBlockPattern.makeRange(-1, -1, 0, 1, 1, 0, Blocks.AIR.defaultBlockState());
    private static final Map<Integer, Vec3i> X_OFFSETS = X_PATTERN.encode();
    private static final Map<Integer, Vec3i> Z_OFFSETS = Z_PATTERN.encode();

    public BallMillBlock() {
        super(LivingMachines.BALL_MILL_STRUCTURE);
        registerDefaultState(defaultBlockState().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    public BigBlockPattern volume(BlockState state) {
        return state.getValue(AXIS) == Direction.Axis.Z ? Z_PATTERN : X_PATTERN;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return volume(state).toVoxelShape();
    }

    @Override
    public Map<Integer, Vec3i> offsets(BlockState state) {
        return state.getValue(AXIS) == Direction.Axis.Z ? Z_OFFSETS : X_OFFSETS;
    }

    @Override
    protected MapCodec<? extends net.minecraft.world.level.block.BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(INDEX) == 0 ? new BallMillBlockEntity(pos, state) : null;
    }
}
