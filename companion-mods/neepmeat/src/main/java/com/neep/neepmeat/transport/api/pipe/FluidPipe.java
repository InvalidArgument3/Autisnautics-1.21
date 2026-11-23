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

import com.neep.meatlib.util.PipeConnectionsC;
import com.neep.meatlib.util.graph.ConnectablePipe;
import com.neep.neepmeat.transport.block.fluid_transport.FluidPipeBlock;
import com.neep.neepmeat.transport.block.fluid_transport.machine.FluidPipeBlockEntity;
import com.neep.neepmeat.transport.fluid_network.FluidNodeHelper;
import com.neep.neepmeat.transport.fluid_network.FluidPipeVertex;
import com.neep.neepmeat.transport.fluid_network.node.AcceptorModes;
import com.neep.neepmeat.transport.fluid_network.node.NodePos;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface FluidPipe extends ConnectablePipe
{
    static boolean isConnectedIn(BlockGetter world, BlockPos pos, BlockState state, Direction direction)
    {
        if (state.getBlock() instanceof FluidPipeBlock fluidPipeBlock)
        {
            return state.getValue(fluidPipeBlock.getConnection(direction)).isConnected();
        }
        else if (state.getBlock() instanceof FluidPipe acceptor)
        {
            return acceptor.connectInDirection(world, pos, state, direction);
        }
        return false;
    }

    @Override
    default boolean isConnected(BlockGetter world, BlockPos pos, BlockState state, Direction direction)
    {
        // Jank
        return isConnectedIn(world, pos, state, direction);
    }

    static FluidPipe findFluidPipe(Level world, BlockPos pos, BlockState state)
    {
        if (state.getBlock() instanceof FluidPipe pipe)
            return pipe;

        return null;
    }

//    default void updateNeighbourPipes(World world, BlockPos pos, BlockState oldState)
//    {
//        BlockPos.Mutable mutable = pos.mutableCopy();
//        for (Direction direction : Direction.values())
//        {
//            mutable.set(pos, direction);
//
//            BlockState adjState = world.getBlockState(mutable);
//
//            FluidPipe pipe;
//            if ((pipe = findFluidPipe(world, mutable, world.getBlockState(mutable))) != null)
//            {
//                pipe.onNeighbourPipeUpdate(world, mutable.toImmutable(), adjState, pos);
//            }
//        }
//    }
//
//    /**
//     * Pipe-aware replacement for onNeighborUpdate.
//     */
//    default void onNeighbourPipeUpdate(World world, BlockPos pos, BlockState state, BlockPos fromPos)
//    {
//        FluidPipeBlockEntity.find(world, pos).ifPresent(be -> be.updateHiddenConnections(state));
//    }

    // Call this first
    static void onStateReplaced(Level world, BlockPos pos, BlockState state, BlockState newState, FluidPipe fluidPipe)
    {
        if (!world.isClientSide() && !newState.is(state.getBlock()))
        {
            FluidPipeBlockEntity.find(world, pos).ifPresent(be -> be.prepareForRemoval(state, newState));
        }
    }

    default boolean createStorageNodes(Level world, BlockPos pos, BlockState state)
    {
        if (!world.isClientSide)
        {
            boolean changed = false;
            for (Direction direction : Direction.values())
            {
                if (isConnectedIn(world, pos, state, direction))
                {
                    if (FluidNodeHelper.updatePosition(world, new NodePos(pos, direction)))
                        changed = true;
                }
                else
                {
                    changed |= FluidNodeHelper.removeNode(world, new NodePos(pos, direction));
                }
            }
            return changed;
        }
        return false;
    }

    // TODO: Replace with EnumSet
//    default Iterable<Direction> getConnections(BlockState state, Predicate<Direction> forbidden)
    default PipeConnectionsC getConnections(BlockState state)
    {
        if (state.getBlock() instanceof PrecisePipeBlock pipeBlock)
        {
            com.neep.meatlib.util.PipeConnections set = new com.neep.meatlib.util.PipeConnections();
            for (Direction direction : Direction.values())
            {
                if (state.getValue(pipeBlock.getConnection(direction)).isConnected())
                    set.set(direction);
            }
            return set;
        }
        else if (state.getBlock() instanceof AbstractAxialFluidPipe)
        {
            Direction facing = state.getValue(AbstractAxialFluidPipe.FACING);
            return new com.neep.meatlib.util.PipeConnections(EnumSet.of(facing, facing.getOpposite()));
        }
        else
        {
            return com.neep.meatlib.util.PipeConnections.EMPTY;
        }
    }

    default void removePipe(ServerLevel world, BlockState state, BlockPos pos)
    {
        FluidNodeHelper.removeStorageNodes(world, pos);
//        updateNetwork(world, pos, state, PipeNetwork.UpdateReason.PIPE_REMOVED);

    }

    default boolean connectInDirection(BlockGetter world, BlockPos pos, BlockState state, Direction direction)
    {
        return true;
    }

    default AcceptorModes getDirectionMode(Level world, BlockPos pos, BlockState state, Direction direction)
    {
        return AcceptorModes.INSERT_EXTRACT;
    }

    default FluidPipeVertex getPipeVertex(ServerLevel world, BlockPos pos, BlockState state)
    {
        if (world.getBlockEntity(pos) instanceof FluidPipeBlockEntity<?> be)
        {
            // TODO: remove this cast
            // What cast?
            be.getPipeVertex().updateNodes(world, pos.immutable(), state);
            return be.getPipeVertex();
        }
        return null;
    }

    default PipeCol getCol(Level world, BlockPos pos, BlockState blockState)
    {
        return PipeCol.ANY;
    }

    enum PipeCol
    {
        ANY(DyeColor.WHITE),
        WHITE(DyeColor.WHITE),
        ORANGE(DyeColor.ORANGE),
        MAGENTA(DyeColor.MAGENTA),
        LIGHT_BLUE(DyeColor.LIGHT_BLUE),
        YELLOW(DyeColor.YELLOW),
        LIME(DyeColor.LIME),
        PINK(DyeColor.PINK),
        GRAY(DyeColor.GRAY),
        LIGHT_GRAY(DyeColor.LIGHT_GRAY),
        CYAN(DyeColor.CYAN),
        PURPLE(DyeColor.PURPLE),
        BLUE(DyeColor.BLUE),
        BROWN(DyeColor.BROWN),
        GREEN(DyeColor.GREEN),
        RED(DyeColor.RED),
        BLACK(DyeColor.BLACK) ;

        public final DyeColor dyeCol;

        PipeCol(DyeColor dyeCol)
        {
            this.dyeCol = dyeCol;
        }

        public static PipeCol get(DyeColor dyeCol)
        {
            return values()[dyeCol.ordinal() + 1];
        }

        public boolean matches(PipeCol other)
        {
            return this == ANY || other == ANY || this == other;
        }

        public int hexCode()
        {
            return dyeCol.getFireworkColor();
        }
    }
}
