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

package com.neep.neepmeat.machine.live_machine.process;

import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineBlockEntity;
import com.neep.neepmeat.api.live_machine.Process;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.block.TreeVacuumBlock;
import com.neep.neepmeat.machine.live_machine.block.entity.TreeVacuumBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class TreeVacuumProcess implements Process {
    public static final float MIN_POWER = 0.01f;
    public static final float MAX_POWER = 1f;
    public static final float WORK_MULTIPLIER = 2f;
    public static final TagKey<Block> HARVESTABLE = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "tree_vacuum_harvestable"));
    private static final List<ComponentType<?>> REQUIRED = List.of(
            LivingMachineComponents.TREE_VACUUM,
            LivingMachineComponents.ITEM_OUTPUT,
            LivingMachineComponents.MOTOR_PORT);

    @Override
    public void serverTick(LivingMachineBlockEntity be) {
        be.withComponents(LivingMachineComponents.TREE_VACUUM, LivingMachineComponents.ITEM_OUTPUT, LivingMachineComponents.MOTOR_PORT).ifPresent(result -> {
            TreeVacuumBlockEntity vacuum = (TreeVacuumBlockEntity) result.t1().iterator().next();
            if (be.getProgressIncrement() < MIN_POWER) {
                return;
            }
            float work = WORK_MULTIPLIER * Math.min(be.getProgressIncrement(), MAX_POWER);
            vacuum.progress = Math.min(vacuum.progress + work, TreeVacuumBlockEntity.MAX_PROGRESS);
            if (vacuum.progress < TreeVacuumBlockEntity.MAX_PROGRESS || be.getLevel() == null) {
                return;
            }
            Direction facing = vacuum.getBlockState().getValue(TreeVacuumBlock.FACING);
            BlockPos trunk = vacuum.getBlockPos().relative(facing, 2);
            try (Transaction transaction = Transaction.openOuter()) {
                vacuum.progress = 0;
                vacuum.syncAnimation(traverseTree(be.getLevel(), trunk, 300, 7, be.getCombinedItemOutput(), transaction));
                transaction.commit();
            }
        });
    }

    private boolean traverseTree(Level world, BlockPos origin, int maxVisit, int maxBreak, Storage<ItemVariant> output, TransactionContext transaction) {
        Direction[] directions = {Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.DOWN};
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        if (!isTree(world.getBlockState(origin))) {
            return false;
        }
        visited.add(origin);
        queue.add(origin);
        int visitedCount = 0;
        int broken = 0;
        while (!queue.isEmpty() && visitedCount < maxVisit && broken < maxBreak) {
            BlockPos current = queue.poll();
            boolean foundOther = false;
            for (Direction direction : directions) {
                BlockPos next = current.relative(direction);
                if (!visited.add(next)) {
                    continue;
                }
                if (isTree(world.getBlockState(next))) {
                    foundOther = true;
                    visitedCount++;
                    queue.add(next);
                }
            }
            if (!foundOther || visitedCount >= maxVisit) {
                if (world instanceof ServerLevel server) {
                    for (ItemStack stack : Block.getDrops(world.getBlockState(current), server, current, world.getBlockEntity(current))) {
                        output.insert(ItemVariant.of(stack), stack.getCount(), transaction);
                    }
                }
                world.destroyBlock(current, false);
                broken++;
            }
        }
        return broken > 0;
    }

    private static boolean isTree(BlockState state) {
        Block block = state.getBlock();
        return !(block instanceof TreeVacuumBlock) && (block instanceof LeavesBlock || state.is(HARVESTABLE));
    }

    @Override
    public List<ComponentType<?>> getRequired() {
        return REQUIRED;
    }

    @Override
    public Component getName() {
        return NeepMeat.translationKey("process", "tree_vacuum");
    }
}
