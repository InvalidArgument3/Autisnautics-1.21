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

package com.neep.neepmeat.machine.fluid_exciter;

import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.block.multi.TallBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.item.FluidComponentItem;
import com.neep.neepmeat.transport.api.pipe.VascularConduit;
import com.neep.neepmeat.transport.api.pipe.VascularConduitEntity;
import com.neep.neepmeat.transport.block.energy_transport.VascularConduitBlock;
import com.neep.neepmeat.util.ItemUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class FluidExciterBlock extends TallBlock implements EntityBlock
{
    public FluidExciterBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings.factory(FluidComponentItem::new), settings.noOcclusion());
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
        world.getBlockEntity(pos, NMBlockEntities.FLUID_EXCITER).ifPresent(FluidExciterBlockEntity::updateCache);
        super.neighborChanged(state, world, pos, block, fromPos, notify);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (ItemUtil.playerHoldingPipe(player, hand))
            return InteractionResult.PASS;

        if (!world.isClientSide() && world.getBlockEntity(pos) instanceof FluidExciterBlockEntity be)
        {
            player.displayClientMessage(PowerUtil.perUnitToLabelText((double) be.getOutput() / PowerUtil.BASE_POWER), true);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected Structure createStructure(RegistrationContext ctx)
    {
        return ctx.append(this, new FluidExciterStructure(ctx, MeatlibBlockSettings.copyOf(this.properties)), s -> s + "_structure");
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.FLUID_EXCITER.create(pos, state);
    }

    @Nullable
    public static VascularConduitEntity getConduitFromTop(Level world, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, Void unused)
    {
        if (world.getBlockEntity(pos.below()) instanceof FluidExciterBlockEntity be)
            return be.getConduitEntity(unused);

        return null;
    }

    private class FluidExciterStructure extends Structure implements VascularConduit
    {
        public FluidExciterStructure(RegistrationContext ctx, Properties settings)
        {
            super(settings);
        }

        @Override
        public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
        {
            super.setPlacedBy(world, pos, state, placer, itemStack);
        }

        @Override
        public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify)
        {
            super.onPlace(state, world, pos, oldState, notify);
            updatePosition(world, pos, state, VascularConduitEntity.UpdateReason.ADDED);
        }

        @Override
        protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
        {
            if (VascularConduit.find(world, sourcePos, world.getBlockState(sourcePos)) == null && !sourceBlock.equals(this))
            {
                updatePosition(world, pos, state, VascularConduitEntity.UpdateReason.CHANGED);
            }
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
        {
        InteractionHand hand = InteractionHand.MAIN_HAND;
            if (!VascularConduitBlock.matches(player.getItemInHand(hand)))
            {
                if (!world.isClientSide() && world.getBlockEntity(pos.below()) instanceof FluidExciterBlockEntity be)
                {
                    player.displayClientMessage(PowerUtil.perUnitToLabelText((double) be.getOutput() / PowerUtil.BASE_POWER), true);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.SUCCESS;
            }
            return super.useWithoutItem(state, world, pos, player, hit);
        }

        @Override
        public void updatePosition(Level world, BlockPos pos, BlockState state, VascularConduitEntity.UpdateReason reason)
        {
            VascularConduit.super.updatePosition(world, pos, state, reason);
        }

        @Override
        public boolean isConnectedIn(BlockGetter world, BlockPos pos, BlockState state, Direction direction)
        {
            return true;
        }

        @Override
        public VascularConduitEntity getEntity(Level world, BlockPos pos, BlockState state)
        {
            if (world.getBlockEntity(pos.below()) instanceof FluidExciterBlockEntity be)
            {
                return be.getConduitEntity(null);
            }
            throw new IllegalStateException();
        }
    }
}
