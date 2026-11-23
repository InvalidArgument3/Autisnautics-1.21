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
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.Transaction;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.recipe.BallMillRecipe;
import com.neep.neepmeat.recipe.NeepRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BallMillBlockEntity extends BlockEntity implements LivingMachineComponent {
    private ItemStack stack = ItemStack.EMPTY;
    private float progress;
    private @Nullable BallMillRecipe recipe;

    public BallMillBlockEntity(BlockPos pos, BlockState state) {
        super(LivingMachines.BALL_MILL_BE.get(), pos, state);
    }

    public void tick(float increment, Storage<ItemVariant> input, Storage<ItemVariant> output) {
        if (stack.isEmpty()) {
            try (Transaction transaction = Transaction.openOuter()) {
                for (var view : input.nonEmptyViews()) {
                    ItemStack offered = view.getResource().toStack((int) Math.min(view.getAmount(), Integer.MAX_VALUE));
                    BallMillRecipe found = MeatlibRecipes.getInstance().matchBallMill(NeepRecipes.BALL_MILL.get(), offered).orElse(null);
                    if (found == null) {
                        continue;
                    }
                    long extracted = input.extract(view.getResource(), found.inputAmount(), transaction);
                    if (extracted > 0) {
                        stack = view.getResource().toStack((int) extracted);
                        recipe = found;
                        transaction.commit();
                        break;
                    }
                }
            }
        }
        if (recipe == null || stack.isEmpty()) {
            return;
        }
        progress += increment;
        if (progress < 20) {
            return;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            if (recipe.eject(output, transaction)) {
                stack = ItemStack.EMPTY;
                recipe = null;
                progress = 0;
                transaction.commit();
            }
        }
    }

    @Override
    public boolean componentRemoved() {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType() {
        return LivingMachineComponents.BALL_MILL;
    }
}
