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
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.transport.api.pipe.AbstractPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.entity.HopperExtenderBlockEntity;
import com.neep.neepmeat.transport.fluid_network.PipeConnectionType;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class HopperExtenderBlock extends AbstractPipeBlock implements EntityBlock
{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public HopperExtenderBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);

        this.registerDefaultState(super.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new HopperExtenderBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        BlockState pipeState = super.getStateForPlacement(context);

//        return pipeState.with(FACING, context.getSide().getOpposite());
        Direction looking = context.getNearestLookingDirection();
        Direction side = context.getClickedFace();
        boolean storage = ItemStorage.SIDED.find(context.getLevel(), context.getClickedPos().relative(looking), looking.getOpposite()) != null;
        boolean sneaking = context.getPlayer() != null && context.getPlayer().isShiftKeyDown();
        return pipeState.setValue(FACING, sneaking ? (storage ? looking : looking.getOpposite()) : looking);
    }

    @Override
    public VoxelShape getShapeForState(BlockState state)
    {
        VoxelShape shape = getCentreShape();
        for (Direction direction : Direction.values())
        {
            for (Direction direction1 : Direction.values())
            {
                if (state.getValue(DIR_TO_CONNECTION.get(direction)) == PipeConnectionType.SIDE)
                {
                    shape = Shapes.or(shape, DIR_SHAPES.get(direction));
                }
                if (state.getValue(FACING) == direction1)
                {
                    shape = Shapes.or(shape, DIR_SHAPES.get(direction1));
                }
            }
        }
        return shape;
    }

    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A>
    checkType(BlockEntityType<A> givenType, BlockEntityType<E> expectedType, BlockEntityTicker<E> ticker, Level world)
    {
        return expectedType == givenType && !world.isClientSide ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return checkType(type, NMBlockEntities.HOPPER_EXTENDER, HopperExtenderBlockEntity::serverTick, world);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        InteractionHand hand = InteractionHand.MAIN_HAND;
//        ItemStack handStack = player.getStackInHand(hand);
//        ((BaseBlock) (NMBlocks.ITEM_DUCT)).getBlockItem();
        return InteractionResult.PASS;
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (state.getBlock() != newState.getBlock())
        {
            BlockEntity blockEntity = world.getBlockEntity(pos);

            // Scatter contents in world
            if (blockEntity instanceof HopperExtenderBlockEntity be)
            {
                Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), be.getResource().toStack((int) be.getAmount()));
                world.updateNeighbourForOutputSignal(pos,this);
            }
            super.onRemove(state, world, pos, newState, moved);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos)
    {
        boolean connection = canConnectTo(neighborState, direction.getOpposite(), (Level) world, neighborPos);

        // Already connected in direction
        if (connection && (state.getValue(DIR_TO_CONNECTION.get(direction)).isConnected()))
        {
            return state.setValue(DIR_TO_CONNECTION.get(direction), PipeConnectionType.SIDE);
        }

        return state.setValue(DIR_TO_CONNECTION.get(direction), connection ? PipeConnectionType.SIDE : PipeConnectionType.NONE);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
        super.neighborChanged(state, world, pos, block, fromPos, notify);
        if (world.getBlockEntity(pos) instanceof HopperExtenderBlockEntity be)
        {
            be.updateApiCache(pos, state);
        }
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
    {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        if (world.getBlockEntity(pos) instanceof HopperExtenderBlockEntity be)
        {
            be.updateApiCache(pos, state);
        }
    }

    @Override
    public boolean canConnectTo(BlockState toState, Direction toFace, Level world, BlockPos toPos)
    {
        if (toState.getBlock() instanceof HopperExtenderBlock)
        {
//            return ((FluidAcceptor) state.getBlock()).connectInDirection(state, direction);
            return toState.getValue(FACING) == toFace;
        }
        else if (toState.getBlock() instanceof HopperBlock)
        {
            return toState.getValue(HopperBlock.FACING) == toFace;
        }
        return false;
    }
}
