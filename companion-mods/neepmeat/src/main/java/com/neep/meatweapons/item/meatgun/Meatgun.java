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

import com.neep.meatweapons.meatgun.RootModuleCache;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.meatgun.module.MeatgunModule;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;

public interface Meatgun
{
    MeatgunModule createBase(RootModuleHolder.Listener listener);

    /// Override to preinstall certain modules. For implants, use getDefaultStack.
    default void createDefaultTree(MeatgunModule root, RootModuleHolder.Listener listener) { }

    /**
     * @return An anonymous instance of {@link Supplier<MeatgunAnimationManager>} so that it won't get loaded on the server.
     */
    Supplier<Object> createAnimationManager();

    int getMaxComplexity(ItemStack stack);

    default float getWorldHeightOffset() { return 0; }

    RootModuleCache getCache();

    default boolean supportsAds() { return true; }
}
