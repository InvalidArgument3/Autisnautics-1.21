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

import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.api.big_block.BigBlock;
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.api.big_block.BigBlockStructure;
import com.neep.neepmeat.api.big_block.BigBlockStructureEntity;
import com.neep.neepmeat.api.big_block.StructureBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

public class ReprocessorBlock extends BigBlock<BigBlockStructure.Simple<ReprocessorBlock.StructureBlockEntity>> {
    private final BigBlockPattern pattern = new BigBlockPattern().range(-1, 0, -1, 1, 2, 1, () -> getStructure().defaultBlockState());
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ReprocessorBlock(RegistrationContext ctx, net.minecraft.world.level.block.state.BlockBehaviour.Properties settings) {
        super(ctx, settings);
    }

    @Override
    protected BigBlockStructure.Simple<StructureBlockEntity> registerStructureBlock(RegistrationContext ctx) {
        BlockEntityType[] box = new BlockEntityType[1];
        BigBlockStructure.BlockEntityRegisterererer<StructureBlockEntity> register = structure -> {
            box[0] = BlockEntityType.Builder.of((pos, state) -> new StructureBlockEntity(box[0], pos, state), structure).build(null);
            return StructureBlockEntities.register("reprocessor_structure", box[0]);
        };
        return ctx.append(this, new BigBlockStructure.Simple<>(this, MeatlibBlockSettings.copyOf(properties), register, (pos, state) -> new StructureBlockEntity(box[0], pos, state)), "reprocessor_structure");
    }

    @Override
    public BigBlockPattern getVolume(BlockState blockState) {
        return pattern;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    public static class StructureBlockEntity extends BigBlockStructureEntity {
        public StructureBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }
    }
}
