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
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import java.util.function.Supplier;

public class FluidPortBlock<T extends BlockEntity> extends PortBlock<T>
{
    public FluidPortBlock(RegistrationContext ctx, ItemSettings itemSettings, Supplier<BlockEntityType<T>> factory, Properties settings)
    {
        super(ctx, itemSettings.factory(ItemPortBlockItem.forList(LivingMachines.ITEM_PORTS)), factory, settings);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (ItemUtil.playerHoldingPipe(player, hand))
            return InteractionResult.PASS;

        // So many unchecked casts, but I don't care anymore.
        if (world.getBlockEntity(pos) instanceof StorageProvider<?> provider)
        {
            Storage<FluidVariant> storage = ((StorageProvider<FluidVariant>) provider).getStorage(null);

            if (storage instanceof StorageView<?> view)
            {
                if (WritableSingleFluidStorage.handleInteract(storage, world, player, hand))
                {
                    return InteractionResult.SUCCESS;
                }
                else if (!world.isClientSide())
                {
                    TankBlockEntity.showContents((ServerPlayer) player, world, pos, (StorageView<FluidVariant>) view);
                    return InteractionResult.SUCCESS;
                }

            }
        }
        return super.use(state, world, pos, player, hand, hit);
    }

//    public enum Mode
//    {
//        INPUT,
//        OUTPUT,
//    }
}
