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

import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.transport.block.EncasedBlock;
import com.neep.neepmeat.transport.block.item_transport.entity.EncasedItemPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.neep.meatlib.util.MiscUtil;
import org.jetbrains.annotations.Nullable;

public class EncasedItemPipeBlock extends ItemPipeBlock implements EncasedBlock
{
    public EncasedItemPipeBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings.noOcclusion().isRedstoneConductor(EncasedBlock.solidBlock()));
    }

    public VoxelShape getPipeOutlineShape(BlockState state, BlockGetter world, BlockPos pos)
    {
        return super.getShape(state, world, pos, CollisionContext.empty());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext context)
    {
        if (view.getBlockEntity(pos) instanceof EncasedItemPipeBlockEntity be)
        {
            return be.getCamoShape();
        }
        return super.getShape(state, view, pos, context);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
    {
        super.neighborChanged(state, world, pos, sourceBlock, sourcePos, notify);
        if (world.getBlockEntity(pos) instanceof EncasedItemPipeBlockEntity be)
        {
            be.setBlockState(state);
            be.onNeighbourUpdate();
        }
    }

    @Override
    public void onConnectionUpdate(Level world, BlockState state, BlockState newState, BlockPos pos, Player entity)
    {
        super.onConnectionUpdate(world, state, newState, pos, entity);
        if (world.getBlockEntity(pos) instanceof EncasedItemPipeBlockEntity be)
        {
            be.onNeighbourUpdate();
        }
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NMBlockEntities.ENCASED_PNEUMATIC_PIPE, (world1, pos, state1, blockEntity) -> blockEntity.serverTick(world1, pos, state1), null, world);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (replaceUse(state, world, pos, player, hand, hit))
        {
            return InteractionResult.SUCCESS;
        }
        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.ENCASED_PNEUMATIC_PIPE.create(pos, state);
    }

    @Override
    public boolean canReplace(ItemStack stack, BlockItem blockItem)
    {
        return !(blockItem.getBlock() instanceof EncasedBlock) && !ItemPipeBlock.matches(stack);
    }
}
