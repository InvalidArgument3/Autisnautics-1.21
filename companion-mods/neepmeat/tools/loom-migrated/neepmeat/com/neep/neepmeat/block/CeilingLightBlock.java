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

import com.neep.meatlib.block.BaseLightBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CeilingLightBlock extends BaseLightBlock
{
    public static final EnumProperty<Direction.Axis> AXIS = EnumProperty.create("axis", Direction.Axis.class, Direction.Axis.X, Direction.Axis.Z);
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape Z_TOP = Block.box(2, 14, 0, 14, 16, 16);
    private static final VoxelShape X_TOP = Block.box(0, 14, 2, 16, 16, 14);
    private static final VoxelShape Z_BOTTOM = Block.box(2, 0, 0, 14, 2, 16);
    private static final VoxelShape X_BOTTOM = Block.box(0, 0, 2, 16, 2, 14);

    public CeilingLightBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings.noOcclusion().forceSolidOn());
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false).setValue(BOTTOM, false));
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter world, BlockPos pos, PathComputationType type)
    {
        return false;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        var state = super.getStateForPlacement(ctx);
        if (state == null)
            return null;

        FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
        boolean lit = ctx.getLevel().hasNeighborSignal(ctx.getClickedPos());

        Direction look = ctx.getHorizontalDirection();
        boolean waterlogged = fluidState.getType() == Fluids.WATER;

        double y = ctx.getClickLocation().y();
        boolean bottom = y - Math.floor(y) < 0.5;
        if (ctx.getClickedFace() == Direction.UP)
            bottom = true;
        else if (ctx.getClickedFace() == Direction.DOWN)
            bottom = false;

        return state
                .setValue(BOTTOM, bottom)
                .setValue(LIT, lit)
                .setValue(AXIS, look.getAxis())
                .setValue(WATERLOGGED, waterlogged);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        if (state.getValue(BOTTOM))
        {
            return switch (state.getValue(AXIS))
            {
                case X -> X_BOTTOM;
                case Z -> Z_BOTTOM;
                default -> Shapes.block();
            };
        }
        else
        {
            return switch (state.getValue(AXIS))
            {
                case X -> X_TOP;
                case Z -> Z_TOP;
                default -> Shapes.block();
            };
        }
    }

    @Override
    public FluidState getFluidState(BlockState state)
    {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos)
    {
        if (state.getValue(WATERLOGGED))
        {
            world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }

        return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos)
    {
        return 1;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(AXIS, WATERLOGGED, BOTTOM);
    }
}
