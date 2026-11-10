/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Upright volume is the upstream disc of radius 1. The four cardinal faces expose blood.
 */

package com.neep.neepmeat.machine.live_machine;

import com.mojang.serialization.MapCodec;
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.api.big_block_2.BigBlock2;
import com.neep.neepmeat.machine.live_machine.block.entity.MetaboliserSegmentBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class MetaboliserSegmentBlock extends BigBlock2 {
    private static final BigBlockPattern Y_PATTERN = BigBlockPattern.makeOddCylinder(1, 0, 0, Blocks.AIR.defaultBlockState())
            .enableApi(-1, 0, 0, BLOOD_API)
            .enableApi(1, 0, 0, BLOOD_API)
            .enableApi(0, 0, -1, BLOOD_API)
            .enableApi(0, 0, 1, BLOOD_API);
    private static final BigBlockPattern X_PATTERN = BigBlockPattern.makeRange(0, -1, -1, 0, 1, 1, Blocks.AIR.defaultBlockState())
            .enableApi(0, -1, 0, BLOOD_API)
            .enableApi(0, 1, 0, BLOOD_API)
            .enableApi(0, 0, -1, BLOOD_API)
            .enableApi(0, 0, 1, BLOOD_API);
    private static final BigBlockPattern Z_PATTERN = BigBlockPattern.makeRange(-1, -1, 0, 1, 1, 0, Blocks.AIR.defaultBlockState())
            .enableApi(-1, 0, 0, BLOOD_API)
            .enableApi(1, 0, 0, BLOOD_API)
            .enableApi(0, -1, 0, BLOOD_API)
            .enableApi(0, 1, 0, BLOOD_API);
    private static final Map<Integer, Vec3i> Y_OFFSETS = Y_PATTERN.encode();
    private static final Map<Integer, Vec3i> X_OFFSETS = X_PATTERN.encode();
    private static final Map<Integer, Vec3i> Z_OFFSETS = Z_PATTERN.encode();
    public static final MapCodec<MetaboliserSegmentBlock> CODEC = simpleCodec(properties -> new MetaboliserSegmentBlock());

    public MetaboliserSegmentBlock() {
        super(LiveMachines.METABOLISER_STRUCTURE);
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(INDEX) == 0 ? new MetaboliserSegmentBlockEntity(pos, state) : null;
    }
}
