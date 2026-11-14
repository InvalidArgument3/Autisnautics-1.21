/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.transfer;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

@GameTestHolder(NeepMeat.ID)
@PrefixGameTestTemplate(false)
public class TransferGameTests {
    @GameTest(template = "empty")
    public static void abortRollsBackAndCommitKeepsDroplets(GameTestHelper helper) {
        WritableSingleFluidStorage storage = new WritableSingleFluidStorage(FluidConstants.BUCKET, () -> {});
        try (Transaction transaction = Transaction.openOuter()) {
            storage.insert(FluidVariant.of(Fluids.WATER), 200, transaction);
            helper.assertTrue(storage.amount == 200, "during " + storage.amount);
            transaction.abort();
        }
        helper.assertTrue(storage.amount == 0, "aborted " + storage.amount);
        try (Transaction transaction = Transaction.openOuter()) {
            storage.insert(FluidVariant.of(Fluids.WATER), 200, transaction);
            transaction.onRename();
        }
        helper.assertTrue(storage.amount == 200, "committed " + storage.amount);
        int filled = storage.fill(new FluidStack(Fluids.LAVA, 1), IFluidHandler.FluidAction.SIMULATE);
        helper.assertTrue(filled == 0, "foreign fluid simulated " + filled);
        storage.amount = 0;
        storage.variant = FluidVariant.BLANK;
        int water = storage.fill(new FluidStack(Fluids.WATER, 1), IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(water == 1 && storage.amount == 81, "millibucket " + storage.amount);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void abortedItemInsertLeavesTheChestAlone(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.CHEST.defaultBlockState());
        ChestBlockEntity chest = helper.getBlockEntity(pos);
        IItemHandler capability = chest.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(pos), Direction.UP);
        helper.assertTrue(capability != null, "chest has no item handler");
        Storage<ItemVariant> storage = ItemHandlerStorage.of(capability);
        try (Transaction transaction = Transaction.openOuter()) {
            long moved = storage.insert(ItemVariant.of(Items.COBBLESTONE), 4, transaction);
            helper.assertTrue(moved == 4, "simulated " + moved);
            helper.assertTrue(chest.getItem(0).isEmpty(), "simulate wrote the chest");
            transaction.abort();
        }
        helper.assertTrue(chest.getItem(0).isEmpty(), "aborted insert changed the chest");
        try (Transaction transaction = Transaction.openOuter()) {
            long moved = storage.insert(ItemVariant.of(Items.COBBLESTONE), 4, transaction);
            helper.assertTrue(moved == 4, "committed simulate " + moved);
            transaction.onRename();
        }
        helper.assertTrue(chest.getItem(0).is(Items.COBBLESTONE) && chest.getItem(0).getCount() == 4,
                "committed " + chest.getItem(0));
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fluidRoundTripUsesWholeMillibuckets(GameTestHelper helper) {
        Tank tank = new Tank();
        Storage<FluidVariant> storage = FluidHandlerStorage.of(tank);
        try (Transaction transaction = Transaction.openOuter()) {
            long moved = storage.insert(FluidVariant.of(Fluids.WATER), 80, transaction);
            helper.assertTrue(moved == 0, "remainder created fluid " + moved);
            transaction.onRename();
        }
        helper.assertTrue(tank.stack.isEmpty(), "remainder filled " + tank.stack.getAmount());
        try (Transaction transaction = Transaction.openOuter()) {
            long moved = storage.insert(FluidVariant.of(Fluids.WATER), 81, transaction);
            helper.assertTrue(moved == 81, "insert " + moved);
            transaction.abort();
        }
        helper.assertTrue(tank.stack.isEmpty(), "aborted fluid insert");
        try (Transaction transaction = Transaction.openOuter()) {
            storage.insert(FluidVariant.of(Fluids.WATER), 81 + 80, transaction);
            transaction.onRename();
        }
        helper.assertTrue(tank.stack.getAmount() == 1, "stored mB " + tank.stack.getAmount());
        try (Transaction transaction = Transaction.openOuter()) {
            long moved = storage.extract(FluidVariant.of(Fluids.WATER), 81, transaction);
            helper.assertTrue(moved == 81, "extract " + moved);
            transaction.onRename();
        }
        helper.assertTrue(tank.stack.isEmpty(), "round trip left " + tank.stack.getAmount());
        helper.succeed();
    }

    private static final class Tank implements IFluidHandler {
        private FluidStack stack = FluidStack.EMPTY;

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return stack;
        }

        @Override
        public int getTankCapacity(int tank) {
            return 1000;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack fluid) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) {
                return 0;
            }
            if (!stack.isEmpty() && !stack.isFluidEqual(resource)) {
                return 0;
            }
            int filled = Math.min(resource.getAmount(), 1000 - stack.getAmount());
            if (filled > 0 && action.execute()) {
                if (stack.isEmpty()) {
                    stack = resource.copy();
                    stack.setAmount(filled);
                } else {
                    stack.grow(filled);
                }
            }
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || stack.isEmpty() || !stack.isFluidEqual(resource)) {
                return FluidStack.EMPTY;
            }
            return drain(resource.getAmount(), action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            int drained = Math.min(maxDrain, stack.getAmount());
            if (drained <= 0) {
                return FluidStack.EMPTY;
            }
            FluidStack result = stack.copy();
            result.setAmount(drained);
            if (action.execute()) {
                stack.shrink(drained);
                if (stack.isEmpty()) {
                    stack = FluidStack.EMPTY;
                }
            }
            return result;
        }
    }
}
