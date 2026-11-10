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

package com.neep.neepmeat.transport.block.fluid_transport;

import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.item.FluidComponentItem;
import com.neep.neepmeat.transport.api.pipe.AbstractPipeBlock;
import com.neep.neepmeat.transport.api.pipe.FluidPipe;
import com.neep.neepmeat.transport.fluid_network.FluidNodeHelper;
import com.neep.neepmeat.transport.fluid_network.PipeConnectionType;
import com.neep.neepmeat.transport.fluid_network.node.NodePos;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class CapillaryFluidPipeBlock extends AbstractPipeBlock implements EntityBlock, FluidPipe, CapillaryPipe
{
    public CapillaryFluidPipeBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings.factory(FluidComponentItem::new), settings);
    }

    public static void removeStorageNodes(Level world, BlockPos pos)
    {
        for (Direction direction : Direction.values())
        {
            NodePos nodePos = new NodePos(pos, direction);
            FluidNodeHelper.removeNode(world, nodePos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!state.is(newState.getBlock()))
        {
            removeStorageNodes(world, pos);
            world.removeBlockEntity(pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
        BlockState state2 = enforceApiConnections(world, pos, state);
        world.setBlock(pos, state2, Block.UPDATE_ALL);

        if (!(world.getBlockState(fromPos).getBlock() instanceof CapillaryFluidPipeBlock))
        {
            createStorageNodes(world, pos, state2);
        }

    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
    {
        BlockState updatedState = enforceApiConnections(world, pos, state);
        world.setBlock(pos, updatedState,  Block.UPDATE_ALL);
        createStorageNodes(world, pos, updatedState);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos)
    {
        boolean connection = canConnectTo(neighborState, direction.getOpposite(), (Level) world, neighborPos);
        if (!world.isClientSide())
        {
            connection = connection || canConnectApi((Level) world, pos, state, direction);
        }

        // Check if neighbour is forced
        boolean neighbourForced = false;
        if (neighborState.getBlock() instanceof CapillaryFluidPipeBlock)
        {
            neighbourForced = neighborState.getValue(DIR_TO_CONNECTION.get(direction.getOpposite())) == PipeConnectionType.FORCED;
        }

        PipeConnectionType connection1 = neighbourForced
                ? PipeConnectionType.FORCED
                : connection ? PipeConnectionType.SIDE : PipeConnectionType.NONE;

        // I don't know what this bit was for.

        return state.setValue(DIR_TO_CONNECTION.get(direction), connection1);
    }

    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    public void onConnectionUpdate(Level world, BlockState state, BlockState newState, BlockPos pos, Player entity)
    {
        createStorageNodes(world, pos, newState);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return null;
    }

    // Only takes into account other pipes, connections to storages are enforced later.
    @Override
    public boolean canConnectTo(BlockState toState, Direction toFace, Level world, BlockPos toPos)
    {
        if (toState.getBlock() instanceof FluidPipe)
        {
            return ((FluidPipe) toState.getBlock()).connectInDirection(world, toPos, toState, toFace);
        }
        return false;
    }

    // Creates blockstate connections to fluid containers after placing
    private BlockState enforceApiConnections(Level world, BlockPos pos, BlockState state)
    {
        if (!world.isClientSide)
        {
            BlockState state2 = state;
            for (Direction direction : Direction.values())
            {
                if (canConnectApi(world, pos, state, direction))
                {
                    state2 = state2.setValue(DIR_TO_CONNECTION.get(direction), PipeConnectionType.SIDE);
                }
            }
            return state2;
        }
        return state;
    }

//    public void createStorageNodes(World world, BlockPos pos, BlockState state)
//    {
//        if (!world.isClient)
//        {
//            for (Direction direction : Direction.values())
//            {
//                if (state.get(DIR_TO_CONNECTION.get(direction)) == PipeConnectionType.SIDE)
//                {
//                    FluidNetwork.getInstance(world).updatePosition(world, new NodePos(pos, direction));
//                }
//                else
//                {
//                    FluidNetwork.getInstance(world).removeNode(world, new NodePos(pos, direction));
//                }
//            }
//            // TODO: avoid creating instances that will fail immediately
//            Optional<PipeNetwork> net = PipeNetwork.tryCreateNetwork((ServerWorld) world, pos, Direction.NORTH);
//        }
//    }

    private boolean canConnectApi(Level world, BlockPos pos, BlockState state, Direction direction)
    {
        Storage<FluidVariant> storage = FluidStorage.SIDED.find(world, pos.relative(direction), direction.getOpposite());
        return storage != null;
    }
}
