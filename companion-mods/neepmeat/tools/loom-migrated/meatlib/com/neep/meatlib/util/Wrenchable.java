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

package com.neep.meatlib.util;

import com.neep.meatlib.init.MeatlibSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
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

public interface Wrenchable
{
    default boolean onWrench(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, BlockHitResult hitResult, ItemStack stack)
    {
        BlockState newState = rotate(state, hitResult);

        setBlockState(world, pos, newState);

        playWrenchSound(world, pos);
        return true;
    }

    default BlockState rotate(BlockState state, BlockHitResult hitResult)
    {
        var properties = state.getProperties();

        if (properties.contains(BlockStateProperties.HORIZONTAL_FACING))
        {
            return Wrenchable.rotateHorizontal(state, BlockStateProperties.HORIZONTAL_FACING);
        }
        else if (properties.contains(BlockStateProperties.FACING))
        {
            return Wrenchable.rotateFacing(state, BlockStateProperties.FACING, hitResult.getDirection().getAxis());
        }

        return state;
    }

    static void setBlockState(Level world, BlockPos pos, BlockState newState)
    {
        BlockEntity be = world.getBlockEntity(pos);

        if (be != null)
            be.setBlockState(newState);

        world.setBlock(pos, newState, Block.UPDATE_ALL);
    }

    static BlockState rotateFacing(BlockState state, DirectionProperty property, Direction.Axis axis)
    {
        Direction facing = state.getValue(property);

        Direction rotated = facing.getClockWise(axis);

        return state.setValue(property, rotated);
    }

    static Direction.Axis rotateAxis(Direction.Axis axis, Direction.Axis rotationAxis)
    {
        Direction direction = switch (axis)
        {
            case X -> Direction.EAST;
            case Y -> Direction.UP;
            case Z -> Direction.NORTH;
        };

        direction = direction.getClockWise(rotationAxis);

        return direction.getAxis();
    }

    static BlockState rotateHorizontal(BlockState state, DirectionProperty property)
    {
        return state.setValue(property, state.getValue(property).getClockWise());
    }

    default void playWrenchSound(Level world, BlockPos pos)
    {
        playStandardWrenchSound(world, pos);
    }

    static void playStandardWrenchSound(Level world, BlockPos pos)
    {
        if (world instanceof ServerLevel serverWorld)
        {
            serverWorld.playSound(null, pos, MeatlibSounds.WRENCH_CLICK, SoundSource.BLOCKS, 1, 1);
        }
    }
}
