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

package com.neep.meatlib.datagen.loot;

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.block.MeatlibBlockExtension;
import com.neep.meatlib.registry.BlockRegistry;
import com.neep.meatlib.datagen.FabricDataOutput;
import com.neep.meatlib.datagen.FabricBlockLootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public class BlockLootTableProvider extends FabricBlockLootTableProvider
{
    public BlockLootTableProvider(FabricDataOutput output)
    {
        super(output);
    }

    @Override
    public void generate()
    {
        for (Block entry : BlockRegistry.REGISTERED_BLOCKS)
        {
            if (entry instanceof MeatlibBlock meatBlock)
            {
                var lootTableIdd = entry.getLootTable();
                var id = entry.builtInRegistryHolder().unwrapKey().get().location();

                // True by default for MeatlibBlock
                // If BlockSettings.dropsLike is used, we should never generate a loot table, otherwise the copied block's loot table will be overridden.
                if (meatBlock.autoGenDrop() && lootTableIdd.equals(ResourceLocation.tryBuild(id.getNamespace(), "blocks/" + id.getPath())))
                {
                    var builder = meatBlock.genLoot(this);
                    if (builder != null)
                    {
                        add(entry, builder);
                    }
                    else
                    {
                        // Legacy behaviour
                        ItemLike like = meatBlock.dropsLike();
                        if (like != null)
                        {
                            this.dropOther(entry, like);
                        }
                    }
                }
            }
                else if (entry instanceof MeatlibBlockExtension extension)
            {
                // The default behaviour injected into Item delegates to MeatlibItemSettings if applicable.
                ItemLike drop = extension.neepmeat$simpleDrop();
                if (drop != null)
                {
                    this.dropOther(entry, drop);
                }
            }
        }
    }
}
