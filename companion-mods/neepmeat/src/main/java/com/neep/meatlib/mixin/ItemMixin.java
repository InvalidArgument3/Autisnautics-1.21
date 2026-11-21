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
import com.neep.meatlib.item.MeatlibItemExtension;
import com.neep.meatlib.item.MeatlibItemSettings;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(Item.class)
public abstract class ItemMixin implements MeatlibItemExtension
{
    @Shadow public abstract InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand);

    @Unique private Set<TagKey<Item>> tags = Set.of();
    @Unique private CreativeModeTab itemGroup;
    @Unique private boolean supportsGuideLookup;

    @Inject(method = "<init>", at = @At("TAIL"))
    void onInit(Item.Properties settings, CallbackInfo ci)
    {
        if (settings instanceof MeatlibItemSettings mis)
        {
            this.tags = mis.getTags();
            this.itemGroup = mis.group;
            this.supportsGuideLookup = mis.supportsGuideLookup;
        }
    }

    @Override
    public @Nullable CreativeModeTab meatlib$getItemGroup()
    {
        return itemGroup;
    }

    @Override
    public boolean meatlib$supportsGuideLookup()
    {
        return supportsGuideLookup;
    }

    @Override
    public void meatlib$appendTags(MeatlibBlockExtension.TagConsumer<Item> consumer)
    {
        for (var tag : tags)
        {
            consumer.offer(tag);
        }
    }
}
