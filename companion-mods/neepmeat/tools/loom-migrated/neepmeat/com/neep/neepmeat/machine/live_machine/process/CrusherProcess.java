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

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineBlockEntity;
import com.neep.neepmeat.api.live_machine.Process;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.block.entity.CrusherSegmentBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.entity.LargestHopperBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.entity.LuckyOneBlockEntity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CrusherProcess implements Process
{
    @Override
    public void serverTick(LivingMachineBlockEntity be)
    {
        be.withComponents(LivingMachineComponents.ITEM_INPUT, LivingMachineComponents.CRUSHER_SEGMENT, LivingMachineComponents.ITEM_OUTPUT, LivingMachineComponents.MOTOR_PORT).ifPresent(result ->
        {
            var hoppers = result.t1();
            var crushers = result.t2();

            if (crushers.size() > 4)
                return;

            float chanceMod = 0;

            Collection<LuckyOneBlockEntity> luckies = be.getComponent(LivingMachineComponents.LUCKY_ONE);
            for (var lucky : luckies)
            {
                if (chanceMod >= 0.2)
                    break;

                if (lucky.isActive())
                    chanceMod += 0.1f;
            }

            if (be.getPower() < 0.1)
            {
                return;
            }

            float progressIncrement = be.getProgressIncrement();

            var hopper = hoppers.iterator().next();

            // Handle entities
            List<ItemStack> entityResultStacks = new ObjectArrayList<>();
            if (hopper instanceof LargestHopperBlockEntity hopperBlockEntity)
                hopperBlockEntity.damageEntities(progressIncrement, entityResultStacks, luckies.size());

            Storage<ItemVariant> input = be.getCombinedItemInput();
            Storage<ItemVariant> output = be.getCombinedItemOutput();

            List<StorageView<ItemVariant>> inputViews = new ArrayList<>();
            for (var view : input)
            {
                if (!view.isResourceBlank() && view.getAmount() > 0)
                    inputViews.add(view);
            }

            try (Transaction transaction = Transaction.openOuter())
            {
                for (var crusher : crushers)
                {
                    try (Transaction inner = transaction.openNested())
                    {
                        CrusherSegmentBlockEntity.InputSlot slot = crusher.getStorage();

                        if (!inputViews.isEmpty() && slot.isEmpty())
                        {
                            // Round-robin behaviour
                            be.inputSequence = (be.inputSequence + 1) % inputViews.size();

                            try (Transaction inner2 = inner.openNested())
                            {
                                StorageView<ItemVariant> view = inputViews.get(be.inputSequence);
                                if (!view.isResourceBlank() && view.getAmount() > 0)
                                {
                                    long inserted = slot.insert(view.getResource(), view.getAmount(), inner2);
                                    long extracted = view.extract(view.getResource(), inserted, inner2);

                                    if (inserted == extracted)
                                        inner2.commit();
                                    else
                                        inner2.abort();
                                }
                            }
                        }

                        if (!slot.isEmpty())
                        {
                            slot.tick(progressIncrement, output, chanceMod, inner);
                        }
                        inner.commit();
                    }
                }

                if (!entityResultStacks.isEmpty())
                {
                    try (Transaction inner = transaction.openNested())
                    {
                        for (var stack : entityResultStacks)
                        {
                            if (!stack.isEmpty()) // Not sure how this could be the case, but someone reported a crash.
                                output.insert(ItemVariant.of(stack), stack.getCount(), inner);
                        }

                        inner.commit();
                    }
                }

                transaction.commit();
            }
        });
    }

    @Override
    public List<ComponentType<?>> getRequired()
    {
        return List.of(
                LivingMachineComponents.ITEM_INPUT,
                LivingMachineComponents.CRUSHER_SEGMENT,
                LivingMachineComponents.ITEM_OUTPUT,
                LivingMachineComponents.MOTOR_PORT
        );
    }

    @Override
    public List<Component> getProblems(LivingMachineBlockEntity be)
    {
        return be.withComponents(LivingMachineComponents.CRUSHER_SEGMENT).map(result ->
        {
            List<Component> problems = new ObjectArrayList<>();
            var pbe = result.t1().iterator().next();
            if (be.getProgressIncrement() <= 0)
                problems.add(Component.nullToEmpty("Power = " + PowerUtil.formatEnergy(0, 0)));
            return problems;
        }).orElse(List.of());
    }

    @Override
    public Component getName()
    {
        return NeepMeat.translationKey("process", "crusher");
    }
}
