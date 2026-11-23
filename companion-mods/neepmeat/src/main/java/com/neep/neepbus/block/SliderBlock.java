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
import com.neep.neepbus.block.entity.SliderBlockEntity;
import com.neep.neepbus.screen.NeepBusConfigScreenHandler;
import com.neep.neepbus.screen.SliderScreenHandler;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import com.neep.meatlib.util.MiscUtil;
import net.minecraft.world.level.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SliderBlock extends FaceAttachedHorizontalDirectionalBlock implements EntityBlock, NeepBusProvider, DataCable, MeatlibBlock
{
    public static final VoxelShape NORTH_WALL_SHAPE = Block.box(4.0, 1.0, 10.0, 12.0, 15.0, 16.0);
    public static final VoxelShape SOUTH_WALL_SHAPE = Block.box(4.0, 1.0, 0.0, 12.0, 15.0, 6.0);
    public static final VoxelShape WEST_WALL_SHAPE = Block.box(10.0, 1.0, 4.0, 16.0, 15.0, 12.0);
    public static final VoxelShape EAST_WALL_SHAPE = Block.box(0.0, 1.0, 4.0, 6.0, 15.0, 12.0);
    public static final VoxelShape FLOOR_Z_AXIS_SHAPE = Block.box(4.0, 0.0, 1.0, 12.0, 6.0, 15.0);
    public static final VoxelShape FLOOR_X_AXIS_SHAPE = Block.box(1.0, 0.0, 4.0, 15.0, 6.0, 12.0);
    public static final VoxelShape CEILING_Z_AXIS_SHAPE = Block.box(4.0, 10.0, 1.0, 12.0, 16.0, 15.0);
    public static final VoxelShape CEILING_X_AXIS_SHAPE = Block.box(1.0, 10.0, 4.0, 15.0, 16.0, 12.0);

    public SliderBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(settings);
        itemSettings.create(this, ctx);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
    {
        return true;
    }

    public static Direction connectionDirection(Direction facing, AttachFace location)
    {
        return location == AttachFace.WALL ? facing : location == AttachFace.CEILING ? Direction.DOWN : Direction.UP;
    }

    @Override
    public boolean connectInDirection(BlockGetter world, BlockPos pos, BlockState state, Direction direction)
    {
        return connectionDirection(state.getValue(FACING), state.getValue(FACE)).getOpposite() == direction;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (world.getBlockEntity(pos) instanceof SliderBlockEntity be)
        {
            if (!player.isShiftKeyDown())
            {
                MiscUtil.openHandledScreen(player, ((syncId, playerInventory, player1) -> new SliderScreenHandler(syncId, playerInventory, be)));
//                player.openHandledScreen((SimpleScreenHandlerFactory) (syncId, playerInventory, player1) ->
//                        new SliderScreenHandler(syncId, playerInventory, be));
            }
            else
            {
                player.openMenu(NeepBusConfigScreenHandler.getFactory(be.getConfig()));
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NeepBus.LINEAR_LEVER_BE.create(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        switch (state.getValue(FACE))
        {
            case FLOOR ->
            {
                return switch ((state.getValue(FACING)).getAxis())
                {
                    case X -> FLOOR_X_AXIS_SHAPE;
                    default -> FLOOR_Z_AXIS_SHAPE;
                };
            }
            case WALL ->
            {
                return switch (state.getValue(FACING))
                {
                    case EAST -> EAST_WALL_SHAPE;
                    case WEST -> WEST_WALL_SHAPE;
                    case SOUTH -> SOUTH_WALL_SHAPE;
                    default -> NORTH_WALL_SHAPE;
                };
            }
            default ->
            {
                return switch (state.getValue(FACING).getAxis())
                {
                    case X -> CEILING_X_AXIS_SHAPE;
                    default -> CEILING_Z_AXIS_SHAPE;
                };
            }
        }
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACE, FACING);
    }

    @Override
    public void networkChanged(Level world, BlockPos pos, BlockPos whereChanged)
    {
        if (world.getBlockEntity(pos) instanceof SliderBlockEntity be)
        {
            be.updateNetwork();
        }
    }

    @Override
    public boolean isConnected(Level world, BlockPos pos, BlockState state, Direction direction, boolean outgoing)
    {
        AttachFace face = state.getValue(FACE);
        return outgoing && ((direction == Direction.UP && face == AttachFace.CEILING)
                || (direction == Direction.DOWN && face == AttachFace.FLOOR)
                || (face == AttachFace.WALL && direction.getOpposite() == state.getValue(FACING)));
    }
}
