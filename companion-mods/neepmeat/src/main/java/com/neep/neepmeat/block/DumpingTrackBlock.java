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

import com.neep.meatlib.item.BaseBlockItem;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;


public class DumpingTrackBlock extends BaseRailBlock implements SpecialRail
{
    public static final EnumProperty<RailShape> RAIL_SHAPE_NO_SLOPE = EnumProperty.create(
            "shape",
            RailShape.class,
            shape -> (shape != RailShape.NORTH_EAST && shape != RailShape.NORTH_WEST && shape != RailShape.SOUTH_EAST && shape != RailShape.SOUTH_WEST)
    );

    public static final EnumProperty<RailShape> SHAPE = RAIL_SHAPE_NO_SLOPE;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public DumpingTrackBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(true, settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH)
                .setValue(POWERED, false)
                .setValue(WATERLOGGED, false));
        ctx.append(this, new BaseBlockItem(this, ctx, itemSettings));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
    {
        return true;
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
    {
        if (!world.isClientSide && world.getBlockState(pos).is(this))
        {
            this.updateState(state, world, pos, sourceBlock);
        }
    }

    @Override
    public void apply(Level world, BlockPos pos, BlockState state, AbstractMinecart minecart)
    {
        if (world.isClientSide() || !state.getValue(POWERED))
            return;

        Entity passenger = minecart.getFirstPassenger();
        double thisX = pos.getX() + 0.5;
        double thisZ = pos.getZ() + 0.5;
        if (passenger != null
                && Math.abs(minecart.getX() - thisX) < 0.5
                && Math.abs(minecart.getZ() - thisZ) < 0.5)
        {
            passenger.stopRiding();
            Vec3 prevPassengerPos = passenger.position();
            passenger.setPos(thisX, prevPassengerPos.y, thisZ);
        }
    }

    @Override
    public Property<RailShape> getShapeProperty()
    {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(SHAPE, POWERED, WATERLOGGED);
    }

    @Override
    protected void updateState(BlockState state, Level world, BlockPos pos, Block neighbor)
    {
        boolean powered = state.getValue(POWERED);
        boolean receiving = world.hasNeighborSignal(pos);
        if (receiving != powered)
        {
            if (powered)
                world.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1f, 0.7f);
            else
                world.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1f, 0.7f);

            BlockState newState = state.setValue(POWERED, receiving);
            world.setBlock(pos, newState, Block.UPDATE_ALL);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
        boolean waterlogged = fluidState.getType() == Fluids.WATER;
        BlockState blockState = super.defaultBlockState();
        Direction direction = ctx.getHorizontalDirection();
        boolean eastWest = direction == Direction.EAST || direction == Direction.WEST;
        return blockState
                .setValue(this.getShapeProperty(), eastWest ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH)
                .setValue(WATERLOGGED, waterlogged);
    }
}
