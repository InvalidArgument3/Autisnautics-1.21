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

package com.neep.neepmeat.api.plc.recipe;

import com.mojang.serialization.Codec;
import com.neep.meatlib.util.MeatLibCodecs;
import com.neep.neepmeat.NeepMeat;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import java.util.List;
import java.util.function.Function;

public interface ManufactureStep<T>
{
    Registry<Provider<?>> REGISTRY = FabricRegistryBuilder.createSimple(
        Provider.asClass(),
        ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "step_provider")).buildAndRegister();

    Codec<ManufactureStep<?>> CODEC = new MeatLibCodecs.JankDispatch<>("id", REGISTRY.byNameCodec(), p -> (Codec<ManufactureStep<?>>) p.codec()).codec();

    Codec<List<ManufactureStep<?>>> LIST_CODEC = Codec.list(CODEC);

    static <T> Provider<T> register(ResourceLocation id, Provider<T> provider)
    {
        return Registry.register(REGISTRY, id, provider);
    }

    void mutate(T t);

    ResourceLocation getId();

    Component getName();

    void appendText(List<Component> tooltips);

    CompoundTag toNbt();

    boolean equalsOther(ManufactureStep<?> other);

    static boolean matches(ManufactureStep<?> template, ManufactureStep<?> b)
    {
        return (template == b) || (template != null && template.equalsOther(b));
    }

    interface Provider<T>
    {
        @SuppressWarnings("unchecked")
        static Class<Provider<?>> asClass()
        {
            return (Class<Provider<?>>) (Object) Provider.class;
        }

        static <T> Provider<T> of(Function<CompoundTag, ManufactureStep<T>> nbtConstructor, Codec<? extends ManufactureStep<T>> codec)
        {
            return new Provider<T>()
            {
                @Override
                public ManufactureStep<T> create(CompoundTag nbt)
                {
                    return nbtConstructor.apply(nbt);
                }

                @Override
                public Codec<? extends ManufactureStep<T>> codec()
                {
                    return codec;
                }
            };
        }

        ManufactureStep<T> create(CompoundTag nbt);

        Codec<? extends ManufactureStep<T>> codec();
    }
}
