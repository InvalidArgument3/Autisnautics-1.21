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

package com.neep.neepmeat.block;

import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepbus.util.NeepBusUtil;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.transport.api.pipe.AbstractPipeBlock;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import com.neep.neepmeat.transport.fluid_network.PipeConnectionType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

public class DataCableBlock extends AbstractPipeBlock implements DataCable
{
    public DataCableBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
    }

    @Override
    public boolean checkAdjacentConnection()
    {
        return false;
    }

    @Override
    public boolean canConnectTo(BlockState toState, Direction toFace, Level world, BlockPos toPos)
    {
        if (toState.getBlock() instanceof DataCable cable)
        {
            return cable.connectInDirection(world, toPos, toState, toFace);
        }
        return false;
    }

    @Override
    public boolean connectInDirection(BlockGetter world, BlockPos pos, BlockState state, Direction direction)
    {
        return state.getValue(DIR_TO_CONNECTION.get(direction)).canBeChanged();
    }

    @Override
    public boolean isConnected(Level world, BlockPos pos, BlockState state, Direction direction, boolean outgoing)
    {
        return state.getValue(DIR_TO_CONNECTION.get(direction)).isConnected();
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
//        boolean updated = false;

        super.onRemove(state, world, pos, newState, moved);

        if (!newState.is(state.getBlock()))
        {
            NeepBusUtil.updateImmediateNeighbours(world, pos, state);
        }

        if (newState.is(this))
        {
            NeepBusUtil.floodUpdate(world, pos);
        }
    }

    @Override
    protected boolean handCanChangeConnections(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);

        return hand != InteractionHand.OFF_HAND && stack.is(NMTags.WRENCH);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
//        if (player.isSneaking())
//        {
//            HashSet<BlockPos> visited = new HashSet<>();
//            Queue<BlockPos> queue = new ArrayDeque<>();
//
//            queue.add(pos);
//            visited.add(pos);
//
//            int cables = 0;
//
//            while (!queue.isEmpty())
//            {
//                BlockPos current = queue.poll();
//
//                BlockPos.Mutable mutable = current.mutableCopy();
//
//                for (Direction direction : Direction.values())
//                {
//                    mutable.set(current, direction);
//                    if (visited.contains(mutable))
//                        continue;
//
//                    visited.add(mutable.toImmutable());
//
//                    if (world.getBlockState(mutable).getBlock() instanceof DataCable dataCable)
//                    {
//                        queue.add(mutable.toImmutable());
//                        cables++;
//                    }
//                }
//            }
//
//            System.out.println("Cables: " + cables);
//            return ActionResult.SUCCESS;
//        }
        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    public void onConnectionUpdate(Level world, BlockState state, BlockState newState, BlockPos pos, Player entity)
    {
        NeepBusUtil.floodUpdate(world, pos);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos)
    {
        if (state.getValue(WATERLOGGED))
        {
            world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }

        PipeConnectionType type = state.getValue(DIR_TO_CONNECTION.get(direction));
        boolean forced = type == PipeConnectionType.FORCED;
        boolean otherConnected = false;

        boolean canConnect = canConnectTo(neighborState, direction.getOpposite(), (Level) world, neighborPos);

        // Check if neighbour is forced
        if (neighborState.getBlock() instanceof DataCableBlock)
        {
            forced = forced || neighborState.getValue(DIR_TO_CONNECTION.get(direction.getOpposite())) == PipeConnectionType.FORCED;
            otherConnected = neighborState.getValue(DIR_TO_CONNECTION.get(direction.getOpposite())) == PipeConnectionType.SIDE;

        }

        // AAAAAAAAAAAAAAAAAAAA
        PipeConnectionType finalConnection =
                otherConnected ? PipeConnectionType.SIDE :
                        forced ? PipeConnectionType.FORCED
                                : canConnect ? PipeConnectionType.SIDE : PipeConnectionType.NONE;

        return state.setValue(DIR_TO_CONNECTION.get(direction), finalConnection);
    }
}
