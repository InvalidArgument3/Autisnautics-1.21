/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Items cross a pipe at the upstream default speed of 0.1 progress per tick
 * (ten ticks). Output choice is the upstream round-robin over connected sides,
 * skipping the side the item entered from. The route byte list, filters, and
 * the vertex graph are not stored yet.
 */

package com.neep.neepmeat.machine.transport;

import com.neep.neepmeat.machine.NeepMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public class ItemPipeBlockEntity extends BlockEntity {
    /** Upstream {@code ItemInPipe} default. */
    public static final float SPEED = 0.1F;

    private final List<TravellingItem> items = new ArrayList<>();
    private int lastOutput;

    public ItemPipeBlockEntity(BlockPos pos, BlockState state) {
        super(NeepMachines.ITEM_PIPE_BE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide && getBlockState().getBlock() instanceof ConnectedPipeBlock pipe) {
            pipe.refreshConnections(level, worldPosition);
        }
    }

    /**
     * Accept a stack entering from {@code in}. Returns the number taken.
     * Nothing is taken when no other connected side can be an output.
     */
    public int insert(ItemStack stack, Direction in) {
        if (stack.isEmpty() || level == null) {
            return 0;
        }
        Direction out = chooseOutput(in);
        if (out == null) {
            return 0;
        }
        items.add(new TravellingItem(stack.copy(), in, out, 0));
        setChanged();
        return stack.getCount();
    }

    public void serverTick() {
        Level level = getLevel();
        if (level == null) {
            return;
        }
        boolean changed = false;
        for (int i = items.size() - 1; i >= 0; i--) {
            TravellingItem item = items.get(i);
            item.progress += SPEED;
            if (item.progress < 1F) {
                continue;
            }
            changed = true;
            if (!deliver(level, item)) {
                Direction previousOut = item.out;
                item.out = item.in;
                item.in = previousOut;
                item.progress = 0;
            } else {
                items.remove(i);
            }
        }
        if (changed) {
            setChanged();
        }
    }

    private boolean deliver(Level level, TravellingItem item) {
        BlockPos target = worldPosition.relative(item.out);
        BlockState state = level.getBlockState(target);
        if (state.getBlock() instanceof ItemPipeBlock && level.getBlockEntity(target) instanceof ItemPipeBlockEntity next) {
            return next.insert(item.stack, item.out.getOpposite()) == item.stack.getCount();
        }
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, target, item.out.getOpposite());
        if (handler == null) {
            return false;
        }
        ItemStack leftover = ItemHandlerHelper.insertItem(handler, item.stack.copy(), false);
        return leftover.isEmpty();
    }

    private Direction chooseOutput(Direction in) {
        Direction[] directions = Direction.values();
        for (int step = 0; step < directions.length; step++) {
            int index = Math.floorMod(lastOutput + step, directions.length);
            Direction direction = directions[index];
            if (direction == in || level == null) {
                continue;
            }
            if (accepts(direction)) {
                lastOutput = Math.floorMod(index + 1, directions.length);
                return direction;
            }
        }
        return null;
    }

    private boolean accepts(Direction direction) {
        BlockPos neighbour = worldPosition.relative(direction);
        BlockState state = level.getBlockState(neighbour);
        if (state.getBlock() instanceof ItemPipeBlock) {
            return true;
        }
        return level.getCapability(Capabilities.ItemHandler.BLOCK, neighbour, direction.getOpposite()) != null;
    }

    public int travelling() {
        return items.size();
    }

    private static final class TravellingItem {
        private final ItemStack stack;
        private Direction in;
        private Direction out;
        private float progress;

        private TravellingItem(ItemStack stack, Direction in, Direction out, float progress) {
            this.stack = stack;
            this.in = in;
            this.out = out;
            this.progress = progress;
        }
    }
}
