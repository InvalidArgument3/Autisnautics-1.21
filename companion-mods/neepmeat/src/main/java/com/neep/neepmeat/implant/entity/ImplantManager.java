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

package com.neep.neepmeat.implant.entity;

import com.neep.meatweapons.meatgun.AmmunitionType;
import dev.onyxstudios.cca.api.v3.component.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface ImplantManager extends Component
{
    boolean installImplant(ResourceLocation implantId);
    boolean removeImplant(ResourceLocation id);

    @Nullable <T extends EntityImplant> T getImplant(ResourceLocation id);
    Set<ResourceLocation> getInstalled();
    default boolean isInstalled(ResourceLocation id)
    {
        return getInstalled().contains(id);
    }

    default void sync() {};

    @Nullable
    default AmmunitionType fuelType() { return null; }

    default boolean replace(ResourceLocation id, Consumer<ItemStack> consumer)
    {
        if (removeImplant(id))
        {
            var i = ImplantRegistry.getItem(id);
            if (i != null)
                consumer.accept(i.asItem().getDefaultInstance());

            return true;
        }

        return false;
    }

    ImplantManager EMPTY = new ImplantManager()
    {
        @Override
        public void readFromNbt(CompoundTag tag)
        {

        }

        @Override
        public void writeToNbt(CompoundTag tag)
        {

        }

        @Override
        public boolean installImplant(ResourceLocation implantId)
        {
            return false;
        }

        @Override
        public boolean removeImplant(ResourceLocation id)
        {
            return false;
        }

        @Override
        public <T extends EntityImplant> @Nullable T getImplant(ResourceLocation id)
        {
            return null;
        }

        @Override
        public Set<ResourceLocation> getInstalled()
        {
            return Set.of();
        }
    };
}
