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

package com.neep.neepmeat.machine.grinder;

import com.neep.meatlib.block.BaseHorFacingBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.machine.content_detector.InventoryDetectorBlock;
import com.neep.neepmeat.transport.api.pipe.ItemPipeConnectable;
import com.neep.neepmeat.util.ItemUtil;
import com.neep.meatlib.util.MiscUtil;
import com.neep.meatlib.util.Wrenchable;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CrusherBlock extends BaseHorFacingBlock implements EntityBlock, Wrenchable, ItemPipeConnectable
{
    public CrusherBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings.noOcclusion().isRedstoneConductor(InventoryDetectorBlock::never));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (world.getBlockEntity(pos) instanceof CrusherBlockEntity be)
        {
            if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty())
            {
                if (world.isClientSide()) return InteractionResult.SUCCESS;

                be.ejectXP();
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.sidedSuccess(ItemUtil.singleVariantInteract(player, hand, be.getStorage().getInputStorage()));
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new CrusherBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NMBlockEntities.CRUSHER, null, (world1, pos, state1, blockEntity) -> blockEntity.clientTick(), world);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!state.is(newState.getBlock()) && !world.isClientSide)
        {
            if (world.getBlockEntity(pos) instanceof CrusherBlockEntity be)
            {
                be.storage.dropItems(world, pos);
            }
        }
        super.onRemove(state, world, pos, newState, moved);
    }



    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity)
    {
        super.entityInside(state, world, pos, entity);
        if (world.getBlockEntity(pos) instanceof CrusherBlockEntity be && !world.isClientSide() && entity instanceof ItemEntity item &&  entity.onGround())
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                ItemVariant variant = ItemVariant.of(item.getItem());
                long inserted = be.getStorage().getInputStorage().insert(variant, item.getItem().getCount(), transaction);
                item.getItem().shrink((int) inserted);
                transaction.commit();
            }
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state)
    {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos)
    {
        if (world.getBlockEntity(pos) instanceof CrusherBlockEntity be)
        {
            var storage = be.storage.inputStorage;
            return (int) Math.ceil(15f * ((double) storage.amount / storage.getCapacity()));
        }
        return super.getAnalogOutputSignal(state, world, pos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return CrusherBlock.box(0,0,0,16,15,16);

    }
}
