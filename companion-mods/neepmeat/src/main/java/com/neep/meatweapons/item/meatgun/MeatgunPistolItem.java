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

package com.neep.meatweapons.item.meatgun;

import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.client.meatgun.animation.MeatgunAnimation;
import com.neep.meatweapons.client.meatgun.animation.PistolReloadMeatgunAnimation;
import com.neep.meatweapons.meatgun.RootModuleCache;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.meatgun.module.BasePistolModule;
import com.neep.meatweapons.meatgun.module.MeatgunModule;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;

public class MeatgunPistolItem extends MeatgunItem
{
    private final RootModuleCache cache = new RootModuleCache();

    public MeatgunPistolItem(TooltipSupplier tooltipSupplier, Properties settings)
    {
        super(tooltipSupplier, settings.stacksTo(1));
    }

    @Override
    public MeatgunModule createBase(RootModuleHolder.Listener listener)
    {
        return new BasePistolModule(listener);
    }

    @Override
    public Supplier<Object> createAnimationManager()
    {
        return new Supplier<Object>()
        {
            @Override
            public Object get()
            {
                return new MeatgunAnimationManager(MeatgunAnimation.EMPTY)
                        .add("reload", new PistolReloadMeatgunAnimation());
            }
        };
    }

    @Override
    public float getWorldHeightOffset()
    {
        return -1 / 16f;
    }

    @Override
    public int getMaxComplexity(ItemStack stack)
    {
        return MeatWeapons.CONFIG.meatgun.pistolBaseMaxComplexity.getRealValue();
    }

    @Override
    public RootModuleCache getCache()
    {
        return cache;
    }
}
