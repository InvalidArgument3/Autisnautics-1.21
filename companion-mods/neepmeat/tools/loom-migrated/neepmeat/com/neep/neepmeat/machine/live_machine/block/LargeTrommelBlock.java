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

import com.google.common.collect.ImmutableMap;
import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.item.BaseBlockItem;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.big_block.BigBlock;
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.api.big_block.BigBlockStructure;
import com.neep.neepmeat.api.big_block.BigBlockStructureEntity;
import com.neep.neepmeat.api.live_machine.LivingMachineStructure;
import com.neep.neepmeat.api.live_machine.PropertyValue;
import com.neep.neepmeat.api.live_machine.StructureProperty;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.motor.MotorEntity;
import com.neep.meatlib.util.MiscUtil;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class LargeTrommelBlock extends BigBlock<LargeTrommelBlock.StructureBlock> implements MeatlibBlock, EntityBlock
{
    private final Map<Direction, BigBlockPattern> patternMap;
    private final Map<Direction, VoxelShape> shapeMap;

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public LargeTrommelBlock(RegistrationContext ctx, Properties settings, ItemSettings itemSettings)
    {
        super(ctx, settings.noOcclusion());
        ctx.append(this, new BaseBlockItem(this, ctx, itemSettings));
        this.registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));

        BigBlockPattern volume = BigBlockPattern.makeRange(0, 0, 0, 1, 1, -3, getStructure().defaultBlockState());

        this.patternMap = ImmutableMap.of(
                Direction.NORTH, volume.rotateY(0),
                Direction.EAST, volume.rotateY(90),
                Direction.SOUTH, volume.rotateY(180),
                Direction.WEST, volume.rotateY(270)
        );

        VoxelShape northShape = volume.toVoxelShape();

        this.shapeMap = ImmutableMap.of(
                Direction.NORTH, MiscUtil.rotateShapeY(northShape, 0),
                Direction.EAST, MiscUtil.rotateShapeY(northShape, 90),
                Direction.SOUTH, MiscUtil.rotateShapeY(northShape, 180),
                Direction.WEST, MiscUtil.rotateShapeY(northShape, 270)
        );
    }

    @Override
    protected StructureBlock registerStructureBlock(RegistrationContext ctx)
    {
        // Oh, crumbs
        BigBlockStructure.BlockEntityRegisterererer<LargeTrommelStructureBlockEntity> registerererer = b -> Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE, new ResourceLocation(NeepMeat.NAMESPACE, "large_trommel_structure"),
                FabricBlockEntityTypeBuilder.create(
                        (p, s) -> new LargeTrommelStructureBlockEntity(b.getBlockEntityType(), p, s), b).build());

        return ctx.append(this, new StructureBlock(this, MeatlibBlockSettings.copyOf(this), registerererer), "large_trommel_structure");
    }

    @Override
    public BigBlockPattern getVolume(BlockState blockState)
    {
        return patternMap.get(blockState.getValue(FACING));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return shapeMap.get(state.getValue(FACING));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return LivingMachines.LARGE_TROMMEL_BE.create(pos, state);
    }

    public static class StructureBlock extends BigBlockStructure<LargeTrommelStructureBlockEntity> implements LivingMachineStructure
    {
        public StructureBlock(BigBlock<?> parent, Properties settings, BlockEntityRegisterererer<LargeTrommelStructureBlockEntity> registerBlockEntity)
        {
            super(parent, settings, registerBlockEntity);
        }

        @Override
        public EnumMap<StructureProperty, PropertyValue> getProperties()
        {
            return StructureProperty.EMPTY;
        }
    }

    public static class LargeTrommelStructureBlockEntity extends BigBlockStructureEntity
    {
        public LargeTrommelStructureBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
        {
            super(type, pos, state);
        }
    }
}
