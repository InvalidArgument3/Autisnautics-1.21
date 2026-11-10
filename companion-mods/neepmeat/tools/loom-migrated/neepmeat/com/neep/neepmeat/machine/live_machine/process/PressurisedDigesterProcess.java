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

import com.neep.meatlib.recipe.MeatlibRecipes;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineBlockEntity;
import com.neep.neepmeat.api.live_machine.Process;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.machine.crucible.CrucibleStorage;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.recipe.OreFatRenderingRecipe;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PressurisedDigesterProcess implements Process
{
    private static final List<ComponentType<?>> REQUIRED = List.of(
            LivingMachineComponents.STOMACH,
            LivingMachineComponents.FLUID_OUTPUT,
            LivingMachineComponents.ITEM_INPUT
    );

    @Override
    public void serverTick(LivingMachineBlockEntity be)
    {
        try (Transaction transaction = Transaction.openOuter())
        {
            long airConsumption = 90;
            long airExtracted = be.getCombinedFluidInput().extract(NMFluids.COMPRESSED_AIR.variant(), airConsumption, transaction);
            float discount;
            if (airExtracted >= airConsumption)
                discount = 0.5f;
            else
                discount = 0;

            if (be.getLevel().getGameTime() % 5 == 0)
            {
                be.withComponents(LivingMachineComponents.STOMACH).ifPresent(result ->
                {
                    // Check for compressed air

                    var ctx = new DigesterStorage(be.getCombinedItemInput(), be.getCombinedFluidInput(), be.getCombinedFluidOutput(), discount);
                    OreFatRenderingRecipe recipe = MeatlibRecipes.getInstance().getFirstMatch(NMrecipeTypes.ORE_FAT_RENDERING, ctx).orElse(null);

                    try (Transaction inner = transaction.openNested())
                    {
                        ItemVariant processItem;
                        if (recipe != null && (processItem = recipe.takeInputs(ctx, 1, inner)) != null)
                        {
                            inner.commit();
                        }
                    }
                });
            }

            transaction.commit();
        }
    }

    @Override
    public List<ComponentType<?>> getRequired()
    {
        return REQUIRED;
    }

    @Override
    public Component getName()
    {
        return NeepMeat.translationKey("process", "pressurised_digester");
    }

    private record DigesterStorage(Storage<ItemVariant> itemInput, Storage<FluidVariant> fluidInput,
                                   Storage<FluidVariant> fluidOutput, float discount) implements CrucibleStorage
    {
        @Override
        public Storage<FluidVariant> getStorage(@Nullable Direction direction)
        {
            return fluidInput;
        }

        @Override
        public Storage<ItemVariant> getItemStorage(@Nullable Direction direction)
        {
            return itemInput;
        }

        @Override
        public Storage<FluidVariant> getFluidOutput()
        {
            return fluidOutput;
        }
    }
}
