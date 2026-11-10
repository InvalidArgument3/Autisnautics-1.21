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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.neep.meatlib.util.NbtSerialisable;
import com.neep.neepmeat.NeepMeat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public interface EntityImplant extends NbtSerialisable
{
    Multimap<Attribute, AttributeModifier> NO_MODIFIERS = ArrayListMultimap.create();

    ResourceLocation getId();

    void tick(Runnable remove);

    default void clientTick() {}

    // Post movement
//    default void endTick() {};

    default Multimap<Attribute, AttributeModifier> getModifiers()
    {
        return NO_MODIFIERS;
    }

    default void onPlayerInit() {}

    default void onPlayerRemove() {}

    default void onDeath(Runnable remove) {}

    default void onInstall() {}

    default void onUninstall() {}

    default float getProtectionAmount(DamageSource source, float amount) { return 0; }

    ResourceLocation EMPTY_ID = new ResourceLocation(NeepMeat.NAMESPACE, "empty");

    EntityImplant DEFAULT = new EntityImplant()
    {
        @Override
        public ResourceLocation getId()
        {
            return EMPTY_ID;
        }

        @Override
        public void tick(Runnable remove)
        {

        }

        @Override
        public CompoundTag writeNbt(CompoundTag nbt)
        {
            return nbt;
        }

        @Override
        public void readNbt(CompoundTag nbt)
        {

        }
    };

}
