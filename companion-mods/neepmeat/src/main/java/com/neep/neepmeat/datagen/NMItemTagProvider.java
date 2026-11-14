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

package com.neep.neepmeat.datagen;

import com.neep.meatlib.datagen.MeatLibDataGen;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.init.NMItems;
import com.neep.meatlib.datagen.FabricDataOutput;
import com.neep.meatlib.datagen.FabricTagProvider;
import com.neep.meatlib.datagen.ConventionalTags.ConventionalBlockTags;
import com.neep.meatlib.datagen.ConventionalTags.ConventionalItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import java.util.concurrent.CompletableFuture;

public class NMItemTagProvider extends FabricTagProvider.ItemTagProvider
{
    public NMItemTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture)
    {
        super(output, registriesFuture);
    }

    @Override
    public String getName()
    {
        return "Tags for " + this.registryKey.location() + " (" + NeepMeat.NAMESPACE + ")";
    }

    @Override
    protected void addTags(HolderLookup.Provider arg)
    {
        tag(NMTags.CHARNEL_COMPACTOR)
                .addOptionalTag(NMTags.RAW_MEAT)
                .addOptionalTag(NMTags.RAW_FISH)
                .addOptionalTag(TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.fromNamespaceAndPath("chestcavity", "salvageable_human_organ_meat")))
                .addOptionalTag(TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.fromNamespaceAndPath("chestcavity", "salvageable_animal_organ_meat")))
                .addOptionalTag(TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.fromNamespaceAndPath("chestcavity", "salvageable_rotten_flesh")))
                .add(BuiltInRegistries.ITEM.getResourceKey(Items.BONE).orElseThrow())
                .add(BuiltInRegistries.ITEM.getResourceKey(Items.SPIDER_EYE).orElseThrow())
                .add(BuiltInRegistries.ITEM.getResourceKey(Items.FERMENTED_SPIDER_EYE).orElseThrow())
                .add(BuiltInRegistries.ITEM.getResourceKey(Items.EGG).orElseThrow())
                .add(BuiltInRegistries.ITEM.getResourceKey(Items.TURTLE_EGG).orElseThrow())
                .add(BuiltInRegistries.ITEM.getResourceKey(Items.FROGSPAWN).orElseThrow())
                .add(BuiltInRegistries.ITEM.getResourceKey(NMItems.ASSORTED_BIOMASS).orElseThrow())
                .add(BuiltInRegistries.ITEM.getResourceKey(Items.SNIFFER_EGG).orElseThrow())
        ;

        tag(NMTags.BLOCK_CRUSHING_OUTPUTS)
                .addOptionalTag(ConventionalItemTags.RAW_ORES)
                .addOptionalTag(TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.fromNamespaceAndPath("mythicmetals", "raw_ores")))
        ;

        tag(NMTags.DOSIMETER)
                .add(NMItems.DOSIMETER)
        ;

        tag(NMTags.BROWNE_ORES_ITEM)
                .add(NMBlocks.BROWNE_ORE.asItem())
                .add(NMBlocks.DEEPSLATE_BROWNE_ORE.asItem())
        ;

        tag(ConventionalItemTags.ORES)
                .addTag(NMTags.BROWNE_ORES_ITEM)
        ;
    }
}
