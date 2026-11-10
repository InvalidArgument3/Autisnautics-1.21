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

package com.neep.meatlib.block;

import com.neep.meatlib.item.BaseSeedsItem;
import com.neep.meatlib.registry.RegistrationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BaseCropBlock extends CropBlock implements MeatlibBlock, MeatlibBlockExtension
{
    protected final BaseSeedsItem seedsItem;

    private static final VoxelShape[] AGE_TO_SHAPE = new VoxelShape[]{Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 3.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 5.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 7.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 9.0D, 16.0D)
    };

    public BaseCropBlock(RegistrationContext ctx, String seedsAppend, int itemMaxStack, int lore, Properties settings)
    {
        super(settings);
        this.seedsItem = new BaseSeedsItem(this, ctx, itemMaxStack, lore);
        ctx.append(this, seedsItem, s -> s + seedsAppend);
    }

    public BaseCropBlock(RegistrationContext ctx, int itemMaxStack, int lore, Properties settings)
    {
        this(ctx, "_seeds", itemMaxStack, lore, settings);
    }

//    public BaseCropBlock(RegistrationContext ctx, int itemMaxStack, int lore, ItemFactory factory, Settings settings)
//    {
//        super(settings);
//        this.seedsItem = new BaseSeedsItem(this, ctx, itemMaxStack, lore);
//    }

    @Override
    public ItemLike dropsLike()
    {
        return null;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return AGE_TO_SHAPE[state.getValue(this.getAgeProperty())];
    }

    @Override
    public ItemLike getBaseSeedId()
    {
        return seedsItem;
    }

    public Item getSeedsItemItem()
    {
        return seedsItem;
    }

    @Override
    public void neepmeat$appendTags(TagConsumer<Block> consumer)
    {
        super.neepmeat$appendTags(consumer);
    }
}
