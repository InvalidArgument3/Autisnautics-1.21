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

package com.neep.neepmeat.transport.block.item_transport;

import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.storage.MeatlibStorageUtil;
import com.neep.neepmeat.api.storage.WritableStackStorage;
import com.neep.neepmeat.block.entity.DisplayPlateBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class DisplayPlateBlock extends BaseBlock implements EntityBlock
{
    protected static final VoxelShape DEFAULT_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    protected static final VoxelShape TOP_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);

    public static final BooleanProperty TOP = BooleanProperty.create("top");

    public DisplayPlateBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
        registerDefaultState(defaultBlockState().setValue(TOP, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext context)
    {
        return state.getValue(TOP) ? TOP_SHAPE : DEFAULT_SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new DisplayPlateBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (player.isShiftKeyDown()
                && player.getMainHandItem().isEmpty()
                && world.getBlockEntity(pos) instanceof MenuProvider factory)
        {
            player.openMenu(factory);
            return InteractionResult.SUCCESS;
        }

        if (world.getBlockEntity(pos) instanceof DisplayPlateBlockEntity be && !world.isClientSide)
        {
            return InteractionResult.sidedSuccess(WritableStackStorage.handleInteract(player, hand, be.getStorage(null)));
//            ItemStack stack = player.getStackInHand(hand);
//            WritableStackStorage storage = be.getStorage(null);
//            if ((storage.isEmpty() || storage.getResource().matches(stack)) && !stack.isEmpty())
//            {
//                Transaction transaction = Transaction.openOuter();
//
//                long inserted = storage.insert(ItemVariant.of(stack), stack.getCount(), transaction);
//                stack.decrement((int) inserted);
//
//                transaction.commit();
//            }
//            else if ((stack.isEmpty() || !storage.getResource().matches(stack)) && !storage.isEmpty())
//            {
//                Transaction transaction = Transaction.openOuter();
//
//                player.giveItemStack(storage.getAsStack());
//                storage.extract(storage.getResource(), storage.getAmount(), transaction);
//
//                transaction.commit();
//            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity)
    {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof DisplayPlateBlockEntity be && !world.isClientSide())
        {
            onEntityCollided(world, pos, state, entity, be);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (state.getBlock() != newState.getBlock())
        {
            BlockEntity blockEntity = world.getBlockEntity(pos);

            if (blockEntity instanceof DisplayPlateBlockEntity be)
            {
                MeatlibStorageUtil.scatterNoTransaction(world, pos, be.getStorage(null));
                world.updateNeighbourForOutputSignal(pos,this);
            }
            super.onRemove(state, world, pos, newState, moved);
        }
    }


    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        BlockState state = super.getStateForPlacement(ctx);
        double remainder = ctx.getClickLocation().y - ctx.getClickedPos().getY();

        if (state == null)
            return null;

        return state.setValue(TOP, remainder > 0.5 || (ctx.getPlayer() != null && ctx.getPlayer().isShiftKeyDown()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(TOP);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state)
    {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos)
    {
        DisplayPlateBlockEntity be = (DisplayPlateBlockEntity) world.getBlockEntity(pos);
        int maxCount = be.getStorage(null).getResource().toStack().getMaxStackSize();
        return maxCount > 0 ? (int) Math.ceil((float) be.getStorage(null).getAmount() / (float) maxCount * 16) : 0;
    }

    // Add dropped items to inventory
    public static void onEntityCollided(Level world, BlockPos pos, BlockState state, Entity entity, DisplayPlateBlockEntity be)
    {
        if (!world.isClientSide && entity instanceof ItemEntity item)
        {
            be.extractFromItem(item);
        }
    }

}
