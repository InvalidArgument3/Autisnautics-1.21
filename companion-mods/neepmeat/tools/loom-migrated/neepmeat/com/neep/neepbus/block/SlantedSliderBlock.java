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

package com.neep.neepbus.block;

import com.neep.meatlib.block.BaseHorFacingBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.screen.SimpleScreenHandlerFactory;
import com.neep.neepbus.NeepBus;
import com.neep.neepbus.block.entity.SliderBlockEntity;
import com.neep.neepbus.screen.NeepBusConfigScreenHandler;
import com.neep.neepbus.screen.SliderScreenHandler;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import com.neep.neepmeat.transport.api.pipe.SimplePipeBlock;
import com.neep.neepmeat.transport.fluid_network.ConnectionType;
import com.neep.neepmeat.transport.fluid_network.SimplePipeProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SlantedSliderBlock extends BaseHorFacingBlock implements EntityBlock, DataCable, NeepBusProvider
{
    public static final EnumProperty<ConnectionType> NORTH_CONNECTION = SimplePipeProperties.NORTH_CONNECTION;
    public static final EnumProperty<ConnectionType> EAST_CONNECTION = SimplePipeProperties.EAST_CONNECTION;
    public static final EnumProperty<ConnectionType> SOUTH_CONNECTION = SimplePipeProperties.SOUTH_CONNECTION;
    public static final EnumProperty<ConnectionType> WEST_CONNECTION = SimplePipeProperties.WEST_CONNECTION;

    public static final EnumProperty<Attachment> FACE = EnumProperty.create("face", Attachment.class);

    private static final VoxelShape NS_SHAPE = Block.box(4, 0, 1, 12, 16, 15);
    private static final VoxelShape EW_SHAPE = Block.box(1, 0, 4, 15, 16, 12);

    public SlantedSliderBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
        registerDefaultState(getStateDefinition().any()
                .setValue(NORTH_CONNECTION, ConnectionType.NONE)
                .setValue(EAST_CONNECTION, ConnectionType.NONE)
                .setValue(SOUTH_CONNECTION, ConnectionType.NONE)
                .setValue(WEST_CONNECTION, ConnectionType.NONE));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        BlockState state = super.getStateForPlacement(context);
        if (state == null)
            return null;

        if (context.getClickedFace().getAxis().isVertical())
        {
            state = state.setValue(FACE, context.getClickedFace() == Direction.DOWN ? Attachment.CEILING : Attachment.FLOOR);
        }
        else
        {
            double y = context.getClickLocation().y;
            boolean floor = y - Math.floor(y) < 0.5;
            state = state.setValue(FACE, floor ? Attachment.FLOOR : Attachment.CEILING);
        }

        return withConnections(context.getLevel(), context.getClickedPos(), state);
    }

    private boolean canConnectTo(BlockState toState, Direction toFace, Level world, BlockPos toPos)
    {
        return toState.getBlock() instanceof DataCable dataCable && dataCable.connectInDirection(world, toPos, toState, toFace.getOpposite());
    }

    private BlockState withConnections(Level world, BlockPos pos, BlockState state)
    {
        for (Direction direction : Direction.values())
        {
            if (direction.getAxis().isVertical() || state.getValue(FACING).getOpposite() == direction)
                continue;

            BlockPos toPos = pos.relative(direction);
            if (canConnectTo(world.getBlockState(toPos), direction, world, toPos))
            {
                state = state.setValue(SimplePipeBlock.DIR_TO_CONNECTION.get(direction), ConnectionType.SIDE);
            }
        }

        return state;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos)
    {
        if (direction.getAxis().isVertical())
            return state;

        boolean connect = false;

        if (direction != state.getValue(FACING).getOpposite() && world.getBlockState(neighborPos).getBlock() instanceof DataCable dataCable)
        {
            connect = dataCable.connectInDirection(world, neighborPos, neighborState, direction.getOpposite());
        }

        return state.setValue(SimplePipeBlock.DIR_TO_CONNECTION.get(direction), connect ? ConnectionType.SIDE : ConnectionType.NONE);
    }

    @Override
    public boolean connectInDirection(BlockGetter world, BlockPos pos, BlockState state, Direction direction)
    {
        return direction != state.getValue(FACING).getOpposite();
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (world.getBlockEntity(pos) instanceof SliderBlockEntity be)
        {
            if (!player.isShiftKeyDown())
            {
                player.openMenu((SimpleScreenHandlerFactory) (syncId, playerInventory, player1) -> new SliderScreenHandler(syncId, playerInventory, be));
            }
            else
            {
                player.openMenu(NeepBusConfigScreenHandler.getFactory(be.getConfig()));
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        Direction facing = state.getValue(FACING);
        if (facing == Direction.EAST || facing == Direction.WEST)
        {
            return EW_SHAPE;
        }
        else
        {
            return NS_SHAPE;
        }

    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(FACE);
        builder.add(
                NORTH_CONNECTION,
                EAST_CONNECTION,
                SOUTH_CONNECTION,
                WEST_CONNECTION
        );
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NeepBus.LINEAR_LEVER_BE.create(pos, state);
    }

    @Override
    public void networkChanged(Level world, BlockPos pos, BlockPos whereChanged)
    {
        if (world.getBlockEntity(pos) instanceof SliderBlockEntity be)
        {
            be.invalidate();
        }
    }

    public enum Attachment implements StringRepresentable
    {

        FLOOR("floor"),
        CEILING("ceiling");

        private final String name;

        Attachment(String name)
        {
            this.name = name;
        }

        @Override
        public String getSerializedName()
        {
            return name;
        }
    }
}
