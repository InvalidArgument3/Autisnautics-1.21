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

import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.api.big_block.BigBlock;
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.api.big_block.BigBlockStructure;
import com.neep.neepmeat.api.big_block.BigBlockStructureEntity;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.machine.reactor.IntrusionReactor;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DisruptorSegmentBlock extends BigBlock<DisruptorSegmentBlock.StructureBlock> implements EntityBlock
{
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    private final EnumMap<Direction.Axis, BigBlockPattern> patternMap;

    public DisruptorSegmentBlock(RegistrationContext ctx, Properties settings, ItemSettings itemSettings)
    {
        super(ctx, settings);
        itemSettings.create(this, ctx);

        patternMap = new EnumMap<>(Map.of(
                Direction.Axis.Y, BigBlockPattern.makeOddCylinder(1, 0, 0, getStructure().defaultBlockState()),
                Direction.Axis.X, BigBlockPattern.makeRange(0, -1, -1, 0, 1, 1, getStructure().defaultBlockState()),
                Direction.Axis.Z, BigBlockPattern.makeRange(-1, -1, 0, 1, 1, 0, getStructure().defaultBlockState())
        ));
    }

    @Override
    protected StructureBlock registerStructureBlock(RegistrationContext ctx)
    {
        return ctx.append(this, new StructureBlock(this, MeatlibBlockSettings.copyOf(properties)), "disruptor_segment_structure");
    }

    @Override
    public BigBlockPattern getVolume(BlockState blockState)
    {
        return patternMap.get(blockState.getValue(AXIS));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(AXIS);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        EnumMap<Direction.Axis, VoxelShape> shapeMap = new EnumMap<>(Map.of(
                Direction.Axis.Y, Shapes.joinUnoptimized(
                        Block.box(-8, 0, -16, 24, 16, 32),
                        Block.box(-16, 0, -8, 32, 16, 24),
                        BooleanOp.OR),
                Direction.Axis.X, Shapes.joinUnoptimized(
                        Block.box(0, -8, -16, 16, 24, 32),
                        Block.box(0, -16, -8, 16, 32, 24),
                        BooleanOp.OR),
                Direction.Axis.Z, Shapes.joinUnoptimized(
                        Block.box(-8, -16, 0, 24, 32, 16),
                        Block.box(-16, -8, 0, 32, 24, 16),
                        BooleanOp.OR)
        ));

        return shapeMap.get(state.getValue(AXIS));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        return this.defaultBlockState().setValue(AXIS, ctx.getClickedFace().getAxis());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return IntrusionReactor.DISRUPTOR_SEGMENT_BE.create(pos, state);
    }

    public static class StructureBlock extends BigBlockStructure<StructureBlockEntity>
    {
        public StructureBlock(BigBlock<?> parent, Properties settings)
        {
            super(parent, settings);
        }

        @Override
        protected BlockEntityType<StructureBlockEntity> registerBlockEntity()
        {
            return NMBlockEntities.register("disruptor_segment_structure", (p, s) -> new StructureBlockEntity(getBlockEntityType(), p, s), this);
        }
    }

    public static class StructureBlockEntity extends BigBlockStructureEntity
    {
        public StructureBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
        {
            super(type, pos, state);
        }
    }
}
