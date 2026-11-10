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

package com.neep.meatlib.recipe.ingredient;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

public class NbtMatcher
{
    public static NbtMatcher ANY = new NbtMatcher(null);

    public static final Codec<NbtMatcher> CODEC = CompoundTag.CODEC.xmap(NbtMatcher::new, NbtMatcher::toNbt);

    @Nullable private final CompoundTag nbt;
    private final List<Pair<String, Tag>> elements;

    public NbtMatcher(@Nullable CompoundTag nbt)
    {
        elements = new ObjectArrayList<>();
        if (nbt != null)
        {
            for (var key : nbt.getAllKeys())
            {
                elements.add(Pair.of(key, nbt.get(key)));
            }
        }

        if (elements.isEmpty())
            this.nbt = null;
        else
            this.nbt = nbt;
    }

    public static NbtMatcher read(FriendlyByteBuf buf)
    {
        return new NbtMatcher(buf.readNbt());
    }

    public boolean test(@Nullable CompoundTag toTest)
    {
        if (empty())
            return true;

        // Not empty, so the toTest must also not be empty
        if (toTest == null || toTest.isEmpty())
            return false;

        for (var element : elements)
        {
            if (!Objects.equals(toTest.get(element.key()), element.value()))
                return false;
        }

        return true;
    }

    public boolean empty()
    {
        return nbt == null;
    }

    public CompoundTag toNbt()
    {
        return nbt != null ? nbt : new CompoundTag();
    }

    @Nullable
    public CompoundTag getNbt()
    {
        return nbt;
    }

    public void write(FriendlyByteBuf buf)
    {
        buf.writeNbt(toNbt());
    }
}
