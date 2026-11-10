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

package com.neep.neepmeat.transport.api.pipe;

import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.transport.block.fluid_transport.FluidPipeBlock;
import com.neep.neepmeat.transport.fluid_network.node.AcceptorModes;
import com.neep.neepmeat.transport.fluid_network.node.BlockFluidPipeVertex;
import com.neep.neepmeat.transport.block.fluid_transport.machine.FluidPipeBlockEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractAxialFluidPipe extends BaseFacingBlock implements FluidPipe, EntityBlock
{
    public static final VoxelShape X_SHAPE = Block.box(0, 4, 4, 16, 12, 12);
    public static final VoxelShape Y_SHAPE = Block.box(4, 0, 4, 12, 16, 12);
    public static final VoxelShape Z_SHAPE = Block.box(4, 4, 0, 12, 12, 16);

    public AbstractAxialFluidPipe(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
    }

    @Override
    protected VoxelShape getShape(BlockState state)
    {
        Direction facing = state.getValue(FACING);
        return switch (facing.getAxis())
                {
                    case X -> X_SHAPE;
                    case Y -> Y_SHAPE;
                    case Z -> Z_SHAPE;
                };
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return getShape(state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext context)
    {
        return getShape(state);
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
        if (world.isClientSide())
            return;

        if (!(world.getBlockState(fromPos).getBlock() instanceof FluidPipeBlock))
        {
            createStorageNodes(world, pos, state);
        }

        FluidPipeBlockEntity.find(world, pos).ifPresent(be -> be.updateHiddenConnections(state));
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
    {
        if (world.isClientSide())
            return;

        createStorageNodes(world, pos, state);
        FluidPipeBlockEntity.find(world, pos).ifPresent(be -> be.updateHiddenConnections(state));
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        FluidPipe.onStateReplaced(world, pos, state, newState, this);
        super.onRemove(state, world, pos, newState, moved);

        if (world.isClientSide())
            return;

        if (!state.is(newState.getBlock()))
        {
            removePipe((ServerLevel) world, state, pos);
        }
    }

    @Override
    public boolean connectInDirection(BlockGetter world, BlockPos pos, BlockState state, Direction direction)
    {
        if (state.getBlock() instanceof DirectionalBlock)
        {
            Direction facing = state.getValue(DirectionalBlock.FACING);
            return direction == facing || direction == facing.getOpposite();
        }
        return true;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (player.getItemInHand(hand).is(Items.STICK))
        {
            if (!world.isClientSide() && world.getBlockEntity(pos) instanceof FluidPipeBlockEntity<?> be)
            {
                if (be.getPipeVertex() instanceof BlockFluidPipeVertex vertex && !vertex.canSimplify())
                {
//                    System.out.println(vertex.getAmount());
//                    System.out.println(vertex.getVariant());
//                    System.out.println(vertex.getPumpHeight());
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.useWithoutItem(state, world, pos, player, hit);
    }

    public AcceptorModes getDirectionMode(Level world, BlockPos pos, BlockState state, Direction direction)
    {
        return AcceptorModes.INSERT_EXTRACT;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        if (!world.isClientSide())
        {
            // TODO: jank
            return ((world1, pos, state1, blockEntity) -> ((FluidPipeBlockEntity<?>) blockEntity).serverTick());
        }
        return null;
    }
}
