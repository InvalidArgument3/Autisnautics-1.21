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

package com.neep.neepmeat.plc.recipe;

import com.mojang.serialization.Codec;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.plc.recipe.ManufactureStep;
import com.neep.neepmeat.init.NMComponents;
import com.neep.meatlib.transfer.ItemVariant;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.WeakHashMap;

public record CombineStep(ItemVariant item) implements ManufactureStep<ItemStack>
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "combine");

    public static Codec<CombineStep> CODEC = BuiltInRegistries.ITEM.byNameCodec().xmap(ItemVariant::of, ItemVariant::getItem)
            .xmap(CombineStep::get, CombineStep::item).fieldOf("resource").codec();

    // Instance cache to save memory
    private static final WeakHashMap<ItemVariant, CombineStep> CACHE = new WeakHashMap<>();

    public static CombineStep get(ItemStack stack)
    {
        return CACHE.computeIfAbsent(ItemVariant.of(stack), CombineStep::new);
    }

    public static CombineStep get(CompoundTag nbt)
    {
        ItemVariant item = ItemVariant.fromNbt(nbt.getCompound("variant"));
        return CACHE.computeIfAbsent(item, CombineStep::new);
    }

    public static CombineStep get(ItemVariant itemVariant)
    {
        return CACHE.computeIfAbsent(itemVariant, CombineStep::new);
    }

    @Override
    public void mutate(ItemStack stack)
    {
        NMComponents.WORKPIECE.maybeGet(stack).ifPresent(workpiece ->
        {
            workpiece.addStep(this);
        });
    }

    @Override
    public Component getName()
    {
        return Component.translatable(ID.toLanguageKey("step")).withStyle(ChatFormatting.UNDERLINE);
    }

    @Override
    public void appendText(List<Component> tooltips)
    {
        tooltips.add(Component.translatable(ID.toLanguageKey("step")).withStyle(ChatFormatting.UNDERLINE));
        tooltips.add(Component.literal("   ").append(item.getObject().getDescription()));
    }

    public CompoundTag toNbt()
    {
        CompoundTag nbt = new CompoundTag();
        nbt.put("variant", item.toNbt());
        return nbt;
    }

    @Override
    public boolean equalsOther(ManufactureStep<?> o)
    {
        if (o instanceof CombineStep other)
        {
            return other.item.equals(item);
        }
        return false;
    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    public Item getItem()
    {
        return item.getItem();
    }
}
