/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.meatlib.config;

import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.SerializedName;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Walks a Kaleido-shaped config object and binds each {@link TrackedValue} to a NeoForge spec.
 * Values load from the mod's config file after the spec is registered.
 */
public final class ConfigFiles {
    private ConfigFiles() {}

    public static void register(ModContainer container, ModConfig.Type type, String fileName, Object config) {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        bind(builder, config);
        container.registerConfig(type, builder.build(), fileName);
    }

    private static void bind(ModConfigSpec.Builder builder, Object config) {
        for (Field field : config.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            Object value;
            try {
                value = field.get(config);
            } catch (IllegalAccessException e) {
                continue;
            }
            if (value == null) {
                continue;
            }
            String name = field.isAnnotationPresent(SerializedName.class)
                    ? field.getAnnotation(SerializedName.class).value()
                    : field.getName();
            if (field.isAnnotationPresent(Comment.class)) {
                builder.comment(field.getAnnotation(Comment.class).value());
            }
            if (value instanceof TrackedValue<?> tracked) {
                bindValue(builder, name, tracked);
            } else if (!field.getType().isPrimitive() && !field.getType().getName().startsWith("java.")) {
                builder.push(name);
                bind(builder, value);
                builder.pop();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void bindValue(ModConfigSpec.Builder builder, String name, TrackedValue<?> tracked) {
        Object fallback = tracked.getDefaultValue();
        if (fallback instanceof Boolean bool) {
            ModConfigSpec.ConfigValue<Boolean> value = builder.define(name, bool);
            ((TrackedValue<Boolean>) tracked).bind(value::get);
        } else if (fallback instanceof Integer integer) {
            ModConfigSpec.IntValue value = builder.defineInRange(name, integer, Integer.MIN_VALUE, Integer.MAX_VALUE);
            ((TrackedValue<Integer>) tracked).bind(value::get);
        } else if (fallback instanceof Long number) {
            ModConfigSpec.ConfigValue<Long> value = builder.define(name, number);
            ((TrackedValue<Long>) tracked).bind(value::get);
        } else if (fallback instanceof Float number) {
            ModConfigSpec.ConfigValue<Double> value = builder.define(name, number.doubleValue());
            ((TrackedValue<Float>) tracked).bind(() -> value.get().floatValue());
        } else if (fallback instanceof Double number) {
            ModConfigSpec.ConfigValue<Double> value = builder.define(name, number);
            ((TrackedValue<Double>) tracked).bind(value::get);
        } else if (fallback instanceof String text) {
            ModConfigSpec.ConfigValue<String> value = builder.define(name, text);
            ((TrackedValue<String>) tracked).bind(value::get);
        }
    }
}
