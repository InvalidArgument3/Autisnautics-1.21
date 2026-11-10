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

package com.neep.meatlib.api.network;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;

// More shitty reflection.
/// A ParamCodec for records. Only works for records.
public class RecordParamCodec<T> implements ParamCodec<T>
{
    private final Class<T> clazz;
    private final List<ParamCodec<?>> paramCodecs;
    private final Constructor<T> constructor;
    private final List<Field> recordFields = new ObjectArrayList<>();

    public RecordParamCodec(Class<T> clazz, List<ParamCodec<?>> paramCodecs)
    {
        this.clazz = clazz;
        this.paramCodecs = paramCodecs;

        Class<?>[] parameters = paramCodecs.stream().map(ParamCodec::clazz).toArray(Class[]::new);
        try
        {
            this.constructor = clazz.getConstructor(parameters);
        }
        catch (NoSuchMethodException e)
        {
            throw new IllegalArgumentException("Constructor not found in given class.");
        }

        RecordComponent[] recordComponents = clazz.getRecordComponents();

        if (paramCodecs.size() != recordComponents.length)
        {
            throw new IllegalArgumentException(String.format("Number of ParamCodecs does not match number of fields in record '%s'", clazz));
        }

        for (int i = 0; i < recordComponents.length; ++i)
        {
            RecordComponent comp = recordComponents[i];
            ParamCodec<?> paramCodec = paramCodecs.get(i);

            if (!paramCodec.clazz().isAssignableFrom(comp.getType()))
                throw new IllegalArgumentException(String.format("Record component of type '%s' does not match ParamCodec class '%s'",
                        comp.getType(), paramCodec.clazz()));

            try
            {
                Field field = clazz.getDeclaredField(comp.getName());
                field.setAccessible(true);

                recordFields.add(field);
            }
            catch (NoSuchFieldException e)
            {
                throw new IllegalArgumentException(String.format("No field named '%s' in class '%s'", comp.getName(), clazz));
            }
        }
    }

    @Override
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
        try
        {
            for (int i = 0; i < paramCodecs.size(); i++)
            {
                // Shhhh
                ParamCodec<Object> codec = (ParamCodec<Object>) paramCodecs.get(i);

                Field field = recordFields.get(i);

                codec.encode(field.get(t), buf);
            }
        }
            catch (IllegalAccessException e)
        {
            throw new IllegalStateException(String.format("Unable to access a field in '%s'", clazz));
        }
    }

    public static <T> Builder<T> builder(Class<T> tClass)
    {
        return new Builder<>(tClass);
    }

    public static class Builder<T>
    {
        private final Class<T> clazz;
        private final List<ParamCodec<?>> paramCodecs = new ObjectArrayList<>();

        public Builder(Class<T> clazz)
        {
            this.clazz = clazz;
            if (!clazz.isRecord())
                throw new IllegalArgumentException("Class '" + clazz + "' is not a record");
        }

        public <V> Builder<T> param(ParamCodec<V> paramCodec)
        {
            paramCodecs.add(paramCodec);
            return this;
        }

        public RecordParamCodec<T> build()
        {
            return new RecordParamCodec<>(clazz, paramCodecs);
        }
    }
}
