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

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.init.NMSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class ClawsImplant implements EntityImplant
{
    public static final ResourceLocation ID = new ResourceLocation(NeepMeat.NAMESPACE, "claws");
    private final Entity entity;

    private static final Multimap<Attribute, AttributeModifier> MODIFIERS = ImmutableMultimap.<Attribute, AttributeModifier>builder()
            .put(Attributes.ATTACK_DAMAGE, new AttributeModifier("Claws damage modifier", 4, AttributeModifier.Operation.ADDITION))
            .build();

    public ClawsImplant(Entity entity)
    {
        this.entity = entity;
    }

    public static void onTryAttack(LivingEntity livingEntity, Entity target)
    {
        if (livingEntity.getMainHandItem().isEmpty())
        {
            var component = NMComponents.IMPLANT_MANAGER.getNullable(livingEntity);
            if (component != null)
            {
                var implant = component.getImplant(ClawsImplant.ID);
                if (implant != null)
                    livingEntity.level().playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), NMSounds.SCALPEL_HIT, SoundSource.PLAYERS, 1, 1);
            }
        }
    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getModifiers()
    {
        // Mob implants aren't ticked, so we don't bother with the empty hand logic
        if (!(entity instanceof Player))
        {
            return MODIFIERS;
        }

        return NO_MODIFIERS;
    }

    @Override
    public void tick(Runnable remove)
    {
        if (!entity.level().isClientSide() && entity instanceof Player player)
        {
            if (player.getMainHandItem().isEmpty())
            {
                player.getAttributes().addTransientAttributeModifiers(MODIFIERS);
            }
            else
            {
                player.getAttributes().removeAttributeModifiers(MODIFIERS);
            }
        }
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
}
