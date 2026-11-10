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

package com.neep.neepmeat.machine.alloy_kiln;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.recipe.MeatlibRecipes;
import com.neep.meatlib.recipe.ingredient.RecipeOutput;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.machine.Heatable;
import com.neep.neepmeat.recipe.AlloyKilnRecipe;
import com.neep.neepmeat.screen_handler.AlloyKilnScreenHandler;
import com.neep.meatlib.util.MiscUtil;
import com.neep.meatlib.transfer.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class AlloyKilnBlockEntity extends SyncableBlockEntity implements Heatable, MenuProvider
{
    private boolean updateRecipe;

    protected int fuelTime;
    protected int burnTime;
    protected int cookTime;
    protected int cookTimeTotal;

    protected AlloyKilnRecipe currentRecipe;

    protected AlloyKilnStorage storage;

    protected float heatMultiplier;

    protected final ContainerData propertyDelegate = new ContainerData()
    {
        @Override
        public int get(int index)
        {
            switch (index)
            {
                case 0 -> {
                    return burnTime;
                }
                case 1 -> {
                    return fuelTime;
                }
                case 2 -> {
                    return cookTime;
                }
                case 3 -> {
                    return cookTimeTotal;
                }
            }
            return 0;
        }

        @Override
        public void set(int index, int value)
        {
            switch (index)
            {
                case 0 -> burnTime = value;
                case 1 -> fuelTime = value;
                case 2 -> cookTime = value;
                case 3 -> cookTimeTotal = value;
            }
        }

        @Override
        public int getCount()
        {
            return 4;
        }
    };

    public AlloyKilnBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        this.storage = new AlloyKilnStorage(this);
    }

    public AlloyKilnBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.ALLOY_KILN, pos, state);
    }

    public static void serverTick(Level world, BlockPos pos, BlockState state, AlloyKilnBlockEntity be)
    {
        be.tick(world, pos, state);
    }

    public void tick(Level world, BlockPos pos, BlockState state)
    {
        tryUpdateComparators();

        if (updateRecipe)
        {
            AlloyKilnRecipe recipe = MeatlibRecipes.getInstance().getFirstMatch(NMrecipeTypes.ALLOY_SMELTING, storage).orElse(null);
            this.currentRecipe = recipe;
            if (currentRecipe == null)
            {
                cookTimeTotal = -1;
                cookTime = 0;
            }
            setChanged();
            updateRecipe = false;
        }

        boolean wasBurning = isBurning();

        this.burnTime = Math.max(0, this.burnTime - 1);

        if (!isBurning())
        {
            int time;
            if (currentRecipe != null
                    && canAcceptRecipeOutput(currentRecipe, storage.inventory.getItems())
                    && (time = storage.decrementFuel()) > 0)
            {
                this.fuelTime = time;
                this.burnTime = time;
                updateState(world, pos, state);
            }
            else
            {
                this.cookTime = Math.max(0, this.cookTime - 1);

                if (wasBurning)
                    updateState(world, pos, state);
            }
        }

        if (isBurning())
        {
            if (!isCooking())
            {
                if (currentRecipe != null && canAcceptRecipeOutput(currentRecipe, storage.inventory.getItems()))
                {
                    cookTimeTotal = currentRecipe.getTime();
                }
            }

            if (isCooking())
            {
                int tickIncrement = Heatable.getFurnaceTickIncrement(heatMultiplier);
                this.cookTime = Math.min(this.cookTimeTotal, this.cookTime + tickIncrement);
            }
        }

        if (this.cookTime == this.cookTimeTotal)
        {
            finishCooking();
        }
    }

    public static boolean canAcceptRecipeOutput(@Nullable AlloyKilnRecipe recipe, NonNullList<ItemStack> slots)
    {
        if (slots.get(AlloyKilnStorage.INPUT_1).isEmpty()
                || slots.get(AlloyKilnStorage.INPUT_2).isEmpty() || recipe == null)
        {
            return false;
        }

        RecipeOutput<Item> output = recipe.getItemOutput();
        ItemStack outputStack = slots.get(AlloyKilnStorage.OUTPUT);
        if (outputStack.isEmpty())
        {
            return true;
        }
        if (!outputStack.is(output.resource()))
        {
            return false;
        }
        if (outputStack.getCount() < outputStack.getMaxStackSize())
        {
            return true;
        }
        return outputStack.getCount() < output.maxAmount();
    }

    protected void finishCooking()
    {
        if (currentRecipe != null)
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                if (currentRecipe.takeInputs(storage, transaction) && currentRecipe.ejectOutput(storage, transaction))
                    transaction.commit();
                else
                    transaction.abort();
            }
            sync();
        }

        this.currentRecipe = null;
        this.cookTimeTotal = -1;
        this.cookTime = 0;
    }

    protected boolean isCooking()
    {
        return cookTimeTotal != -1;
    }

    public boolean isBurning()
    {
        return burnTime > 0;
    }

    @Override
    public void setBurning()
    {
        this.burnTime = 2;
    }

    @Override
    public void updateState(Level world, BlockPos pos, BlockState oldState)
    {
        BlockState state = getBlockState();
        if (state.getValue(AlloyKilnBlock.LIT) != this.isBurning())
        {
            state = state.setValue(AlloyKilnBlock.LIT, this.isBurning());
            getLevel().setBlock(pos, state, Block.UPDATE_ALL);
        }
    }

    @Override
    public void setHeat(float heat)
    {
        this.heatMultiplier = heat;
    }

    @Override
    public Component getDisplayName()
    {
        return Component.translatable("container." + NeepMeat.NAMESPACE + ".alloy_kiln");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player)
    {
        return new AlloyKilnScreenHandler(syncId, inv, storage.inventory, propertyDelegate);
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        nbt.putShort("fuelTime", (short) fuelTime);
        nbt.putShort("burnTime", (short) burnTime);
        nbt.putShort("cookTime", (short) cookTime);
        nbt.putShort("cookTimeTotal", (short) cookTimeTotal);

        storage.writeNbt(nbt);

        if (currentRecipe != null)
            nbt.putString("current_recipe", currentRecipe.getId().toString());
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        this.fuelTime = nbt.getShort("fuelTime");
        this.burnTime = nbt.getShort("burnTime");
        this.cookTime = nbt.getShort("cookTime");
        this.cookTimeTotal = nbt.getShort("cookTimeTotal");

        storage.readNbt(nbt);

        this.currentRecipe = MiscUtil.ifPresentOrNull(nbt, "current_recipe",
                s -> (AlloyKilnRecipe) MeatlibRecipes.getInstance().get((ResourceLocation.tryParse(s))).orElse(null));
    }

    public AlloyKilnStorage getStorage()
    {
        return storage;
    }

    public void updateRecipe()
    {
        updateRecipe = true;
    }

    @Override
    public void setChanged()
    {
        super.setChanged();
    }
}
