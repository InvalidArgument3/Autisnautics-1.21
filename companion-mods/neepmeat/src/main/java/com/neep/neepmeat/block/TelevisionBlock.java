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

package com.neep.neepmeat.block;

import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.init.NMBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

public class TelevisionBlock extends BaseBlock
{
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;

    public TelevisionBlock(RegistrationContext ctx, ItemSettings itemSettings, FabricBlockSettings settings)
    {
        super(ctx, itemSettings, settings.emissiveRendering(((state, world, pos) ->
                state.getBlock() == NMBlocks.TELEVISION_STATIC)));
        registerDefaultState(defaultBlockState().setValue(ROTATION, 0));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        return this.defaultBlockState().setValue(ROTATION, Mth.floor((ctx.getRotation() * 16.0f / 360.0f) + 0.5) & 0xF);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
    {
        super.neighborChanged(state, world, pos, sourceBlock, sourcePos, notify);
        if (sourceBlock.isSignalSource(world.getBlockState(sourcePos)))
        {
            world.setBlockAndUpdate(pos, getState(world, pos, state));
        }
    }

    private BlockState getState(Level world, BlockPos pos, BlockState state)
    {
        if (world.getBestNeighborSignal(pos) > 0)
        {
            return NMBlocks.TELEVISION_STATIC.defaultBlockState().setValue(ROTATION, state.getValue(ROTATION));
        }
        else
        {
            return NMBlocks.TELEVISION_OFF.defaultBlockState().setValue(ROTATION, state.getValue(ROTATION));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(ROTATION);
    }
}
