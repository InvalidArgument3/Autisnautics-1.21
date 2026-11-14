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
 *
 * Every ten ticks a matching enlightening recipe looks along data cables for a mature integrator with enough data.
 */

package com.neep.neepmeat.machine.pedestal;

import com.neep.meatlib.transfer.Transaction;
import com.neep.neepmeat.api.DataResource;
import com.neep.neepmeat.machine.StageMachines;
import com.neep.neepmeat.machine.integrator.Integrator;
import com.neep.neepmeat.recipe.EnlighteningRecipe;
import com.neep.neepmeat.recipe.NeepRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PedestalBlockEntity extends BlockEntity {
    public static final int MAX_COOLDOWN = 10;

    private ItemStack stack = ItemStack.EMPTY;
    private int cooldown;

    public PedestalBlockEntity(BlockPos pos, BlockState state) {
        super(StageMachines.PEDESTAL_BE.get(), pos, state);
    }

    public ItemStack stack() {
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
        setChanged();
    }

    public void tick() {
        cooldown = Math.max(0, cooldown - 1);
        if (cooldown == 0) {
            cooldown = MAX_COOLDOWN;
            craft();
        }
    }

    public void craft() {
        if (level == null || stack.isEmpty()) {
            return;
        }
        for (var holder : level.getRecipeManager().getAllRecipesFor(NeepRecipes.ENLIGHTENING.get())) {
            EnlighteningRecipe recipe = holder.value();
            if (!recipe.matches(stack)) {
                continue;
            }
            Integrator integrator = Integrator.findIntegrator(level, worldPosition, 10, recipe.data());
            if (integrator == null || !integrator.canEnlighten()) {
                return;
            }
            try (Transaction transaction = Transaction.openOuter()) {
                float extracted = integrator.extract(DataResource.NORMAL, recipe.data(), transaction);
                if (extracted < recipe.data()) {
                    transaction.abort();
                    return;
                }
                integrator.setLookPos(worldPosition);
                integrator.spawnBeam(level, worldPosition);
                stack.shrink(recipe.inputCount());
                stack = recipe.resultStack();
                transaction.commit();
                setChanged();
                return;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("cooldown", cooldown);
        if (!stack.isEmpty()) {
            tag.put("item", stack.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        cooldown = tag.getInt("cooldown");
        stack = tag.contains("item") ? ItemStack.parse(registries, tag.getCompound("item")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
    }
}
