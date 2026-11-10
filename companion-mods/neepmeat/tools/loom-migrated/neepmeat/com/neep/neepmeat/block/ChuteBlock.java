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

import com.neep.meatlib.block.BaseHorFacingBlock;
import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.api.storage.DynamicLazyBlockApiCache;
import com.neep.neepmeat.api.storage.LazyBlockApiCache;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.meatlib.util.MiscUtil;
import com.neep.neepmeat.init.NMSounds;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ChuteBlock extends BaseHorFacingBlock implements EntityBlock
{
    public static final BooleanProperty OPEN = BooleanProperty.create("open");

    private final VoxelShape NORTH_CLOSED = Block.box(1, 1, 0, 15, 15, 2);
    private final VoxelShape NORTH_OPEN = Block.box(1, 1, 0, 15, 15, 7);

    private final Map<Direction, VoxelShape> closedShapeMap = Map.of(
            Direction.NORTH, MiscUtil.rotateShapeY(NORTH_CLOSED, 0),
            Direction.EAST, MiscUtil.rotateShapeY(NORTH_CLOSED, 90),
            Direction.SOUTH, MiscUtil.rotateShapeY(NORTH_CLOSED, 180),
            Direction.WEST, MiscUtil.rotateShapeY(NORTH_CLOSED, 270)
    );

    private final Map<Direction, VoxelShape> openShapeMap = Map.of(
            Direction.NORTH, MiscUtil.rotateShapeY(NORTH_OPEN, 0),
            Direction.EAST, MiscUtil.rotateShapeY(NORTH_OPEN, 90),
            Direction.SOUTH, MiscUtil.rotateShapeY(NORTH_OPEN, 180),
            Direction.WEST, MiscUtil.rotateShapeY(NORTH_OPEN, 270)
    );

    public ChuteBlock(RegistrationContext itemName, ItemSettings itemSettings, Properties settings)
    {
        super(itemName, itemSettings, settings);
        registerDefaultState(defaultBlockState().setValue(OPEN, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        if (state.getValue(OPEN))
        {
            return openShapeMap.get(state.getValue(FACING));
        }
        else
        {
            return closedShapeMap.get(state.getValue(FACING));
        }
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity)
    {
        if (!world.isClientSide() && state.getValue(OPEN) && world.getGameTime() % 5 == 0 && !entity.isRemoved())
        {
            if (world.getBlockEntity(pos) instanceof ChuteBlockEntity be && entity instanceof ItemEntity itemEntity)
            {
                be.eat(itemEntity);
            }
        }
        super.entityInside(state, world, pos, entity);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        Direction side = context.getClickedFace().getOpposite();
        if (side.getAxis().isVertical())
        {
            BlockPos.MutableBlockPos mutable = context.getClickedPos().mutable();
            for (Direction direction : Direction.values())
            {
                if (direction.getAxis().isVertical())
                    continue;

                mutable.setWithOffset(context.getClickedPos(), direction);
                if (!context.getLevel().getBlockState(mutable).isAir())
                {
                    return defaultBlockState().setValue(FACING, direction);
                }
            }
            return defaultBlockState();
        }
        return defaultBlockState().setValue(FACING, side);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
    {
        return canExist(world, pos, state) && super.canSurvive(state, world, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
    {
        super.neighborChanged(state, world, pos, sourceBlock, sourcePos, notify);
        if (!canExist(world, pos, state))
        {
            world.destroyBlock(pos, true);
        }
    }

    public static boolean canExist(LevelReader world, BlockPos pos, BlockState state)
    {
        BlockPos backPos = pos.relative(state.getValue(FACING));
        return !world.getBlockState(backPos).isAir();
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        boolean open = state.getValue(OPEN);
        if (player.isShiftKeyDown() && open && world.getBlockEntity(pos) instanceof ChuteBlockEntity be)
        {
            if (!world.isClientSide())
                be.extract(player);
            return InteractionResult.SUCCESS;
        }
        else
        {
            world.playSound(null, pos, open ? SoundEvents.IRON_TRAPDOOR_CLOSE : SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1, 1);
            world.setBlockAndUpdate(pos, state.cycle(OPEN));
            return InteractionResult.SUCCESS;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(OPEN);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.CHUTE.create(pos, state);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter world, BlockPos pos, PathComputationType type)
    {
        return false;
    }

    public static class ChuteBlockEntity extends SyncableBlockEntity
    {
        private final LazyBlockApiCache<Storage<ItemVariant>, Direction> cache = addListener(DynamicLazyBlockApiCache.facing(
                ItemStorage.SIDED, this, () -> getBlockState().getValue(FACING)));

        public ChuteBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
        {
            super(type, pos, state);
        }

        public void extract(Player player)
        {
            Storage<ItemVariant> storage = cache.find();

            if (storage == null)
                return;


            try (Transaction transaction = Transaction.openOuter())
            {
                var resource = StorageUtil.findExtractableResource(storage, transaction);
                if (resource != null)
                {
                    long toExtract = Math.min(resource.getItem().getMaxStackSize(), 64);
                    long extracted = storage.extract(resource, toExtract, transaction);
                    if (extracted > 0)
                    {
                        ItemStack stack = resource.toStack((int) extracted);
                        player.addItem(stack);
                        level.playSound(null, player, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1, 1);
                        transaction.commit();
                    }
                }
            }
        }

        public void eat(ItemEntity itemEntity)
        {
            Storage<ItemVariant> storage = cache.find();

            if (storage == null)
                return;

            try (Transaction transaction = Transaction.openOuter())
            {
                ItemStack stack = itemEntity.getItem();
                long inserted = storage.insert(ItemVariant.of(stack), stack.getCount(), transaction);
                stack.shrink((int) inserted);
                if (stack.isEmpty())
                {
                    itemEntity.remove(Entity.RemovalReason.DISCARDED);
                    transaction.commit();
                    return;
                }
                itemEntity.setItem(stack);
                transaction.commit();
            }
        }
    }
}
