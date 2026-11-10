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

package com.neep.neepmeat.item;

import com.neep.meatlib.item.BaseItem;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.neepmeat.api.live_machine.LivingMachineBlockEntity;
import com.neep.neepmeat.api.live_machine.LivingMachineStructure;
import com.neep.neepmeat.machine.integrator.IntegratorBlockEntity;
import com.neep.neepmeat.machine.reactor.IntrusionReactor;
import com.neep.neepmeat.machine.reactor.ReceiverOrganismStructure;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedIntegrationChargeItem extends BaseItem
{
    public AdvancedIntegrationChargeItem(TooltipSupplier tooltipSupplier, Properties settings)
    {
        super(tooltipSupplier, settings);
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Level world = context.getLevel();
        BlockState blockState = world.getBlockState(context.getClickedPos());

        if (blockState.getBlock() instanceof ReceiverOrganismStructure)
        {
            if (context.getPlayer() != null)
                context.getPlayer().awardStat(Stats.ITEM_USED.get(this));

            if (!world.isClientSide())
            {
                boolean success = findController(world, context.getClickedPos(),
                        found ->
                        {
                            if (found instanceof IntegratorBlockEntity be)
                            {
                                world.setBlockAndUpdate(be.getBlockPos(), IntrusionReactor.REACTION_CORE.defaultBlockState());

                                world.playSound(null, context.getClickedPos(), SoundEvents.HONEYCOMB_WAX_ON, SoundSource.PLAYERS, 1, 1);
                                ItemStack newStack = context.getItemInHand().copy();
                                newStack.shrink(1);
                                context.getPlayer().setItemInHand(context.getHand(), newStack);
                                return true;
                            }
                            return false;
                        },
                        s -> s.getBlock() instanceof ReceiverOrganismStructure, 64);
            }

            return InteractionResult.SUCCESS;
        }

        if (blockState.getBlock() instanceof LivingMachineStructure)
        {
            if (context.getPlayer() != null)
                context.getPlayer().awardStat(Stats.ITEM_USED.get(this));

            if (!world.isClientSide())
            {
                boolean success = findController(world, context.getClickedPos(),
                    found ->
                    {
                        if (found instanceof LivingMachineBlockEntity be)
                        {
                            be.getDegradationManager().subtract(0.5f);
                            world.playSound(null, context.getClickedPos(), SoundEvents.HONEYCOMB_WAX_ON, SoundSource.PLAYERS, 1, 1);
                            ItemStack newStack = context.getItemInHand().copy();
                            newStack.shrink(1);
                            context.getPlayer().setItemInHand(context.getHand(), newStack);
                            return true;
                        }
                        return false;
                    },
                    s -> s.getBlock() instanceof LivingMachineStructure, 64);
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Nullable
    private static <T extends BlockEntity> boolean findController(Level world, BlockPos origin, Function<BlockEntity, Boolean> function, Predicate<BlockState> isStructure, int maxStructures)
    {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();

        queue.add(origin);
        visited.add(origin);

        int structuresVisited = 0;

        while (!queue.isEmpty() && structuresVisited < maxStructures)
        {
            BlockPos current = queue.poll();

            BlockPos.MutableBlockPos mutable = current.mutable();

            for (Direction direction : Direction.values())
            {
                mutable.setWithOffset(current, direction);

                if (!visited.contains(mutable))
                {
                    visited.add(mutable.immutable());

                    BlockState nextState = world.getBlockState(mutable);

                    BlockEntity be;
                    if (nextState.isAir())
                    {
                        continue;
                    }
                    else if (isStructure.test(nextState))
                    {
                        queue.add(mutable.immutable());
                        structuresVisited++;
                    }
                    else if ((be = world.getBlockEntity(mutable)) != null)
                    {
                        if (function.apply(be))
                            return true;
                    }
                }
            }
        }

        return false;
    }
}
