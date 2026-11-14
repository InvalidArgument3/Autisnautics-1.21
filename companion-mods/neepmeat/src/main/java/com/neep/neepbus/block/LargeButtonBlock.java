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

package com.neep.neepbus.block;

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepbus.NeepBus;
import com.neep.neepbus.block.entity.LargeButtonBlockEntity;
import com.neep.neepbus.screen.ButtonConfigScreenHandler;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import com.neep.meatlib.util.MiscUtil;
import net.minecraft.world.level.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class LargeButtonBlock extends FaceAttachedHorizontalDirectionalBlock implements MeatlibBlock, DataCable, NeepBusProvider, EntityBlock
{
    public static final VoxelShape FLOOR_SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 3.0, 12.0);
    public static final VoxelShape CEILING_SHAPE = Block.box(4.0, 13.0, 4.0, 12.0, 16.0, 12.0);
    public static final VoxelShape NORTH_WALL_SHAPE = Block.box(4.0, 4.0, 13.0, 12.0, 12.0, 16.0);
    public static final VoxelShape EAST_WALL_SHAPE = MiscUtil.rotateShapeY(NORTH_WALL_SHAPE, 90);
    public static final VoxelShape SOUTH_WALL_SHAPE = MiscUtil.rotateShapeY(NORTH_WALL_SHAPE, 180);
    public static final VoxelShape WEST_WALL_SHAPE = MiscUtil.rotateShapeY(NORTH_WALL_SHAPE, 270);

    public LargeButtonBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(settings);
        itemSettings.create(this, ctx);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
    {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, FACE);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (world.getBlockEntity(pos) instanceof LargeButtonBlockEntity be)
        {
            if (!player.isShiftKeyDown())
            {
                world.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 1, 1);
                if (!world.isClientSide())
                    be.trigger();
            }
            else
            {
                player.openMenu(ButtonConfigScreenHandler.getFactory(be.getConfig(), be));
            }
            return InteractionResult.SUCCESS;
        }

        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random)
    {
        super.tick(state, world, pos, random);
        if (world.getBlockEntity(pos) instanceof LargeButtonBlockEntity be)
        {
            be.onScheduledTick();
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return switch (state.getValue(FACE))
        {
            case FLOOR -> FLOOR_SHAPE;
            case CEILING -> CEILING_SHAPE;
            case WALL -> switch (state.getValue(FACING))
            {
                case EAST -> EAST_WALL_SHAPE;
                case SOUTH -> SOUTH_WALL_SHAPE;
                case WEST -> WEST_WALL_SHAPE;
                default -> NORTH_WALL_SHAPE;
            };
        };
    }

    @Override
    public void networkChanged(Level world, BlockPos pos, BlockPos whereChanged)
    {
        if (world.getBlockEntity(pos) instanceof LargeButtonBlockEntity be)
        {
            be.invalidate();
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NeepBus.LARGE_BUTTON_BE.create(pos, state);
    }

}
