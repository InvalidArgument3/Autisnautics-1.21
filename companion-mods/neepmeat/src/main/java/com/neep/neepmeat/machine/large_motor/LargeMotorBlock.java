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

package com.neep.neepmeat.machine.large_motor;

import com.google.common.collect.ImmutableMap;
import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.item.BaseBlockItem;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.api.big_block.BigBlock;
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.machine.motor.MotorEntity;
import com.neep.meatlib.util.MiscUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LargeMotorBlock extends BigBlock<LargeMotorStructureBlock> implements MeatlibBlock, EntityBlock
{
    private final BigBlockPattern volume;
    public final VoxelShape normalShape;

    private static VoxelShape cuboid(double minX, double minY, double minZ, double sizeX, double sizeY, double sizeZ)
    {
        return Shapes.box(minX, minY, minZ, minX + sizeX, minY + sizeY, minZ + sizeZ);
    }

    private final Map<Direction, VoxelShape> shapeMap = ImmutableMap.of(
        Direction.NORTH, cuboid(-0.5, 0, -1, 2, 2.5, 2),
        Direction.EAST, cuboid(0, 0, -0.5, 2, 2.5, 2),
        Direction.SOUTH, cuboid(-0.5, 0, 0, 2, 2.5, 2),
        Direction.WEST, cuboid(-1, 0, -0.5, 2, 2.5, 2)
    );

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public LargeMotorBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, settings);
        ctx.append(this, new BaseBlockItem(this, ctx, itemSettings));
        this.registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));

        volume = BigBlockPattern.makeRange( -1, 0, 0, 1, 2, -1, getStructure().defaultBlockState());
        normalShape = volume.toVoxelShape();
    }

    @Override
    protected LargeMotorStructureBlock registerStructureBlock(RegistrationContext ctx)
    {
        return ctx.append(this, new LargeMotorStructureBlock(this, MeatlibBlockSettings.copyOf(this)), "large_motor_structure");
    }

    @Override
    public BigBlockPattern getVolume(BlockState blockState)
    {
        Direction facing = blockState.getValue(FACING);
        return volume.rotateY(facing.toYRot() - 180);
    }


    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
//        return rotateShape(NORMAL_SHAPE, state.get(FACING));
        return shapeMap.get(state.getValue(FACING));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!state.is(newState.getBlock()))
        {
            world.getBlockEntity(pos, NMBlockEntities.LARGE_MOTOR).ifPresent(MotorEntity::onRemoved);
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    public static VoxelShape rotateShape(VoxelShape shape, Direction direction)
    {
        AtomicReference<VoxelShape> newShape = new AtomicReference<>(Shapes.empty());
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
        {
            newShape.set(Shapes.or(newShape.get(), rotatedCuboid(direction, minX, minY, minZ, maxX, maxY, maxZ)));
        });

        return newShape.get().optimize();
    }

    public static VoxelShape rotatedCuboid(Direction direction, double xMin, double yMin, double zMin, double xMax, double yMax, double zMax)
    {
        return switch (direction)
        {
            case NORTH -> Shapes.box(xMin, yMin, zMin, xMax, yMax, zMax);
            case SOUTH -> Shapes.box(xMax, yMin, 1 - zMin, xMin, yMax, 1 - zMax);
            case WEST -> Shapes.box(zMin, yMin, xMin, zMax, yMax, xMax);
            case EAST -> Shapes.box(1 - zMin, yMin, xMin, 1 - zMax, yMax, xMax);
            default -> throw new IllegalStateException("Unexpected value: " + direction);
        };
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NMBlockEntities.LARGE_MOTOR, (world1, pos, state1, blockEntity) -> blockEntity.serverTick(), null, world);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.LARGE_MOTOR.create(pos, state);
    }
}
