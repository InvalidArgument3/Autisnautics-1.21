/*
 * Copyright (c) MeatWheeze 2026.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.meatlib.api.network;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;

public class CompositeParamCodec<T> implements ParamCodec<T>
{
    private final Class<T> clazz;
    private final List<Function<T, ?>> getters;
    private final List<ParamCodec<?>> paramCodecs;
    private final Constructor<T> constructor;

    public CompositeParamCodec(Class<T> clazz, List<Function<T, ?>> getters, List<ParamCodec<?>> codecs)
    {
        assert getters.size() == codecs.size();
        this.clazz = clazz;
        this.getters = getters;
        this.paramCodecs = codecs;

        Class<?>[] parameters = paramCodecs.stream().map(ParamCodec::clazz).toArray(Class[]::new);
        try
        {
            this.constructor = clazz.getConstructor(parameters);
        }
        catch (NoSuchMethodException e)
        {
            throw new IllegalArgumentException("Constructor not found in given class.");
        }
    }

    public Class<T> clazz()
    {
        return clazz;
    }

    @Override
    public T decode(FriendlyByteBuf buf)
    {
        Object[] arguments = new Object[paramCodecs.size()];
        for (int i = 0; i < paramCodecs.size(); i++)
        {
            arguments[i] = paramCodecs.get(i).decode(buf);
        }

        try
        {
            return constructor.newInstance(arguments);
        }
        catch (IllegalAccessException | InstantiationException e)
        {
            throw new IllegalArgumentException("This should probably not happen.");
        }
        catch (InvocationTargetException e)
        {
            throw new IllegalStateException(e.getCause());
        }
    }

    @Override
    public void encode(T t, FriendlyByteBuf buf)
    {
        for (int i = 0; i < paramCodecs.size(); i++)
        {
            // Shhhh
            ParamCodec<Object> codec = (ParamCodec<Object>) paramCodecs.get(i);

            codec.encode(getters.get(i).apply(t), buf);
        }
    }

    public static <T> Builder<T> builder(Class<T> clazz)
    {
        return new Builder<>(clazz);
    }

    public static class Builder<T>
    {
        private final Class<T> clazz;

        private final List<Function<T, ?>> getters = new ObjectArrayList<>();
        private final List<ParamCodec<?>> codecs = new ObjectArrayList<>();

        private Builder(Class<T> clazz)
        {
            this.clazz = clazz;
        }

        public <T1> Builder<T> param(Function<T, T1> getter, ParamCodec<T1> codec)
        {
            getters.add(getter);
            codecs.add(codec);
            return this;
        }

        public CompositeParamCodec<T> build()
        {
            return new CompositeParamCodec<>(clazz, getters, codecs);
        }
    }
}
