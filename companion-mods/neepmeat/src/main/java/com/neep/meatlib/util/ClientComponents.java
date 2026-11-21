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

package com.neep.meatlib.util;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ClientComponents
{
    private static final Map<Object, Constructor<?>> MAP = new HashMap<>();

    public static <T extends BlockEntity> void register(BlockEntityType<T> type, Constructor<T> constructor)
    {
        MAP.put(type, constructor);
    }

    public static <T extends Entity> void register(EntityType<T> type, Constructor<T> constructor)
    {
        MAP.put(type, constructor);
    }

    public static <T extends BlockEntity, C extends ClientComponent> ClientComponent get(T be)
    {
        Constructor<T> constructor = (Constructor<T>) MAP.get(be.getType());
        if (constructor == null)
            throw new IllegalStateException("No client component registered for type '" + be.getType() + "'");

        return constructor.get(be);
    }

    public static <T extends Entity, C extends ClientComponent> ClientComponent get(T entity)
    {
        Constructor<T> constructor = (Constructor<T>) MAP.get(entity.getType());
        if (constructor == null)
            throw new IllegalStateException("No client component registered for type '" + entity.getType() + "'");

        return constructor.get(entity);
    }

    public interface Holder<T>
    {
        ClientComponent get();
    }

    public static class BlockEntityHolder<T extends BlockEntity> implements Holder<T>
    {
        private final T be;
        @Nullable private ClientComponent component = null;

        public BlockEntityHolder(T be)
        {
            this.be = be;
        }


        public ClientComponent get()
        {
            if (component == null)
            {
                component = ClientComponents.get(be);
            }
            return component;
        }
    }

    public static class EntityHolder<T extends Entity> implements Holder<T>
    {
        private final T entity;
        @Nullable private ClientComponent component = null;

        public EntityHolder(T entity)
        {
            this.entity = entity;
        }

        public ClientComponent get()
        {
            if (component == null)
            {
                component = ClientComponents.get(entity);
            }
            return component;
        }
    }

    @FunctionalInterface
    public interface Constructor<T>
    {
        ClientComponent get(T t);
    }
}
