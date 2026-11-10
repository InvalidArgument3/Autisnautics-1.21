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

package com.neep.neepmeat.advancement;

import com.google.gson.JsonObject;
import com.neep.neepmeat.NeepMeat;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class VivisectionCriterion extends SimpleCriterionTrigger<VivisectionCriterion.Conditions>
{
    public static final ResourceLocation ID = new ResourceLocation(NeepMeat.NAMESPACE, "vivisection");

    public void trigger(ServerPlayer player, Entity usedOn)
    {
        this.trigger(player, (conditions) -> conditions.matches(usedOn));
    }

    @Override
    protected Conditions createInstance(JsonObject obj, ContextAwarePredicate playerPredicate, DeserializationContext predicateDeserializer)
    {
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.tryParse(GsonHelper.getAsString(obj, "entity_type"))).orElseThrow();

        return new Conditions(entityType);
    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    public static final class Conditions extends AbstractCriterionTriggerInstance
    {
        private final EntityType<?> entityType;

        public Conditions(EntityType<?> entityType)
        {
            super(ID, ContextAwarePredicate.ANY);
            this.entityType = entityType;
        }

        public boolean matches(Entity entity)
        {
            return entity.getType().equals(entityType);
        }
    }
}
