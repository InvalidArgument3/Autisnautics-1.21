/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * The origin is index 0. Placement fills the other indexes with the structure block.
 * Removing the origin clears those blocks. Removing a structure block removes the origin.
 */

package com.neep.neepmeat.api.big_block_2;

import com.mojang.serialization.MapCodec;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.api.big_block.BlockPlacementPreview;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.Map;

public abstract class BigBlock2 extends BaseEntityBlock implements BlockPlacementPreview {
    public static final IntegerProperty INDEX = IntegerProperty.create("index", 0, 26);
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    public static final String BLOOD_API = "blood";

    private final DeferredBlock<BigBlockStructureBlock> structure;

    protected BigBlock2(DeferredBlock<BigBlockStructureBlock> structure) {
        super(MeatlibBlockSettings.create().strength(2.0F).properties());
        this.structure = structure;
        registerDefaultState(defaultBlockState().setValue(INDEX, 0).setValue(AXIS, Direction.Axis.Y));
    }

    public abstract BigBlockPattern volume(BlockState state);

    @Override
    public BigBlockPattern getVolume(BlockState state) {
        return volume(state);
    }

    public abstract Map<Integer, Vec3i> offsets(BlockState state);

    public Block structure() {
        return structure.get();
    }

    public Vec3i offsetOf(BlockState state) {
        return offsets(state).get(state.getValue(INDEX));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(INDEX, AXIS);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        if (level.isClientSide || state.getValue(INDEX) != 0) {
            return;
        }
        BlockState structureState = structure.get().defaultBlockState().setValue(AXIS, state.getValue(AXIS));
        for (Map.Entry<Integer, Vec3i> entry : offsets(state).entrySet()) {
            if (entry.getKey() == 0) {
                continue;
            }
            level.setBlock(pos.offset(entry.getValue()), structureState.setValue(INDEX, entry.getKey()), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        super.onRemove(state, level, pos, newState, moved);
        if (newState.is(this) || newState.is(structure.get())) {
            return;
        }
        for (Vec3i offset : volume(state).iterable()) {
            if (offset.equals(Vec3i.ZERO)) {
                continue;
            }
            BlockPos target = pos.offset(offset);
            if (level.getBlockState(target).is(structure.get())) {
                level.setBlock(target, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    @Override
    protected abstract MapCodec<? extends BaseEntityBlock> codec();
}
