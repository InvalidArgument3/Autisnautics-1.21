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

package com.neep.neepmeat.machine.reactor.disruptor;

import com.neep.meatlib.block.BaseColumnBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.machine.reactor.IntrusionReactor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

public class DisruptorNeedleBlock extends BaseColumnBlock implements EntityBlock
{
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public DisruptorNeedleBlock(RegistrationContext ctx, Properties settings)
    {
        super(ctx, ItemSettings.block(), settings);
        registerDefaultState(getStateDefinition().any().setValue(AXIS, Direction.Axis.Y).setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }

//    @Override
//    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
//    {
//        if (!newState.isOf(this) && world.getBlockEntity(pos) instanceof DisruptorNeedleBlockEntity be)
//        {
//            be.onStateReplaced();
//        }
//        super.onStateReplaced(state, world, pos, newState, moved);
//    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return IntrusionReactor.DISRUPTOR_NEEDLE_BE.create(pos, state);
    }
}
