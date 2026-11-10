/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

/**
 * Upstream extended Fabric's block settings. NeoForge's {@link BlockBehaviour.Properties}
 * is not a useful superclass, so this is a builder that unwraps with {@link #properties()}.
 * Tag membership is recorded for the next phase's datagen. It does not inject tags by itself.
 */
public final class MeatlibBlockSettings {
    private final BlockBehaviour.Properties properties;
    private final Set<TagKey<Block>> tags = new HashSet<>();

    private MeatlibBlockSettings(BlockBehaviour.Properties properties) {
        this.properties = properties;
    }

    public static MeatlibBlockSettings create() {
        return new MeatlibBlockSettings(BlockBehaviour.Properties.of());
    }

    /** Loom sources call {@code of()}. */
    public static MeatlibBlockSettings of() {
        return create();
    }

    public static MeatlibBlockSettings copyOf(Block block) {
        return new MeatlibBlockSettings(BlockBehaviour.Properties.ofFullCopy(block));
    }

    /** Upstream passes the parent {@link BlockBehaviour.Properties} into a structure block. */
    public static BlockBehaviour.Properties copyOf(BlockBehaviour.Properties properties) {
        return properties;
    }

    public MeatlibBlockSettings destroyTime(float destroyTime) {
        properties.destroyTime(destroyTime);
        return this;
    }

    public MeatlibBlockSettings noOcclusion() {
        return nonOpaque();
    }

    public MeatlibBlockSettings strength(float strength) {
        properties.strength(strength);
        return this;
    }

    public MeatlibBlockSettings strength(float destroyTime, float explosionResistance) {
        properties.strength(destroyTime, explosionResistance);
        return this;
    }

    public MeatlibBlockSettings sound(SoundType sound) {
        return sounds(sound);
    }

    public MeatlibBlockSettings isValidSpawn(net.minecraft.world.level.block.state.BlockBehaviour.StateArgumentPredicate<net.minecraft.world.entity.EntityType<?>> predicate) {
        properties.isValidSpawn(predicate);
        return this;
    }

    public MeatlibBlockSettings isSuffocating(net.minecraft.world.level.block.state.BlockBehaviour.StatePredicate predicate) {
        properties.isSuffocating(predicate);
        return this;
    }

    public MeatlibBlockSettings isViewBlocking(net.minecraft.world.level.block.state.BlockBehaviour.StatePredicate predicate) {
        properties.isViewBlocking(predicate);
        return this;
    }

    public MeatlibBlockSettings sounds(SoundType sound) {
        properties.sound(sound);
        return this;
    }

    public MeatlibBlockSettings nonOpaque() {
        properties.noOcclusion();
        return this;
    }

    public MeatlibBlockSettings luminance(ToIntFunction<BlockState> light) {
        properties.lightLevel(light);
        return this;
    }

    public MeatlibBlockSettings randomTicks() {
        properties.randomTicks();
        return this;
    }

    public MeatlibBlockSettings requiresTool() {
        properties.requiresCorrectToolForDrops();
        return this;
    }

    @SafeVarargs
    public final MeatlibBlockSettings tags(TagKey<Block>... tags) {
        this.tags.clear();
        for (TagKey<Block> tag : tags) {
            this.tags.add(tag);
        }
        return this;
    }

    @SafeVarargs
    public final MeatlibBlockSettings addTags(TagKey<Block>... extra) {
        for (TagKey<Block> tag : extra) {
            tags.add(tag);
        }
        return this;
    }

    public MeatlibBlockSettings simpleDrop(Supplier<Item> drop) {
        return this;
    }

    public MeatlibBlockSettings addTag(TagKey<Block> tag) {
        tags.add(tag);
        return this;
    }

    public Set<TagKey<Block>> tags() {
        return Set.copyOf(tags);
    }

    public BlockBehaviour.Properties properties() {
        return properties;
    }

    public static TagKey<Block> blockTag(String namespace, String path) {
        return TagKey.create(Registries.BLOCK, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(namespace, path));
    }
}
