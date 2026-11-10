/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.machine;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.machine.transport.FluidPipeBlockEntity;
import com.neep.neepmeat.machine.transport.FluidTankBlockEntity;
import com.neep.neepmeat.machine.transport.ItemPipeBlockEntity;
import com.neep.neepmeat.machine.transport.DeferredRequest;
import com.neep.neepmeat.machine.transport.ItemRequesterBlockEntity;
import com.neep.neepmeat.machine.transport.NetworkCrafterBlockEntity;
import com.neep.neepmeat.machine.transport.PipeDriverBlockEntity;
import com.neep.neepmeat.machine.transport.RoutingNetwork;
import com.neep.neepmeat.machine.transport.MergePipeBlock;
import com.neep.neepmeat.machine.transport.PipeRouteFinder;
import com.neep.neepmeat.machine.transport.PumpBlockEntity;
import com.neep.neepmeat.transport.ItemRoute;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(NeepMeat.ID)
@PrefixGameTestTemplate(false)
public class PipeGraphGameTests {
    @GameTest(template = "empty")
    public static void junctionsLinkAcrossAStraightPipe(GameTestHelper helper) {
        helper.setBlock(new BlockPos(0, 1, 0), NeepMachines.ITEM_PIPE.get().defaultBlockState());
        helper.setBlock(new BlockPos(0, 1, 1), NeepMachines.ITEM_PIPE.get().defaultBlockState());
        helper.setBlock(new BlockPos(0, 1, 2), NeepMachines.ITEM_PIPE.get().defaultBlockState());
        helper.setBlock(new BlockPos(1, 1, 1), NeepMachines.ITEM_PIPE.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 1, 0), NeepMachines.ITEM_PIPE.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 1, 1), NeepMachines.ITEM_PIPE.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 1, 2), NeepMachines.ITEM_PIPE.get().defaultBlockState());
        helper.runAfterDelay(1, () -> {
            ItemPipeBlockEntity left = helper.getBlockEntity(new BlockPos(0, 1, 1));
            ItemPipeBlockEntity right = helper.getBlockEntity(new BlockPos(2, 1, 1));
            left.rebuildGraph(helper.getLevel());
            right.rebuildGraph(helper.getLevel());
            helper.assertTrue(left.vertex().adjacent(Direction.EAST) == right.vertex(), "junctions did not link");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void filterPipeRejectsOtherItems(GameTestHelper helper) {
        BlockPos filter = new BlockPos(1, 1, 1);
        helper.setBlock(new BlockPos(2, 1, 1), Blocks.CHEST.defaultBlockState());
        helper.setBlock(filter, NeepMachines.FILTER_ITEM_PIPE.get().defaultBlockState());
        ItemPipeBlockEntity pipe = helper.getBlockEntity(filter);
        pipe.allow(new ItemStack(Items.STONE));
        helper.assertTrue(pipe.insert(new ItemStack(Items.DIRT), Direction.WEST) == 0, "dirt passed the filter");
        helper.assertTrue(pipe.insert(new ItemStack(Items.STONE), Direction.WEST) == 1, "stone was refused");
        helper.runAfterDelay(15, () -> {
            ChestBlockEntity chest = helper.getBlockEntity(new BlockPos(2, 1, 1));
            helper.assertTrue(chest.getItem(0).is(Items.STONE), "stone did not arrive");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void mergePipeOutputsOnFacing(GameTestHelper helper) {
        BlockPos merge = new BlockPos(1, 1, 1);
        helper.setBlock(new BlockPos(2, 1, 1), Blocks.CHEST.defaultBlockState());
        helper.setBlock(new BlockPos(1, 1, 2), Blocks.CHEST.defaultBlockState());
        helper.setBlock(merge, NeepMachines.MERGE_PIPE.get().defaultBlockState().setValue(MergePipeBlock.FACING, Direction.EAST));
        ItemPipeBlockEntity pipe = helper.getBlockEntity(merge);
        helper.assertTrue(pipe.insert(new ItemStack(Items.DIRT), Direction.WEST) == 1, "merge refused the item");
        helper.runAfterDelay(15, () -> {
            ChestBlockEntity east = helper.getBlockEntity(new BlockPos(2, 1, 1));
            ChestBlockEntity south = helper.getBlockEntity(new BlockPos(1, 1, 2));
            helper.assertTrue(east.getItem(0).is(Items.DIRT), "item missed the facing output");
            helper.assertTrue(south.isEmpty(), "item left on a side");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void requesterPullsThroughStorageBus(GameTestHelper helper) {
        helper.setBlock(new BlockPos(0, 1, 1), Blocks.CHEST.defaultBlockState());
        helper.setBlock(new BlockPos(1, 1, 1), NeepMachines.STORAGE_BUS.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 1, 1), NeepMachines.ITEM_PIPE.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 1, 2), NeepMachines.ITEM_REQUESTER.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 1, 0), NeepMachines.PIPE_DRIVER.get().defaultBlockState());
        ChestBlockEntity source = helper.getBlockEntity(new BlockPos(0, 1, 1));
        source.setItem(0, new ItemStack(Items.DIRT));
        ItemRequesterBlockEntity requester = helper.getBlockEntity(new BlockPos(2, 1, 2));
        requester.request(new ItemStack(Items.DIRT));
        helper.runAfterDelay(40, () -> {
            helper.assertTrue(requester.output().is(Items.DIRT), "request did not arrive");
            PipeDriverBlockEntity driver = helper.getBlockEntity(new BlockPos(2, 1, 0));
            helper.assertTrue(driver.stateFor(helper.absolutePos(new BlockPos(2, 1, 2))) == DeferredRequest.State.SUCCESS, "deferred callback");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 120)
    public static void crafterRecursesForIngredients(GameTestHelper helper) {
        helper.setBlock(new BlockPos(0, 1, 1), Blocks.CHEST.defaultBlockState());
        helper.setBlock(new BlockPos(1, 1, 1), NeepMachines.STORAGE_BUS.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 1, 1), NeepMachines.ITEM_PIPE.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 1, 0), NeepMachines.NETWORK_CRAFTER.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 1, 2), NeepMachines.ITEM_REQUESTER.get().defaultBlockState());
        helper.setBlock(new BlockPos(1, 1, 0), NeepMachines.PIPE_DRIVER.get().defaultBlockState());
        ChestBlockEntity source = helper.getBlockEntity(new BlockPos(0, 1, 1));
        source.setItem(0, new ItemStack(Items.COBBLESTONE));
        NetworkCrafterBlockEntity crafter = helper.getBlockEntity(new BlockPos(2, 1, 0));
        crafter.setRecipe(Items.STONE, List.of(Items.COBBLESTONE));
        ItemRequesterBlockEntity requester = helper.getBlockEntity(new BlockPos(2, 1, 2));
        requester.request(new ItemStack(Items.STONE));
        helper.runAfterDelay(80, () -> {
            helper.assertTrue(requester.output().is(Items.STONE), "crafted stone did not arrive");
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void craftingRecursionStopsAtThirty(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), NeepMachines.ITEM_PIPE.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 1, 1), NeepMachines.NETWORK_CRAFTER.get().defaultBlockState());
        helper.setBlock(new BlockPos(0, 1, 1), NeepMachines.PIPE_DRIVER.get().defaultBlockState());
        NetworkCrafterBlockEntity crafter = helper.getBlockEntity(new BlockPos(2, 1, 1));
        crafter.setRecipe(Items.STONE, List.of(Items.STONE));
        boolean started = RoutingNetwork.request(helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 1)), new ItemStack(Items.STONE));
        helper.assertTrue(!started, "recursion did not stop");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void routeSearchPrefersFilterPipes(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), NeepMachines.ITEM_PIPE.get().defaultBlockState());
        helper.setBlock(new BlockPos(1, 1, 2), NeepMachines.FILTER_ITEM_PIPE.get().defaultBlockState());
        helper.setBlock(new BlockPos(0, 1, 2), Blocks.CHEST.defaultBlockState());
        helper.setBlock(new BlockPos(2, 1, 1), NeepMachines.ITEM_PIPE.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 1, 2), Blocks.CHEST.defaultBlockState());
        ItemRoute route = PipeRouteFinder.find(helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 1)), Direction.NORTH, new ItemStack(Items.DIRT));
        helper.assertFalse(route.isEmpty(), "no route");
        helper.assertTrue(route.get(0) == Direction.SOUTH, "first step " + route.get(0));
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void filterTagAndInvertedEntries(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, NeepMachines.FILTER_ITEM_PIPE.get().defaultBlockState());
        ItemPipeBlockEntity pipe = helper.getBlockEntity(pos);
        pipe.allowTag(net.minecraft.tags.ItemTags.PLANKS, false);
        helper.assertTrue(pipe.allows(new ItemStack(Items.OAK_PLANKS)), "planks refused");
        helper.assertTrue(!pipe.allows(new ItemStack(Items.DIRT)), "dirt matched planks");
        pipe = helper.getBlockEntity(pos);
        pipe.setEntry(0, com.neep.neepmeat.machine.transport.FilterEntry.tag(net.minecraft.tags.ItemTags.PLANKS, true));
        helper.assertTrue(!pipe.allows(new ItemStack(Items.OAK_PLANKS)), "inverted tag allowed planks");
        helper.assertTrue(pipe.allows(new ItemStack(Items.DIRT)), "inverted tag refused dirt");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void pumpMovesOneThroughput(GameTestHelper helper) {
        helper.setBlock(new BlockPos(0, 1, 1), NeepMachines.FLUID_TANK.get().defaultBlockState());
        helper.setBlock(new BlockPos(1, 1, 1), NeepMachines.PUMP.get().defaultBlockState().setValue(com.neep.neepmeat.machine.transport.PumpBlock.FACING, Direction.EAST));
        helper.setBlock(new BlockPos(2, 1, 1), NeepMachines.FLUID_TANK.get().defaultBlockState());
        FluidTankBlockEntity source = helper.getBlockEntity(new BlockPos(0, 1, 1));
        source.tank().fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        PumpBlockEntity pump = helper.getBlockEntity(new BlockPos(1, 1, 1));
        pump.changeMode();
        helper.runAfterDelay(2, () -> {
            FluidTankBlockEntity destination = helper.getBlockEntity(new BlockPos(2, 1, 1));
            helper.assertTrue(destination.tank().getFluidAmount() == FluidPipeBlockEntity.THROUGHPUT_MB,
                    "pumped " + destination.tank().getFluidAmount());
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void colouredPipesDoNotCross(GameTestHelper helper) {
        helper.setBlock(new BlockPos(0, 1, 1), NeepMachines.FLUID_TANK.get().defaultBlockState());
        helper.setBlock(new BlockPos(1, 1, 1), NeepMachines.COLORED_FLUID_PIPES.get(DyeColor.RED).get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 1, 1), NeepMachines.COLORED_FLUID_PIPES.get(DyeColor.BLUE).get().defaultBlockState());
        FluidPipeBlockEntity red = helper.getBlockEntity(new BlockPos(1, 1, 1));
        int taken = red.insert(new FluidStack(Fluids.WATER, 1000), Direction.WEST);
        helper.assertTrue(taken == 0, "red pipe accepted fluid headed into a blue pipe");
        helper.setBlock(new BlockPos(2, 1, 1), NeepMachines.COLORED_FLUID_PIPES.get(DyeColor.RED).get().defaultBlockState());
        taken = red.insert(new FluidStack(Fluids.WATER, 1000), Direction.WEST);
        helper.assertTrue(taken == FluidPipeBlockEntity.THROUGHPUT_MB, "matching colour refused fluid");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void idlePumpKeepsItsBufferUntilActive(GameTestHelper helper) {
        helper.setBlock(new BlockPos(0, 1, 1), NeepMachines.FLUID_TANK.get().defaultBlockState());
        helper.setBlock(new BlockPos(1, 1, 1), NeepMachines.PUMP.get().defaultBlockState().setValue(com.neep.neepmeat.machine.transport.PumpBlock.FACING, Direction.EAST));
        FluidTankBlockEntity source = helper.getBlockEntity(new BlockPos(0, 1, 1));
        source.tank().fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        PumpBlockEntity pump = helper.getBlockEntity(new BlockPos(1, 1, 1));
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(pump.bufferAmount() == 0, "idle pump moved fluid");
            pump.changeMode();
            helper.runAfterDelay(2, () -> {
                helper.assertTrue(pump.bufferAmount() == FluidPipeBlockEntity.THROUGHPUT_MB, "buffer " + pump.bufferAmount());
                helper.succeed();
            });
        });
    }
}
