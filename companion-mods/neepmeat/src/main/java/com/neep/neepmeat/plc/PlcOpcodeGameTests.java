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
import com.neep.neepmeat.neepasm.vm.WorldTargetImpl;
import com.neep.neepmeat.plc.block.entity.BuilderBlockEntity;
import com.neep.neepmeat.plc.block.entity.ExecutorBlockEntity;
import com.neep.neepmeat.plc.block.entity.PLCBlockEntity;
import com.neep.neepmeat.plc.instruction.PushInstruction;
import com.neep.neepmeat.plc.runtime.InstructionPlc;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
    public static void countCobblestoneInAChest(GameTestHelper helper) {
        BlockPos chest = new BlockPos(1, 0, 1);
        helper.setBlock(chest, Blocks.CHEST.defaultBlockState());
        ChestBlockEntity blockEntity = helper.getBlockEntity(chest);
        blockEntity.setItem(0, new ItemStack(Items.COBBLESTONE, 4));
        BlockPos absolute = helper.absolutePos(chest);
        try {
            PLCProgram program = compile(helper, "count @(" + absolute.getX() + " " + absolute.getY() + " " + absolute.getZ() + " north) item \"minecraft:cobblestone\"\n");
            InstructionPlc plc = new InstructionPlc(program, idle());
            run(plc, 8);
            helper.assertTrue(plc.error() == null, "error " + plc.error());
            helper.assertTrue(plc.dataStack().pop().getAsInt() == 4, "counted cobblestone");
            helper.succeed();
        } catch (NeepASM.ProgramBuildException | NeepASM.RuntimeException exception) {
            helper.fail(exception.getMessage());
        }
    }

    @GameTest(template = "empty")
    public static void stockReadsAChestThroughThePipeDriver(GameTestHelper helper) {
        BlockPos chest = new BlockPos(1, 1, 1);
        BlockPos bus = new BlockPos(2, 1, 1);
        BlockPos driver = new BlockPos(3, 1, 1);
        helper.setBlock(chest, Blocks.CHEST.defaultBlockState());
        ChestBlockEntity blockEntity = helper.getBlockEntity(chest);
        blockEntity.setItem(0, new ItemStack(Items.COBBLESTONE, 4));
        helper.setBlock(bus, NeepMachines.STORAGE_BUS.get().defaultBlockState());
        helper.setBlock(driver, NeepMachines.PIPE_DRIVER.get().defaultBlockState());
        com.neep.neepmeat.transport.block.item_transport.entity.PipeDriverBlockEntity pipe = helper.getBlockEntity(driver);
        try {
            PLCProgram program = compile(helper, "stock \"minecraft:cobblestone\"\n");
            InstructionPlc plc = new InstructionPlc(program, pipe);
            run(plc, 8);
            helper.assertTrue(plc.error() == null, "error " + plc.error());
            helper.assertTrue(plc.dataStack().pop().getAsInt() == 4, "stock through the driver");
            helper.succeed();
        } catch (NeepASM.ProgramBuildException | NeepASM.RuntimeException exception) {
            helper.fail(exception.getMessage());
        }
    }

    @GameTest(template = "empty")
    public static void pipeDriverDropsItsStoredItem(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, NeepMachines.PIPE_DRIVER.get().defaultBlockState());
        com.neep.neepmeat.transport.block.item_transport.entity.PipeDriverBlockEntity driver = helper.getBlockEntity(pos);
        driver.spawnItem(new ItemStack(Items.COBBLESTONE, 3));
        net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(helper.absolutePos(pos)).inflate(2);
        int cobble = 0;
        for (net.minecraft.world.entity.item.ItemEntity entity : helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, box)) {
            if (entity.getItem().is(Items.COBBLESTONE)) {
                cobble += entity.getItem().getCount();
            }
        }
        helper.assertTrue(cobble == 3, "dropped " + cobble);
        try {
            PLCProgram program = compile(helper, "push 1\n");
            InstructionPlc plc = new InstructionPlc(program, driver);
            driver.setStored(plc, new ItemStack(Items.DIRT));
            helper.assertTrue(plc.error() != null, "setStored was silent");
        } catch (NeepASM.ProgramBuildException exception) {
            helper.fail(exception.getMessage());
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void filteredPipeAcceptsOnlyTheMatchingItem(GameTestHelper helper) {
        BlockPos from = new BlockPos(1, 1, 1);
        BlockPos pipePos = new BlockPos(2, 1, 1);
        BlockPos chestPos = new BlockPos(3, 1, 1);
        helper.setBlock(chestPos, Blocks.CHEST.defaultBlockState());
        helper.setBlock(pipePos, NeepMachines.FILTER_ITEM_PIPE.get().defaultBlockState());
        com.neep.neepmeat.transport.block.item_transport.entity.ItemPipeBlockEntity pipe = helper.getBlockEntity(pipePos);
        pipe.allow(new ItemStack(Items.COBBLESTONE));
        net.minecraft.core.BlockPos absoluteFrom = helper.absolutePos(from);
        try (com.neep.meatlib.transfer.Transaction transaction = com.neep.meatlib.transfer.Transaction.openOuter()) {
            long moved = com.neep.neepmeat.transport.util.ItemPipeUtil.EjectOperation.of(helper.getLevel(), absoluteFrom, Direction.EAST)
                    .pipeToAny(new com.neep.neepmeat.transport.item_network.ItemInPipe(com.neep.meatlib.transfer.ItemVariant.of(Items.COBBLESTONE), 1), transaction)
                    .amount();
            transaction.onRename();
            helper.assertTrue(moved == 1, "cobble " + moved);
        }
        for (int tick = 0; tick < 12; tick++) {
            pipe.serverTick();
        }
        ChestBlockEntity chest = helper.getBlockEntity(chestPos);
        helper.assertTrue(chest.getItem(0).is(Items.COBBLESTONE) && chest.getItem(0).getCount() == 1, "chest " + chest.getItem(0));
        try (com.neep.meatlib.transfer.Transaction transaction = com.neep.meatlib.transfer.Transaction.openOuter()) {
            long moved = com.neep.neepmeat.transport.util.ItemPipeUtil.EjectOperation.of(helper.getLevel(), absoluteFrom, Direction.EAST)
                    .pipeToAny(new com.neep.neepmeat.transport.item_network.ItemInPipe(com.neep.meatlib.transfer.ItemVariant.of(Items.DIRT), 1), transaction)
                    .amount();
            transaction.onRename();
            helper.assertTrue(moved == 0, "dirt passed " + moved);
        }
        for (int tick = 0; tick < 12; tick++) {
            pipe.serverTick();
        }
        helper.assertTrue(chest.getItem(0).getCount() == 1 && !chest.getItem(1).is(Items.DIRT), "dirt reached " + chest.getItem(1));
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void disableInterruptsClearsTheFlag(GameTestHelper helper) {
        try {
            PLCProgram program = compile(helper, "idisable\n");
            InstructionPlc plc = new InstructionPlc(program, idle());
            helper.assertTrue(plc.getInterrupts().isEnabled(), "interrupts start enabled");
            run(plc, 4);
            helper.assertTrue(plc.error() == null, "error " + plc.error());
            helper.assertTrue(!plc.getInterrupts().isEnabled(), "idisable left interrupts on");
            helper.succeed();
        } catch (NeepASM.ProgramBuildException exception) {
            helper.fail(exception.getMessage());
        }
    }

    @GameTest(template = "empty")
    public static void shiftLeftThenRightRestoresTheValue(GameTestHelper helper) {
        try {
            PLCProgram program = compile(helper, """
                    push 3
                    shl 1
                    shr 1
                    """);
            InstructionPlc plc = new InstructionPlc(program, idle());
            run(plc, 8);
            helper.assertTrue(plc.error() == null, "error " + plc.error());
            helper.assertTrue(plc.dataStack().pop().getAsInt() == 3, "shifted value");
            helper.succeed();
        } catch (NeepASM.ProgramBuildException | NeepASM.RuntimeException exception) {
            helper.fail(exception.getMessage());
        }
    }

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

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void builderPlacesAndBreaks(GameTestHelper helper) {
        BlockPos plcPos = new BlockPos(1, 0, 1);
        BlockPos builderPos = new BlockPos(1, 0, 0);
        BlockPos placeAt = new BlockPos(2, 0, 1);
        helper.setBlock(plcPos, NeepMachines.PLC.get().defaultBlockState());
        helper.setBlock(builderPos, NeepMachines.BUILDER.get().defaultBlockState());
        BuilderBlockEntity builder = helper.getBlockEntity(builderPos);
        builder.inventory().setItem(0, new ItemStack(Items.COBBLESTONE));
        PLCBlockEntity plc = helper.getBlockEntity(plcPos);
        plc.setSource("bplace");
        plc.start();
        try {
            plc.machine().dataStack().push(new WorldTargetImpl(helper.absolutePos(placeAt), Direction.UP));
        } catch (NeepASM.RuntimeException exception) {
            helper.fail(exception.getMessage());
            return;
        }
        for (int tick = 0; tick < 30; tick++) {
            plc.serverTick();
        }
        helper.assertTrue(helper.getBlockState(placeAt).is(Blocks.COBBLESTONE), "bplace did not place cobblestone");

        helper.setBlock(placeAt, Blocks.DIRT.defaultBlockState());
        plc.setSource("bbreak");
        plc.start();
        try {
            plc.machine().dataStack().push(new WorldTargetImpl(helper.absolutePos(placeAt), Direction.UP));
        } catch (NeepASM.RuntimeException exception) {
            helper.fail(exception.getMessage());
            return;
        }
        for (int tick = 0; tick < 30; tick++) {
            plc.serverTick();
        }
        helper.assertTrue(helper.getBlockState(placeAt).isAir(), "bbreak left " + helper.getBlockState(placeAt));
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void executorRunsAPush(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), NeepMachines.EXECUTOR.get().defaultBlockState());
        ExecutorBlockEntity executor = helper.getBlockEntity(new BlockPos(1, 0, 1));
        try {
            executor.receiveInstruction(new PushInstruction(4));
        } catch (NeepASM.RuntimeException exception) {
            helper.fail(exception.getMessage());
            return;
        }
        executor.serverTick();
        try {
            helper.assertTrue(executor.stack().pop().getAsInt() == 4, "executor stack");
        } catch (NeepASM.RuntimeException exception) {
            helper.fail(exception.getMessage());
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void motorPowersTheArm(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), NeepMachines.ROBOTIC_ARM.get().defaultBlockState());
        helper.setBlock(new BlockPos(1, 0, 2), NeepMachines.MOTOR.get().defaultBlockState());
        com.neep.neepmeat.machine.motor.MotorBlockEntity motor = helper.getBlockEntity(new BlockPos(1, 0, 2));
        motor.serverTick();
        RoboticArmBlockEntity arm = helper.getBlockEntity(new BlockPos(1, 0, 1));
        helper.assertTrue(arm.power() == 0, "empty motor powered the arm");
        motor.tank().storeDroplets(com.neep.neepmeat.fluid.EtherealFuel.STILL.get(), 200);
        motor.serverTick();
        helper.assertTrue(Math.abs(arm.power() - 0.4f) < 0.001f, "fuel curve " + arm.power());
        helper.assertTrue(motor.tank().droplets() == 0, "droplets left " + motor.tank().droplets());
        int filled = motor.tank().fill(new net.neoforged.neoforge.fluids.FluidStack(com.neep.neepmeat.fluid.EtherealFuel.STILL.get(), 1000),
                net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(filled == 2 && motor.tank().droplets() == 162, "bucket fill " + filled + " droplets " + motor.tank().droplets());
        helper.succeed();
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
