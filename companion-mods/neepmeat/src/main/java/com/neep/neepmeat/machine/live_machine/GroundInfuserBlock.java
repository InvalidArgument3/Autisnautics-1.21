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

package com.neep.neepmeat.machine.live_machine;

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.block.multi.TallerBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GroundInfuserBlock extends TallerBlock
{
    public static final IntegerProperty HEIGHT_3 = IntegerProperty.create("height", 1, 2);

    public GroundInfuserBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, HEIGHT_3, itemSettings, settings);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(HEIGHT_3);
    }

    @Override
    protected Structure createStructure(RegistrationContext ctx)
    {
        return ctx.append(this, new Structure(MeatlibBlockSettings.copyOf(properties))
        {
            @Override
            protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
            {
                super.createBlockStateDefinition(builder);
            }

            @Override
            protected BlockState getState(BlockState baseState)
            {
                return super.getState(baseState);
            }

            @Override
            protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
            {
                return Shapes.block();
            }
        }, MeatlibBlock::structure);
    }
}
