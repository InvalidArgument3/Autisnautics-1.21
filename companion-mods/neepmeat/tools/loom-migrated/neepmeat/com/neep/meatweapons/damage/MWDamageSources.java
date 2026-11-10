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

package com.neep.meatweapons.damage;

import com.neep.meatweapons.MeatWeapons;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class MWDamageSources
{
    public static final ResourceKey<DamageType> BULLET = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(MeatWeapons.NAMESPACE, "bullet"));
    public static final ResourceKey<DamageType> BLOOD_BULLET = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(MeatWeapons.NAMESPACE, "blood_bullet"));
    public static final ResourceKey<DamageType> MEATGUN_EXPLOSION = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(MeatWeapons.NAMESPACE, "meatgun_explosion"));

    public static final TagKey<DamageType> IGNORES_ITEMS = TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(MeatWeapons.NAMESPACE, "ignores_items"));

    public static Holder<DamageType> get(Level world, ResourceKey<DamageType> key)
    {
        return world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key);
    }

    public static DamageSource of(ResourceKey<DamageType> key, Level world, @Nullable Entity attacker)
    {
        return new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key), attacker);
    }

    public static DamageSource of(ResourceKey<DamageType> key, Level world)
    {
        return new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key));
    }
}
