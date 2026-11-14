/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.network;

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class ChannelFormatFormatImpl<T> implements ChannelFormat<T> {
    private final List<ParamCodec<Object>> codecs;
    private final List<Class<?>> invokeParameters;
    private final Method method;

    private ChannelFormatFormatImpl(Class<T> clazz, List<ParamCodec<?>> codecs) {
        this.codecs = codecs.stream().map(codec -> {
            @SuppressWarnings("unchecked")
            ParamCodec<Object> cast = (ParamCodec<Object>) codec;
            return cast;
        }).toList();
        this.invokeParameters = codecs.stream().<Class<?>>map(ParamCodec::clazz).toList();
        this.method = findMethod(clazz, invokeParameters.toArray(Class[]::new));
    }

    public static <T> Builder<T> builder(Class<T> clazz) {
        return new Builder<>(clazz);
    }

    @Override
    public T emitter(Sender<T> sender) {
        @SuppressWarnings("unchecked")
        T proxy = (T) Proxy.newProxyInstance(method.getDeclaringClass().getClassLoader(), new Class[]{method.getDeclaringClass()}, (unused, called, args) -> {
            if (called.getName().equals(method.getName())) {
                send(sender, args);
                return null;
            }
            return switch (called.getName()) {
                case "toString" -> "proxy of " + method;
                case "equals" -> false;
                case "hashCode" -> 0;
                default -> throw new IllegalStateException("Unexpected method " + called.getName());
            };
        });
        return proxy;
    }

    public void send(Sender<T> sender, Object... objects) {
        if (!invokeParameters.isEmpty() && (objects == null || objects.length != invokeParameters.size())) {
            throw new IllegalStateException("Incorrect number of parameters");
        }
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        if (objects != null) {
            for (int i = 0; i < objects.length; i++) {
                codecs.get(i).encode(objects[i], buf);
            }
        }
        sender.send(buf);
    }

    @Override
    public void receive(T listener, FriendlyByteBuf buf, Executor executor) {
        Object[] arguments = new Object[invokeParameters.size()];
        for (int i = 0; i < codecs.size(); i++) {
            arguments[i] = codecs.get(i).decode(buf);
        }
        executor.execute(() -> {
            try {
                method.invoke(listener, arguments);
            } catch (IllegalAccessException exception) {
                throw new IllegalArgumentException(exception);
            } catch (InvocationTargetException exception) {
                throw new IllegalStateException(exception.getCause());
            }
        });
    }

    private static Method findMethod(Class<?> clazz, Class<?>[] invokeParameters) {
        for (Method candidate : clazz.getMethods()) {
            if (parametersMatch(candidate, invokeParameters)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("Method not found on " + clazz.getName());
    }

    private static boolean parametersMatch(Method method, Class<?>[] invokeParameters) {
        Class<?>[] methodParameters = method.getParameterTypes();
        if (methodParameters.length != invokeParameters.length) {
            return false;
        }
        for (int i = 0; i < methodParameters.length; i++) {
            if (!methodParameters[i].isAssignableFrom(invokeParameters[i])) {
                return false;
            }
        }
        return true;
    }

    public static class Builder<T> {
        private final Class<T> clazz;
        private final List<ParamCodec<?>> codecs = new ArrayList<>();

        public Builder(Class<T> clazz) {
            this.clazz = clazz;
        }

        public <V> Builder<T> param(ParamCodec<V> codec) {
            codecs.add(codec);
            return this;
        }

        public ChannelFormat<T> build() {
            return new ChannelFormatFormatImpl<>(clazz, codecs);
        }
    }
}
