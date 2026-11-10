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

package com.neep.neepmeat.transport.block.item_transport;

import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.util.PipeConnectionsC;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.meatlib.util.PipeConnections;
import com.neep.neepmeat.transport.block.item_transport.entity.MergePipeBlockEntity;
import com.neep.neepmeat.transport.fluid_network.ConnectionType;
import com.neep.neepmeat.transport.item_network.ItemInPipe;
import com.neep.meatlib.util.MiscUtil;
import com.neep.meatlib.util.Wrenchable;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.ResourceAmount;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class MergePipeBlock extends ItemPipeBlock implements Wrenchable
{
    public static final DirectionProperty FACING = DirectionProperty.create("facing");

    public MergePipeBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
        this.registerDefaultState(super.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShapeForState(BlockState state)
    {
        VoxelShape shape = getCentreShape();
        Direction facing = state.getValue(FACING);
        for (Direction direction : Direction.values())
        {
            if (state.getValue(DIR_TO_CONNECTION.get(direction)) == ConnectionType.SIDE)
            {
                shape = Shapes.or(shape, DIR_SHAPES.get(direction));
            }
        }
        shape = Shapes.or(shape, DIR_SHAPES.get(facing));
//        shape = VoxelShapes.union(shape, DIR_SHAPES.get(facing.getOpposite()));
        return shape;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
//        BlockState state = this.getConnectedState(ctx.getWorld(), this.getDefaultState(), ctx.getBlockPos());
        BlockState state = super.getStateForPlacement(ctx);
        return state.setValue(FACING, ctx.getPlayer().isShiftKeyDown() ? ctx.getNearestLookingDirection().getOpposite() : ctx.getNearestLookingDirection());
    }

    @Override
    public Direction getOutputDirection(ItemInPipe item, BlockPos pos, BlockState state, Level world, Direction in, @Nullable BlockEntity be, boolean overridePathNode, TransactionContext transaction)
    {
        if (item.hasRoute())
            return super.getOutputDirection(item, pos, state, world, in, be, overridePathNode, transaction);

        return state.getValue(MergePipeBlock.FACING);
    }

    public boolean alwaysAutoConnect(BlockState state, Direction face)
    {
        return state.getValue(FACING) == face;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    protected boolean isNewConnectionValid(Level world, BlockPos pos, BlockState state, Direction face, ConnectionType connectionType)
    {
        boolean b = super.isNewConnectionValid(world, pos, state, face, connectionType);
        if (connectionType == ConnectionType.NONE)
            return b && face != state.getValue(FACING);

        return b;
    }

    public PipeConnections getConnections(BlockState state, @Nullable Direction forbidden)
    {
        PipeConnections set = new PipeConnections();
        for (Direction direction : Direction.values())
        {
            if (state.getValue(DIR_TO_CONNECTION.get(direction)).isConnected()
                    && forbidden != direction)
            {
                set.set(direction);
            }
            set.set(state.getValue(FACING));
        }
        return set;
    }

    public boolean singleOutput()
    {
        return true;
    }

    public boolean canItemEnter(ItemVariant variant, long amount, Level world, BlockPos pos, BlockState state, Direction inFace, boolean routed)
    {
        return (inFace != state.getValue(FACING) || routed)
                && super.canItemEnter(variant, amount, world, pos, state, inFace, routed);
    }

    public boolean canItemLeave(ResourceAmount<ItemVariant> item, Level world, BlockPos pos, BlockState state, Direction outFace, boolean routed)
    {
        return (outFace == state.getValue(FACING) || routed)
                && super.canItemLeave(item, world, pos, state, outFace, routed);
    }

    @Override
    public boolean isConnected(BlockGetter blockView, BlockPos pos, BlockState state, Direction face)
    {
        return face == state.getValue(FACING) || super.isConnected(blockView, pos, state, face);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new MergePipeBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NMBlockEntities.MERGE_ITEM_PIPE, (world1, pos, state1, blockEntity) -> blockEntity.serverTick(world1, pos, state1), null, world);
    }
}
