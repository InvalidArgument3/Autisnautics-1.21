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

import com.neep.meatlib.item.BaseBlockItem;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import net.minecraft.world.level.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

public class BaseTrapdoorBlock extends TrapDoorBlock implements MeatlibBlock
{
    public BaseBlockItem blockItem;
    private final boolean manual;

    public BaseTrapdoorBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings, BlockSetType blockSetType)
    {
        this(ctx, itemSettings, settings, blockSetType, true);
    }

    public BaseTrapdoorBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings, BlockSetType blockSetType, boolean manual)
    {
        super(blockSetType, settings);
        this.manual = manual;
        this.blockItem = new BaseBlockItem(this, ctx, itemSettings);
    }

    public BlockItem getBlockItem()
    {
        return blockItem;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (!manual)
            return InteractionResult.PASS;

        state = state.cycle(OPEN);
        world.setBlock(pos, state, Block.UPDATE_CLIENTS);

        if (state.getValue(WATERLOGGED))
        {
            world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }

        this.playSound(player, world, pos, state.getValue(OPEN));
        return InteractionResult.sidedSuccess(world.isClientSide);
    }
}
