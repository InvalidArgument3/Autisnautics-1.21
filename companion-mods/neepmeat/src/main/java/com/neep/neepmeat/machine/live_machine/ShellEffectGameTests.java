/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat.machine.live_machine;

import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Transaction;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.fluid.BloodFluids;
import com.neep.neepmeat.fluid.CompressedAir;
import com.neep.neepmeat.fluid.MeatFluid;
import com.neep.neepmeat.fluid.WorkFluid;
import com.neep.neepmeat.machine.EffectMachines;
import com.neep.neepmeat.machine.live_machine.block.IntegrationPortBlock;
import com.neep.neepmeat.machine.live_machine.block.entity.FluidInputPortBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.entity.FluidOutputPortBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.entity.IntegrationPortBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.entity.ItemInputPortBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.entity.ItemOutputPortBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.entity.LargestHopperBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.entity.LivingMachineControllerBlockEntity;
import com.neep.neepmeat.machine.phage_ray.PhageRayBlockEntity;
import com.neep.neepmeat.machine.reactor.block.entity.DisruptorPortBlockEntity;
import com.neep.neepmeat.machine.reactor.block.entity.ReactionCoreBlockEntity;
import com.neep.neepmeat.machine.well_head.WellHeadBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(NeepMeat.ID)
@PrefixGameTestTemplate(false)
public class ShellEffectGameTests {
    @GameTest(template = "empty")
    public static void largestHopperSwallowsAnItemEntity(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, LivingMachines.HOPPER.get().defaultBlockState());
        BlockPos absolute = helper.absolutePos(pos);
        ItemEntity entity = new ItemEntity(helper.getLevel(), absolute.getX() + 0.5, absolute.getY() + 0.5, absolute.getZ() + 0.5, new ItemStack(Items.DIRT));
        entity.setOnGround(true);
        helper.getLevel().addFreshEntity(entity);
        LivingMachines.HOPPER.get().collect(helper.getLevel(), absolute, entity);
        LargestHopperBlockEntity hopper = helper.getBlockEntity(pos);
        helper.assertTrue(hopper.inventory().getItem(0).is(Items.DIRT), "dirt was not swallowed");
        helper.assertTrue(entity.isRemoved(), "item entity remained");
        LivingMachines.HOPPER.get().setPlacedBy(helper.getLevel(), absolute, helper.getBlockState(pos), null, ItemStack.EMPTY);
        helper.assertTrue(helper.getBlockState(pos.east()).is(LivingMachines.HOPPER.get().getStructure()), "hopper disc missing");
        net.minecraft.world.entity.animal.Chicken chicken = helper.spawn(net.minecraft.world.entity.EntityType.CHICKEN, pos);
        chicken.setHealth(1f);
        java.util.List<ItemStack> loot = new java.util.ArrayList<>();
        hopper.strike(1f, loot);
        helper.assertTrue(!chicken.isAlive() || chicken.getHealth() < 1f, "chicken was not hurt");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void integrationPortTakesAChargeOfWorkFluid(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, LivingMachines.INTEGRATION.get().defaultBlockState());
        IntegrationPortBlockEntity port = helper.getBlockEntity(pos);
        ItemStack stack = new ItemStack(Items.PAPER, 2);
        helper.assertTrue(port.apply(null, InteractionHand.MAIN_HAND, stack), "charge was refused");
        helper.assertTrue(((com.neep.meatlib.transfer.StorageView<FluidVariant>) port.getFluidStorage(null)).getAmount() == FluidConstants.INGOT,
                "work fluid " + ((com.neep.meatlib.transfer.StorageView<FluidVariant>) port.getFluidStorage(null)).getAmount());
        helper.assertTrue(stack.getCount() == 1, "stack was not consumed");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void integrationPortClosesWhenTheMachineHasPower(GameTestHelper helper) {
        BlockPos controller = new BlockPos(1, 2, 1);
        BlockPos motor = new BlockPos(2, 2, 1);
        BlockPos port = new BlockPos(3, 2, 1);
        helper.setBlock(controller, LivingMachines.CONTROLLER.get().defaultBlockState());
        helper.setBlock(motor, LivingMachines.MOTOR_PORT.get().defaultBlockState());
        helper.setBlock(port, LivingMachines.INTEGRATION.get().defaultBlockState());
        com.neep.neepmeat.machine.live_machine.block.entity.MotorPortBlockEntity motorEntity = helper.getBlockEntity(motor);
        motorEntity.setInputPower(1f);
        LivingMachineControllerBlockEntity machine = helper.getBlockEntity(controller);
        machine.serverTick();
        helper.assertTrue(!helper.getBlockState(port).getValue(IntegrationPortBlock.OPEN), "port stayed open");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void treeVacuumBreaksTheLogItFaces(GameTestHelper helper) {
        BlockPos controller = new BlockPos(1, 2, 2);
        BlockPos motor = new BlockPos(2, 2, 2);
        BlockPos vacuum = new BlockPos(3, 2, 2);
        BlockPos output = new BlockPos(4, 2, 2);
        helper.setBlock(controller, LivingMachines.CONTROLLER.get().defaultBlockState());
        helper.setBlock(motor, LivingMachines.MOTOR_PORT.get().defaultBlockState());
        helper.setBlock(vacuum, LivingMachines.TREE_VACUUM.get().defaultBlockState());
        helper.setBlock(output, LivingMachines.ITEM_OUTPUT.get().defaultBlockState());
        helper.setBlock(vacuum.north(2), Blocks.OAK_LOG.defaultBlockState());
        com.neep.neepmeat.machine.live_machine.block.entity.MotorPortBlockEntity motorEntity = helper.getBlockEntity(motor);
        motorEntity.setInputPower(1f);
        LivingMachineControllerBlockEntity machine = helper.getBlockEntity(controller);
        for (int i = 0; i < 8; i++) {
            machine.serverTick();
        }
        helper.assertTrue(helper.getBlockState(vacuum.north(2)).isAir(), "log remained");
        ItemOutputPortBlockEntity out = helper.getBlockEntity(output);
        helper.assertTrue(contains(out, Items.OAK_LOG), "log was not collected");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void bioreactorTurnsBloodAndMeatIntoWorkFluid(GameTestHelper helper) {
        BlockPos stomach = new BlockPos(5, 2, 5);
        BlockPos controller = new BlockPos(5, 4, 5);
        helper.setBlock(stomach, LivingMachines.STOMACH.get().defaultBlockState());
        helper.setBlock(controller, LivingMachines.CONTROLLER.get().defaultBlockState());
        helper.setBlock(new BlockPos(6, 4, 5), LivingMachines.MOTOR_PORT.get().defaultBlockState());
        helper.setBlock(new BlockPos(7, 4, 5), LivingMachines.FLUID_INPUT.get().defaultBlockState());
        helper.setBlock(new BlockPos(8, 4, 5), LivingMachines.FLUID_INPUT.get().defaultBlockState());
        helper.setBlock(new BlockPos(9, 4, 5), LivingMachines.FLUID_INPUT.get().defaultBlockState());
        helper.setBlock(new BlockPos(10, 4, 5), LivingMachines.FLUID_OUTPUT.get().defaultBlockState());
        com.neep.neepmeat.machine.live_machine.block.entity.MotorPortBlockEntity motor = helper.getBlockEntity(new BlockPos(6, 4, 5));
        motor.setInputPower(1f);
        fill(helper, new BlockPos(7, 4, 5), BloodFluids.BLOOD.get(), 1000);
        fill(helper, new BlockPos(8, 4, 5), MeatFluid.STILL.get(), 1000);
        fill(helper, new BlockPos(9, 4, 5), WorkFluid.STILL.get(), 1000);
        LivingMachineControllerBlockEntity machine = helper.getBlockEntity(controller);
        for (int i = 0; i < 20; i++) {
            machine.serverTick();
        }
        FluidOutputPortBlockEntity output = helper.getBlockEntity(new BlockPos(10, 4, 5));
        helper.assertTrue(amount(output.getStorage(null)) == 3000, "work fluid " + amount(output.getStorage(null)));
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void digesterRendersRawIronIntoMeat(GameTestHelper helper) {
        BlockPos stomach = new BlockPos(5, 2, 5);
        BlockPos controller = new BlockPos(5, 4, 5);
        helper.setBlock(stomach, LivingMachines.STOMACH.get().defaultBlockState());
        helper.setBlock(controller, LivingMachines.CONTROLLER.get().defaultBlockState());
        helper.setBlock(new BlockPos(6, 4, 5), LivingMachines.ITEM_INPUT.get().defaultBlockState());
        helper.setBlock(new BlockPos(7, 4, 5), LivingMachines.FLUID_INPUT.get().defaultBlockState());
        helper.setBlock(new BlockPos(8, 4, 5), LivingMachines.FLUID_OUTPUT.get().defaultBlockState());
        ItemInputPortBlockEntity input = helper.getBlockEntity(new BlockPos(6, 4, 5));
        input.getInventory().setItem(0, new ItemStack(Items.RAW_IRON));
        fill(helper, new BlockPos(7, 4, 5), BloodFluids.BLOOD.get(), FluidConstants.INGOT);
        helper.runAfterDelay(20, () -> {
            FluidOutputPortBlockEntity output = helper.getBlockEntity(new BlockPos(8, 4, 5));
            helper.assertTrue(amount(output.getStorage(null)) == FluidConstants.INGOT, "meat " + amount(output.getStorage(null)));
            helper.assertTrue(input.getInventory().getItem(0).isEmpty(), "raw iron remained");
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void charnelPumpMovesWorkFluidIntoTheWellHead(GameTestHelper helper) {
        BlockPos controller = new BlockPos(1, 2, 2);
        BlockPos pump = new BlockPos(3, 2, 2);
        helper.setBlock(controller, LivingMachines.CONTROLLER.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 2, 2), LivingMachines.MOTOR_PORT.get().defaultBlockState());
        helper.setBlock(pump, EffectMachines.CHARNEL_PUMP.get().defaultBlockState());
        helper.setBlock(new BlockPos(4, 2, 2), LivingMachines.FLUID_INPUT.get().defaultBlockState());
        helper.setBlock(new BlockPos(5, 2, 2), LivingMachines.FLUID_INPUT.get().defaultBlockState());
        helper.setBlock(pump.north(), EffectMachines.WELL_HEAD.get().defaultBlockState());
        com.neep.neepmeat.machine.live_machine.block.entity.MotorPortBlockEntity motor = helper.getBlockEntity(new BlockPos(2, 2, 2));
        motor.setInputPower(1f);
        fill(helper, new BlockPos(4, 2, 2), CompressedAir.STILL.get(), 1000);
        fill(helper, new BlockPos(5, 2, 2), WorkFluid.STILL.get(), FluidConstants.BOTTLE);
        LivingMachineControllerBlockEntity machine = helper.getBlockEntity(controller);
        machine.serverTick();
        WellHeadBlockEntity well = helper.getBlockEntity(pump.north());
        helper.assertTrue(well.received() == FluidConstants.BOTTLE, "received " + well.received());
        FluidInputPortBlockEntity air = helper.getBlockEntity(new BlockPos(4, 2, 2));
        helper.assertTrue(amount(air.getStorage(null)) < 1000, "air was not consumed");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void phageRayHarvestsTheBlockItPointsAt(GameTestHelper helper) {
        BlockPos controller = new BlockPos(1, 2, 1);
        BlockPos rayPos = new BlockPos(2, 2, 1);
        BlockPos stone = rayPos.south(2);
        helper.setBlock(controller, LivingMachines.CONTROLLER.get().defaultBlockState());
        helper.setBlock(rayPos, EffectMachines.PHAGE_RAY.get().defaultBlockState());
        helper.setBlock(new BlockPos(3, 2, 1), LivingMachines.EXTRACTOR.get().defaultBlockState());
        helper.setBlock(new BlockPos(4, 2, 1), LivingMachines.ITEM_OUTPUT.get().defaultBlockState());
        helper.setBlock(stone, Blocks.COBBLESTONE.defaultBlockState());
        PhageRayBlockEntity ray = helper.getBlockEntity(rayPos);
        ray.setPower(1f);
        ray.setAim(0f, 0f, 6, true);
        helper.runAfterDelay(40, () -> {
            ItemOutputPortBlockEntity output = helper.getBlockEntity(new BlockPos(4, 2, 1));
            helper.assertTrue(helper.getBlockState(stone).isAir(), "cobblestone remained");
            helper.assertTrue(contains(output, Items.COBBLESTONE), "no cobblestone drop");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void disruptorLowersCoreOrganisation(GameTestHelper helper) {
        BlockPos controller = new BlockPos(1, 2, 1);
        BlockPos corePos = new BlockPos(5, 2, 1);
        helper.setBlock(controller, LivingMachines.CONTROLLER.get().defaultBlockState());
        helper.setBlock(new BlockPos(1, 3, 1), LivingMachines.MOTOR_PORT.get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 2, 1), EffectMachines.DISRUPTOR_PORT.get().defaultBlockState());
        helper.setBlock(new BlockPos(3, 2, 1), EffectMachines.DISRUPTOR_SEGMENT.get().defaultBlockState());
        helper.setBlock(new BlockPos(4, 2, 1), EffectMachines.DISRUPTOR_NEEDLE.get().defaultBlockState());
        helper.setBlock(corePos, EffectMachines.REACTION_CORE.get().defaultBlockState());
        com.neep.neepmeat.machine.live_machine.block.entity.MotorPortBlockEntity motor = helper.getBlockEntity(new BlockPos(1, 3, 1));
        motor.setInputPower(1f);
        DisruptorPortBlockEntity port = helper.getBlockEntity(new BlockPos(2, 2, 1));
        port.setDisruption(10);
        ReactionCoreBlockEntity core = helper.getBlockEntity(corePos);
        core.setOrganisation(100, 100);
        helper.runAfterDelay(30, () -> {
            helper.assertTrue(core.organisation() < 100, "organisation " + core.organisation());
            helper.succeed();
        });
    }

    private static long amount(com.neep.meatlib.transfer.Storage<FluidVariant> storage) {
        return ((com.neep.meatlib.transfer.StorageView<FluidVariant>) storage).getAmount();
    }

    private static void fill(GameTestHelper helper, BlockPos pos, Fluid fluid, long amount) {
        FluidInputPortBlockEntity port = helper.getBlockEntity(pos);
        try (Transaction transaction = Transaction.openOuter()) {
            port.getStorage(null).insert(FluidVariant.of(fluid), amount, transaction);
            transaction.commit();
        }
    }

    private static boolean contains(ItemOutputPortBlockEntity output, net.minecraft.world.item.Item item) {
        for (int slot = 0; slot < output.getInventory().getContainerSize(); slot++) {
            if (output.getInventory().getItem(slot).is(item)) {
                return true;
            }
        }
        return false;
    }
}
