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

package com.neep.neepmeat.meatgun;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.neep.meatweapons.init.MWComponents;
import com.neep.meatweapons.meatgun.module.MeatgunModule;
import com.neep.meatweapons.meatgun.module.MeatgunModules;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMLootConditions;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public record ModuleLootCondition(TagKey<MeatgunModule.Type<? extends MeatgunModule>> tagKey) implements LootItemCondition
{
    public static final Codec<ModuleLootCondition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    TagKey.hashedCodec(MeatgunModules.REGISTRY.key()).fieldOf("tag").forGetter(ModuleLootCondition::tagKey))
//                    MeatgunModules.REGISTRY.getCodec().listOf().xmap(Set::copyOf, List::copyOf).fieldOf("module_type").forGetter(ModuleLootCondition::type))
                    .apply(instance, ModuleLootCondition::new));

    @Override
    public LootItemConditionType getType()
    {
        return NMLootConditions.MEATGUN_MODULE;
    }

    @Override
    public boolean test(LootContext lootContext)
    {
        return test(lootContext.getParamOrNull(LootContextParams.TOOL), tagKey);
    }

    public static boolean test(@Nullable ItemStack tool, TagKey<MeatgunModule.Type<? extends MeatgunModule>> tag)
    {
        return test(tool, m ->
        {
//            System.out.println(" -- testing module %s against tag %s".formatted(m, tag));
//            NeepMeat.LOGGER.info(" -- modules in tag: \n%s".formatted(MeatgunModules.REGISTRY.getEntryList(tag).orElseThrow().stream().map(
//                    e -> "  %s: %s\n".formatted(e.getKey().orElseThrow().getValue(), e.value())).collect(Collectors.joining())
//            ));
            return MeatgunModules.REGISTRY.wrapAsHolder(m).is(tag);
        });
    }

    public static boolean test(@Nullable ItemStack tool, Predicate<MeatgunModule.Type<? extends MeatgunModule>> predicate)
    {
        if (tool != null)
        {
            var component = MWComponents.MEATGUN.getNullable(tool);
//            System.out.println(" -- testing component " + component);
            if (component != null)
            {
                return component.getRootHolder().moduleCache().getModules().stream().anyMatch(
                        m ->
                                predicate.test(m.getType()));
            }

        }
        return false;
    }

    public record CodecSerialiser<T>(Codec<T> codec) implements Serializer<T>
    {
        public void serialize(JsonObject json, T object, JsonSerializationContext context)
        {
            codec.encode(object, JsonOps.INSTANCE, json);
        }

        public T deserialize(JsonObject json, JsonDeserializationContext context)
        {
            return codec.parse(JsonOps.INSTANCE, json).result().orElseThrow();
        }
    }
}
