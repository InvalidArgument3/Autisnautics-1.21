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

package com.neep.meatlib.mixin;

import com.mojang.authlib.GameProfile;
import com.neep.meatlib.screen.SimpleScreenHandlerPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.OptionalInt;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin extends Player implements SimpleScreenHandlerPlayer
{
    @Shadow protected abstract void incrementScreenHandlerSyncId();

    @Shadow private int screenHandlerSyncId;

    @Shadow public ServerGamePacketListenerImpl networkHandler;

    @Shadow protected abstract void onScreenHandlerOpened(AbstractContainerMenu screenHandler);

    public ServerPlayerEntityMixin(Level world, BlockPos pos, float yaw, GameProfile gameProfile)
    {
        super(world, pos, yaw, gameProfile);
    }

    @Override
    public OptionalInt meatlib$openHandledScreen(@Nullable MenuProvider factory)
    {
        if (factory == null)
        {
            return OptionalInt.empty();
        }
        else
        {
            if (this.containerMenu != this.inventoryMenu)
            {
                this.closeContainer();
            }

           incrementScreenHandlerSyncId();
            AbstractContainerMenu screenHandler = factory.createMenu(screenHandlerSyncId, this.getInventory(), this);
            if (screenHandler == null)
            {
                if (this.isSpectator())
                {
                    this.displayClientMessage(Component.translatable("container.spectatorCantOpen").withStyle(ChatFormatting.RED), true);
                }

                return OptionalInt.empty();
            }
            else
            {
                networkHandler.send(new ClientboundOpenScreenPacket(screenHandler.containerId, screenHandler.getType(), factory.getDisplayName()));
//                onScreenHandlerOpened(screenHandler);
                this.containerMenu = screenHandler;
                return OptionalInt.of(this.screenHandlerSyncId);
            }
        }
    }
}
