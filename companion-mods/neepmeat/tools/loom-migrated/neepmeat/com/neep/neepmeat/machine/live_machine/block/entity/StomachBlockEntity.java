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

package com.neep.neepmeat.machine.live_machine.block.entity;

import com.neep.meatlib.recipe.MeatlibRecipes;
import com.neep.meatlib.util.NbtSerialisable;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.component.FluidInputComponent;
import com.neep.neepmeat.machine.live_machine.process.BioreactorProcess;
import com.neep.neepmeat.recipe.BioreactorRecipe;
import com.neep.meatlib.util.MiscUtil;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class StomachBlockEntity extends BlockEntity implements FluidInputComponent
{
    private final InputSlot slot = new InputSlot();

    private final WritableSingleFluidStorage fluidInputStorage = new WritableSingleFluidStorage(64 * FluidConstants.BUCKET, this::setChanged);

    public StomachBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public boolean componentRemoved()
    {
        return isRemoved();
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        slot.readNbt(nbt);
        fluidInputStorage.readNbt(nbt.getCompound("fluid_input"));
    }

    @Override
    protected void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        slot.writeNbt(nbt);
        nbt.put("fluid_input", fluidInputStorage.toNbt(new CompoundTag()));
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType()
    {
        return LivingMachineComponents.STOMACH;
    }

    public InputSlot getSlot()
    {
        return slot;
    }

    @Override
    public WritableSingleFluidStorage getStorage(Direction ctx)
    {
        return fluidInputStorage;
    }

    public static class InputSlot implements NbtSerialisable
    {
        private float counter;

        @Nullable private BioreactorRecipe recipe;

        public void tickRecipe(float progressIncrement, BioreactorProcess.Context context)
        {
            if (recipe == null)
            {
                recipe = MeatlibRecipes.getInstance().getFirstMatch(NMrecipeTypes.BIOREACTOR, context).orElse(null);
                if (recipe != null)
                {
                    counter = recipe.getProcessTime();
                }
            }

            if (recipe != null)
            {
                if (counter > 0)
                {
                    counter = Math.max(0, counter - progressIncrement);
                }
                else
                {
                    try (Transaction transaction = Transaction.openOuter())
                    {
                        if (recipe.takeInputs(context, transaction) && recipe.ejectOutputs(context, transaction))
                            transaction.commit();
                        else
                            transaction.abort();

                        recipe = null;
                    }
                }
            }
        }

        @Override
        public CompoundTag writeNbt(CompoundTag nbt)
        {
            recipe = MiscUtil.ifPresentOrNull(nbt, "recipe",
                    s -> (BioreactorRecipe) MeatlibRecipes.getInstance().get((ResourceLocation.tryParse(s))).orElse(null));

            return nbt;
        }

        @Override
        public void readNbt(CompoundTag nbt)
        {
            if (recipe != null)
                nbt.putString("recipe", recipe.getId().toString());
        }
    }
}