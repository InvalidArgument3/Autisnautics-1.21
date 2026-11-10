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
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.api.big_block_2.BigBlock2;
import com.neep.neepmeat.api.big_block_2.BigBlock2Structure;
import com.neep.neepmeat.api.big_block_2.PatternOffsetMap;
import com.neep.neepmeat.api.live_machine.LivingMachineStructure;
import com.neep.neepmeat.api.live_machine.PropertyValue;
import com.neep.neepmeat.api.live_machine.StructureProperty;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.live_machine.block.entity.StomachBlockEntity;
import com.neep.neepmeat.machine.live_machine.item.StomachBlockItem;
import com.neep.neepmeat.transport.block.fluid_transport.machine.TankBlockEntity;
import com.neep.neepmeat.util.ItemUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

public class StomachBlock extends BigBlock2 implements EntityBlock
{
    private static final BigBlockPattern Y_PATTERN = new BigBlockPattern().oddCylinder(1, -1, 1, Blocks.AIR::defaultBlockState)
            .enableApi(-1, 0, -1, FluidStorage.SIDED)
            .enableApi(-1, 0, 0, FluidStorage.SIDED)
            .enableApi(-1, 0, 1, FluidStorage.SIDED)
            .enableApi(0, 0, -1, FluidStorage.SIDED)
            .enableApi(0, 0, 1, FluidStorage.SIDED)
            .enableApi(1, 0, -1, FluidStorage.SIDED)
            .enableApi(1, 0, 0, FluidStorage.SIDED)
            .enableApi(1, 0, 1, FluidStorage.SIDED)
            ;
    private static final BigBlockPattern X_PATTERN = new BigBlockPattern().range(-1, -1, -1, 1, 1, 1, Blocks.AIR::defaultBlockState)
            .enableApi(0, -1,-1, FluidStorage.SIDED)
            .enableApi(0, -1, 0, FluidStorage.SIDED)
            .enableApi(0, -1, 1, FluidStorage.SIDED)
            .enableApi(0, 0, -1, FluidStorage.SIDED)
            .enableApi(0, 0, 1, FluidStorage.SIDED)
            .enableApi(0, 1, -1, FluidStorage.SIDED)
            .enableApi(0, 1, 0, FluidStorage.SIDED)
            .enableApi(0, 1, 1, FluidStorage.SIDED)
            ;
    private static final BigBlockPattern Z_PATTERN = new BigBlockPattern().range(-1, -1, -1, 1, 1, 1, Blocks.AIR::defaultBlockState)
            .enableApi(-1, -1, 0, FluidStorage.SIDED)
            .enableApi(-1, 0, 0, FluidStorage.SIDED)
            .enableApi(-1, 1, 0, FluidStorage.SIDED)
            .enableApi(0, -1, 0, FluidStorage.SIDED)
            .enableApi(0, 1, 0, FluidStorage.SIDED)
            .enableApi(1, -1, 0, FluidStorage.SIDED)
            .enableApi(1, 0, 0, FluidStorage.SIDED)
            .enableApi(1, 1, 0, FluidStorage.SIDED)
            ;

    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    private final PatternOffsetMap<Direction.Axis> map;

    public StomachBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings.factory(StomachBlockItem::new), settings, getMaxIndex(Y_PATTERN));

        map = new PatternOffsetMap<>(() -> new EnumMap<>(Direction.Axis.class));

        map.put(Direction.Axis.Y, Y_PATTERN);
        map.put(Direction.Axis.X, X_PATTERN);
        map.put(Direction.Axis.Z, Z_PATTERN);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        return this.defaultBlockState().setValue(AXIS, ctx.getClickedFace().getAxis());
    }

    @Override
    public BlockHitResult modifyHitPos(BlockHitResult hitResult)
    {
        // Move the hit so that the centre block will be in the air
        Direction side = hitResult.getDirection();
        Vec3 offsetPos = hitResult.getLocation().add(side.getStepX() * 2, side.getStepY() * 2, side.getStepZ() * 2);
        BlockPos offsetBlockPos = hitResult.getBlockPos().relative(side, 2);

        return new BlockHitResult(offsetPos, side, offsetBlockPos, hitResult.isInside());
    }

    @Override
    public BigBlockPattern getVolume(BlockState blockState)
    {
        return map.getPattern(blockState.getValue(AXIS));
    }

    @Override
    public Int2ObjectMap<Vec3i> getIndexToOffset(BlockState state)
    {
        return map.getOffsets(state.getValue(AXIS));
    }

    @Override
    protected BigBlock2Structure makeStructure(MeatlibBlockSettings settings)
    {
        return new StomachStructureBlock(this, settings);
    }

    @Override
    protected BlockState getDefaultStructureState(BlockState parentState)
    {
        return structure.defaultBlockState().setValue(AXIS, parentState.getValue(AXIS));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return LivingMachines.STOMACH_BE.create(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(AXIS);
    }

    @Override
    public @Nullable <T, C> T getApi(Level world, BlockPos pos, BlockState state, Vec3i offset, BlockApiLookup<T, C> api)
    {
        if (api == FluidStorage.SIDED
                && apiEnabled(state, offset, api)
                && world.getBlockEntity(pos) instanceof StomachBlockEntity be)
        {
            return (T) be.getStorage(null);
        }
        return null;
    }

    public static class StomachStructureBlock extends BigBlock2Structure implements LivingMachineStructure
    {
        public StomachStructureBlock(BigBlock2 parent, Properties settings)
        {
            super(parent, settings);
        }

        @Override
        public EnumMap<StructureProperty, PropertyValue> getProperties()
        {
            return StructureProperty.EMPTY;
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
        {
            super.createBlockStateDefinition(builder);
            builder.add(AXIS);
        }

        @Override
        public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
        {
            if (ItemUtil.playerHoldingPipe(player, hand))
                return InteractionResult.PASS;

            if (!world.isClientSide())
            {
                if (world.getBlockEntity(getOrigin(pos, state)) instanceof StomachBlockEntity be)
                {
                    TankBlockEntity.showContents((ServerPlayer) player, world, pos, be.getStorage(null));
                }
            }

            return InteractionResult.SUCCESS;
        }

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
        {
            return super.getShape(state, world, pos, context);
        }

        @Override
        public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
        {
            return Shapes.block();
        }
    }
}
