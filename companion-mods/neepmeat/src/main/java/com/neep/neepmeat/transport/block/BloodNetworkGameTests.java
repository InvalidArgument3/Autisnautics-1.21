/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.transport.block;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.transport.blood_network.BloodNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(NeepMeat.ID)
@PrefixGameTestTemplate(false)
public class BloodNetworkGameTests {
    @GameTest(template = "empty")
    public static void bloodFlowsThroughTwoConduits(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), VascularConduits.SOURCE.get().defaultBlockState());
        helper.setBlock(new BlockPos(4, 0, 1), VascularConduits.SINK.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 0, 1), VascularConduits.CONDUIT.get().defaultBlockState());
        helper.setBlock(new BlockPos(3, 0, 1), VascularConduits.CONDUIT.get().defaultBlockState());
        VascularConduitBlockEntity first = helper.getBlockEntity(new BlockPos(2, 0, 1));
        VascularConduitBlockEntity second = helper.getBlockEntity(new BlockPos(3, 0, 1));
        helper.assertTrue(first.network() != null && first.network() == second.network(), "conduits did not join");
        helper.assertTrue(first.network().conduitCount() == 2, "count " + first.network().conduitCount());
        BloodNetworkManager.get(helper.getLevel()).tick();
        VascularConduits.EndBlock.EndEntity sink = helper.getBlockEntity(new BlockPos(4, 0, 1));
        helper.assertTrue(Math.abs(sink.influx() - 1f) < 0.001f, "influx " + sink.influx());
        helper.succeed();
    }
}
