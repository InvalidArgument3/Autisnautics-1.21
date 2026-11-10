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

package com.neep.meatlib.datagen;

import com.neep.meatlib.block.BaseWallBlock;
import com.neep.meatlib.block.MeatlibBlockExtension;
import com.neep.meatlib.registry.BlockRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Block;
import java.util.concurrent.CompletableFuture;

public class BlockTagProvider extends FabricTagProvider.BlockTagProvider
{
    public BlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> lookupCompletableFuture)
    {
        super(output, lookupCompletableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg)
    {
        for (Block entry : BlockRegistry.REGISTERED_BLOCKS)
        {
            MeatlibBlockExtension.TagConsumer<Block> consumer = t -> tag(t).add(entry);
            entry.neepmeat$appendTags(consumer);

            // JAAAAAAAAAAANK
            if (entry instanceof BaseWallBlock wall)
            {
                this.tag(wall.getWallTag()).add(entry);
            }
        }
    }
}
