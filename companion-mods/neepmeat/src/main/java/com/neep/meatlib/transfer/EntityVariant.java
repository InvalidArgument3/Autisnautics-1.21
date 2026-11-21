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

package com.neep.meatlib.transfer;

import com.neep.meatlib.transfer.TransferVariant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class EntityVariant<T extends Entity> implements TransferVariant<EntityType<T>>
{
    @Nullable
    protected final EntityType<T> entityType;
    @Nullable
    protected final CompoundTag nbt;
    private final int hashCode;

    protected static final EntityVariant<?> BLANK = new EntityVariant<>(null, null);

    protected EntityVariant(@Nullable EntityType<T> entityType, @Nullable CompoundTag nbt)
    {
        this.entityType = entityType;

        if (nbt != null)
            this.nbt = nbt.copy();
        else
            this.nbt = null;

        this.hashCode = Objects.hash(entityType, nbt);
    }

    public static <T extends Entity> EntityVariant<T> of(EntityType<T> type)
    {
        Objects.requireNonNull(type, "EntityType may not be null.");

        // TODO: Store specific entity details rather than just type
        return new EntityVariant<>(type, null);
    }

    public static <T extends Entity> EntityVariant<T> of(Entity entity)
    {
        Objects.requireNonNull(entity, "Entity may not be null.");

        // TODO: Store specific entity details rather than just type
        return new EntityVariant<>((EntityType<T>) entity.getType(), null);
    }

    public static EntityVariant<?> getBlank()
    {
        return BLANK;
    }

    @Override
    public boolean isBlank()
    {
        return entityType == null;
    }

    @Override
    public EntityType<T> getObject()
    {
        return entityType;
    }

    @Override
    public @Nullable CompoundTag getNbt()
    {
        return nbt;
    }

    @Override
    public CompoundTag toNbt()
    {
        throw new NotImplementedException();
//        return ;
    }

    @Override
    public void toPacket(FriendlyByteBuf buf)
    {
        throw new NotImplementedException();
    }

    @Override
    public String toString()
    {
        return "FluidVariantImpl{fluid=" + entityType + ", tag=" + nbt + '}';
    }

    @Override
    public int hashCode()
    {
        return hashCode;
    }
}
