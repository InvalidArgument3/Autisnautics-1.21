/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.machine;

import com.neep.neepmeat.NeepMeat;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.machine.alloy.AlloyKilnBlockEntity;
import com.neep.neepmeat.plc.arm.RoboticArmBlockEntity;
import com.neep.neepmeat.machine.transport.FluidPipeBlockEntity;
import com.neep.neepmeat.machine.transport.FluidTankBlockEntity;
import com.neep.neepmeat.machine.transport.ItemPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
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

    @GameTest(template = "empty")
    public static void upstreamAlloyRecipesLoad(GameTestHelper helper) {
        int alloy = helper.getLevel().getRecipeManager().getAllRecipesFor(NeepMachines.ALLOY_TYPE.get()).size();
        helper.assertTrue(alloy >= 3, "alloy recipes " + alloy);
        boolean kilnCraft = helper.getLevel().getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING).stream()
                .anyMatch(holder -> holder.value().getResultItem(helper.getLevel().registryAccess()).is(NeepMachines.ALLOY_KILN_ITEM.get()));
        helper.assertTrue(kilnCraft, "alloy kiln crafting recipe");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void itemPipeCarriesDirt(GameTestHelper helper) {
        BlockPos pipe = new BlockPos(1, 1, 1);
        BlockPos destination = new BlockPos(2, 1, 1);
        helper.setBlock(new BlockPos(0, 1, 1), Blocks.CHEST.defaultBlockState());
        helper.setBlock(destination, Blocks.CHEST.defaultBlockState());
        helper.setBlock(pipe, NeepMachines.ITEM_PIPE.get().defaultBlockState());
        ItemPipeBlockEntity pipeEntity = helper.getBlockEntity(pipe);
        int taken = pipeEntity.insert(new ItemStack(Items.DIRT), Direction.WEST);
        helper.assertTrue(taken == 1, "taken " + taken);
        helper.runAfterDelay(20, () -> {
            ChestBlockEntity to = helper.getBlockEntity(destination);
            helper.assertTrue(to.getItem(0).is(Items.DIRT), "dirt did not arrive");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void fluidPipeMovesOneThroughput(GameTestHelper helper) {
        BlockPos pipe = new BlockPos(1, 1, 1);
        BlockPos destination = new BlockPos(2, 1, 1);
        helper.setBlock(new BlockPos(0, 1, 1), NeepMachines.FLUID_TANK.get().defaultBlockState());
        helper.setBlock(destination, NeepMachines.FLUID_TANK.get().defaultBlockState());
        helper.setBlock(pipe, NeepMachines.FLUID_PIPE.get().defaultBlockState());
        FluidPipeBlockEntity pipeEntity = helper.getBlockEntity(pipe);
        int taken = pipeEntity.insert(new FluidStack(Fluids.WATER, 1000), Direction.WEST);
        helper.assertTrue(taken == FluidPipeBlockEntity.THROUGHPUT_MB, "taken " + taken);
        helper.runAfterDelay(5, () -> {
            FluidTankBlockEntity to = helper.getBlockEntity(destination);
            helper.assertTrue(to.tank().getFluidAmount() == FluidPipeBlockEntity.THROUGHPUT_MB,
                    "moved " + to.tank().getFluidAmount());
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void roboticArmMovesTowardTargetWhenPowered(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 0, 1);
        helper.setBlock(pos, NeepMachines.ROBOTIC_ARM.get().defaultBlockState());
        RoboticArmBlockEntity arm = helper.getBlockEntity(pos);
        boolean refused = false;
        try {
            arm.setTarget(null, helper.absolutePos(new BlockPos(1, 1, 1)));
        } catch (NeepASM.RuntimeException exception) {
            refused = true;
        }
        helper.assertTrue(refused, "unpowered arm accepted a target");
        arm.setInputPower(1.0F);
        BlockPos target = helper.absolutePos(new BlockPos(1, 1, 2));
        try {
            arm.setTarget(null, target);
        } catch (NeepASM.RuntimeException exception) {
            helper.fail("powered arm refused a target: " + exception.getMessage());
        }
        double before = arm.getX();
        for (int tick = 0; tick < 8; tick++) {
            arm.serverTick();
        }
        helper.assertTrue(arm.getX() != before || arm.reachedTarget(null), "tip did not move");
        double left = arm.getX() - (target.getX() + 0.5);
        double depth = arm.getZ() - (target.getZ() + 0.5);
        helper.assertTrue(left * left + depth * depth < 1.0, "tip stayed away from the target");
        arm.setStored(null, new ItemStack(Items.COBBLESTONE, 3));
        var saved = arm.saveWithoutMetadata(helper.getLevel().registryAccess());
        RoboticArmBlockEntity loaded = new RoboticArmBlockEntity(helper.absolutePos(pos), arm.getBlockState());
        loaded.loadWithComponents(saved, helper.getLevel().registryAccess());
        helper.assertTrue(loaded.getStored(null).is(Items.COBBLESTONE) && loaded.getStored(null).getCount() == 3,
                "stored stack did not survive NBT");
        helper.succeed();
    }
}
