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
import com.neep.meatlib.screen.SimpleScreenHandlerFactory;
import com.neep.neepbus.block.NeepBusProvider;
import com.neep.neepbus.util.NeepBusConfig;
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.api.big_block_2.BigBlock2;
import com.neep.neepmeat.api.big_block_2.BigBlock2Structure;
import com.neep.neepmeat.api.live_machine.LivingMachineStructure;
import com.neep.neepmeat.api.live_machine.PropertyValue;
import com.neep.neepmeat.api.live_machine.StructureProperty;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.live_machine.block.entity.DistributorPointBlockEntity;
import com.neep.neepmeat.screen_handler.DistributorPointScreenHandler;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import com.neep.neepmeat.transport.fluid_network.ConnectionType;
import com.neep.neepmeat.transport.fluid_network.SimplePipeProperties;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.Util;
import net.minecraft.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class DistributorPointBlock extends BigBlock2 implements EntityBlock, NeepBusProvider
{
    public static final BigBlockPattern PATTERN = BigBlockPattern.makeOddCylinder(1, 0, 0, Blocks.AIR.defaultBlockState());
    public static final DirectionProperty VERTICAL_DIRECTION = BlockStateProperties.VERTICAL_DIRECTION;

    private final Int2ObjectMap<Vec3i> encoded;

    public DistributorPointBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings, 8);

        encoded = PATTERN.encode();
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
    {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        if (!world.isClientSide() && world.getBlockEntity(pos) instanceof DistributorPointBlockEntity be)
        {
            be.onPlaced();
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (!world.isClientSide() && world.getBlockEntity(pos) instanceof DistributorPointBlockEntity be)
        {
            player.openMenu((SimpleScreenHandlerFactory) (syncId, playerInventory, player1) ->
                    new DistributorPointScreenHandler(playerInventory, syncId, be));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!world.isClientSide() && !newState.is(this) && world.getBlockEntity(pos) instanceof DistributorPointBlockEntity be)
        {
            be.onBroken();
        }

        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return state.getValue(VERTICAL_DIRECTION) == Direction.UP ?
                Shapes.join(Block.box(-16, 0, -16, 32, 8, 32),
                Block.box(-8, 8, -8, 24, 16, 24), BooleanOp.OR)
                :
                Shapes.join(Block.box(-16, 8, -16, 32, 16, 32),
                        Block.box(-8, 0, -8, 24, 8, 24), BooleanOp.OR)
                ;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        Direction facing = ctx.getNearestLookingVerticalDirection();
        return defaultBlockState().setValue(VERTICAL_DIRECTION, ctx.getPlayer() != null && ctx.getPlayer().isShiftKeyDown() ? facing : facing.getOpposite());
    }

    @Override
    public BigBlockPattern getVolume(BlockState state)
    {
        return PATTERN;
    }

    @Override
    public Int2ObjectMap<Vec3i> getIndexToOffset(BlockState state)
    {
        return encoded;
    }

    @Override
    protected BigBlock2Structure makeStructure(MeatlibBlockSettings settings)
    {
        return new Structure(this, settings);
    }

    @Override
    protected BlockState getDefaultStructureState(BlockState parentState)
    {
        return structure.defaultBlockState();
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return LivingMachines.DISTRIBUTOR_POINT_BE.create(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(VERTICAL_DIRECTION);
    }

    @Override
    public void networkChanged(Level world, BlockPos pos, BlockPos whereChanged)
    {
        if (world.getBlockEntity(pos) instanceof DistributorPointBlockEntity be)
        {
            be.invalidateNetwork();
        }
    }

    public static class Structure extends BigBlock2Structure implements LivingMachineStructure, NeepBusProvider, DataCable
    {
        public static final EnumProperty<ConnectionType> NORTH_CONNECTION = SimplePipeProperties.NORTH_CONNECTION;
        public static final EnumProperty<ConnectionType> EAST_CONNECTION = SimplePipeProperties.EAST_CONNECTION;
        public static final EnumProperty<ConnectionType> SOUTH_CONNECTION = SimplePipeProperties.SOUTH_CONNECTION;
        public static final EnumProperty<ConnectionType> WEST_CONNECTION = SimplePipeProperties.WEST_CONNECTION;

        public static final Map<Direction, EnumProperty<ConnectionType>> DIR_TO_CONNECTION = Util.make(new EnumMap<>(Direction.class), map ->
        {
            map.put(Direction.NORTH, NORTH_CONNECTION);
            map.put(Direction.EAST, EAST_CONNECTION);
            map.put(Direction.SOUTH, SOUTH_CONNECTION);
            map.put(Direction.WEST, WEST_CONNECTION);
        });

        public Structure(BigBlock2 parent, Properties settings)
        {
            super(parent, settings);
            this.registerDefaultState(this.stateDefinition.any()
                    .setValue(NORTH_CONNECTION, ConnectionType.NONE)
                    .setValue(EAST_CONNECTION, ConnectionType.NONE)
                    .setValue(SOUTH_CONNECTION, ConnectionType.NONE)
                    .setValue(WEST_CONNECTION, ConnectionType.NONE)
            );
        }

        @Override
        public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
        {
            var origin = getOrigin(pos, state);

            return parent.use(world.getBlockState(origin), world, origin, player, hand, hit);
        }

        @Override
        protected boolean genEmptyModel()
        {
            return false;
        }

        @Override
        public RenderShape getRenderShape(BlockState state)
        {
            return RenderShape.MODEL;
        }

        @Override
        public EnumMap<StructureProperty, PropertyValue> getProperties()
        {
            return StructureProperty.EMPTY;
        }

        @Override
        public @Nullable NeepBusConfig getConfig(Level world, BlockPos pos, BlockState state, @Nullable Direction side)
        {
            var offset = parent.getOffset(state);
            BlockPos originPos = pos.subtract(offset);
            var originState = world.getBlockState(originPos);

            if (originState.is(parent))
                return ((DistributorPointBlock) parent).getConfig(world, originPos, originState, null);

            return null;
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
        {
            super.createBlockStateDefinition(builder);
            builder.add(NORTH_CONNECTION, EAST_CONNECTION, SOUTH_CONNECTION, WEST_CONNECTION);
        }

        @Override
        public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos)
        {
            if (direction.getAxis().isVertical())
                return state;

            boolean connection = canConnectTo(neighborState, direction.getOpposite(), (Level) world, neighborPos);

            if (connection)
            {
                return state.setValue(DIR_TO_CONNECTION.get(direction), ConnectionType.SIDE);
            }

            return state.setValue(DIR_TO_CONNECTION.get(direction), ConnectionType.NONE);
        }

        public boolean canConnectTo(BlockState toState, Direction toFace, Level world, BlockPos toPos)
        {
            if (toState.getBlock() instanceof DataCable cable && !toState.is(this))
            {
                return cable.connectInDirection(world, toPos, toState, toFace);
            }
            return false;
        }

        @Override
        public void networkChanged(Level world, BlockPos pos, BlockPos whereChanged)
        {
            // No need for anything here yet as there is no sender
        }
    }
}
