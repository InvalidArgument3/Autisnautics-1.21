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

package com.neep.neepmeat.machine.item_mincer;

import com.neep.meatlib.block.BaseHorFacingBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.machine.grinder.CrusherBlock;
import com.neep.neepmeat.util.ItemUtil;
import com.neep.meatlib.util.MiscUtil;
import com.neep.meatlib.util.Wrenchable;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
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

public class ItemMincerBlock extends BaseHorFacingBlock implements EntityBlock, Wrenchable
{
    public ItemMincerBlock(RegistrationContext itemName, ItemSettings itemSettings, Properties settings)
    {
        super(itemName, itemSettings, settings);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (ItemUtil.playerHoldingPipe(player, hand))
            return super.useWithoutItem(state, world, pos, player, hit);

        if (world.getBlockEntity(pos) instanceof ItemMincerBlockEntity be)
        {
        InteractionHand hand = InteractionHand.MAIN_HAND;
            return InteractionResult.sidedSuccess(ItemUtil.singleVariantInteract(player, hand, be.storage.inputStorage));
        }
        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (!newState.is(this) && world.getBlockEntity(pos) instanceof ItemMincerBlockEntity be)
        {
            Containers.dropItemStack(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, be.storage.inputStorage.getAsStack());
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity)
    {
        super.entityInside(state, world, pos, entity);
        if (world.getBlockEntity(pos) instanceof ItemMincerBlockEntity be && !world.isClientSide() && entity instanceof ItemEntity item &&  entity.onGround())
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                ItemVariant variant = ItemVariant.of(item.getItem());
                long inserted = be.storage.inputStorage.insert(variant, item.getItem().getCount(), transaction);
                item.getItem().shrink((int) inserted);
                transaction.commit();
            }
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NMBlockEntities.ITEM_MINCER, null, (world1, pos, state1, blockEntity) -> blockEntity.clientTick(), world);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.ITEM_MINCER.create(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return CrusherBlock.box(0,0,0,16,15,16);

    }
}
