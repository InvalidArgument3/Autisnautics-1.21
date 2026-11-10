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
import com.neep.neepmeat.machine.StageMachines;
import com.neep.neepmeat.plc.component.MutateInPlace;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(NeepMeat.ID)
@PrefixGameTestTemplate(false)
public class EnlighteningGameTests {
    @GameTest(template = "empty")
    public static void roughBrainBecomesEnlightened(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), StageMachines.INTEGRATOR.get().defaultBlockState());
        IntegratorBlockEntity integrator = helper.getBlockEntity(new BlockPos(1, 0, 1));
        integrator.setStack(new ItemStack(MaterialItems.BRAIN_ROUGH.get()));
        integrator.setData(1024);
        integrator.serverTick();
        helper.assertTrue(integrator.stack().is(MaterialItems.ENLIGHTENED_BRAIN.get()), "result " + integrator.stack());
        helper.assertTrue(integrator.data() == 0, "data left " + integrator.data());
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
