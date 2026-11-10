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

package com.neep.neepmeat.block.entity;

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.item.BaseBlockItem;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.item.MeatlibItemSettings;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.NMItemGroups;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.init.NMSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class CronenCakeBlock extends CakeBlock implements MeatlibBlock
{
    private final BlockItem blockItem;

    public CronenCakeBlock(RegistrationContext ctx, Properties settings)
    {
        super(settings);
        this.blockItem = new BaseBlockItem(this, ctx, ItemSettings.block().tooltip(TooltipSupplier.simple(1)), new MeatlibItemSettings().group(NMItemGroups.FOOD));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        ItemStack itemStack = player.getItemInHand(hand);
        Item item = itemStack.getItem();
        if (itemStack.is(ItemTags.CANDLES) && state.getValue(BITES) == 0 && Block.byItem(item) instanceof CandleBlock)
        {
            if (!player.isCreative())
            {
                itemStack.shrink(1);
            }
            world.playSound(null, pos, SoundEvents.CANDLE_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
            world.playSound(null, pos, NMSounds.CAKE_SCREAM.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            world.setBlockAndUpdate(pos, NMBlocks.CANDLE_CRONENCAKE.defaultBlockState());
            world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            player.awardStat(Stats.ITEM_USED.get(item));
            return InteractionResult.SUCCESS;
        }
        if (world.isClientSide)
        {
            if (CakeBlock.eat(world, pos, state, player).consumesAction())
            {
                return InteractionResult.SUCCESS;
            }
            if (itemStack.isEmpty())
            {
                return InteractionResult.CONSUME;
            }
        }
        return CakeBlock.eat(world, pos, state, player);
    }

    protected static InteractionResult tryEatCake(Level world, BlockPos pos, BlockState state, Player player)
    {
        return eat(world, pos, state, player);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
    }

    public boolean autoGenDrop()
    {
        return false;
    }
}
