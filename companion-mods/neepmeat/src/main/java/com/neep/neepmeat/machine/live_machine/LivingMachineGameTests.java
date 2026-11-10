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
import com.neep.neepmeat.machine.live_machine.block.LuckyOneBlock;
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
        helper.setBlock(new BlockPos(1, 0, 1), LivingMachines.CONTROLLER.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 0, 1), LivingMachines.MACHINE_BLOCK.get().defaultBlockState());
        helper.setBlock(new BlockPos(3, 0, 1), LivingMachines.MOTOR_PORT.get().defaultBlockState());
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
        helper.setBlock(new BlockPos(6, 2, 1), LivingMachines.STOMACH.get().defaultBlockState());
        helper.setBlock(new BlockPos(1, 4, 1), LivingMachines.METABOLISER.get().defaultBlockState());
        helper.setBlock(new BlockPos(1, 2, 1), LivingMachines.CONTROLLER.get().defaultBlockState());
        helper.setBlock(new BlockPos(1, 3, 1), LivingMachines.MACHINE_BLOCK.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 2, 1), LivingMachines.MACHINE_BLOCK.get().defaultBlockState());
        helper.setBlock(new BlockPos(3, 2, 1), LivingMachines.MACHINE_BLOCK.get().defaultBlockState());
        helper.setBlock(new BlockPos(4, 2, 1), LivingMachines.MACHINE_BLOCK.get().defaultBlockState());
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
        helper.setBlock(new BlockPos(1, 0, 1), LivingMachines.CONTROLLER.get().defaultBlockState());
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
        helper.setBlock(origin, LivingMachines.STOMACH.get().defaultBlockState());
        helper.assertTrue(helper.getBlockState(origin.north()).is(LivingMachines.STOMACH_STRUCTURE.get()), "north structure missing");
        helper.assertTrue(helper.getBlockState(origin.above()).is(LivingMachines.STOMACH_STRUCTURE.get()), "above structure missing");
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
        helper.setBlock(origin, LivingMachines.METABOLISER.get().defaultBlockState().setValue(BigBlock2.AXIS, net.minecraft.core.Direction.Axis.X));
        helper.assertTrue(helper.getBlockState(origin).getValue(BigBlock2.AXIS) == net.minecraft.core.Direction.Axis.X, "axis");
        helper.assertTrue(helper.getBlockState(origin.below()).is(LivingMachines.METABOLISER_STRUCTURE.get()), "x slab missing below");
        helper.assertTrue(helper.getBlockState(origin.east()).isAir(), "x slab reached east");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void compressorMakes400Droplets(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), LivingMachines.CONTROLLER.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 0, 1), LivingMachines.MOTOR_PORT.get().defaultBlockState());
        helper.setBlock(new BlockPos(3, 0, 1), LivingMachines.COMPRESSOR.get().defaultBlockState());
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
        helper.setBlock(new BlockPos(1, 0, 1), LivingMachines.CONTROLLER.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 0, 1), LivingMachines.MOTOR_PORT.get().defaultBlockState());
        helper.setBlock(new BlockPos(3, 0, 1), LivingMachines.ITEM_INPUT.get().defaultBlockState());
        helper.setBlock(new BlockPos(4, 0, 1), LivingMachines.CRUSHER.get().defaultBlockState());
        helper.setBlock(new BlockPos(5, 0, 1), LivingMachines.ITEM_OUTPUT.get().defaultBlockState());
        MotorPortBlockEntity port = helper.getBlockEntity(new BlockPos(2, 0, 1));
        port.setInputPower(1.0F);
        com.neep.neepmeat.machine.live_machine.block.entity.ItemInputPortBlockEntity input = helper.getBlockEntity(new BlockPos(3, 0, 1));
        input.getInventory().setItem(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COBBLESTONE, 3));
        LivingMachineControllerBlockEntity controller = helper.getBlockEntity(new BlockPos(1, 0, 1));
        controller.serverTick();
        controller.serverTick();
        com.neep.neepmeat.machine.live_machine.block.entity.CrusherSegmentBlockEntity crusher = helper.getBlockEntity(new BlockPos(4, 0, 1));
        com.neep.neepmeat.machine.live_machine.block.entity.ItemOutputPortBlockEntity output = helper.getBlockEntity(new BlockPos(5, 0, 1));
        helper.assertTrue(controller.process() instanceof com.neep.neepmeat.machine.live_machine.process.CrusherProcess, "process");
        helper.assertTrue(crusher.stack().isEmpty(), "slot " + crusher.stack());
        helper.assertTrue(output.getInventory().getItem(0).is(net.minecraft.world.item.Items.GRAVEL), "result " + output.getInventory().getItem(0));
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void luckyOnePlacesTheUpperStructure(GameTestHelper helper) {
        BlockPos origin = new BlockPos(2, 1, 2);
        LuckyOneBlock block = (LuckyOneBlock) LivingMachines.LUCKY_ONE.get();
        net.minecraft.world.level.block.state.BlockState state = block.defaultBlockState();
        helper.setBlock(origin, state);
        block.setPlacedBy(helper.getLevel(), helper.absolutePos(origin), helper.getBlockState(origin), null, net.minecraft.world.item.ItemStack.EMPTY);
        helper.assertTrue(helper.getBlockState(origin.above()).is(block.getStructureBlock()), "upper structure missing");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void extractorAndFluidPortKeepTheirBlockEntities(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), LivingMachines.EXTRACTOR.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 0, 1), LivingMachines.SILKY_EXTRACTOR.get().defaultBlockState());
        helper.setBlock(new BlockPos(3, 0, 1), LivingMachines.SERVICE_PORT.get().defaultBlockState());
        helper.setBlock(new BlockPos(4, 0, 1), LivingMachines.FLUID_INPUT.get().defaultBlockState());
        helper.setBlock(new BlockPos(5, 0, 1), LivingMachines.REPROCESSOR.get().defaultBlockState());
        helper.assertTrue(helper.getBlockEntity(new BlockPos(1, 0, 1)) instanceof com.neep.neepmeat.machine.live_machine.block.entity.ExtractorBlockEntity, "extractor");
        helper.assertTrue(helper.getBlockEntity(new BlockPos(2, 0, 1)) instanceof com.neep.neepmeat.machine.live_machine.block.entity.SilkyExtractorBlockEntity, "silky");
        helper.assertTrue(helper.getBlockEntity(new BlockPos(3, 0, 1)) instanceof com.neep.neepmeat.machine.live_machine.block.entity.ServicePortBlockEntity, "service");
        helper.assertTrue(helper.getBlockEntity(new BlockPos(4, 0, 1)) instanceof com.neep.neepmeat.machine.live_machine.block.entity.FluidInputPortBlockEntity, "fluid");
        helper.assertTrue(helper.getBlockEntity(new BlockPos(5, 0, 1)) instanceof com.neep.neepmeat.machine.live_machine.block.entity.ReprocessorBlockEntity, "reprocessor");
        helper.succeed();
    }
}
