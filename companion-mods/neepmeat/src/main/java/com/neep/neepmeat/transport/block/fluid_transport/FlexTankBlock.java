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

package com.neep.neepmeat.transport.block.fluid_transport;

import com.neep.meatlib.api.block.NormalPlacementSoundBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.network.TankMessagePacket;
import com.neep.neepmeat.transport.block.FlexStorageBlock;
import com.neep.neepmeat.transport.block.fluid_transport.entity.FlexTankBlockEntity;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.StorageView;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import java.util.function.Supplier;

public class FlexTankBlock extends FlexStorageBlock<FlexTankBlockEntity> implements NormalPlacementSoundBlock
{
    public final long capacity;

    public FlexTankBlock(RegistrationContext ctx, long capacity, Supplier<BlockEntityType<FlexTankBlockEntity>> type, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, type, itemSettings, settings);
        this.capacity = capacity;
    }


    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (player.getItemInHand(hand).isEmpty())
        {
            if (!world.isClientSide() && world.getBlockEntity(pos) instanceof FlexTankBlockEntity be)
            {
                StorageView<FluidVariant> view = be.getStorage(null).iterator().next();

                world.playSound(null, pos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1f, 1.5f);
                TankMessagePacket.send((ServerPlayer) player, pos, view.getAmount(), view.getResource());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
