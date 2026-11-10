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

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.recipe.MeatlibRecipes;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.component.PoweredComponent;
import com.neep.neepmeat.machine.small_trommel.TrommelRecipe;
import com.neep.neepmeat.machine.small_trommel.TrommelStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class LargeTrommelBlockEntity extends SyncableBlockEntity implements LivingMachineComponent, PoweredComponent
{
    private float progressIncrement;
    private final InputSlot inputSlot = new InputSlot(TrommelRecipe.INPUT_AMOUNT, this::sync, this::getLevel);

    public LargeTrommelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.putFloat("progress_increment", progressIncrement);
        inputSlot.writeNbt(nbt);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.progressIncrement = nbt.getFloat("progress_increment");
        inputSlot.readNbt(nbt);
    }

    @Override
    public boolean componentRemoved()
    {
        return isRemoved();
    }

    public InputSlot getStorage()
    {
        return inputSlot;
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType()
    {
        return LivingMachineComponents.LARGE_TROMMEL;
    }

    @Override
    public float progressIncrement()
    {
        return progressIncrement;
    }

    @Override
    public void setProgressIncrement(float progressIncrement)
    {
        if (progressIncrement != this.progressIncrement)
        {
            this.progressIncrement = progressIncrement;
            sync();
        }
    }

    public static class InputSlot extends WritableSingleFluidStorage implements StorageView<FluidVariant>
    {
        private final Supplier<Level> worldGetter;
        @Nullable private TrommelRecipe recipe;
        private float progress;
        public final float totalProgress = 30; // TODO: Hardcoded for now, the same as the small trommel.
        public long recipeStartTime;

        public InputSlot(long capacity, Runnable finalCallback, Supplier<Level> worldGetter)
        {
            super(capacity, finalCallback);
            this.worldGetter = worldGetter;
        }

        public void tick(float progressIncrement, Storage<FluidVariant> output, Storage<ItemVariant> itemOutput, TransactionContext transaction)
        {
            if (recipe != null)
            {
                progress += progressIncrement;
                if (progress >= totalProgress)
                {
                    StorageWrapper wrapper = new StorageWrapper(this, output, itemOutput);

                    if (recipe.takeInputs(wrapper, transaction))
                    {
                        recipe.ejectOutputs(wrapper, transaction);
                    }
                    recipe = null;
                    progress = 0;
                }
            }
            else
            {
                StorageWrapper wrapper = new StorageWrapper(this, output, itemOutput);
                recipe = MeatlibRecipes.getInstance().getFirstMatch(NMrecipeTypes.TROMMEL, wrapper).orElse(null);
                if (recipe == null)
                    recipe = MeatlibRecipes.getInstance().getFirstMatch(NMrecipeTypes.FAT_TROMMEL, wrapper).orElse(null);

                if (recipe != null)
                    recipeStartTime = worldGetter.get().getGameTime();
            }
        }

        @Override
        public void writeNbt(CompoundTag nbt)
        {
            super.writeNbt(nbt);
            nbt.putLong("recipe_start_time", recipeStartTime);
            nbt.putFloat("progress", progress);
            if (recipe != null)
                nbt.putString("recipe", recipe.getId().toString());
        }

        @Override
        public CompoundTag readNbt(CompoundTag nbt)
        {
            super.readNbt(nbt);
            this.recipeStartTime = nbt.getLong("recipe_start_time");
            this.progress = nbt.getFloat("progress");
            if (nbt.contains("recipe"))
                this.recipe = (TrommelRecipe) MeatlibRecipes.getInstance().get(ResourceLocation.tryParse(nbt.getString("recipe"))).orElse(null);
            else
                this.recipe = null;

            return nbt;
        }
    }

    private record StorageWrapper(SingleVariantStorage<FluidVariant> input, Storage<FluidVariant> output, Storage<ItemVariant> itemOutput) implements TrommelStorage
    {
    }
}
