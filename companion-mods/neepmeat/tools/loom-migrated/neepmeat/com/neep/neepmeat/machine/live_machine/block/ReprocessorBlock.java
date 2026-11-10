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
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.big_block.BigBlock;
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.api.big_block.BigBlockStructure;
import com.neep.neepmeat.api.big_block.BigBlockStructureEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

public class ReprocessorBlock extends BigBlock<BigBlockStructure.Simple<ReprocessorBlock.StructureBlockEntity>>
{
    private final BigBlockPattern pattern = new BigBlockPattern().range(-1, 0, -1, 1, 2, 1, () -> getStructure().defaultBlockState());

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ReprocessorBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, settings);
        ctx.append(this, itemSettings.create(this, ctx));
    }

    @Override
    protected BigBlockStructure.Simple<StructureBlockEntity> registerStructureBlock(RegistrationContext ctx)
    {
        BigBlockStructure.BlockEntityRegisterererer<StructureBlockEntity> registerererer = b -> Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE, new ResourceLocation(NeepMeat.NAMESPACE, "reprocessor_structure"),
                FabricBlockEntityTypeBuilder.create((p, s) -> new StructureBlockEntity(b.getBlockEntityType(), p, s), b).build());

        return ctx.append(this, new BigBlockStructure.Simple<>(this, MeatlibBlockSettings.copyOf(properties), registerererer), "reprocessor_structure");
    }

    @Override
    public BigBlockPattern getVolume(BlockState blockState)
    {
        return pattern;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        Direction facing = ctx.getHorizontalDirection();
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    public static class StructureBlockEntity extends BigBlockStructureEntity
    {
        public StructureBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
        {
            super(type, pos, state);
        }
    }
}
