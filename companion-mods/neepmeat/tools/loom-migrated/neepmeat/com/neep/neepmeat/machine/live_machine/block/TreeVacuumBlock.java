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
import com.neep.meatlib.item.BaseBlockItem;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.big_block.BigBlock;
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.api.big_block.BigBlockStructure;
import com.neep.neepmeat.api.big_block.BigBlockStructureEntity;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.live_machine.block.entity.TreeVacuumBlockEntity;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class TreeVacuumBlock extends BigBlock<TreeVacuumBlock.Structure> implements MeatlibBlock, EntityBlock
{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private final Map<Direction, BigBlockPattern> patternMap;
    private final Map<Direction, VoxelShape> shapeMap;

    public TreeVacuumBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, settings);

        ctx.append(this, new BaseBlockItem(this, ctx, itemSettings));
        this.registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));

        BigBlockPattern volume = BigBlockPattern.makeRange(-1, 0, 0, 1, 0, 0, getStructure().defaultBlockState())
                .set(0, 0, -1, getStructure().defaultBlockState())
                .set(0, 1, 0, getStructure().defaultBlockState())
                ;

        this.patternMap = new EnumMap<>(Direction.class);
        patternMap.put(Direction.NORTH, volume.rotateY(0));
        patternMap.put(Direction.EAST, volume.rotateY(90));
        patternMap.put(Direction.SOUTH, volume.rotateY(180));
        patternMap.put(Direction.WEST, volume.rotateY(270));

        VoxelShape northShape = Shapes.or(
                Block.box(-8, 0, 0, 24, 16, 16),
                Block.box(0, 16, 0, 16, 24, 16),
                Block.box(0, 0, -16, 16, 16, 0)
        );

        this.shapeMap = new EnumMap<>(Direction.class);
        shapeMap.put(Direction.NORTH, MiscUtil.rotateShapeY(northShape, 0));
        shapeMap.put(Direction.EAST, MiscUtil.rotateShapeY(northShape, 90));
        shapeMap.put(Direction.SOUTH, MiscUtil.rotateShapeY(northShape, 180));
        shapeMap.put(Direction.WEST, MiscUtil.rotateShapeY(northShape, 270));
    }

    @Override
    protected TreeVacuumBlock.Structure registerStructureBlock(RegistrationContext ctx)
    {
        BigBlockStructure.BlockEntityRegisterererer<BigBlockStructureEntity> register = b -> Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE, new ResourceLocation(NeepMeat.NAMESPACE, "tree_vacuum_structure"),
                FabricBlockEntityTypeBuilder.create(
                        (p, s) -> new BigBlockStructureEntity(b.getBlockEntityType(), p, s), b).build());

        return ctx.append(this, new Structure(this, MeatlibBlockSettings.copyOf(properties), register), "tree_vacuum_structure");
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        if (ctx.getPlayer() == null)
            return defaultBlockState();

        return defaultBlockState().setValue(FACING, ctx.getPlayer().isShiftKeyDown() ? ctx.getHorizontalDirection().getOpposite() : ctx.getHorizontalDirection());
    }

    @Override
    public BigBlockPattern getVolume(BlockState blockState)
    {
        return patternMap.get(blockState.getValue(FACING));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
//        if (state.get(FACING) == Direction.NORTH)
//        {
//            return VoxelShapes.union(
//                    Block.createCuboidShape(-8, 0, 0, 24, 16, 16),
//                    Block.createCuboidShape(0, 16, 0, 16, 24, 16),
//                    Block.createCuboidShape(0, 0, -16, 16, 16, 0)
//            );
//        }
        return shapeMap.get(state.getValue(FACING));
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
        return LivingMachines.TREE_VACUUM_BE.create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, LivingMachines.TREE_VACUUM_BE, null, TreeVacuumBlockEntity::clientTick, world);
    }

    public static class Structure extends BigBlockStructure<BigBlockStructureEntity>
    {
        public Structure(BigBlock<?> parent, Properties settings, BlockEntityRegisterererer<BigBlockStructureEntity> registerBlockEntity)
        {
            super(parent, settings, registerBlockEntity);
        }

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
        {
            if (world.getBlockEntity(pos) instanceof BigBlockStructureEntity be)
            {
                BlockPos controllerPos = be.getControllerPos();
                if (controllerPos == null)
                    return Shapes.block();

                BlockState parentState = world.getBlockState(controllerPos);
                if (parentState.is(parent)) // Sometimes air replaces the parent (not sure why)
                    return be.translateChopShape(parent.getShape(parentState, world, pos, context));
            }

            return Shapes.empty();
        }
    }
}
