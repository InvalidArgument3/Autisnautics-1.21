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
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

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
}
