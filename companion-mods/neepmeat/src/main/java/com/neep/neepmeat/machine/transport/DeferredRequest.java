/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * A request that stays open while items are still in the pipes. SUCCESS when
 * the destination holds the stack, PASS while a pipe is carrying it, FAIL once
 * the wait expires with nothing moving.
 */

package com.neep.neepmeat.machine.transport;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public final class DeferredRequest {
    public enum State { PASS, SUCCESS, FAIL }

    private final BlockPos destination;
    private final ItemStack stack;
    private int age;
    private State state = State.PASS;

    public DeferredRequest(BlockPos destination, ItemStack stack) {
        this.destination = destination;
        this.stack = stack.copyWithCount(1);
    }

    public BlockPos destination() {
        return destination;
    }

    public ItemStack stack() {
        return stack;
    }

    public State state() {
        return state;
    }

    public void tick() {
        age++;
    }

    public int age() {
        return age;
    }

    public void succeed() {
        state = State.SUCCESS;
    }

    public void fail() {
        state = State.FAIL;
    }
}
