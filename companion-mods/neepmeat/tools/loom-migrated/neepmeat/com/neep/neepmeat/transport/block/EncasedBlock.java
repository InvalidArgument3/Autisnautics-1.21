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

package com.neep.neepmeat.transport.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public interface EncasedBlock
{
    static BlockBehaviour.StatePredicate solidBlock()
    {
        // This only applies to redstone and is executed thousands (hyperbole) of times in redstone updates.
        // This makes it unwise to query the block entity.
        return (state, world, pos) ->
        {
//            if (world.getBlockEntity(pos) instanceof EncasedBlockEntity be)
//            {
//                return be.getCamoState().isSolidBlock(world, pos);
//            }

            return false;
        };
    }

    default boolean replaceUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof BlockItem bi && bi.getBlock() != this
                && canReplace(stack, bi)
                && world.getBlockEntity(pos) instanceof EncasedBlockEntity be
                && be.getCamoState().isAir()
        )
        {
            world.playSound(player, pos, SoundEvents.SCAFFOLDING_BREAK, SoundSource.BLOCKS, 1, 1);

            if (world.isClientSide())
                return true;

            BlockState camoState = bi.getBlock().getStateForPlacement(new BlockPlaceContext(player, hand, stack, hit));
            if (camoState != null)
            {
                be.setCamoState(camoState);

                world.sendBlockUpdated(pos, state, state, Block.UPDATE_IMMEDIATE | Block.UPDATE_CLIENTS);
            }

            return true;
        }
        return false;
    }

    boolean canReplace(ItemStack stack, BlockItem blockItem);
}
