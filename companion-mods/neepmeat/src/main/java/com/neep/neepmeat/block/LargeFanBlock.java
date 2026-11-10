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

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.item.BaseBlockItem;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.item.MeatlibItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.NMItemGroups;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.big_block.BigBlock;
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.api.big_block.BigBlockStructure;
import com.neep.neepmeat.api.big_block.BigBlockStructureEntity;
import com.neep.neepmeat.init.NMBlockEntities;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class LargeFanBlock extends BigBlock<LargeFanBlock.LargeFanStructureBlock> implements MeatlibBlock, EntityBlock
{
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    private final Map<Direction.Axis, BigBlockPattern> patternMap;

    public LargeFanBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, settings);
        BigBlockPattern upPattern = BigBlockPattern.makeOddCylinder(1, 0, 0, getStructure().defaultBlockState());
        BigBlockPattern northPattern = BigBlockPattern.makeRange(-1, -1, 0, 1, 1, 0, getStructure().defaultBlockState());
        ctx.append(this, new BaseBlockItem(this, ctx, itemSettings, new MeatlibItemSettings().group(NMItemGroups.GENERAL)));

        patternMap = new EnumMap<>(Map.of(
                Direction.Axis.X, northPattern.rotateY(90),
                Direction.Axis.Z, northPattern,
                Direction.Axis.Y, upPattern
        ));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        return defaultBlockState().setValue(AXIS, ctx.getClickedFace().getAxis());
    }

    @Override
    protected LargeFanStructureBlock registerStructureBlock(RegistrationContext ctx)
    {
        return ctx.append(this, new LargeFanStructureBlock(this, MeatlibBlockSettings.copyOf(this)), "large_fan_structure");
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.LARGE_FAN.create(pos, state);
    }

    public static class LargeFanStructureBlock extends BigBlockStructure<LargeFanStructureEntity>
    {
        public LargeFanStructureBlock(BigBlock<?> parent, Properties settings)
        {
            super(parent, settings);
        }

        @Override
        protected BlockEntityType<LargeFanStructureEntity> registerBlockEntity()
        {
            return Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "large_fan_structure"),
                    FabricBlockEntityTypeBuilder.create(
                            (p, s) -> new LargeFanStructureEntity(getBlockEntityType(), p, s),
                            this).build());
        }
    }

    public static class LargeFanStructureEntity extends BigBlockStructureEntity
    {
        public LargeFanStructureEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
        {
            super(type, pos, state);
        }
    }
}
