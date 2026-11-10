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

package com.neep.neepmeat.machine.live_machine.block;

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.api.big_block.BigBlock;
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.api.big_block.BigBlockStructure;
import com.neep.neepmeat.api.big_block.BigBlockStructureEntity;
import com.neep.neepmeat.api.big_block.StructureBlockEntities;
import com.neep.neepmeat.machine.live_machine.block.entity.LargeTrommelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import com.neep.meatlib.util.MiscUtil;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class LargeTrommelBlock extends BigBlock<BigBlockStructure.Simple<BigBlockStructureEntity>> implements MeatlibBlock, EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private final Map<Direction, BigBlockPattern> patterns;
    private final Map<Direction, VoxelShape> shapes;

    public LargeTrommelBlock(RegistrationContext ctx, Properties settings) {
        super(ctx, settings.noOcclusion());
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
        BigBlockPattern volume = BigBlockPattern.makeRange(0, 0, 0, 1, 1, -3, getStructure().defaultBlockState());
        patterns = Map.of(
                Direction.NORTH, volume.rotateY(0),
                Direction.EAST, volume.rotateY(90),
                Direction.SOUTH, volume.rotateY(180),
                Direction.WEST, volume.rotateY(270));
        VoxelShape north = volume.toVoxelShape();
        shapes = Map.of(
                Direction.NORTH, MiscUtil.rotateShapeY(north, 0),
                Direction.EAST, MiscUtil.rotateShapeY(north, 90),
                Direction.SOUTH, MiscUtil.rotateShapeY(north, 180),
                Direction.WEST, MiscUtil.rotateShapeY(north, 270));
    }

    @Override
    protected BigBlockStructure.Simple<BigBlockStructureEntity> registerStructureBlock(RegistrationContext ctx) {
        BlockEntityType[] box = new BlockEntityType[1];
        BigBlockStructure.BlockEntityRegisterererer<BigBlockStructureEntity> register = structure -> {
            box[0] = BlockEntityType.Builder.of((pos, state) -> new BigBlockStructureEntity(box[0], pos, state), structure).build(null);
            return StructureBlockEntities.register("large_trommel_structure", box[0]);
        };
        return ctx.append(this, new BigBlockStructure.Simple<>(this, MeatlibBlockSettings.copyOf(properties), register,
                (pos, state) -> new BigBlockStructureEntity(box[0], pos, state)), "large_trommel_structure");
    }

    @Override
    public BigBlockPattern getVolume(BlockState state) {
        return patterns.get(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return shapes.get(state.getValue(FACING));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LargeTrommelBlockEntity(pos, state);
    }

    @Override
    public BlockItem blockItem() {
        return (BlockItem) net.minecraft.world.item.Item.BY_BLOCK.get(this);
    }
}
