/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Without a route, items leave through FACING. They cannot enter from FACING
 * unless they already have a route. The pipe is a vertex.
 */

package com.neep.neepmeat.machine.transport;

import com.mojang.serialization.MapCodec;
import com.neep.meatlib.block.MeatlibBlockSettings;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class MergePipeBlock extends ItemPipeBlock {
    public static final DirectionProperty FACING = DirectionProperty.create("facing");
    public static final MapCodec<MergePipeBlock> CODEC = simpleCodec(properties -> new MergePipeBlock());

    public MergePipeBlock() {
        super();
    }

    @Override
    protected MapCodec<? extends net.minecraft.world.level.block.BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockState completeDefault(BlockState state) {
        return state.setValue(FACING, Direction.NORTH);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState placed = super.getStateForPlacement(context);
        Direction facing = context.getPlayer() != null && context.getPlayer().isShiftKeyDown()
                ? context.getNearestLookingDirection().getOpposite()
                : context.getNearestLookingDirection();
        return placed.setValue(FACING, facing);
    }
}
