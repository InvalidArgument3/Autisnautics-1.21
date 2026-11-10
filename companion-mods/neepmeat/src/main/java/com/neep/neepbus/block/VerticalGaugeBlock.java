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

import com.neep.meatlib.block.BaseHorFacingBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepbus.NeepBus;
import com.neep.neepbus.block.entity.VerticalGaugeBlockEntity;
import com.neep.neepbus.screen.GaugeScreenHandler;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.neep.meatlib.util.MiscUtil;
import org.jetbrains.annotations.Nullable;

public class VerticalGaugeBlock extends BaseHorFacingBlock implements EntityBlock, DataCable, NeepBusProvider, GaugeProvider
{
    public static final VoxelShape NORTH_SHAPE = Block.box(4, 1, 14, 12, 15, 16);
    public static final VoxelShape EAST_SHAPE = MiscUtil.rotateShapeY(NORTH_SHAPE, 90);
    public static final VoxelShape SOUTH_SHAPE = MiscUtil.rotateShapeY(NORTH_SHAPE, 180);
    public static final VoxelShape WEST_SHAPE = MiscUtil.rotateShapeY(NORTH_SHAPE, 270);

    public VerticalGaugeBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings.noOcclusion());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NeepBus.VERTICAL_GAUGE_BE.create(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        for (Direction direction : ctx.getNearestLookingDirections())
        {
            BlockState blockState = defaultBlockState();
            if (direction.getAxis() == Direction.Axis.Y)
            {
                blockState = blockState.setValue(FACING, ctx.getHorizontalDirection().getOpposite());
            }
            else
            {
                blockState = blockState.setValue(FACING, direction.getOpposite());
            }

            if (blockState.canSurvive(ctx.getLevel(), ctx.getClickedPos()))
            {
                return blockState;
            }
        }
        return defaultBlockState();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {

        return switch (state.getValue(FACING))
        {
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (world.getBlockEntity(pos) instanceof VerticalGaugeBlockEntity be)
        {
            if (player.isShiftKeyDown())
            {
                player.openMenu(GaugeScreenHandler.getFactory(be.getConfig(), be));
            }
        }
        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    public boolean connectInDirection(BlockGetter world, BlockPos pos, BlockState state, Direction direction)
    {
        return state.getValue(FACING).getOpposite() == direction;
    }

    @Override
    public void networkChanged(Level world, BlockPos pos, BlockPos whereChanged)
    {

    }

    @Override
    public boolean isConnected(Level world, BlockPos pos, BlockState state, Direction direction, boolean outgoing)
    {
        return outgoing && direction == state.getValue(FACING).getOpposite();
    }
}
