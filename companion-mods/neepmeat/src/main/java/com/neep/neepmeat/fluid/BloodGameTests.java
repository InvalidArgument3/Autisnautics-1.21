/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.fluid;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.machine.NeepMachines;
import com.neep.neepmeat.machine.transport.FluidTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(NeepMeat.ID)
@PrefixGameTestTemplate(false)
public class BloodGameTests {
    @GameTest(template = "empty")
    public static void basicTankHoldsBlood(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), NeepMachines.BASIC_TANK.get().defaultBlockState());
        FluidTankBlockEntity tank = helper.getBlockEntity(new BlockPos(1, 1, 1));
        int filled = tank.tank().fill(new FluidStack(BloodFluids.BLOOD.get(), 1000), IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(filled == 1000, "filled " + filled);
        helper.assertTrue(tank.tank().getFluid().getFluid() == BloodFluids.BLOOD.get(), "not blood");
        helper.assertTrue(BloodFluids.BLOOD_BLOCK.get().defaultBlockState().getFluidState().is(BloodFluids.BLOOD.get()), "liquid block");
        helper.succeed();
    }
}
