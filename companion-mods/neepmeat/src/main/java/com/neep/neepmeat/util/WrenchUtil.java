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

package com.neep.neepmeat.util;

import com.neep.meatlib.util.Wrenchable;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.init.NMItems;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

public class WrenchUtil
{
    public static void init()
    {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) ->
        {
            ItemStack stack = player.getItemInHand(hand);

            if (stack.is(NMTags.WRENCH))
            {
                BlockPos pos = hitResult.getBlockPos();
                BlockState state = world.getBlockState(pos);

                // Allow using wrenches from other mods for Wrenchable blocks.
                // Disallow rotating of non-Wrenchable blocks with wrenches from other mods.
                if (state.getBlock() instanceof Wrenchable wrenchable)
                {
                    if (wrenchable.onWrench(world, pos, state, player, hand, hitResult, stack))
                        return InteractionResult.SUCCESS;
                }
                else if (stack.is(NMItems.TISSUE_OPERATOR) && state.is(NMTags.WRENCHABLE))
                {
                    handleNormalBlock(world, pos, state, player, hand, hitResult, stack);
                    return InteractionResult.SUCCESS;
                }
            }

            return InteractionResult.PASS;
        });
    }

    public static void handleNormalBlock(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, BlockHitResult hitResult, ItemStack stack)
    {
        var properties = state.getProperties();

        BlockState newState = state;
        if (properties.contains(BlockStateProperties.HORIZONTAL_FACING))
        {
            newState = Wrenchable.rotateHorizontal(state, BlockStateProperties.HORIZONTAL_FACING);
        }
        else if (properties.contains(BlockStateProperties.FACING))
        {
            newState = Wrenchable.rotateFacing(state, BlockStateProperties.FACING, hitResult.getDirection().getAxis());
        }

        if (newState != state)
        {
            Wrenchable.setBlockState(world, pos, newState);
            Wrenchable.playStandardWrenchSound(world, pos);
        }

    }
}
