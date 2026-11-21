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
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.SingleVariantStorage;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageView;
import com.neep.meatlib.transfer.TransactionContext;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.live_machine.component.PoweredComponent;
import com.neep.neepmeat.machine.small_trommel.TrommelRecipe;
import com.neep.neepmeat.machine.small_trommel.TrommelStorage;
import com.neep.neepmeat.recipe.NeepRecipes;
import com.neep.neepmeat.recipe.NormalTrommelRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LargeTrommelBlockEntity extends SyncableBlockEntity implements LivingMachineComponent, PoweredComponent {
    private float progressIncrement;
    private final InputSlot inputSlot = new InputSlot(TrommelRecipe.INPUT_AMOUNT, this::sync);

    public LargeTrommelBlockEntity(BlockPos pos, BlockState state) {
        super(LivingMachines.TROMMEL_BE.get(), pos, state);
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.putFloat("progress_increment", progressIncrement);
        inputSlot.writeNbt(nbt);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        this.progressIncrement = nbt.getFloat("progress_increment");
        inputSlot.readNbt(nbt);
    }

    @Override
    public boolean componentRemoved() {
        return isRemoved();
    }

    public InputSlot getStorage() {
        return inputSlot;
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType() {
        return LivingMachineComponents.LARGE_TROMMEL;
    }

    @Override
    public float progressIncrement() {
        return progressIncrement;
    }

    @Override
    public void setProgressIncrement(float progressIncrement) {
        this.progressIncrement = progressIncrement;
    }

    public static class InputSlot extends WritableSingleFluidStorage implements StorageView<FluidVariant> {
        private @Nullable TrommelRecipe recipe;
        private float progress;
        public final float totalProgress = 30;

        public InputSlot(long capacity, Runnable finalCallback) {
            super(capacity, finalCallback);
        }

        public void tick(float progressIncrement, Storage<FluidVariant> output, Storage<ItemVariant> itemOutput, TransactionContext transaction) {
            StorageWrapper wrapper = new StorageWrapper(this, output, itemOutput);
            if (recipe != null) {
                progress += progressIncrement;
                if (progress >= totalProgress) {
                    if (recipe.takeInputs(wrapper, transaction)) {
                        recipe.ejectOutputs(wrapper, transaction);
                    }
                    recipe = null;
                    progress = 0;
                }
            } else {
                recipe = MeatlibRecipes.getInstance().matchTrommel(NeepRecipes.TROMMEL.get(), wrapper).orElse(null);
            }
        }
    }

    private record StorageWrapper(SingleVariantStorage<FluidVariant> input, Storage<FluidVariant> output, Storage<ItemVariant> itemOutput) implements TrommelStorage {}
}
