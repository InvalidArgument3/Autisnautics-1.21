/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Upright volume is the upstream odd cylinder: radius 1, from one below to one above.
 */

package com.neep.neepmeat.machine.live_machine.block;

import com.mojang.serialization.MapCodec;
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.api.big_block_2.BigBlock2;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.live_machine.block.entity.StomachBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class StomachBlock extends BigBlock2 {
    private static final BigBlockPattern Y_PATTERN = BigBlockPattern.makeOddCylinder(1, -1, 1, Blocks.AIR.defaultBlockState());
    private static final BigBlockPattern X_PATTERN = BigBlockPattern.makeRange(-1, -1, -1, 1, 1, 1, Blocks.AIR.defaultBlockState());
    private static final BigBlockPattern Z_PATTERN = BigBlockPattern.makeRange(-1, -1, -1, 1, 1, 1, Blocks.AIR.defaultBlockState());
    private static final Map<Integer, Vec3i> Y_OFFSETS = Y_PATTERN.encode();
    private static final Map<Integer, Vec3i> X_OFFSETS = X_PATTERN.encode();
    private static final Map<Integer, Vec3i> Z_OFFSETS = Z_PATTERN.encode();
    public static final MapCodec<StomachBlock> CODEC = simpleCodec(properties -> new StomachBlock());

    public StomachBlock() {
        super(LivingMachines.STOMACH_STRUCTURE);
    }

    @Override
    public BigBlockPattern volume(BlockState state) {
        return switch (state.getValue(AXIS)) {
            case X -> X_PATTERN;
            case Z -> Z_PATTERN;
            case Y -> Y_PATTERN;
        };
    }

    @Override
    public Map<Integer, Vec3i> offsets(BlockState state) {
        return switch (state.getValue(AXIS)) {
            case X -> X_OFFSETS;
            case Z -> Z_OFFSETS;
            case Y -> Y_OFFSETS;
        };
    }

    @Override
    protected MapCodec<? extends net.minecraft.world.level.block.BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(INDEX) == 0 ? new StomachBlockEntity(pos, state) : null;
    }

    @SuppressWarnings("unused")
    private static Direction.Axis axis(BlockState state) {
        return state.getValue(AXIS);
    }
}
