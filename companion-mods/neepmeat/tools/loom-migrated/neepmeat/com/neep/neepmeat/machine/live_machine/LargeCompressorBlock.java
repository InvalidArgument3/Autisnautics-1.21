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

package com.neep.neepmeat.machine.live_machine;

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.item.BaseBlockItem;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.FluidPump;
import com.neep.neepmeat.api.big_block.BigBlock;
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.api.big_block.BigBlockStructure;
import com.neep.neepmeat.api.big_block.BigBlockStructureEntity;
import com.neep.neepmeat.api.live_machine.LivingMachineStructure;
import com.neep.neepmeat.api.live_machine.PropertyValue;
import com.neep.neepmeat.api.live_machine.StructureProperty;
import com.neep.neepmeat.machine.live_machine.block.entity.LargeCompressorBlockEntity;
import com.neep.neepmeat.transport.fluid_network.node.AcceptorModes;
import com.neep.meatlib.util.MiscUtil;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class LargeCompressorBlock extends BigBlock<LargeCompressorBlock.Structure> implements MeatlibBlock, EntityBlock
{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private final Map<Direction, BigBlockPattern> patternMap;
    private final Map<Direction, VoxelShape> shapeMap;

    public LargeCompressorBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, settings);

        ctx.append(this, new BaseBlockItem(this, ctx, itemSettings));

        BigBlockPattern pattern = new BigBlockPattern().oddCylinder(1, 0, 2, () -> getStructure().defaultBlockState())
                .enableApi(-1, 2, -1, FluidStorage.SIDED)
                .enableApi(-1, 2, -1, FluidPump.SIDED);
//                .range(0, 2, 1, 0, 2, -1, () -> getStructure().getDefaultState());

        patternMap = new EnumMap<>(Direction.class);
        patternMap.put(Direction.NORTH, pattern);
        patternMap.put(Direction.EAST, pattern.rotateY(90));
        patternMap.put(Direction.SOUTH, pattern.rotateY(180));
        patternMap.put(Direction.WEST, pattern.rotateY(270));

        VoxelShape northShape = Shapes.join(
                Shapes.joinUnoptimized(
                        Block.box(-16, 35, -13, 0, 45, -3),
                        Block.box(-10, 0, -16, 26, 16, 32), BooleanOp.OR),
                Block.box(-6, 16, -16, 22, 47, 32),
                BooleanOp.OR);

        shapeMap = new EnumMap<>(Direction.class);
        shapeMap.put(Direction.NORTH, northShape);
        shapeMap.put(Direction.EAST, MiscUtil.rotateShapeY(northShape, 90));
        shapeMap.put(Direction.SOUTH, MiscUtil.rotateShapeY(northShape, 180));
        shapeMap.put(Direction.WEST, MiscUtil.rotateShapeY(northShape, 270));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        return this.defaultBlockState().setValue(FACING, ctx.getPlayer().isShiftKeyDown() ? ctx.getHorizontalDirection().getOpposite() : ctx.getHorizontalDirection());
    }

    @Override
    protected Structure registerStructureBlock(RegistrationContext ctx)
    {
        // Oh, crumbs
        BigBlockStructure.BlockEntityRegisterererer<StructureBlockEntity> registerererer = b -> Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE, new ResourceLocation(NeepMeat.NAMESPACE, "large_compressor_structure"),
                FabricBlockEntityTypeBuilder.create(
                        (p, s) -> new StructureBlockEntity(b.getBlockEntityType(), p, s), b).build());

        return ctx.append(this, new Structure(this, MeatlibBlockSettings.copyOf(this), registerererer), "large_compressor_structure");
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
        return LivingMachines.LARGE_COMPRESSOR_BE.create(pos, state);
    }

    public static class Structure extends BigBlockStructure<StructureBlockEntity> implements LivingMachineStructure
    {
        public Structure(BigBlock<?> parent, Properties settings, BlockEntityRegisterererer<StructureBlockEntity> registerBlockEntity)
        {
            super(parent, settings, registerBlockEntity);
        }

        @Override
        public EnumMap<StructureProperty, PropertyValue> getProperties()
        {
            return StructureProperty.EMPTY;
        }
    }

    public static class StructureBlockEntity extends BigBlockStructureEntity
    {

        private static final FluidPump PUMP = FluidPump.of(-1, () -> AcceptorModes.PUSH);

        public StructureBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
        {
            super(type, pos, state);
        }

        @Nullable
        public FluidPump getFluidPump(@Nullable Direction direction)
        {
            if (apis.contains(FluidPump.SIDED.getId()))
            {
                var controller = getControllerBE(LargeCompressorBlockEntity.class);
                if (controller != null)
                {
                    Direction facing = controller.getBlockState().getValue(FACING);

                    if (facing.getClockWise().getOpposite() == direction)
                    {
                        return PUMP;
                    }
                }
            }
            return null;
        }

        @Nullable
        public Storage<FluidVariant> getOutputStorage(@Nullable Direction direction)
        {
            if (apis.contains(FluidStorage.SIDED.getId()))
            {
                var controller = getControllerBE(LargeCompressorBlockEntity.class);
                if (controller != null)
                {
                    Direction facing = controller.getBlockState().getValue(FACING);

                    // Output protrudes on the left side relative to facing/placement direction
                    if (facing.getClockWise().getOpposite() == direction)
                        return controller.getOutputStorage();
                }
            }
            return null;
        }
    }
}
