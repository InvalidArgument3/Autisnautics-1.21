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

package com.neep.neepmeat.machine.live_machine.block.entity;

import com.neep.neepmeat.api.ContaminationUtil;
import com.neep.neepmeat.api.live_machine.LivingMachineBlockEntity;
import com.neep.neepmeat.screen_handler.LivingMachineScreenHandler;
import com.neep.neepmeat.util.IterateRandomly;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class LivingMachineControllerBlockEntity extends LivingMachineBlockEntity implements ExtendedScreenHandlerFactory
{
    public LivingMachineControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    protected void openBlocks()
    {
        float p = 0.4f * getDegradationManager().getDegradation() + 0.002f;

        int toOpen = getRandom().nextFloat() < p ? 1 : 0;
        int toContaminate = getRandom().nextFloat() > 0.1 ? 1 : 0;

        if (toOpen > 0)
        {
            int j = 0;
            for (int i : IterateRandomly.of(damageableStructures.size()))
            {
                var entry = damageableStructures.get(i);
                BlockState state = level.getBlockState(entry.pos());

                if (entry.structure().makeOpen(level, entry.pos(), state, this))
                    j++;


                if (j >= toOpen)
                    break;
            }
        }

        if (toContaminate > 0)
        {
            int j = 0;

            for (int i : IterateRandomly.of(structures.size()))
            {
                var entry = structures.get(i);

                // Try to make the ground manky beneath it
                if (ContaminationUtil.contaminate(level, entry.pos().below(), null, 4, 2))
                    j++;

                if (j >= toContaminate)
                    break;
            }
        }
    }

    public void serverTick()
    {
        super.serverTick();

        if (age % updateInterval == 0)
        {
            openBlocks();

        }
    }

    @Override
    public Component getDisplayName()
    {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player)
    {
        return new LivingMachineScreenHandler(playerInventory, syncId, this);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf)
    {
        buf.writeBlockPos(worldPosition);
    }

}
