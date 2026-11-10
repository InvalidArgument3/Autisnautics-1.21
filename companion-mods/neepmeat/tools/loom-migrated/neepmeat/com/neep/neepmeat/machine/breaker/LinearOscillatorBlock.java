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

package com.neep.neepmeat.machine.breaker;

import com.google.common.collect.ImmutableMap;
import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.transport.api.pipe.ItemPipe;
import com.neep.neepmeat.transport.api.pipe.ItemPipeConnectable;
import com.neep.neepmeat.transport.item_network.ItemInPipe;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class LinearOscillatorBlock extends BaseFacingBlock implements EntityBlock, ItemPipe
{
    public static final VoxelShape NORTH_SHAPE = Block.box(0, 0, 5, 16, 16 ,16);
    public static final VoxelShape EAST_SHAPE = Block.box(0, 0, 0, 11, 16 ,16);
    public static final VoxelShape SOUTH_SHAPE = Block.box(0, 0, 0, 16, 16 ,11);
    public static final VoxelShape WEST_SHAPE = Block.box(5, 0, 0, 16, 16 ,16);
    public static final VoxelShape UP_SHAPE = Block.box(0, 0, 0, 16, 11 ,16);
    public static final VoxelShape DOWN_SHAPE = Block.box(0, 5, 0, 16, 16 ,16);

    public static final Map<Direction, VoxelShape> SHAPES = (new ImmutableMap.Builder<Direction, VoxelShape>())
            .put(Direction.NORTH, NORTH_SHAPE)
            .put(Direction.EAST, EAST_SHAPE)
            .put(Direction.SOUTH, SOUTH_SHAPE)
            .put(Direction.WEST, WEST_SHAPE)
            .put(Direction.UP, UP_SHAPE)
            .put(Direction.DOWN, DOWN_SHAPE)
            .build();

    public LinearOscillatorBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings.noOcclusion());
//        List<VoxelShape> shapes = List.of(NORTH_SHAPE, EAST_SHAPE, SOUTH_SHAPE, WEST_SHAPE, UP_SHAPE, DOWN_SHAPE);
//        SHAPES = Arrays.stream(Direction.values()).map((direction -> shapes.get(direction.getId()))).;
    }

    public VoxelShape getShapeForState(BlockState state)
    {
        return SHAPES.get(state.getValue(FACING));
    }

    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return getShapeForState(state);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (world.getBlockEntity(pos) instanceof LinearOscillatorBlockEntity be && be.onUse(player, hand))
        {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
        if (world.getBlockEntity(pos) instanceof LinearOscillatorBlockEntity be && !world.isClientSide())
        {
//            be.update((ServerWorld) world, pos, fromPos, state);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new LinearOscillatorBlockEntity(pos, state);
    }

    @Override
    public long insert(Level world, BlockPos pos, BlockState state, Direction direction, ItemInPipe item, TransactionContext transaction)
    {
        return 0;
    }

    @Override
    public boolean isConnected(BlockGetter blockView, BlockPos pos, BlockState state, Direction face)
    {
        return face == state.getValue(FACING).getOpposite();
    }

    @Override
    public boolean canConnect(BlockGetter world, BlockPos pos, BlockState state, Direction face)
    {
        return face == state.getValue(FACING).getOpposite();
    }

    //    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
//    {
//        return MiscUitls.checkType(type, NMBlockEntities.LINEAR_OSCILLATOR, LinearOscillatorBlockEntity::serverTick, world);
//    }
}
