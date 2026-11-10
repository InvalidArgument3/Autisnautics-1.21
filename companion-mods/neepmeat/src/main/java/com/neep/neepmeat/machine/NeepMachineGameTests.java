/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.machine;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.machine.alloy.AlloyKilnBlockEntity;
import com.neep.neepmeat.machine.plc.ProgramBoardBlockEntity;
import com.neep.neepmeat.machine.transport.FluidPipeBlock;
import com.neep.neepmeat.machine.transport.FluidTankBlockEntity;
import com.neep.neepmeat.machine.transport.ItemPipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(NeepMeat.ID)
@PrefixGameTestTemplate(false)
public class NeepMachineGameTests {
    @GameTest(template = "empty", timeoutTicks = 400)
    public static void alloyKilnSmeltsAsbestosAndSand(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, NeepMachines.ALLOY_KILN.get().defaultBlockState());
        AlloyKilnBlockEntity kiln = helper.getBlockEntity(pos);
        ItemStack asbestos = new ItemStack(BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath("neepmeat", "asbestos")).orElseThrow());
        kiln.setItem(AlloyKilnBlockEntity.FUEL, new ItemStack(Items.COAL));
        kiln.setItem(AlloyKilnBlockEntity.INPUT_1, asbestos);
        kiln.setItem(AlloyKilnBlockEntity.INPUT_2, new ItemStack(Items.SAND));
        helper.runAfterDelay(280, () -> {
            helper.assertTrue(kiln.getItem(AlloyKilnBlockEntity.OUTPUT).is(Items.BRICKS), "expected bricks");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void itemPipeMovesDirt(GameTestHelper helper) {
        BlockPos source = new BlockPos(0, 1, 1);
        BlockPos pipe = new BlockPos(1, 1, 1);
        BlockPos destination = new BlockPos(2, 1, 1);
        helper.setBlock(source, Blocks.CHEST.defaultBlockState());
        helper.setBlock(pipe, NeepMachines.ITEM_PIPE.get().defaultBlockState().setValue(ItemPipeBlock.FACING, Direction.EAST));
        helper.setBlock(destination, Blocks.CHEST.defaultBlockState());
        ChestBlockEntity from = helper.getBlockEntity(source);
        from.setItem(0, new ItemStack(Items.DIRT));
        helper.runAfterDelay(40, () -> {
            ChestBlockEntity to = helper.getBlockEntity(destination);
            helper.assertTrue(to.getItem(0).is(Items.DIRT), "dirt did not arrive");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void fluidPipeMovesWater(GameTestHelper helper) {
        BlockPos source = new BlockPos(0, 1, 1);
        BlockPos pipe = new BlockPos(1, 1, 1);
        BlockPos destination = new BlockPos(2, 1, 1);
        helper.setBlock(source, NeepMachines.FLUID_TANK.get().defaultBlockState());
        helper.setBlock(pipe, NeepMachines.FLUID_PIPE.get().defaultBlockState().setValue(FluidPipeBlock.FACING, Direction.EAST));
        helper.setBlock(destination, NeepMachines.FLUID_TANK.get().defaultBlockState());
        FluidTankBlockEntity from = helper.getBlockEntity(source);
        from.tank().fill(new FluidStack(Fluids.WATER, 1000), net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        helper.runAfterDelay(40, () -> {
            FluidTankBlockEntity to = helper.getBlockEntity(destination);
            int moved = to.tank().getFluidAmount();
            helper.assertTrue(moved > 0 && moved <= 1000, "moved " + moved);
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void programBoardKeepsText(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, NeepMachines.PROGRAM_BOARD.get().defaultBlockState());
        ProgramBoardBlockEntity board = helper.getBlockEntity(pos);
        board.setProgram("MOVE 1");
        helper.assertTrue("MOVE 1".equals(board.program()), board.program());
        helper.succeed();
    }
}
