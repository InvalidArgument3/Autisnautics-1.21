/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.machine.integrator;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.fluid.BloodFluids;
import com.neep.neepmeat.item.MaterialItems;
import com.neep.neepmeat.machine.StageMachines;
import com.neep.neepmeat.machine.advanced_integrator.AdvancedIntegratorBlockEntity;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.pedestal.PedestalBlockEntity;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(NeepMeat.ID)
@PrefixGameTestTemplate(false)
public class IntegratorGameTests {
    @GameTest(template = "empty")
    public static void integratorMaturesAndGeneratesData(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, StageMachines.INTEGRATOR.get().defaultBlockState());
        IntegratorBlockEntity integrator = helper.getBlockEntity(pos);
        int accepted = integrator.insertBlood(BloodFluids.BLOOD.get(), FluidType.BUCKET_VOLUME);
        helper.assertTrue(accepted == FluidType.BUCKET_VOLUME, "blood " + accepted);
        integrator.setGrowthTimeRemaining(1);
        integrator.serverTick();
        helper.assertTrue(integrator.mature() && integrator.canEnlighten(), "mature");
        long before = integrator.getData(com.neep.neepmeat.api.DataResource.NORMAL);
        integrator.serverTick();
        helper.assertTrue(integrator.getData(com.neep.neepmeat.api.DataResource.NORMAL) == before + 1, "data");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void advancedIntegratorFormsAndGeneratesData(GameTestHelper helper) {
        BlockPos origin = new BlockPos(2, 1, 2);
        helper.setBlock(origin, StageMachines.ADVANCED_INTEGRATOR.get().defaultBlockState());
        StageMachines.ADVANCED_INTEGRATOR.get().setPlacedBy(helper.getLevel(), helper.absolutePos(origin), helper.getBlockState(origin), null, ItemStack.EMPTY);
        helper.assertTrue(helper.getBlockState(origin.north()).is(StageMachines.ADVANCED_INTEGRATOR.get().getStructure()), "structure");
        helper.assertTrue(helper.getBlockState(origin.north()).getBlock() instanceof DataCable, "cable");
        AdvancedIntegratorBlockEntity integrator = helper.getBlockEntity(origin);
        for (int tick = 0; tick < 4; tick++) {
            integrator.serverTick();
        }
        helper.assertTrue(integrator.data() == 4, "data " + integrator.data());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void pedestalEnlightensThroughDistributor(GameTestHelper helper) {
        BlockPos integratorPos = new BlockPos(0, 1, 1);
        BlockPos cablePos = new BlockPos(1, 1, 1);
        BlockPos distributor = new BlockPos(3, 1, 1);
        BlockPos pedestalPos = new BlockPos(3, 1, 3);
        helper.setBlock(integratorPos, StageMachines.INTEGRATOR.get().defaultBlockState());
        helper.setBlock(cablePos, StageMachines.DATA_CABLE.get().defaultBlockState());
        helper.setBlock(distributor, LivingMachines.DISTRIBUTOR.get().defaultBlockState());
        LivingMachines.DISTRIBUTOR.get().setPlacedBy(helper.getLevel(), helper.absolutePos(distributor), helper.getBlockState(distributor), null, ItemStack.EMPTY);
        helper.setBlock(pedestalPos, StageMachines.PEDESTAL.get().defaultBlockState());
        helper.assertTrue(helper.getBlockState(cablePos).getBlock() instanceof DataCable, "cable block");
        helper.assertTrue(helper.getBlockState(distributor.west()).getBlock() instanceof DataCable, "distributor structure");
        IntegratorBlockEntity integrator = helper.getBlockEntity(integratorPos);
        integrator.insertBlood(BloodFluids.BLOOD.get(), FluidType.BUCKET_VOLUME);
        integrator.setGrowthTimeRemaining(0);
        integrator.grow();
        integrator.setData(1024);
        PedestalBlockEntity pedestal = helper.getBlockEntity(pedestalPos);
        pedestal.setStack(new ItemStack(MaterialItems.BRAIN_ROUGH.get()));
        pedestal.craft();
        helper.assertTrue(pedestal.stack().is(MaterialItems.ENLIGHTENED_BRAIN.get()), "result " + pedestal.stack());
        helper.assertTrue(integrator.getData(com.neep.neepmeat.api.DataResource.NORMAL) == 0, "data left");
        helper.succeed();
    }
}
