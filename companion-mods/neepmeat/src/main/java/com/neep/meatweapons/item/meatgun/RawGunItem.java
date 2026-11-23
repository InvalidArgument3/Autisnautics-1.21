/*
 * Copyright (c) MeatWheeze 2026.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.meatweapons.item.meatgun;

import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatweapons.MWItems;
import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.implant.AmmunitionGeneratorImplant;
import com.neep.meatweapons.init.MWComponents;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.meatgun.module.RawGunBaseModule;
import net.minecraft.world.item.ItemStack;
import com.neep.meatweapons.meatgun.module.MeatgunModule;
import com.neep.meatweapons.meatgun.module.MeatgunModules;

public class RawGunItem extends MeatgunPistolItem
{
    public RawGunItem(TooltipSupplier tooltipSupplier, Properties settings)
    {
        super(tooltipSupplier, settings);
    }

    @Override
    public MeatgunModule createBase(RootModuleHolder.Listener listener)
    {
        return new RawGunBaseModule(listener);
    }

    @Override
    public int getMaxComplexity(ItemStack stack)
    {
        return MeatWeapons.CONFIG.meatgun.rawGunMaxComplexity.getRealValue();
    }

    @Override
    public ItemStack getDefaultInstance()
    {
        ItemStack stack = super.getDefaultInstance();

        var meatgun = MWComponents.MEATGUN.get(stack);
        var implants = meatgun.getRootHolder().coreImplants();
        implants.installImplant(AmmunitionGeneratorImplant.ID);

        return stack;
    }

    @Override
    public void createDefaultTree(MeatgunModule root, RootModuleHolder.Listener listener)
    {
        if (root instanceof RawGunBaseModule baseModule)
        {
            baseModule.getFront().set(MeatgunModules.PISTOL.create(listener, root, MWItems.PISTOL.getDefaultInstance()));
        }
    }
}
