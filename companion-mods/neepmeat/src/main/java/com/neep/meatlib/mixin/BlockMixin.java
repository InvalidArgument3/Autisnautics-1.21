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

package com.neep.meatlib.mixin;

import com.neep.meatlib.block.MeatlibBlockExtension;
import com.neep.meatlib.block.MeatlibBlockSettings;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.function.Function;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

@Mixin(Block.class)
public abstract class BlockMixin implements MeatlibBlockExtension
{
    // Oooooh, storing tags in every block may be a waste of space since they are only used in datagen.
    // I'm sure it'll be fine.
    @Unique private Set<TagKey<Block>> tags = Set.of();
    @Unique @Nullable private Function<Block, ItemLike> simpleDrop;

    @Inject(method = "<init>", at = @At("TAIL"))
    void onInit(BlockBehaviour.Properties settings, CallbackInfo ci)
    {
        if (settings instanceof MeatlibBlockSettings mbs)
        {
            this.tags = mbs.getTags();
            this.simpleDrop = mbs.getSimpleDrop();
        }
    }

    @Override
    public void neepmeat$appendTags(TagConsumer<Block> consumer)
    {
        for (var tag : tags)
        {
            consumer.offer(tag);
        }
    }

    @Override
    public @Nullable ItemLike neepmeat$simpleDrop()
    {
        return simpleDrop != null ? simpleDrop.apply((Block) (Object) this) : null;
    }
}
