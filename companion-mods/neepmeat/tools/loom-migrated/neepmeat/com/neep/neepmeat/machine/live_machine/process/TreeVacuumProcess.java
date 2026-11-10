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

import com.neep.meatlib.MeatLib;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineBlockEntity;
import com.neep.neepmeat.api.live_machine.Process;
import com.neep.neepmeat.client.screen.tablet.graph.GraphData;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.block.TreeVacuumBlock;
import com.neep.neepmeat.machine.live_machine.block.entity.TreeVacuumBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;

import java.text.DecimalFormat;
import java.util.*;

public class TreeVacuumProcess implements Process
{
    public static final float MIN_POWER = 0.01f;
    public static final float MAX_POWER = 1;
    public static final float WORK_MULTIPLIER = 2;

    private static final List<ComponentType<?>> REQUIRED = List.of(
            LivingMachineComponents.TREE_VACUUM,
            LivingMachineComponents.ITEM_OUTPUT,
            LivingMachineComponents.MOTOR_PORT
    );

    @Override
    public void serverTick(LivingMachineBlockEntity be)
    {
        be.withComponents(LivingMachineComponents.TREE_VACUUM, LivingMachineComponents.ITEM_OUTPUT, LivingMachineComponents.MOTOR_PORT).ifPresent(result ->
        {
            var vacuums = result.t1();

            TreeVacuumBlockEntity vacuum = vacuums.iterator().next();
            Level world = be.getLevel();

            if (be.getProgressIncrement() < MIN_POWER)
                return;

            float work = WORK_MULTIPLIER * Math.min(be.getProgressIncrement(), MAX_POWER);

            vacuum.progress = Math.min(vacuum.progress + work, TreeVacuumBlockEntity.maxProgress);

            if (vacuum.progress >= TreeVacuumBlockEntity.maxProgress)
            {
                Direction facing = vacuum.getBlockState().getValue(TreeVacuumBlock.FACING);
                BlockPos trunkPos = vacuum.getBlockPos().relative(facing, 2);

                if (MeatLib.vsUtil != null && MeatLib.vsUtil.hasShipAtPosition(be.getBlockPos(), world))
                {
                    Vector3d temp = new Vector3d(0,0,0);
                    MeatLib.vsUtil.getShipToWorldMatrix(trunkPos, world).transformPosition(trunkPos.getX() + 0.5, trunkPos.getY() + 0.5, trunkPos.getZ() + 0.5, temp);
                    trunkPos = new BlockPos(Mth.floor(temp.x), Mth.floor(temp.y), Mth.floor(temp.z));
                }

                try (Transaction transaction = Transaction.openOuter())
                {
                    vacuum.progress = 0;
                    boolean broken = traverseTree(world, trunkPos, 300, 7, be.getCombinedItemOutput(), transaction);
                    vacuum.syncAnimation(broken);
                    transaction.commit();
                }
            }
        });
    }

    private boolean traverseTree(Level world, BlockPos origin, int maxVisit, int maxBreak, Storage<ItemVariant> output, TransactionContext transaction)
    {
        Direction[] directions = new Direction[]{Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.DOWN};

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();

        if (!isTree(world.getBlockState(origin)))
            return false;

        visited.add(origin);
        queue.add(origin);

        int treeBlocksVisited = 0;
        int treeBlocksBroken = 0;
        while (!queue.isEmpty()
                && treeBlocksVisited < maxVisit
                && treeBlocksBroken < maxBreak
        )
        {
            BlockPos current = queue.poll();

            BlockPos.MutableBlockPos mutable = current.mutable();
            boolean foundOther = false;
            for (Direction direction : directions)
            {
                mutable.setWithOffset(current, direction);
                if (!visited.contains(mutable))
                {
                    visited.add(mutable.immutable());

                    BlockState offsetState = world.getBlockState(mutable);
                    if (isTree(offsetState))
                    {
                        foundOther = true;
                        treeBlocksVisited++;
                        queue.add(mutable.immutable());
                    }
                }
            }

            if (!foundOther || treeBlocksVisited >= maxVisit)
            {
                List<ItemStack> dropped = Block.getDrops(world.getBlockState(current), (ServerLevel) world, current, world.getBlockEntity(current));
                for (var stack : dropped)
                {
                    output.insert(ItemVariant.of(stack), stack.getCount(), transaction);
                }

                world.destroyBlock(current, false);
                treeBlocksBroken++;
            }
        }
        return treeBlocksBroken > 0;
    }

    private static boolean isTree(BlockState state)
    {
        Block block = state.getBlock();
        return
                !(block instanceof TreeVacuumBlock)
                && !(block instanceof TreeVacuumBlock.Structure)
                && (block instanceof LeavesBlock
                    || state.is(NMTags.TREE_VACUUM_HARVESTABLE)
                );
    }

    @Override
    public List<ComponentType<?>> getRequired()
    {
        return REQUIRED;
    }

    @Override
    public Component getName()
    {
        return NeepMeat.translationKey("process", "tree_vacuum");
    }

    @Override
    public List<Component> getProblems(LivingMachineBlockEntity be)
    {
        if (be.getProgressIncrement() < MIN_POWER)
            return List.of(Component.literal("Power < %s".formatted(MIN_POWER)));

        return Process.super.getProblems(be);
    }

    public static GraphData createGraph()
    {
        // Minimum and saturation power
        float minAbs = MIN_POWER * 1000;
        float maxAbs = 1 * 1000;

        // Time taken to reach MAX_COOLDOWN
        float time0 = TreeVacuumBlockEntity.maxProgress / (WORK_MULTIPLIER * minAbs / 1000) / 20;
        float time1 = TreeVacuumBlockEntity.maxProgress / (WORK_MULTIPLIER * maxAbs / 1000) / 20;

        float x0 = 0;
        float x1 = minAbs;
        float x2 = maxAbs;
        float x3 = maxAbs * 1.5f;
        float yEnd = 1.1f * time0;

        DecimalFormat df = new DecimalFormat("###.#");

        return new GraphData()
                .semiLogX(true)
                .setTitle("Time between extensions")
                .setXRange(new GraphData.Range(x0, x3))
                .setYRange(new GraphData.Range(0, yEnd))
                .setXTicks(List.of(
                        new GraphData.Tick(0, false),
                        new GraphData.Tick(x1, true, df.format(minAbs)),
                        new GraphData.Tick(x2, true, df.format(maxAbs))))
                .setYTicks(List.of(
                        new GraphData.Tick(time0, false, df.format(time0)),
                        new GraphData.Tick(time1, false, df.format(time1))
                ))
                .setXTitle("Input power", "ej/t")
                .setYTitle("Time", "s")
                .setPoints(GraphData.join(
                        new float[][]{{0, 0}, {x1, 0}, {x1, time0}},
                        GraphData.sample(
                                GraphData.logRange(x1, x2, 10),
                                x -> x, x -> (TreeVacuumBlockEntity.maxProgress / (WORK_MULTIPLIER * x / 1000) / 20)),
                        new float[][]{{x3, time1}}
                ))
                ;
    }
}
