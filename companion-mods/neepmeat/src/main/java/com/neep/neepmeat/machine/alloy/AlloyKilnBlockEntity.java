/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.machine.alloy;

import com.neep.meatlib.inventory.ImplementedInventory;
import com.neep.neepmeat.machine.NeepMachines;
import com.neep.neepmeat.recipe.AlloyKilnInput;
import com.neep.neepmeat.recipe.AlloyKilnRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AlloyKilnBlockEntity extends BlockEntity implements ImplementedInventory, MenuProvider {
    public static final int FUEL = 0;
    public static final int INPUT_1 = 1;
    public static final int INPUT_2 = 2;
    public static final int OUTPUT = 3;

    private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
    private int burnTime;
    private int fuelTime;
    private int cookTime;
    private int cookTimeTotal;

    public int burnTime() { return burnTime; }
    public int fuelTime() { return fuelTime; }
    public int cookTime() { return cookTime; }
    public int cookTimeTotal() { return cookTimeTotal; }

    public AlloyKilnBlockEntity(BlockPos pos, BlockState state) {
        super(NeepMachines.ALLOY_KILN_BE.get(), pos, state);
    }

    public void serverTick() {
        Level level = getLevel();
        if (level == null || level.isClientSide) {
            return;
        }
        boolean wasBurning = isBurning();
        if (burnTime > 0) {
            burnTime--;
        }
        RecipeHolder<AlloyKilnRecipe> match = findRecipe(level);
        boolean canCook = match != null && canOutput(match.value());
        if (!isBurning() && canCook) {
            int fuel = items.get(FUEL).getBurnTime(RecipeType.SMELTING);
            if (fuel > 0) {
                items.get(FUEL).shrink(1);
                burnTime = fuel;
                fuelTime = fuel;
            }
        }
        if (isBurning() && canCook) {
            if (cookTimeTotal <= 0) {
                cookTimeTotal = match.value().processTime();
            }
            cookTime++;
            if (cookTime >= cookTimeTotal) {
                finish(match.value());
                cookTime = 0;
                cookTimeTotal = 0;
            }
        } else if (!isBurning()) {
            cookTime = Math.max(0, cookTime - 1);
        }
        if (wasBurning != isBurning()) {
            level.setBlock(worldPosition, getBlockState().setValue(AlloyKilnBlock.LIT, isBurning()), Block.UPDATE_CLIENTS);
        }
        setChanged();
    }

    private boolean isBurning() {
        return burnTime > 0;
    }

    private RecipeHolder<AlloyKilnRecipe> findRecipe(Level level) {
        AlloyKilnInput input = new AlloyKilnInput(items.get(INPUT_1), items.get(INPUT_2));
        return level.getRecipeManager().getAllRecipesFor(NeepMachines.ALLOY_TYPE.get()).stream()
                .filter(holder -> holder.value().matches(input, level))
                .findFirst()
                .orElse(null);
    }

    private boolean canOutput(AlloyKilnRecipe recipe) {
        ItemStack output = items.get(OUTPUT);
        ItemStack result = recipe.output().stack();
        if (output.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(output, result)) {
            return false;
        }
        return output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void finish(AlloyKilnRecipe recipe) {
        items.get(INPUT_1).shrink(recipe.input1().amount());
        items.get(INPUT_2).shrink(recipe.input2().amount());
        ItemStack result = recipe.output().stack();
        ItemStack output = items.get(OUTPUT);
        if (output.isEmpty()) {
            items.set(OUTPUT, result);
        } else {
            output.grow(result.getCount());
        }
    }

    @Override
    public NonNullList<ItemStack> items() {
        return items;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.neepmeat.alloy_kiln");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AlloyKilnMenu(id, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        writeNbt(tag, registries);
        tag.putInt("Burn", burnTime);
        tag.putInt("Fuel", fuelTime);
        tag.putInt("Cook", cookTime);
        tag.putInt("CookTotal", cookTimeTotal);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        readNbt(tag, registries);
        burnTime = tag.getInt("Burn");
        fuelTime = tag.getInt("Fuel");
        cookTime = tag.getInt("Cook");
        cookTimeTotal = tag.getInt("CookTotal");
    }
}
