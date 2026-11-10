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
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.util.AxialDirection;
import com.neep.meatlib.util.MiscUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;


public class HoldingTrackBlock extends BaseRailBlock implements EntityBlock
{
    public static final EnumProperty<AxialDirection> FACING = EnumProperty.create("direction", AxialDirection.class);
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public HoldingTrackBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(true, settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(POWERED, false).setValue(WATERLOGGED, false).setValue(FACING, AxialDirection.POSITIVE));
        ctx.append(this, new BaseBlockItem(this, ctx, itemSettings));
    }

    @Override
    public Property<RailShape> getShapeProperty()
    {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(SHAPE, POWERED, FACING, WATERLOGGED);
    }

    @Override
    protected void updateState(BlockState state, Level world, BlockPos pos, Block neighbor)
    {
        boolean powered = state.getValue(POWERED);
        boolean receiving = world.hasNeighborSignal(pos);
        if (receiving != powered)
        {
            BlockState newState = state.setValue(POWERED, receiving);
            world.setBlock(pos, state.setValue(POWERED, receiving), Block.UPDATE_ALL);
            world.getBlockEntity(pos, NMBlockEntities.HOLDING_TRACK).ifPresent(be ->
            {
                be.setBlockState(newState);
            });
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
                .setValue(FACING, AxialDirection.from(direction))
                .setValue(WATERLOGGED, waterlogged);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.HOLDING_TRACK.create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NMBlockEntities.HOLDING_TRACK, (world1, pos, state1, blockEntity) -> ((HoldingTrackBlockEntity) blockEntity).serverTick(), null, world);
    }

    public static class HoldingTrackBlockEntity extends BlockEntity
    {
        public HoldingTrackBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
        {
            super(type, pos, state);
        }

        public void serverTick()
        {
            boolean powered = getBlockState().getValue(POWERED);
            AxialDirection direction = getBlockState().getValue(FACING);
            RailShape railShape = getBlockState().getValue(SHAPE);

            AABB box = new AABB(getBlockPos()).contract(0.24, 0.24, 0.24);

            double x = worldPosition.getX() + 0.5;
            double y = worldPosition.getY();
            double z = worldPosition.getZ() + 0.5;

            List<AbstractMinecart> result = level.getEntitiesOfClass(AbstractMinecart.class, box);
            if (!result.isEmpty())
            {
                AbstractMinecart e = result.get(0);

                if (!powered)
                {
                    e.setPos(x, y, z);
                    e.setDeltaMovement(0, 0, 0);
                }
                else
                {
                    Vector3f vel = direction.with(axis(railShape));
                    e.setDeltaMovement(vel.x, vel.y, vel.z);
                }
            }
        }

        protected static Direction.Axis axis(RailShape railShape)
        {
            return switch (railShape)
            {
                case NORTH_SOUTH, ASCENDING_NORTH, ASCENDING_SOUTH -> Direction.Axis.Z;
                case EAST_WEST, ASCENDING_EAST, ASCENDING_WEST -> Direction.Axis.X;
                default -> throw new IllegalStateException();
            };
        }
    }
}
