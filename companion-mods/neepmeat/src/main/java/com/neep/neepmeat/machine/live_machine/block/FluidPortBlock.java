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

import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.live_machine.block.entity.StorageProvider;
import com.neep.neepmeat.transport.block.fluid_transport.machine.TankBlockEntity;
import com.neep.neepmeat.util.ItemUtil;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageView;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.ItemInteractionResult;
import java.util.function.BiFunction;

public class FluidPortBlock<T extends BlockEntity> extends PortBlock<T>
{
    public FluidPortBlock(RegistrationContext ctx, ItemSettings itemSettings, BiFunction<BlockPos, BlockState, T> factory, Properties settings)
    {
        super(ctx, itemSettings.factory(ItemPortBlockItem.forList(LivingMachines.ITEM_PORTS)), factory, settings);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (ItemUtil.playerHoldingPipe(player, hand))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (world.getBlockEntity(pos) instanceof StorageProvider<?> provider)
        {
            Storage<FluidVariant> storage = ((StorageProvider<FluidVariant>) provider).getStorage(null);
            if (WritableSingleFluidStorage.handleInteract(storage, world, player, hand))
            {
                return ItemInteractionResult.sidedSuccess(world.isClientSide);
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (world.getBlockEntity(pos) instanceof StorageProvider<?> provider)
        {
            Storage<FluidVariant> storage = ((StorageProvider<FluidVariant>) provider).getStorage(null);
            if (storage instanceof StorageView<?> view && !world.isClientSide() && player instanceof ServerPlayer serverPlayer)
            {
                TankBlockEntity.showContents(serverPlayer, world, pos, (StorageView<FluidVariant>) view);
                return InteractionResult.SUCCESS;
            }
        }
        return super.useWithoutItem(state, world, pos, player, hit);
    }

//    public enum Mode
//    {
//        INPUT,
//        OUTPUT,
//    }
}
