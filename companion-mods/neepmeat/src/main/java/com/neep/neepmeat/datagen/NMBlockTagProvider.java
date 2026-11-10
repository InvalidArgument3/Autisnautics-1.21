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
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import java.util.concurrent.CompletableFuture;

public class NMBlockTagProvider extends FabricTagProvider.BlockTagProvider
{
    public NMBlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture)
    {
        super(output, registriesFuture); }

    @Override
    public String getName()
    {
        return "Tags for " + this.registryKey.location() + " (" + NeepMeat.NAMESPACE + ")";
    }

    @Override
    protected void addTags(HolderLookup.Provider arg)
    {
        // Blocks whose corresponding items will be used as recipe inputs for crushing if their loot table
        // contains a raw ore.
        tag(NMTags.BLOCK_CRUSHING_INPUTS)
                .addOptionalTag(ConventionalBlockTags.ORES)
        ;

        tag(NMTags.CHARNEL_PUMP_OUTPUT_ORES)
                .addOptionalTag(ConventionalBlockTags.ORES);

        tag(NMTags.WRITHING_EARTH_REPLACABLE)
                .addOptionalTag(BlockTags.STONE_ORE_REPLACEABLES)
                .addOptionalTag(BlockTags.NETHER_CARVER_REPLACEABLES)
                .addOptionalTag(BlockTags.LUSH_GROUND_REPLACEABLE)
                .addOptionalTag(BlockTags.LUSH_GROUND_REPLACEABLE)
                .addOptionalTag(BlockTags.SNOW)
                .addOptionalTag(BlockTags.MINEABLE_WITH_SHOVEL)
                .addOptionalTag(BlockTags.DIRT)
        ;

        tag(NMTags.CONTAMINATED_RUBBLE_REPLACABLE)
                .addOptionalTag(BlockTags.SNOW)
                .addOptionalTag(BlockTags.LUSH_GROUND_REPLACEABLE)
                .addOptionalTag(BlockTags.MINEABLE_WITH_SHOVEL)
                .addOptionalTag(BlockTags.DIRT)
                .addOptionalTag(BlockTags.NETHER_CARVER_REPLACEABLES)
        ;

        tag(NMTags.ROCK_DRILL_MINEABLE)
                .addOptionalTag(BlockTags.STONE_ORE_REPLACEABLES)
                .addOptionalTag(BlockTags.NETHER_CARVER_REPLACEABLES)
                .addOptionalTag(BlockTags.DEEPSLATE_ORE_REPLACEABLES)
                .add(NMBlocks.ASBESTOS)
        ;

        tag(NMTags.WRENCHABLE)
                .addOptionalTag(BlockTags.STAIRS)
                .addOptionalTag(BlockTags.FENCE_GATES)
        ;

        tag(NMTags.FARMLAND)
                .add(Blocks.FARMLAND)
        ;

        tag(NMTags.WORM_ALTAR)
                .add(NMBlocks.RUSTY_METAL_BLOCK)
                .add(NMBlocks.RUSTY_METAL_SHEET)
                .add(NMBlocks.RUSTY_METAL_BLOCK.stairs)
                .add(NMBlocks.RUSTY_METAL_SHEET.stairs)
                .add(NMBlocks.RUSTY_METAL_BLOCK.slab)
                .add(NMBlocks.RUSTY_METAL_SHEET.slab)
        ;

        tag(NMTags.TREE_VACUUM_HARVESTABLE)
                .addOptionalTag(BlockTags.LEAVES)
                .addOptionalTag(BlockTags.LOGS)
                .add(Blocks.WARPED_WART_BLOCK)
                .add(Blocks.NETHER_WART_BLOCK)
                .add(Blocks.MUSHROOM_STEM)
                .add(Blocks.BROWN_MUSHROOM_BLOCK)
                .add(Blocks.RED_MUSHROOM_BLOCK)
                .add(Blocks.SHROOMLIGHT)
                ;

        tag(NMTags.CONTAMINATED_DIRT)
                .add(NMBlocks.CONTAMINATED_DIRT)
                .add(NMBlocks.CONTAMINATED_GRASS_BLOCK)
                ;

        tag(NMTags.BROWNE_ORES)
                .add(NMBlocks.BROWNE_ORE)
                .add(NMBlocks.DEEPSLATE_BROWNE_ORE)
        ;

        tag(ConventionalBlockTags.ORES)
                .addTag(NMTags.BROWNE_ORES)
        ;
    }
}
