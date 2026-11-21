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

package com.neep.neepmeat.api.processing.random_ores;

import com.neep.neepmeat.api.processing.BlockCrushingRegistry;
import it.unimi.dsi.fastutil.floats.FloatObjectPair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class RandomOreProvider
{
    private final RandomSource random;
    private final List<FloatObjectPair<RandomOres.Entry>> entries;
    private final WeightedMap<RandomOres.Entry> entryMap = new WeightedMap<>();

    public RandomOreProvider(Level world, BlockPos pos, RandomSource random, List<RandomOres.Entry> entries)
    {
        this.random = random;
        this.entries = new ArrayList<>();

        for (var entry : entries)
        {
            float mw = entry.modifiedWeight(world, pos);
//            NeepMeat.LOGGER.info("State: {}, Weight: {}, Modified Weight: {}", entry.state(), entry.weight(), mw);
            entryMap.put(mw, entry);
            this.entries.add(FloatObjectPair.of(mw, entry));
        }
    }

    public List<ItemStack> random(ServerLevel world, BlockPos origin)
    {
        float p = random.nextFloat();
        RandomOres.Entry entry = entryMap.get(random::nextInt, p);
        if (entry == null)
        {
//            NeepMeat.LOGGER.info("How strange: {}", p);
            return List.of();
        }

        ResourceLocation identifier = entry.state().getBlock().getLootTable();
        if (identifier == BuiltInLootTables.EMPTY)
        {
            return Collections.emptyList();
        }
        else
        {
            LootParams.Builder builder = new LootParams.Builder(world)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(origin))
                    .withParameter(LootContextParams.TOOL, Items.NETHERITE_PICKAXE.getDefaultInstance())
                    .withOptionalParameter(LootContextParams.THIS_ENTITY, null)
                    .withOptionalParameter(LootContextParams.BLOCK_ENTITY, null);

            return BlockCrushingRegistry.INSTANCE.modifyDroppedStacks(entry.state(), entry.state().getDrops(builder), true);
        }
    }

    public String print()
    {
        StringBuilder builder = new StringBuilder();
         for (int i = 0; i < entries.size(); ++i)
         {
             var pair = entries.get(i);
             builder.append(pair.value().state()).append(": ").append(pair.firstFloat()).append("\n");
         }
         return builder.toString();
    }
}
