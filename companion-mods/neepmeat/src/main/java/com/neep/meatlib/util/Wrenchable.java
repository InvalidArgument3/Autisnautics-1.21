/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.util;

import com.neep.meatlib.init.MeatlibSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;

public interface Wrenchable {
    default boolean onWrench(Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand, BlockHitResult hit, ItemStack stack) {
        BlockState rotated = rotate(state, hit);
        setBlockState(level, pos, rotated);
        playWrenchSound(level, pos);
        return true;
    }

    default BlockState rotate(BlockState state, BlockHitResult hit) {
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return rotateHorizontal(state, BlockStateProperties.HORIZONTAL_FACING);
        }
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return rotateFacing(state, BlockStateProperties.FACING, hit.getDirection().getAxis());
        }
        return state;
    }

    static void setBlockState(Level level, BlockPos pos, BlockState newState) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            blockEntity.setBlockState(newState);
        }
        level.setBlock(pos, newState, Block.UPDATE_ALL);
    }

    static BlockState rotateFacing(BlockState state, DirectionProperty property, Direction.Axis axis) {
        Direction facing = state.getValue(property);
        return state.setValue(property, facing.getClockWise(axis));
    }

    static BlockState rotateHorizontal(BlockState state, DirectionProperty property) {
        return state.setValue(property, state.getValue(property).getClockWise());
    }

    default void playWrenchSound(Level level, BlockPos pos) {
        if (level instanceof ServerLevel server) {
            server.playSound(null, pos, MeatlibSounds.WRENCH_CLICK.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

}
