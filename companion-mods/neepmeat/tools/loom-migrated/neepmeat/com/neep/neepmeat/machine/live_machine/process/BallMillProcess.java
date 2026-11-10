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

import com.google.common.collect.Lists;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineBlockEntity;
import com.neep.neepmeat.api.live_machine.Process;
import com.neep.neepmeat.api.processing.ball_mill.BallMillRecipe;
import com.neep.neepmeat.api.processing.ball_mill.BallMillRecipeGenerator;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.block.entity.BallMillBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.network.chat.Component;
import java.util.Collection;
import java.util.List;

public class BallMillProcess implements Process
{
    private final List<ComponentType<?>> REQUIRED = List.of(
            LivingMachineComponents.ITEM_INPUT,
            LivingMachineComponents.BALL_MILL,
            LivingMachineComponents.ITEM_OUTPUT,
            LivingMachineComponents.MOTOR_PORT
    );

    @Override
    public void serverTick(LivingMachineBlockEntity be)
    {
        be.withComponents(LivingMachineComponents.BALL_MILL).ifPresent(r ->
        {
            var inputPorts = be.getCombinedItemInput();
            var output = be.getCombinedItemOutput();

            Collection<BallMillBlockEntity> mills = r.t1();
            if (mills.size() > 4 || be.getProgressIncrement() == 0)
                return;

            var inputViews = Lists.newArrayList(inputPorts.nonEmptyViews());

            try (Transaction transaction = Transaction.openOuter())
            {
                for (var mill : mills)
                {
                    try (Transaction inner = transaction.openNested())
                    {
                        if (!inputViews.isEmpty() && mill.inputSlot.isEmpty())
                        {
                            // Round-robin behaviour
                            be.inputSequence = (be.inputSequence + 1) % inputViews.size();

                            try (Transaction inner2 = inner.openNested())
                            {
                                StorageView<ItemVariant> view = inputViews.get(be.inputSequence);

                                var matching = BallMillRecipeGenerator.INSTANCE.getFirstMatch(new BallMillRecipe.BallMillContext.Match(view));
                                if (matching.isPresent())
                                {
                                    long inserted = mill.inputSlot.insert(view.getResource(), view.getAmount(), inner2);
                                    long extracted = view.extract(view.getResource(), inserted, inner2);

                                    if (inserted == extracted)
                                        inner2.commit();
                                    else
                                        inner2.abort();
                                }
                            }
                        }

                        if (!mill.inputSlot.isEmpty())
                        {
                            mill.inputSlot.tick(be.getProgressIncrement(), output, 0, inner);
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
        return REQUIRED;
    }

    @Override
    public Component getName()
    {
        return NeepMeat.translationKey("process", "ball_mill");
    }
}
