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

import com.neep.meatlib.block.BaseHorFacingBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SinkBlock extends BaseHorFacingBlock
{
    public static final Map<Direction, VoxelShape> SHAPES = Map.of(
            Direction.NORTH, makeShape(Direction.NORTH),
            Direction.EAST, makeShape(Direction.EAST),
            Direction.SOUTH, makeShape(Direction.SOUTH),
            Direction.WEST, makeShape(Direction.WEST)
        );

    public SinkBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings.noOcclusion());
    }

    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    public static VoxelShape makeShape(Direction facing)
    {
        // Auto-generated
        VoxelShape shape = Shapes.or(
                Shapes.box(0.125, -0.125, 0.8125, 0.875, 0.1875, 1),
                Shapes.box(0.125, 0.1875, 0.9375, 0.875, 0.375, 1),
                Shapes.box(0.4375, 0.28125, 0.625, 0.5625, 0.34375, 0.9375),
                Shapes.box(0.4375, 0.15625, 0.875, 0.5625, 0.28125, 0.9375),
                Shapes.box(0.1875, 0.375, 0.9375, 0.8125, 0.4375, 1),
                Shapes.box(0.125, -0.0625, 0.375, 0.875, 0.1875, 0.4375),
                Shapes.box(0.125, -0.0625, 0.4375, 0.1875, 0.1875, 0.8125),
                Shapes.box(0.1875, -0.125, 0.4375, 0.8125, 0.015625, 0.8125),
                Shapes.box(0.8125, -0.0625, 0.4375, 0.875, 0.1875, 0.8125)
        );

        final VoxelShape[] transformed = {Shapes.empty()};

        double angle = getFacingAngle(facing);
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
        {
            Vec3 min = new Vec3(minX, minY, minZ)
                    .add(-0.5, -0.5, -0.5)
                    .yRot((float) angle)
                    .add(0.5, 0.5, 0.5);
            Vec3 max = new Vec3(maxX, maxY, maxZ)
                    .add(-0.5, -0.5, -0.5)
                    .yRot((float) angle)
                    .add(0.5, 0.5, 0.5);

            VoxelShape next = Shapes.create(new AABB(min, max));
            transformed[0] = Shapes.or(transformed[0], next);
        });

        transformed[0].optimize();
        return transformed[0];
    }

    protected static double getFacingAngle(Direction facing)
    {
        return switch (facing)
        {
            case NORTH -> 0;
            case EAST -> -Math.PI / 2;
            case SOUTH -> Math.PI;
            case WEST -> Math.PI / 2;
            default -> 0;
        };
    }
}