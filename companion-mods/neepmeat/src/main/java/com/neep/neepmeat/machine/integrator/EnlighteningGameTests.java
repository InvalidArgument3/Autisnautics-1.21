/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.machine.integrator;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.item.MaterialItems;
import com.neep.neepmeat.fluid.BloodFluids;
import com.neep.neepmeat.machine.StageMachines;
import com.neep.neepmeat.machine.pedestal.PedestalBlockEntity;
import com.neep.neepmeat.plc.component.MutateInPlace;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(NeepMeat.ID)
@PrefixGameTestTemplate(false)
public class EnlighteningGameTests {
    @GameTest(template = "empty")
    public static void roughBrainBecomesEnlightened(GameTestHelper helper) {
        BlockPos integratorPos = new BlockPos(1, 1, 1);
        BlockPos pedestalPos = new BlockPos(3, 1, 1);
        helper.setBlock(integratorPos, StageMachines.INTEGRATOR.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 1, 1), StageMachines.DATA_CABLE.get().defaultBlockState());
        helper.setBlock(pedestalPos, StageMachines.PEDESTAL.get().defaultBlockState());
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

    @GameTest(template = "empty")
    public static void surgeryPlatformFindsTheEntityAbove(GameTestHelper helper) {
        BlockPos platform = new BlockPos(1, 0, 1);
        helper.setBlock(platform, StageMachines.SURGERY.get().defaultBlockState());
        Pig pig = helper.spawn(EntityType.PIG, platform.above());
        MutateInPlace<net.minecraft.world.entity.Entity> place = MutateInPlace.ENTITY.find(
                helper.getLevel(), helper.absolutePos(platform), null);
        helper.assertTrue(place != null && place.get() == pig, "entity " + (place == null ? null : place.get()));
        helper.succeed();
    }
}
