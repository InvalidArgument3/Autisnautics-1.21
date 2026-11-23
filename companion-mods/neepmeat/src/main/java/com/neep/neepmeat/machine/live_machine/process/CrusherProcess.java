/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * More than four segments does nothing. Below 0.1 power does nothing.
 * An empty segment takes the input stack, then progress increases by the increment.
 * An empty segment takes one recipe's worth of input. Advanced crushing wins over grinding.
 */

package com.neep.neepmeat.machine.live_machine.process;

import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.Process;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.api.live_machine.LivingMachineBlockEntity;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageView;
import com.neep.meatlib.transfer.Transaction;
import com.neep.neepmeat.machine.live_machine.block.entity.CrusherSegmentBlockEntity;
import com.neep.neepmeat.machine.live_machine.component.ItemInputComponent;
import com.neep.meatlib.recipe.MeatlibRecipes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CrusherProcess implements Process {
    private static final List<ComponentType<?>> REQUIRED = List.of(
            LivingMachineComponents.ITEM_INPUT,
            LivingMachineComponents.CRUSHER_SEGMENT,
            LivingMachineComponents.ITEM_OUTPUT,
            LivingMachineComponents.MOTOR_PORT);

    @Override
    public void serverTick(LivingMachineBlockEntity machine) {
        List<CrusherSegmentBlockEntity> crushers = machine.components(LivingMachineComponents.CRUSHER_SEGMENT);
        List<ItemInputComponent> inputs = machine.components(LivingMachineComponents.ITEM_INPUT);
        if (crushers.isEmpty() || inputs.isEmpty() || crushers.size() > 4 || machine.getPower() < 0.1f) {
            return;
        }
        ItemInputComponent input = inputs.getFirst();
        Storage<ItemVariant> storage = input.getStorage(null);
        for (CrusherSegmentBlockEntity crusher : crushers) {
            if (crusher.isEmpty()) {
                takeInput(storage, crusher);
            } else if (crusher.addProgress(machine.getProgressIncrement())) {
                List<com.neep.neepmeat.machine.live_machine.component.ItemOutputComponent> outputs = machine.components(LivingMachineComponents.ITEM_OUTPUT);
                if (!outputs.isEmpty()) {
                    net.minecraft.world.item.ItemStack result = crusher.takeResult();
                    try (Transaction transaction = Transaction.openOuter()) {
                        outputs.getFirst().getInternalStorage().insert(ItemVariant.of(result), result.getCount(), transaction);
                        transaction.commit();
                    }
                }
            }
        }
    }

    private static void takeInput(Storage<ItemVariant> storage, CrusherSegmentBlockEntity crusher) {
        for (StorageView<ItemVariant> view : storage) {
            if (view.getAmount() <= 0 || view.getResource().isBlank()) {
                continue;
            }
            try (Transaction transaction = Transaction.openOuter()) {
                ItemStack offered = view.getResource().toStack((int) Math.min(view.getAmount(), Integer.MAX_VALUE));
                var found = MeatlibRecipes.getInstance().matchCrushing(com.neep.neepmeat.recipe.NeepRecipes.ADVANCED_CRUSHING.get(), offered)
                        .or(() -> MeatlibRecipes.getInstance().matchCrushing(com.neep.neepmeat.recipe.NeepRecipes.GRINDING.get(), offered));
                if (found.isEmpty()) {
                    continue;
                }
                long extracted = storage.extract(view.getResource(), found.get().inputAmount(), transaction);
                if (extracted <= 0) {
                    continue;
                }
                transaction.commit();
                crusher.insert(view.getResource().toStack((int) extracted));
                crusher.assignRecipe();
                return;
            }
        }
    }

    @Override
    public List<ComponentType<?>> getRequired() {
        return REQUIRED;
    }

    @Override
    public Component getName() {
        return Component.translatable("process.neepmeat.crusher");
    }
}
