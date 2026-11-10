/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Items cross a pipe at the upstream speed of 0.1. A non-empty route is consumed
 * from the front (ItemInPipe.getPreferredOutputDirection). Otherwise output is
 * round-robin. Straight runs are hidden vertices; junctions link across them.
 */

package com.neep.neepmeat.machine.transport;

import com.neep.meatlib.util.PipeConnections;
import com.neep.meatlib.util.PipeConnectionsC;
import com.neep.meatlib.util.graph.ConnectablePipe;
import com.neep.meatlib.util.graph.PipeVertex;
import com.neep.neepmeat.machine.NeepMachines;
import com.neep.neepmeat.transport.ConnectionType;
import com.neep.neepmeat.transport.ItemRoute;
import com.neep.neepmeat.transport.PipeGraph;
import com.neep.neepmeat.transport.PipeProperties;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public class ItemPipeBlockEntity extends BlockEntity {
    public static final float SPEED = 0.1F;

    private final List<TravellingItem> items = new ArrayList<>();
    private final PipeNode vertex = new PipeNode();
    private final List<FilterEntry> filters = new ArrayList<>();
    private int lastOutput;

    public ItemPipeBlockEntity(BlockPos pos, BlockState state) {
        super(NeepMachines.ITEM_PIPE_BE.get(), pos, state);
    }

    public PipeNode vertex() {
        return vertex;
    }

    public void allow(ItemStack stack) {
        setEntry(filters.size(), FilterEntry.item(stack.getItem(), false));
    }

    public void allowTag(TagKey<Item> tag, boolean inverted) {
        setEntry(filters.size(), FilterEntry.tag(tag, inverted));
    }

    public void setEntry(int index, FilterEntry entry) {
        if (index < 0 || index >= 3 || entry == null) {
            return;
        }
        while (filters.size() <= index) {
            filters.add(null);
        }
        filters.set(index, entry);
        filters.removeIf(candidate -> candidate == null);
        setChanged();
    }

    public List<FilterEntry> entries() {
        return List.copyOf(filters);
    }

    public boolean prioritise() {
        return getBlockState().getBlock() instanceof FilterItemPipeBlock;
    }

    /** Upstream FilterList: an empty filter matches everything. Any entry may match. */
    public boolean allows(ItemStack stack) {
        if (!(getBlockState().getBlock() instanceof FilterItemPipeBlock) || filters.isEmpty()) {
            return true;
        }
        for (FilterEntry entry : filters) {
            if (entry.matches(stack)) {
                return true;
            }
        }
        return false;
    }

    public int insert(ItemStack stack, Direction in) {
        return insert(stack, in, ItemRoute.EMPTY);
    }

    public int insert(ItemStack stack, Direction in, ItemRoute route) {
        if (stack.isEmpty() || level == null || !allows(stack)) {
            return 0;
        }
        if (getBlockState().getBlock() instanceof MergePipeBlock merge
                && in == getBlockState().getValue(MergePipeBlock.FACING)
                && (route == null || route.isEmpty())) {
            return 0;
        }
        Direction out = chooseOutput(in, route);
        if (out == null) {
            return 0;
        }
        ItemRoute remaining = route == null ? new ItemRoute() : new ItemRoute(route);
        if (!remaining.isEmpty() && remaining.get(0) == out) {
            remaining.pop();
        }
        items.add(new TravellingItem(stack.copy(), in, out, 0, remaining));
        setChanged();
        return stack.getCount();
    }

    public void serverTick() {
        if (level == null) {
            return;
        }
        for (int i = items.size() - 1; i >= 0; i--) {
            TravellingItem item = items.get(i);
            item.progress += SPEED;
            if (item.progress < 1F) {
                continue;
            }
            if (deliver(item)) {
                items.remove(i);
            } else {
                Direction previous = item.out;
                item.out = item.in;
                item.in = previous;
                item.progress = 0;
            }
            setChanged();
        }
    }

    public void rebuildGraph(ServerLevel level) {
        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof ItemPipeBlock block)) {
            return;
        }
        vertex.clear();
        PipeGraph.eraseHiddenFromNeighbours(level, worldPosition, state, vertex, ItemPipeBlockEntity::findPipe);
        int connections = vertex.countConnections(state);
        if (connections > 2 || !vertex.canSimplify()) {
            for (Direction direction : vertex.getConnections(state)) {
                Pair<PipeNode, Direction> next = PipeGraph.findNextVertex(level, worldPosition, direction, ItemPipeBlockEntity::findNode, ItemPipeBlockEntity::findPipe);
                if (next != null) {
                    vertex.setAdjVertex(direction.get3DDataValue(), next.first());
                    next.first().setAdjVertex(next.second().get3DDataValue(), vertex);
                }
            }
        } else if (connections == 2) {
            PipeGraph.linkVertices(level, worldPosition, state, vertex, ItemPipeBlockEntity::findNode, ItemPipeBlockEntity::findPipe);
        }
    }

    private boolean deliver(TravellingItem item) {
        BlockPos target = worldPosition.relative(item.out);
        BlockState state = level.getBlockState(target);
        if (state.getBlock() instanceof ItemPipeBlock && level.getBlockEntity(target) instanceof ItemPipeBlockEntity next) {
            if (!next.allows(item.stack)) {
                return false;
            }
            return next.insert(item.stack, item.out.getOpposite(), item.route) == item.stack.getCount();
        }
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, target, item.out.getOpposite());
        if (handler == null) {
            return false;
        }
        ItemStack leftover = ItemHandlerHelper.insertItem(handler, item.stack.copy(), true);
        if (!leftover.isEmpty()) {
            return false;
        }
        ItemHandlerHelper.insertItem(handler, item.stack.copy(), false);
        return true;
    }

    private Direction chooseOutput(Direction in, ItemRoute route) {
        if (getBlockState().getBlock() instanceof MergePipeBlock && (route == null || route.isEmpty())) {
            Direction facing = getBlockState().getValue(MergePipeBlock.FACING);
            return facing != in && accepts(facing) ? facing : null;
        }
        if (route != null && !route.isEmpty()) {
            Direction preferred = route.get(0);
            if (preferred != in && accepts(preferred)) {
                return preferred;
            }
        }
        Direction[] directions = Direction.values();
        for (int step = 0; step < directions.length; step++) {
            int index = Math.floorMod(lastOutput + step, directions.length);
            Direction direction = directions[index];
            if (direction != in && accepts(direction)) {
                lastOutput = Math.floorMod(index + 1, directions.length);
                return direction;
            }
        }
        return null;
    }

    private boolean accepts(Direction direction) {
        if (level == null) {
            return false;
        }
        BlockPos neighbour = worldPosition.relative(direction);
        BlockState state = level.getBlockState(neighbour);
        if (state.getBlock() instanceof ItemPipeBlock) {
            return true;
        }
        return level.getCapability(Capabilities.ItemHandler.BLOCK, neighbour, direction.getOpposite()) != null;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server && getBlockState().getBlock() instanceof ConnectedPipeBlock pipe) {
            pipe.refreshConnections(level, worldPosition);
            rebuildGraph(server);
        }
    }

    private static ConnectablePipe findPipe(ServerLevel level, BlockPos pos, BlockState state) {
        return level.getBlockEntity(pos) instanceof ItemPipeBlockEntity pipe ? pipe.vertex : null;
    }

    private static PipeNode findNode(ServerLevel level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ItemPipeBlockEntity pipe ? pipe.vertex : null;
    }

    public boolean carrying(ItemStack stack) {
        for (TravellingItem item : items) {
            if (ItemStack.isSameItem(item.stack, stack)) {
                return true;
            }
        }
        return false;
    }

    public final class PipeNode implements PipeVertex, ConnectablePipe {
        private final PipeVertex[] adjacent = new PipeVertex[6];

        @Override
        public boolean canSimplify() {
            Block block = getBlockState().getBlock();
            return !(block instanceof FilterItemPipeBlock)
                    && !(block instanceof MergePipeBlock)
                    && !(block instanceof StorageBusBlock);
        }

        @Override
        public void setAdjVertex(int id, PipeVertex other) {
            adjacent[id] = other;
        }

        public PipeVertex adjacent(Direction direction) {
            return adjacent[direction.get3DDataValue()];
        }

        public void clear() {
            for (int i = 0; i < adjacent.length; i++) {
                adjacent[i] = null;
            }
        }

        @Override
        public PipeConnectionsC getConnections(BlockState state) {
            PipeConnections connections = new PipeConnections();
            for (Direction direction : Direction.values()) {
                if (state.getValue(PipeProperties.of(direction)) == ConnectionType.SIDE) {
                    connections.set(direction);
                }
            }
            if (state.getBlock() instanceof MergePipeBlock) {
                connections.set(state.getValue(MergePipeBlock.FACING));
            }
            return connections;
        }

        @Override
        public boolean isConnected(BlockGetter level, BlockPos pos, BlockState state, Direction direction) {
            return state.getValue(PipeProperties.of(direction)).connected();
        }

        @Override
        public PipeVertex getPipeVertex(ServerLevel level, BlockPos pos, BlockState state) {
            return this;
        }
    }

    private static final class TravellingItem {
        private final ItemStack stack;
        private Direction in;
        private Direction out;
        private float progress;
        private final ItemRoute route;

        private TravellingItem(ItemStack stack, Direction in, Direction out, float progress, ItemRoute route) {
            this.stack = stack;
            this.in = in;
            this.out = out;
            this.progress = progress;
            this.route = route;
        }
    }
}
