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
import com.neep.neepmeat.interfaces.AbstractMinecartEntityAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.Vec3;
import com.neep.meatlib.util.MiscUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;


public class PlayerControlTrack extends BaseRailBlock implements EntityBlock
{
    public static final EnumProperty<RailShape> RAIL_SHAPE_NO_SLOPE = EnumProperty.create(
            "shape",
            RailShape.class,
            shape -> !shape.isAscending() && (shape != RailShape.NORTH_EAST && shape != RailShape.NORTH_WEST && shape != RailShape.SOUTH_EAST && shape != RailShape.SOUTH_WEST)
    );

//    public static final EnumProperty<AxialDirection> FACING = EnumProperty.of("direction", AxialDirection.class);
    public static final EnumProperty<RailShape> SHAPE = RAIL_SHAPE_NO_SLOPE;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public PlayerControlTrack(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(true, settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH)
                .setValue(POWERED, false)
                .setValue(WATERLOGGED, false));
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
        builder.add(SHAPE, POWERED, WATERLOGGED);
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
            world.getBlockEntity(pos, NMBlockEntities.CONTROL_TRACK).ifPresent(be ->
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
//                .with(FACING, AxialDirection.from(direction))
                .setValue(WATERLOGGED, waterlogged);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.CONTROL_TRACK.create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NMBlockEntities.CONTROL_TRACK, (world1, pos, state1, blockEntity) -> blockEntity.serverTick(), null, world);
    }

    public static class TrackBlockEntity extends BlockEntity
    {
        public TrackBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
        {
            super(type, pos, state);
        }

        @Nullable
        private Direction getDirection(AbstractMinecart minecart, boolean powered)
        {
            boolean playerPassenger = minecart.getFirstPassenger() instanceof Player;

            if (playerPassenger)
            {
                Vec3 playerVel = ((AbstractMinecartEntityAccess) minecart).neepmeat$getControllerVelocity();
                if (playerVel.horizontalDistanceSqr() > 0)
                {
                    Direction direction = Direction.getNearest(playerVel.x, 0, playerVel.z);
                    BlockState offsetState = level.getBlockState(worldPosition.relative(direction));

                    if (isRail(offsetState))
                        return direction;

                    // Fall back to minecart velocity
                }
            }

            if (!powered)
                return null;

            Vec3 minecartVel = minecart.getDeltaMovement();
            if (minecartVel.horizontalDistanceSqr() > 0)
            {
                Direction direction = Direction.getNearest(minecartVel.x, 0, minecartVel.z);
                BlockState offsetState = level.getBlockState(worldPosition.relative(direction));

                if (isRail(offsetState))
                    return direction;

                // Search for a valid adjacent rail that isn't the input direction.
                BlockPos.MutableBlockPos mutable = worldPosition.mutable();
                for (Direction face : Direction.values())
                {
                    if (face.getAxis().isVertical() || face == direction.getOpposite())
                        continue;

                    mutable.setWithOffset(worldPosition, face);

                    if (isRail(level, mutable))
                        return face;
                }

                // Derail if there are no output rails.
                return direction;
            }

            return null;
        }

        public void serverTick()
        {
            AABB box = new AABB(getBlockPos()).contract(0.24, 0.24, 0.24);

            double x = worldPosition.getX() + 0.5;
            double z = worldPosition.getZ() + 0.5;

            level.getEntitiesOfClass(AbstractMinecart.class, box).forEach(minecart ->
            {
                BlockState state = level.getBlockState(worldPosition);
                RailShape shape = state.getValue(RAIL_SHAPE_NO_SLOPE);

                Direction direction = getDirection(minecart, state.getValue(POWERED));
                if (direction != null)
                {
                    if (direction == Direction.NORTH || direction == Direction.SOUTH)
                    {
                        if (shape != RailShape.NORTH_SOUTH)
                            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(RAIL_SHAPE_NO_SLOPE, RailShape.NORTH_SOUTH));
                    }
                    else
                    {
                        if (shape != RailShape.EAST_WEST)
                            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(RAIL_SHAPE_NO_SLOPE, RailShape.EAST_WEST));
                    }

                    Vector3f unit = direction.step().mul(0.3f);
                    minecart.push(unit.x, unit.y, unit.z);
                }
                else
                {
                    minecart.setDeltaMovement(0, 0, 0);
                    minecart.setPos(x, minecart.getY(), z);
                }
            });
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
