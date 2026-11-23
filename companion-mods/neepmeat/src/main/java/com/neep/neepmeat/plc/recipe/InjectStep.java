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

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.plc.recipe.ManufactureStep;
import com.neep.neepmeat.init.NMComponents;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.FluidVariantAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public class InjectStep implements ManufactureStep<ItemStack>
{
    public static Codec<InjectStep> CODEC = BuiltInRegistries.FLUID.byNameCodec().xmap(FluidVariant::of, FluidVariant::getObject)
            .xmap(InjectStep::new, InjectStep::fluid).fieldOf("resource").codec();

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "inject");

    private final FluidVariant fluid;

    public InjectStep(FluidVariant fluid)
    {
        this.fluid = fluid;
    }

    public InjectStep(CompoundTag nbt)
    {
        fluid = FluidVariant.fromNbt(nbt.getCompound("fluid"));
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
        tooltips.add(Component.literal("   ").append(FluidVariantAttributes.getName(fluid)));
    }

    public CompoundTag toNbt()
    {
        CompoundTag nbt = new CompoundTag();
        nbt.put("fluid", fluid.toNbt());
        return nbt;
    }

    @Override
    public boolean equalsOther(ManufactureStep<?> o)
    {
        if (o instanceof InjectStep other)
        {
            return other.fluid == fluid;
        }
        return false;
    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    public FluidVariant fluid()
    {
        return fluid;
    }
}
