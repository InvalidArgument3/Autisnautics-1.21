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
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.init.NMSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class CandleCronenCakeBlock extends CandleCakeBlock implements MeatlibBlock
{
    public CandleCronenCakeBlock(RegistrationContext ctx, Properties settings)
    {
        super(Blocks.WHITE_CANDLE, settings);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        ItemStack itemStack = player.getItemInHand(hand);
        if (itemStack.is(Items.FLINT_AND_STEEL) || itemStack.is(Items.FIRE_CHARGE))
        {
            return InteractionResult.PASS;
        }
        if (!(candleHit(hit) && player.getItemInHand(hand).isEmpty() && state.getValue(LIT)))
        {
            InteractionResult actionResult = CronenCakeBlock.tryEatCake(world, pos, NMBlocks.CRONENCAKE.defaultBlockState(), player);
            if (actionResult.consumesAction())
            {
                CandleCakeBlock.dropResources(state, world, pos);
            }
            return actionResult;
        }
        CandleCakeBlock.extinguish(player, state, world, pos);
        return InteractionResult.sidedSuccess(world.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (newState.is(this) && newState.getValue(LIT) && !state.getValue(LIT))
        {
            world.playSound(null, pos, NMSounds.CAKE_FIRE, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state)
    {
        return NMBlocks.CRONENCAKE.getCloneItemStack(world, pos, state);
    }

    private static boolean candleHit(BlockHitResult hitResult)
    {
        return hitResult.getLocation().y - (double)hitResult.getBlockPos().getY() > 0.5;
    }


}
