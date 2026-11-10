/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.plc;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.plc.program.PLCProgram;
import com.neep.neepmeat.fluid.BloodFluids;
import com.neep.neepmeat.machine.NeepMachines;
import com.neep.neepmeat.machine.transport.FluidTankBlockEntity;
import com.neep.neepmeat.plc.arm.RoboticArmBlockEntity;
import com.neep.neepmeat.plc.block.entity.PLCRedstoneInterfaceBlockEntity;
import com.neep.neepmeat.plc.compiler.NeepAsmCompilePipeline;
import com.neep.neepmeat.plc.robot.PLCActuator;
import com.neep.neepmeat.plc.runtime.InstructionPlc;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(NeepMeat.ID)
@PrefixGameTestTemplate(false)
public class PlcOpcodeGameTests {
    @GameTest(template = "empty")
    public static void memoryStoreThenFetch(GameTestHelper helper) {
        try {
            PLCProgram program = compile(helper, """
                    push 1
                    allot
                    dup
                    push 42
                    swp
                    store
                    fetch
                    """);
            InstructionPlc plc = new InstructionPlc(program, idle());
            run(plc, 16);
            helper.assertTrue(plc.error() == null, "error " + plc.error());
            helper.assertTrue(plc.dataStack().pop().getAsInt() == 42, "fetched value");
            helper.succeed();
        } catch (NeepASM.ProgramBuildException | NeepASM.RuntimeException exception) {
            helper.fail(exception.getMessage());
        }
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void moveCarriesOneItemWithTheArm(GameTestHelper helper) {
        BlockPos source = new BlockPos(0, 0, 1);
        BlockPos armPos = new BlockPos(1, 0, 1);
        BlockPos destination = new BlockPos(2, 0, 1);
        helper.setBlock(source, Blocks.CHEST.defaultBlockState());
        helper.setBlock(destination, Blocks.CHEST.defaultBlockState());
        helper.setBlock(armPos, NeepMachines.ROBOTIC_ARM.get().defaultBlockState());
        ChestBlockEntity from = helper.getBlockEntity(source);
        from.setItem(0, new ItemStack(Items.COBBLESTONE, 4));
        RoboticArmBlockEntity arm = helper.getBlockEntity(armPos);
        arm.setInputPower(1.0F);
        try {
            PLCProgram program = compile(helper, "move " + target(helper, source) + " " + target(helper, destination) + " 1\n");
            InstructionPlc plc = new InstructionPlc(program, arm);
            run(plc, 40);
            ChestBlockEntity to = helper.getBlockEntity(destination);
            helper.assertTrue(plc.error() == null, "error " + plc.error());
            helper.assertTrue(from.getItem(0).getCount() == 3, "source count " + from.getItem(0).getCount());
            helper.assertTrue(to.getItem(0).is(Items.COBBLESTONE) && to.getItem(0).getCount() == 1, "destination");
            helper.assertTrue(plc.dataStack().pop().getAsInt() == -1, "move result");
            helper.succeed();
        } catch (NeepASM.ProgramBuildException | NeepASM.RuntimeException exception) {
            helper.fail(exception.getMessage());
        }
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void injectTakesFluidWithTheArmThenReturnsIt(GameTestHelper helper) {
        BlockPos source = new BlockPos(0, 0, 1);
        BlockPos armPos = new BlockPos(1, 0, 1);
        BlockPos destination = new BlockPos(2, 0, 1);
        helper.setBlock(source, NeepMachines.FLUID_TANK.get().defaultBlockState());
        helper.setBlock(destination, Blocks.STONE.defaultBlockState());
        helper.setBlock(armPos, NeepMachines.ROBOTIC_ARM.get().defaultBlockState());
        FluidTankBlockEntity tank = helper.getBlockEntity(source);
        tank.tank().fill(new FluidStack(BloodFluids.BLOOD.get(), 1000), IFluidHandler.FluidAction.EXECUTE);
        RoboticArmBlockEntity arm = helper.getBlockEntity(armPos);
        arm.setInputPower(1.0F);
        try {
            PLCProgram program = compile(helper, "inject " + target(helper, source) + " " + target(helper, destination) + "\n");
            InstructionPlc plc = new InstructionPlc(program, arm);
            boolean sawEmpty = false;
            for (int tick = 0; tick < 80 && plc.error() == null && plc.counter() >= 0; tick++) {
                plc.tick();
                if (tank.tank().getFluidAmount() == 0) {
                    sawEmpty = true;
                }
            }
            helper.assertTrue(sawEmpty, "arm did not take the bucket");
            helper.assertTrue(tank.tank().getFluidAmount() == 1000, "fluid was not returned, left " + tank.tank().getFluidAmount());
            helper.assertTrue(plc.error() != null && plc.error().contains("No item or entity"), "error " + plc.error());
            helper.succeed();
        } catch (NeepASM.ProgramBuildException exception) {
            helper.fail(exception.getMessage());
        }
    }

    @GameTest(template = "empty")
    public static void redstoneReadAndEmit(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 0, 1);
        helper.setBlock(pos, NeepMachines.REDSTONE_INTERFACE.get().defaultBlockState());
        PLCRedstoneInterfaceBlockEntity redstone = helper.getBlockEntity(pos);
        redstone.updateReceived(7);
        try {
            PLCProgram read = compile(helper, "rread " + target(helper, pos) + "\n");
            InstructionPlc plc = new InstructionPlc(read, idle());
            run(plc, 8);
            helper.assertTrue(plc.dataStack().pop().getAsInt() == 7, "received");
            PLCProgram emit = compile(helper, "remit " + target(helper, pos) + " 4\n");
            InstructionPlc emitter = new InstructionPlc(emit, idle());
            run(emitter, 8);
            helper.assertTrue(redstone.getEmittedStrength() == 4, "emitted " + redstone.getEmittedStrength());
            helper.succeed();
        } catch (NeepASM.ProgramBuildException | NeepASM.RuntimeException exception) {
            helper.fail(exception.getMessage());
        }
    }

    private static PLCProgram compile(GameTestHelper helper, String source) throws NeepASM.ProgramBuildException {
        ServerLevel level = helper.getLevel();
        NeepAsmCompilePipeline pipeline = new NeepAsmCompilePipeline(() -> level);
        return pipeline.compile(pipeline.parse(source));
    }

    private static String target(GameTestHelper helper, BlockPos relative) {
        BlockPos pos = helper.absolutePos(relative);
        return "@(" + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " up)";
    }

    private static void run(InstructionPlc plc, int ticks) {
        for (int tick = 0; tick < ticks && plc.error() == null && plc.counter() >= 0; tick++) {
            plc.tick();
        }
    }

    private static PLCActuator idle() {
        return new PLCActuator() {
            @Override
            public BlockPos getBasePos() {
                return BlockPos.ZERO;
            }

            @Override
            public void spawnItem(ItemStack stored) {
            }

            @Override
            public ItemStack getStored(com.neep.neepmeat.api.plc.PLC plc) {
                return ItemStack.EMPTY;
            }

            @Override
            public void setStored(com.neep.neepmeat.api.plc.PLC plc, ItemStack stored) {
            }

            @Override
            public void dumpStored(com.neep.neepmeat.api.plc.PLC plc) {
            }

            @Override
            public boolean reachedTarget(com.neep.neepmeat.api.plc.PLC plc) {
                return true;
            }

            @Override
            public void setTarget(com.neep.neepmeat.api.plc.PLC plc, BlockPos target) {
            }

            @Override
            public double getX() {
                return 0;
            }

            @Override
            public double getY() {
                return 0;
            }

            @Override
            public double getZ() {
                return 0;
            }

            @Override
            public boolean actuatorRemoved() {
                return false;
            }
        };
    }
}
