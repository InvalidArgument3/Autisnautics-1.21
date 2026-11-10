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

package com.neep.meatlib.registry;

import com.neep.meatlib.block.BaseColumnBlock;
import com.neep.meatlib.block.BaseLeavesBlock;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.item.TooltipSupplier;
import net.fabricmc.fabric.api.mininglevel.v1.FabricMineableTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import java.util.*;

public class BlockRegistry
{
    public static final List<Block> REGISTERED_BLOCKS = new ArrayList<>();

//    public static <T extends Block & MeatlibBlock> T queue(T block)
//    {
//        MeatLib.assertActive(block);
//        if (block == null)
//        {
//            throw new IllegalArgumentException("tried to queue something that wasn't a block.");
//        }
//
//        BLOCKS.put(new Identifier(MeatLib.CURRENT_NAMESPACE, block.getRegistryName()), block);
//        return block;
//    }

//    public static Block queue(MeatlibBlock block)
//    {
//        MeatLib.assertActive(block);
//        if (!(block instanceof Block))
//        {
//            throw new IllegalArgumentException("tried to queue something that wasn't a block.");
//        }
//
//        BLOCKS.put(new Identifier(MeatLib.CURRENT_NAMESPACE, block.getRegistryName()), (Block) block);
//        return (Block) block;
//    }

//    public static <T extends Block> T queue(T block, String registryName)
//    {
//        MeatLib.assertActive(block);
//        BLOCKS.put(new Identifier(MeatLib.CURRENT_NAMESPACE, registryName), block);
//        return block;
//    }

//    public static <T extends Block> T queueWithItem(T block, String registryName, ItemSettings itemSettings)
//    {
//        MeatLib.assertActive(block);
//        BLOCKS.put(new Identifier(MeatLib.CURRENT_NAMESPACE, registryName), block);
//        itemSettings.getFactory().create(block, ctx, itemSettings);
//        return block;
//    }

//    public static <T extends Block & MeatlibBlock> T queueWithItem(T block, ItemSettings itemSettings)
//    {
//        MeatLib.assertActive(block);
//        BLOCKS.put(new Identifier(MeatLib.CURRENT_NAMESPACE, block.getRegistryName()), block);
//        itemSettings.getFactory().create(block, ctx, itemSettings);
//        return block;
//    }

//    public static <T extends Block> T queueWithItem(T block, String registryName)
//    {
//        MeatLib.assertActive(block);
//        BLOCKS.put(new Identifier(MeatLib.CURRENT_NAMESPACE, registryName), block);
//        ItemRegistry.queue(new BaseBlockItem(block, registryName, ItemSettings.block()));
//        return block;
//    }

//    public static void flush()
//    {
//        for (Map.Entry<Identifier, Block> entry : BLOCKS.entrySet())
//        {
//            Registry.register(Registries.BLOCK, entry.getKey(), entry.getValue());
//
//            REGISTERED_BLOCKS.add(entry.getValue());
//        }
//        BLOCKS.clear();
//    }

    public static BaseColumnBlock createLogBlock(RegistrationContext ctx, TooltipSupplier tooltipSupplier)
    {
        return new BaseColumnBlock(ctx, ItemSettings.block(), MeatlibBlockSettings.of().tags(BlockTags.MINEABLE_WITH_AXE, BlockTags.LOGS).strength(2.0f).sound(SoundType.WOOD));
    }

    public static BaseLeavesBlock createLeavesBlock(RegistrationContext ctx, SoundType soundGroup)
    {
        return new BaseLeavesBlock(ctx, MeatlibBlockSettings.of()
                .tags(FabricMineableTags.SHEARS_MINEABLE, BlockTags.LEAVES)
                .strength(0.2f)
                .randomTicks()
                .sound(soundGroup)
                .noOcclusion()
                .isValidSpawn((p1, p2, p3, p4) -> false)
                .isSuffocating((p1, p2, p3) -> false).isViewBlocking(((state, world, pos) -> false)))
        {
            public ItemLike dropsLike()
            {
                return null;
            }
        };
    }
}
