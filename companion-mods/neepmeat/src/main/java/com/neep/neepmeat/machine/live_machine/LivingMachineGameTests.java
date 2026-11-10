/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.machine.live_machine;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.live_machine.StructureProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(NeepMeat.ID)
@PrefixGameTestTemplate(false)
public class LivingMachineGameTests {
    @GameTest(template = "empty")
    public static void machineFormsFromABlockAndAMotorPort(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), LiveMachines.CONTROLLER.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 0, 1), LiveMachines.MACHINE_BLOCK.get().defaultBlockState());
        helper.setBlock(new BlockPos(3, 0, 1), LiveMachines.MOTOR_PORT.get().defaultBlockState());
        LivingMachineControllerBlockEntity controller = helper.getBlockEntity(new BlockPos(1, 0, 1));
        controller.serverTick();
        helper.assertTrue(controller.formed(), "machine did not form");
        helper.assertTrue(controller.structureCount() == 1, "structures " + controller.structureCount());
        helper.assertTrue(controller.componentCount(LivingMachineComponents.MOTOR_PORT) == 1, "motor port missing");
        helper.assertTrue(controller.property(StructureProperty.MAX_POWER) == 300f, "power " + controller.property(StructureProperty.MAX_POWER));
        helper.assertTrue(controller.property(StructureProperty.MASS) == 101f, "mass " + controller.property(StructureProperty.MASS));
        MotorPortBlockEntity port = helper.getBlockEntity(new BlockPos(3, 0, 1));
        helper.assertTrue(port.controller().equals(helper.absolutePos(new BlockPos(1, 0, 1))), "controller link");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void metaboliserConsumesMeat(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), LiveMachines.CONTROLLER.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 0, 1), LiveMachines.MACHINE_BLOCK.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 0, 2), LiveMachines.STOMACH.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 1, 1), LiveMachines.METABOLISER.get().defaultBlockState());
        com.neep.neepmeat.machine.live_machine.block.entity.StomachBlockEntity stomach = helper.getBlockEntity(new BlockPos(2, 0, 2));
        stomach.store(com.neep.neepmeat.fluid.MeatFluid.STILL.get(), 4);
        LivingMachineControllerBlockEntity controller = helper.getBlockEntity(new BlockPos(1, 0, 1));
        controller.serverTick();
        com.neep.neepmeat.machine.live_machine.block.entity.MetaboliserSegmentBlockEntity segment = helper.getBlockEntity(new BlockPos(2, 1, 1));
        helper.assertTrue(controller.process() instanceof com.neep.neepmeat.machine.live_machine.process.MetaboliserProcess, "process " + controller.process());
        helper.assertTrue(segment.energy() == 240, "energy " + segment.energy());
        helper.assertTrue(stomach.droplets() == 0, "meat left " + stomach.droplets());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void brokenMachineBecomesAWorm(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), LiveMachines.CONTROLLER.get().defaultBlockState());
        LivingMachineControllerBlockEntity controller = helper.getBlockEntity(new BlockPos(1, 0, 1));
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        tag.putDouble("degradation", 1);
        controller.readDegradation(tag);
        controller.serverTick();
        helper.assertTrue(helper.getBlockState(new BlockPos(1, 0, 1)).isAir(), "controller remained");
        helper.assertTrue(!helper.getLevel().getEntitiesOfClass(com.neep.neepmeat.entity.MachineWormEntity.class,
                new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(1, 0, 1))).inflate(1)).isEmpty(), "no worm");
        helper.succeed();
    }
}
