/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.transport.block;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.machine.live_machine.LiveMachines;
import com.neep.neepmeat.machine.live_machine.block.entity.LuckyOneBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.entity.MetaboliserSegmentBlockEntity;
import com.neep.neepmeat.transport.api.pipe.BalancingBloodAcceptor;
import com.neep.neepmeat.transport.blood_network.BloodNetworkManager;
import com.neep.neepmeat.transport.fluid_network.ConnectionType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(NeepMeat.ID)
@PrefixGameTestTemplate(false)
public class BloodNetworkGameTests {
    @GameTest(template = "empty")
    public static void bloodFlowsFromMetaboliserToLuckyOne(GameTestHelper helper) {
        helper.setBlock(new BlockPos(2, 1, 2), LiveMachines.METABOLISER.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 1, 6), LiveMachines.LUCKY_ONE.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 1, 4), VascularConduits.CONDUIT.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 1, 5), VascularConduits.CONDUIT.get().defaultBlockState());
        MetaboliserSegmentBlockEntity segment = helper.getBlockEntity(new BlockPos(2, 1, 2));
        segment.setOutputEnergy(1000);
        VascularConduitBlockEntity first = helper.getBlockEntity(new BlockPos(2, 1, 4));
        VascularConduitBlockEntity second = helper.getBlockEntity(new BlockPos(2, 1, 5));
        helper.assertTrue(first.network() != null && first.network() == second.network(), "conduits did not join");
        helper.assertTrue(helper.getBlockState(new BlockPos(2, 1, 4)).getValue(VascularConduitBlock.NORTH) == ConnectionType.SIDE
                || helper.getBlockState(new BlockPos(2, 1, 4)).getValue(VascularConduitBlock.SOUTH) == ConnectionType.SIDE, "face open");
        BloodNetworkManager.get(helper.getLevel()).tick();
        LuckyOneBlockEntity lucky = helper.getBlockEntity(new BlockPos(2, 1, 6));
        helper.assertTrue(Math.abs(lucky.influx() - 0.05f) < 0.001f, "influx " + lucky.influx());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void breakingTheMiddleConduitSplitsTheNetwork(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), VascularConduits.CONDUIT.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 0, 1), VascularConduits.CONDUIT.get().defaultBlockState());
        helper.setBlock(new BlockPos(3, 0, 1), VascularConduits.CONDUIT.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 0, 1), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        VascularConduitBlockEntity left = helper.getBlockEntity(new BlockPos(1, 0, 1));
        VascularConduitBlockEntity right = helper.getBlockEntity(new BlockPos(3, 0, 1));
        helper.assertTrue(left.network() != null && right.network() != null && left.network() != right.network(), "networks stayed joined");
        helper.assertTrue(left.network().conduitCount() == 1 && right.network().conduitCount() == 1, "split sizes");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void balancerTakesEnergyBeforeTheSink(GameTestHelper helper) {
        helper.setBlock(new BlockPos(2, 1, 2), LiveMachines.METABOLISER.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 1, 6), LiveMachines.LUCKY_ONE.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 1, 4), VascularConduits.CONDUIT.get().defaultBlockState());
        MetaboliserSegmentBlockEntity segment = helper.getBlockEntity(new BlockPos(2, 1, 2));
        segment.setOutputEnergy(1000);
        VascularConduitBlockEntity conduit = helper.getBlockEntity(new BlockPos(2, 1, 4));
        conduit.network().include(new BalancingBloodAcceptor() {
            @Override
            public long update(long internal) {
                return -internal;
            }

            @Override
            public int priority() {
                return 1;
            }
        });
        BloodNetworkManager.get(helper.getLevel()).tick();
        LuckyOneBlockEntity lucky = helper.getBlockEntity(new BlockPos(2, 1, 6));
        helper.assertTrue(lucky.influx() == 0, "sink still received " + lucky.influx());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void unloadingDropsTheConduitFromTheNetwork(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), VascularConduits.CONDUIT.get().defaultBlockState());
        VascularConduitBlockEntity conduit = helper.getBlockEntity(new BlockPos(1, 0, 1));
        helper.assertTrue(conduit.network() != null && conduit.network().conduitCount() == 1, "missing network");
        conduit.onChunkUnloaded();
        helper.assertTrue(conduit.network() == null, "still attached");
        helper.succeed();
    }

    @SuppressWarnings("unused")
    private static Direction facing() {
        return Direction.EAST;
    }
}
