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

package com.neep.neepmeat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.neep.neepmeat.machine.Heatable;
import com.neep.neepmeat.machine.HeatableFurnace;
import com.neep.neepmeat.recipe.GeneralOreDustSmeltingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin implements HeatableFurnace
{
    @Shadow protected abstract boolean isBurning();
    @Shadow int burnTime;

    @Shadow protected NonNullList<ItemStack> inventory;

    @Shadow @Final private RecipeManager.CachedCheck<Container, ? extends AbstractCookingRecipe> matchGetter;
    @Shadow
    int cookTime;
    @Shadow
    int cookTimeTotal;

    @Shadow
    public abstract boolean isEmpty();

    @Shadow
    public abstract boolean isValid(int slot, ItemStack stack);

    @Unique
    protected float heatMultiplier;

    @Override
    public void neepMeat$updateState(Level world, BlockPos pos, BlockState oldState)
    {
        if (oldState.getValue(FurnaceBlock.LIT) != isBurning())
        {
            BlockState newState = oldState.setValue(FurnaceBlock.LIT, isBurning());
            world.setBlock(pos, newState, Block.UPDATE_ALL);
        }
    }

    @Override
    public void neepMeat$setBurning()
    {
        this.burnTime = 2;
    }

    @Override
    public void neepMeat$setHeat(float heat)
    {
        this.heatMultiplier = heat;
    }

    @Override
    public float neepMeat$getHeat()
    {
        return heatMultiplier;
    }

    @Override
    public int neepMeat$getCookTime()
    {
        return cookTime;
    }

    @Override
    public void neepMeat$setCookTime(int time)
    {
        cookTime = time;
    }

    @Override
    public int neepMeat$getCookTimeTotal()
    {
        return cookTimeTotal;
    }

    @Override
    public boolean neepMeat$isCooking()
    {
//        ItemStack itemStack = inventory.get(AbstractFurnaceBlockEntity.)
        ItemStack fuelStack = inventory.get(1);
        Level world = ((BlockEntity) (Object) this).getLevel();
        AbstractFurnaceBlockEntity furnace = ((AbstractFurnaceBlockEntity) (Object) this);
        if (isBurning() || !fuelStack.isEmpty() && !inventory.get(0).isEmpty())
        {
//            Recipe<?> recipe = world.getRecipeManager().getFirstMatch(recipe, furnace, world).orElse(null);
//            Recipe<?> recipe = getLastRecipe();
            Recipe<?> recipe = matchGetter.getRecipeFor(furnace, world).orElse(null);
            int i = furnace.getMaxStackSize();
            return isBurning() && AbstractFurnaceBlockEntity.canBurn(world.registryAccess(), recipe, inventory, i);
        }
        return false;
    }

//    @Unique
//    private static boolean canAcceptRecipeOutput(@Nullable Recipe<?> recipe, DefaultedList<ItemStack> slots, int count, DynamicRegistryManager dynamicRegistryManager)
//    {
//        if (slots.get(0).isEmpty() || recipe == null)
//            return false;
//
//        ItemStack itemStack;
//        if (recipe instanceof OreDustSmeltingRecipe oreDustRecipe)
//            itemStack = oreDustRecipe.getOutput(dynamicRegistryManager, slots.get(0));
//        else
//            itemStack =recipe.getOutput(dynamicRegistryManager);
//
//        if (itemStack.isEmpty())
//            return false;
//
//        ItemStack itemStack2 = slots.get(2);
//        if (itemStack2.isEmpty())
//            return true;
//
//        if (!ItemStack.areItemsEqual(itemStack2, itemStack))
//            return false;
//
//        if (itemStack2.getCount() < count && itemStack2.getCount() < itemStack2.getMaxCount())
//            return true;
//
//        return itemStack2.getCount() < itemStack.getMaxCount();
//    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private static void tick(Level world, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci)
    {
        // Increment cookTime twice per tick if the heat baseEnergy rises above a certain value.
        float multiplier = blockEntity.neepMeat$getHeat();
        if (blockEntity.neepMeat$isCooking())
        {
            int cookTime = blockEntity.neepMeat$getCookTime();
            int total = blockEntity.neepMeat$getCookTimeTotal();
            // Subtract 1 since the Vanilla method will increment the counter once after this
            int tickIncrement = Heatable.getFurnaceTickIncrement(multiplier) - 1;
            // The comparison in AbstractFurnaceBlockEntity uses ==
            blockEntity.neepMeat$setCookTime(Math.min(total - 1, cookTime + tickIncrement));
        }
    }


    @WrapOperation(method = "canAcceptRecipeOutput", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/Recipe;getOutput(Lnet/minecraft/registry/DynamicRegistryManager;)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack handleOreDustSmeltingRecipe(Recipe<?> instance, RegistryAccess dynamicRegistryManager, Operation<ItemStack> original,
                                                         @Local(argsOnly = true) NonNullList<ItemStack> inventory)
    {
        if (instance instanceof GeneralOreDustSmeltingRecipe dustRecipe)
            return dustRecipe.getOutput(dynamicRegistryManager, inventory.get(0));

        return original.call(instance, dynamicRegistryManager);
    }

    @WrapOperation(method = "craftRecipe", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/Recipe;getOutput(Lnet/minecraft/registry/DynamicRegistryManager;)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack handleOreDustSmeltingRecipe2(Recipe<?> instance, RegistryAccess dynamicRegistryManager, Operation<ItemStack> original,
                                                         @Local(argsOnly = true) NonNullList<ItemStack> inventory)
    {
        if (instance instanceof GeneralOreDustSmeltingRecipe dustRecipe)
            return dustRecipe.getOutput(dynamicRegistryManager, inventory.get(0));

        return original.call(instance, dynamicRegistryManager);
    }
}