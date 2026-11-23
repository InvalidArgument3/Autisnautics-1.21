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

import com.neep.meatlib.block.BaseTrapdoorBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ScaffoldTrapdoorBlock extends BaseTrapdoorBlock
{
    protected static final VoxelShape EAST_SHAPE = Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 16.0);
    protected static final VoxelShape WEST_SHAPE = Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape SOUTH_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 2.0);
    protected static final VoxelShape NORTH_SHAPE = Block.box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape OPEN_BOTTOM_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
    protected static final VoxelShape OPEN_TOP_SHAPE = Block.box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0);

    public ScaffoldTrapdoorBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings.noOcclusion(), BlockSetType.IRON);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        if (!state.getValue(OPEN).booleanValue())
        {
            return state.getValue(HALF) == Half.TOP ? TOP_AABB : BOTTOM_AABB;
        }
        switch (state.getValue(FACING))
        {
            default:
            {
                return NORTH_OPEN_AABB;
            }
            case SOUTH:
            {
                return SOUTH_OPEN_AABB;
            }
            case WEST:
            {
                return WEST_OPEN_AABB;
            }
            case EAST:
        }
        return EAST_OPEN_AABB;
    }

}
