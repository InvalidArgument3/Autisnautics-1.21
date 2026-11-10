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

package com.neep.neepmeat.plc.component;

import com.neep.neepmeat.NeepMeat;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface MutateInPlace<T>
{
    BlockApiLookup<MutateInPlace<ItemStack>, Void> ITEM = BlockApiLookup.get(
            new ResourceLocation(NeepMeat.NAMESPACE, "mutate_in_place_entity"),
            asClass(), Void.class
    );

    BlockApiLookup<MutateInPlace<Entity>, Void> ENTITY = BlockApiLookup.get(
            new ResourceLocation(NeepMeat.NAMESPACE, "mutate_in_place_item"),
            asClass(), Void.class
    );

//    BlockApiLookup<MutateInPlace<?>, Void> ANY = BlockApiLookup.get(
//            new Identifier(NeepMeat.NAMESPACE, "mutate_in_place_any"),
//            asClass1(), Void.class
//    );

    @SuppressWarnings("unchecked")
    static <T> Class<MutateInPlace<T>> asClass()
    {
        return (Class<MutateInPlace<T>>) (Object) TableComponent.class;
    }

    @Nullable
    T get();

    void set(T t);

    BlockPos getOutputPos();

    Level getWorld();

    default void setResult(int consume, T t)
    {
        set(t);
    }

    static <T> MutateInPlace<T> of(T prev, Level world, BlockPos pos)
    {
        return new Simple<>(prev, world, pos);
    }

    class Simple<T> implements MutateInPlace<T>
    {
        private T object;
        private final Level world;
        private final BlockPos pos;

        public Simple(T prev, Level world, BlockPos pos)
        {
            this.object = prev;
            this.world = world;
            this.pos = pos;
        }

        @Override
        public @Nullable T get()
        {
            return object;
        }

        @Override
        public void set(T t)
        {
            this.object = t;
        }

        @Override
        public BlockPos getOutputPos()
        {
            return null;
        }

        @Override
        public Level getWorld()
        {
            return null;
        }
    }
}
