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

package com.neep.neepmeat.init;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.processing.DrillHarvesting;
import com.neep.neepmeat.meatgun.ModuleLootCondition;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class NMLootConditions
{
    public static final LootItemConditionType MEATGUN_MODULE = new LootItemConditionType(new ModuleLootCondition.CodecSerialiser<>(ModuleLootCondition.CODEC));

    public static <T> Codec<T> thing(Serializer<T> s)
    {
        // I have no idea where to get the serialisation context objects from
        // This won't be used though.
        return ExtraCodecs.JSON.xmap(j -> s.deserialize((JsonObject) j, null), t -> {
            JsonObject o = new JsonObject();
            s.serialize(o, t, null);
            return o;
        });
    }

    public static final Codec<LootItemCondition> LOOT_CONDITION_CODEC =
                    BuiltInRegistries.LOOT_CONDITION_TYPE.byNameCodec().dispatch(
                            "type",
                            LootItemCondition::getType,
                            type -> NMLootConditions.thing(type.getSerializer()));

    public static final LootItemFunctionType DRILL_HARVEST = new LootItemFunctionType(new ModuleLootCondition.CodecSerialiser<DrillHarvesting.DrillHarvestingLootFunction>(
            DrillHarvesting.DrillHarvestingLootFunction.CODEC));

    public static void init()
    {
        register(new ResourceLocation(NeepMeat.NAMESPACE, "meatgun_module"), MEATGUN_MODULE);

        register(new ResourceLocation(NeepMeat.NAMESPACE, "drill_harvest"), DRILL_HARVEST);
    }

    public static void register(ResourceLocation id, LootItemConditionType type)
    {
        Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, id, type);
    }

    public static void register(ResourceLocation id, LootItemFunctionType type)
    {
        Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, id, type);
    }
}
