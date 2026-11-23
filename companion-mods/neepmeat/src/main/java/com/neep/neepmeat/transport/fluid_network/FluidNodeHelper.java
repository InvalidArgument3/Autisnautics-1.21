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

package com.neep.neepmeat.transport.fluid_network;

import com.neep.neepmeat.transport.fluid_network.node.FluidNode;
import com.neep.neepmeat.transport.fluid_network.node.NodePos;
import com.neep.meatlib.transfer.FluidStorage;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class FluidNodeHelper
{
    protected final ServerLevel world;

    public FluidNodeHelper(ServerLevel world)
    {
        this.world = world;
    }

    public static FluidNodeHelper getInstance(ServerLevel world)
    {
        return new FluidNodeHelper(world);
    }

    public static void removeStorageNodes(Level world, BlockPos pos)
    {
        var holder = FluidNodeHolder.find(world, pos);
        if (holder != null)
        {
            holder.clearNodes();
        }
        for (Direction direction : Direction.values())
        {
            NodePos nodePos = new NodePos(pos, direction);
            FluidNodeHelper.removeNode(world, nodePos);
        }
    }

    public static boolean removeNode(Level world, NodePos pos)
    {
        var holder = FluidNodeHolder.find(world, pos.pos());
        if (holder != null)
        {
            return holder.removeNode(pos.face());
        }

        return false;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static boolean updatePosition(Level world, NodePos pos)
    {
        // Get connected storage, remove node if there isn't one
        Storage<FluidVariant> storage = FluidStorage.SIDED.find(world, pos.facingBlock(), pos.face().getOpposite());
        if (storage == null)
        {
            var holder = FluidNodeHolder.find(world, pos.pos());
            if (holder != null && holder.getNode(pos.face()) != null)
            {
                removeNode(world, pos);
                return true;
            }
            else
            {
                return false;
            }
        }

        var holder = FluidNodeHolder.find(world, pos.pos());
        if (holder != null)
        {
            FluidNode prevNode = holder.getNode(pos.face());
            if (prevNode == null)
            {
                // Create new node with params
                FluidNode node;
                node = new FluidNode(pos, (ServerLevel) world);
                holder.putNode(pos.face(), node);
                return true;
            }
            else if (prevNode.getStorage((ServerLevel) world) != storage)
            {
                prevNode.findStorage((ServerLevel) world);
                // No nodes have been added or removed, so we can return false.
                return false;
            }
        }

        return false;
    }

    public static FluidNode get(Level world, NodePos nodePos)
    {
        var holder = FluidNodeHolder.find(world, nodePos.pos());
        return holder != null ? holder.getNode(nodePos.face()) : null;
    }

    public static void readNodes(FluidNodeHolder holder, CompoundTag nbt, ServerLevel world)
    {
        for (Direction direction : Direction.values())
        {
            Tag nodeNbt = nbt.get(direction.toString());
            if (nodeNbt == null)
            {
                continue;
            }
            holder.putNode(direction, FluidNode.fromNbt((CompoundTag) nodeNbt, world));
        }
    }

    public static CompoundTag writeNodes(FluidNodeHolder holder, CompoundTag nbt)
    {
        for (Direction direction : Direction.values())
        {
            FluidNode node = holder.getNode(direction);
            if (node != null)
            {
                CompoundTag nodeNbt = new CompoundTag();
                nodeNbt = node.writeNbt(nodeNbt);
                nbt.put(node.getNodePos().face().toString(), nodeNbt);
            }
        }
        return nbt;
    }
}
