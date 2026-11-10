/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.machine.live_machine;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.big_block_2.BigBlock2;
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
        helper.setBlock(new BlockPos(6, 2, 1), LiveMachines.STOMACH.get().defaultBlockState());
        helper.setBlock(new BlockPos(1, 4, 1), LiveMachines.METABOLISER.get().defaultBlockState());
        helper.setBlock(new BlockPos(1, 2, 1), LiveMachines.CONTROLLER.get().defaultBlockState());
        helper.setBlock(new BlockPos(1, 3, 1), LiveMachines.MACHINE_BLOCK.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 2, 1), LiveMachines.MACHINE_BLOCK.get().defaultBlockState());
        helper.setBlock(new BlockPos(3, 2, 1), LiveMachines.MACHINE_BLOCK.get().defaultBlockState());
        helper.setBlock(new BlockPos(4, 2, 1), LiveMachines.MACHINE_BLOCK.get().defaultBlockState());
        com.neep.neepmeat.machine.live_machine.block.entity.StomachBlockEntity stomach = helper.getBlockEntity(new BlockPos(6, 2, 1));
        stomach.store(com.neep.neepmeat.fluid.MeatFluid.STILL.get(), 4);
        LivingMachineControllerBlockEntity controller = helper.getBlockEntity(new BlockPos(1, 2, 1));
        controller.serverTick();
        com.neep.neepmeat.machine.live_machine.block.entity.MetaboliserSegmentBlockEntity segment = helper.getBlockEntity(new BlockPos(1, 4, 1));
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

    @GameTest(template = "empty")
    public static void stomachPlacesAndBreaksAsABigBlock(GameTestHelper helper) {
        BlockPos origin = new BlockPos(4, 2, 4);
        helper.setBlock(origin, LiveMachines.STOMACH.get().defaultBlockState());
        helper.assertTrue(helper.getBlockState(origin.north()).is(LiveMachines.STOMACH_STRUCTURE.get()), "north structure missing");
        helper.assertTrue(helper.getBlockState(origin.above()).is(LiveMachines.STOMACH_STRUCTURE.get()), "above structure missing");
        helper.setBlock(origin.north(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        helper.assertTrue(helper.getBlockState(origin).isAir(), "origin remained");
        helper.assertTrue(helper.getBlockState(origin.south()).isAir(), "south structure remained");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dropletsRoundDownToMillibuckets(GameTestHelper helper) {
        helper.assertTrue(com.neep.neepmeat.transfer.TransferAmounts.dropletsToMillibuckets(200) == 2, "millibuckets");
        helper.assertTrue(com.neep.neepmeat.transfer.TransferAmounts.leftoverDroplets(200) == 38, "remainder");
        helper.assertTrue(com.neep.neepmeat.transfer.TransferAmounts.millibucketsToDroplets(2) == 162, "back to droplets");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void metaboliserOnItsSideUsesTheXVolume(GameTestHelper helper) {
        BlockPos origin = new BlockPos(4, 2, 4);
        helper.setBlock(origin, LiveMachines.METABOLISER.get().defaultBlockState().setValue(BigBlock2.AXIS, net.minecraft.core.Direction.Axis.X));
        helper.assertTrue(helper.getBlockState(origin).getValue(BigBlock2.AXIS) == net.minecraft.core.Direction.Axis.X, "axis");
        helper.assertTrue(helper.getBlockState(origin.below()).is(LiveMachines.METABOLISER_STRUCTURE.get()), "x slab missing below");
        helper.assertTrue(helper.getBlockState(origin.east()).isAir(), "x slab reached east");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void compressorMakes400Droplets(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), LiveMachines.CONTROLLER.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 0, 1), LiveMachines.MOTOR_PORT.get().defaultBlockState());
        helper.setBlock(new BlockPos(3, 0, 1), LiveMachines.COMPRESSOR.get().defaultBlockState());
        MotorPortBlockEntity port = helper.getBlockEntity(new BlockPos(2, 0, 1));
        port.setInputPower(1.0F);
        LivingMachineControllerBlockEntity controller = helper.getBlockEntity(new BlockPos(1, 0, 1));
        controller.serverTick();
        com.neep.neepmeat.machine.live_machine.block.entity.LargeCompressorBlockEntity compressor = helper.getBlockEntity(new BlockPos(3, 0, 1));
        helper.assertTrue(controller.process() instanceof com.neep.neepmeat.machine.live_machine.process.LargeCompressorProcess, "process");
        helper.assertTrue(compressor.droplets() == 400 && compressor.fluid().isOf(com.neep.neepmeat.fluid.CompressedAir.STILL.get()),
                "air " + compressor.droplets() + " " + compressor.fluid());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void crusherTakesTheInputStack(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), LiveMachines.CONTROLLER.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 0, 1), LiveMachines.MOTOR_PORT.get().defaultBlockState());
        helper.setBlock(new BlockPos(3, 0, 1), LiveMachines.ITEM_INPUT.get().defaultBlockState());
        helper.setBlock(new BlockPos(4, 0, 1), LiveMachines.CRUSHER.get().defaultBlockState());
        helper.setBlock(new BlockPos(5, 0, 1), LiveMachines.ITEM_OUTPUT.get().defaultBlockState());
        MotorPortBlockEntity port = helper.getBlockEntity(new BlockPos(2, 0, 1));
        port.setInputPower(1.0F);
        com.neep.neepmeat.machine.live_machine.block.entity.ItemPortBlockEntity input = helper.getBlockEntity(new BlockPos(3, 0, 1));
        input.setStack(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COBBLESTONE, 3));
        LivingMachineControllerBlockEntity controller = helper.getBlockEntity(new BlockPos(1, 0, 1));
        controller.serverTick();
        controller.serverTick();
        com.neep.neepmeat.machine.live_machine.block.entity.CrusherSegmentBlockEntity crusher = helper.getBlockEntity(new BlockPos(4, 0, 1));
        com.neep.neepmeat.machine.live_machine.block.entity.ItemPortBlockEntity output = helper.getBlockEntity(new BlockPos(5, 0, 1));
        helper.assertTrue(controller.process() instanceof com.neep.neepmeat.machine.live_machine.process.CrusherProcess, "process");
        helper.assertTrue(crusher.stack().isEmpty(), "slot " + crusher.stack());
        helper.assertTrue(output.stack().is(net.minecraft.world.item.Items.GRAVEL), "result " + output.stack());
        helper.succeed();
    }
}
