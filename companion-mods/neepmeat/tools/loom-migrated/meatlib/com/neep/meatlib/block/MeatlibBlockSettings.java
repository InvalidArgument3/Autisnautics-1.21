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

package com.neep.meatlib.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.mixin.object.builder.AbstractBlockAccessor;
import net.fabricmc.fabric.mixin.object.builder.AbstractBlockSettingsAccessor;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class MeatlibBlockSettings extends FabricBlockSettings
{
    //    lic final List<TagKey<Block>> tags = new ArrayList<>(Collections.singleton(BlockTags.PICKAXE_MINEABLE));
    public static final Set<TagKey<Block>> DEFAULT_TAGS = Set.of(BlockTags.MINEABLE_WITH_PICKAXE);

    private Set<TagKey<Block>> tags = DEFAULT_TAGS;
    @Nullable private Function<Block, ItemLike> simpleDrop;

    protected MeatlibBlockSettings()
    {

    }

    // Copied from FabricBlockSettings
    protected MeatlibBlockSettings(BlockBehaviour.Properties settings)
    {
        this();
        // Mostly Copied from vanilla's copy method
        AbstractBlockSettingsAccessor thisAccessor = (AbstractBlockSettingsAccessor) this;
        AbstractBlockSettingsAccessor otherAccessor = (AbstractBlockSettingsAccessor) settings;

        // Copied in vanilla: sorted by vanilla copy order
        this.destroyTime(otherAccessor.getHardness());
        this.explosionResistance(otherAccessor.getResistance());
        this.collidable(otherAccessor.getCollidable());
        thisAccessor.setRandomTicks(otherAccessor.getRandomTicks());
        this.lightLevel(otherAccessor.getLuminance());
        thisAccessor.setMapColorProvider(otherAccessor.getMapColorProvider());
        this.sound(otherAccessor.getSoundGroup());
        this.friction(otherAccessor.getSlipperiness());
        this.speedFactor(otherAccessor.getVelocityMultiplier());
        thisAccessor.setDynamicBounds(otherAccessor.getDynamicBounds());
        thisAccessor.setOpaque(otherAccessor.getOpaque());
        thisAccessor.setIsAir(otherAccessor.getIsAir());
        thisAccessor.setBurnable(otherAccessor.getBurnable());
        thisAccessor.setLiquid(otherAccessor.getLiquid());
        thisAccessor.setForceNotSolid(otherAccessor.getForceNotSolid());
        thisAccessor.setForceSolid(otherAccessor.getForceSolid());
        this.pushReaction(otherAccessor.getPistonBehavior());
        thisAccessor.setToolRequired(otherAccessor.isToolRequired());
        thisAccessor.setOffsetter(otherAccessor.getOffsetter());
        thisAccessor.setBlockBreakParticles(otherAccessor.getBlockBreakParticles());
        thisAccessor.setRequiredFeatures(otherAccessor.getRequiredFeatures());
        this.emissiveRendering(otherAccessor.getEmissiveLightingPredicate());
        this.instrument(otherAccessor.getInstrument());
        thisAccessor.setReplaceable(otherAccessor.getReplaceable());

        // Not copied in vanilla: field definition order
        this.jumpFactor(otherAccessor.getJumpVelocityMultiplier());
        this.drops(otherAccessor.getLootTableId());
        this.isValidSpawn(otherAccessor.getAllowsSpawningPredicate());
        this.isRedstoneConductor(otherAccessor.getSolidBlockPredicate());
        this.isSuffocating(otherAccessor.getSuffocationPredicate());
        this.isViewBlocking(otherAccessor.getBlockVisionPredicate());
        this.hasPostProcess(otherAccessor.getPostProcessPredicate());

        if (settings instanceof MeatlibBlockSettings mbs)
        {
            this.tags = mbs.tags != null ? new HashSet<>(mbs.tags) : null;
        }
    }

    public static MeatlibBlockSettings of()
    {
        return new MeatlibBlockSettings();
    }

    @SafeVarargs // I choose convenience!
    public static MeatlibBlockSettings create(TagKey<Block>... tagKey)
    {
        var settings = new MeatlibBlockSettings();
        settings.tags = new HashSet<>();
        settings.tags.addAll(List.of(tagKey));
        return settings;
    }

    public static MeatlibBlockSettings copyOf(BlockBehaviour block)
    {
        return new MeatlibBlockSettings(((AbstractBlockAccessor) block).getSettings());
    }

    public static MeatlibBlockSettings copyOf(BlockBehaviour.Properties settings)
    {
        return new MeatlibBlockSettings(settings);
    }

    public MeatlibBlockSettings copy()
    {
        return new MeatlibBlockSettings(this);
    }

    public Set<TagKey<Block>> getTags()
    {
        return tags;
    }

    @Nullable
    public Function<Block, ItemLike> getSimpleDrop()
    {
        return simpleDrop;
    }

    /**
     * For generating drops for blocks that do not implement MeatlibBlock.
     */
    public MeatlibBlockSettings simpleDrop(Function<Block, ItemLike> supplier)
    {
        this.simpleDrop = supplier;
        return this;
    }

    public MeatlibBlockSettings tags()
    {
        this.tags = Set.of();
        return this;
    }

    @SafeVarargs // Argh
    public final MeatlibBlockSettings tags(TagKey<Block>... tags)
    {
        this.tags = Set.of(tags);
        return this;
    }

    @SafeVarargs
    public final MeatlibBlockSettings addTags(TagKey<Block>... newTags)
    {
        var newSet = new HashSet<>(tags);
        newSet.addAll(List.of(newTags));
        tags = Set.copyOf(newSet);
        return this;
    }

    @Override
    public MeatlibBlockSettings destroyTime(float hardness)
    {
        super.destroyTime(hardness);
        return this;
    }

    public MeatlibBlockSettings strength(float strength)
    {
        super.strength(strength);
        return this;
    }

    @Override
    public MeatlibBlockSettings sound(SoundType group)
    {
        super.sound(group);
        return this;
    }
}
