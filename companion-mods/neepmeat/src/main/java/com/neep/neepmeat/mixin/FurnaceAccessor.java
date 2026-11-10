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

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface FurnaceAccessor
{
	@Accessor
	int getBurnTime();

	@Accessor
	int getFuelTime();

	@Accessor
	int getCookTimeTotal();

	@Accessor
	int getCookTime();

//	@Accessor
//	RecipeType<? extends AbstractCookingRecipe> getRecipeType();

	@Accessor("inventory")
	NonNullList<ItemStack> getInventory();

	@Accessor("burnTime")
	void setBurnTime(int burnTime);

	@Accessor("fuelTime")
	void setFuelTime(int fuelTime);

	@Accessor("cookTime")
	void setCookTime(int cookTime);

	@Invoker("getFuelTime")
	int callGetFuelTime(ItemStack fuel);

	@Invoker("isBurning")
	boolean callIsBurning();

}

