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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.plc.recipe.ManufactureStep;
import com.neep.neepmeat.init.NMComponents;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class ImplantStep implements ManufactureStep<Entity>
{
//    public static Codec<ImplantStep> CODEC = Registries.ITEM.getCodec()
//            .xmap(ImplantStep::new, ImplantStep::getItem).fieldOf("resource").codec();
    public static Codec<ImplantStep> CODEC = RecordCodecBuilder.create(instance -> 
        instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("resource").forGetter(ImplantStep::getItem),
                Codec.INT.optionalFieldOf("complexity", 0).forGetter(ImplantStep::getComplexity))
                .apply(instance, ImplantStep::new));


    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "implant");

    private final Item item;
    private final int complexity;

    public ImplantStep(Item item, int complexity)
    {
        this.item = item;
        this.complexity = complexity;
    }

    public ImplantStep(CompoundTag nbt)
    {
        ResourceLocation id = ResourceLocation.tryParse(nbt.getString("id"));
        if (id != null)
            item = BuiltInRegistries.ITEM.get(id);
        else
            item = Items.AIR;

        this.complexity = nbt.getInt("complexity");
    }

    public int getComplexity()
    {
        return complexity;
    }

    @Override
    public void mutate(Entity entity)
    {
        NMComponents.WORKPIECE.maybeGet(entity).ifPresent(workpiece ->
        {
            workpiece.addStep(this);
        });
    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
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
        tooltips.add(Component.literal("   ").append(item.getDescription()));
    }

    @Override
    public CompoundTag toNbt()
    {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("id", BuiltInRegistries.ITEM.getKey(item).toString());
        nbt.putInt("complexity", complexity);
        return nbt;
    }

    @Override
    public boolean equalsOther(ManufactureStep<?> o)
    {
        if (o instanceof ImplantStep other)
        {
            return other.item == item && other.complexity >= complexity;
        }
        return false;
    }

    public Item getItem()
    {
        return item;
    }
}
