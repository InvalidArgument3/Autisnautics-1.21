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

package com.neep.meatlib.recipe.ingredient.entity;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

// Everything is horrible
public final class TypeFilterEntityInput extends EntityRecipeInput
{
    public static final Codec<TypeFilterEntityInput> CODEC =
            Codec.STRING.xmap(TypeFilterEntityInput::get, f -> f.name.name().toLowerCase(Locale.ROOT)).fieldOf("filter").codec();

    private final Values name;
    private final Predicate<Entity> predicate;

    private TypeFilterEntityInput(Values name, Predicate<Entity> predicate)
    {
        this.name = name;
        this.predicate = predicate;
    }

    @Override
    public boolean test(Entity entity)
    {
        return predicate.test(entity);
    }

    public static final TypeFilterEntityInput LIVING = new TypeFilterEntityInput(Values.LIVING, e -> e instanceof LivingEntity);
    public static final TypeFilterEntityInput PLAYER = new TypeFilterEntityInput(Values.PLAYER, e -> e instanceof Player);
    public static final TypeFilterEntityInput MOB = new TypeFilterEntityInput(Values.MOB, e -> e instanceof Mob);

    public String getName()
    {
        return name.name().toLowerCase(Locale.ROOT);
    }

    public enum Values
    {
        LIVING(() -> TypeFilterEntityInput.LIVING),
        PLAYER(() -> TypeFilterEntityInput.PLAYER),
        MOB(() -> TypeFilterEntityInput.MOB)
        ;

        private final Supplier<TypeFilterEntityInput> value;

        Values(Supplier<TypeFilterEntityInput> value)
        {
            this.value = value;
        }

        TypeFilterEntityInput get()
        {
            return value.get();
        }
    }

    public static TypeFilterEntityInput get(String name)
    {
        try
        {
            var thing = Values.valueOf(name.toUpperCase(Locale.ROOT));
            return thing.get();
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("filter is not one in " + Arrays.toString(Values.values()));
        }
    }
}
