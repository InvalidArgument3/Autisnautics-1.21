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

package com.neep.neepmeat.screen_handler;

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.ParamCodecs;
import com.neep.meatlib.network.ChannelManager;
import com.neep.meatlib.network.PacketBufUtil;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.implant.config.ImplantConfigHandler;
import com.neep.neepmeat.implant.entity.EntityImplant;
import com.neep.neepmeat.implant.entity.ImplantManager;
import com.neep.neepmeat.init.NMScreenHandlers;
import com.neep.neepmeat.machine.upgrade_manager.ImplantManagerBlockEntity;
import com.neep.neepmeat.machine.upgrade_manager.ImplantManagerDelegate;
import com.neep.neepmeat.plc.component.MutateInPlace;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public class ImplantManagerScreenHandler extends BasicScreenHandler
{
    private final ImplantManagerDelegate manager;

    public final ChannelManager<RemoveImplant> removeImplantC2S;
    public final ChannelManager<SelectImplant> selectImplantC2S;

    @Nullable private ImplantConfigHandler configHandler;
    private boolean shouldVerifyMip;

    public static ExtendedScreenHandlerFactory getFactory(ImplantManagerDelegate delegate, BlockPos pos, boolean verify)
    {
        return new ExtendedScreenHandlerFactory()
        {
            @Override
            public void writeScreenOpeningData(ServerPlayer serverPlayerEntity, FriendlyByteBuf buf)
            {
                PacketBufUtil.writeBlockPos(buf, pos);
                buf.writeBoolean(verify);
            }

            @Override
            public Component getDisplayName()
            {
                return Component.empty();
            }

            @Override
            public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player)
            {
                return new ImplantManagerScreenHandler(syncId, playerInventory, delegate, verify);
            }
        };
    }

    public ImplantManagerScreenHandler(int syncId, Inventory playerInventory, FriendlyByteBuf buf)
    {
        this(syncId, playerInventory, Util.make(() ->
                {
                    BlockPos pos = PacketBufUtil.readBlockPos(buf);
                    var be = playerInventory.player.level().getBlockEntity(pos);
                    return (ImplantManagerDelegate) be;
                }), buf.readBoolean());
    }

    // Server
    public ImplantManagerScreenHandler(int syncId, Inventory pi, ImplantManagerDelegate manager, boolean shouldVerifyMip)
    {
        super(NMScreenHandlers.UPGRADE_MANAGER, pi, null, syncId, null);
        this.manager = manager;
        this.shouldVerifyMip = shouldVerifyMip;

        this.removeImplantC2S = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "upgrade_manager_remove"),
                ChannelFormat.builder(RemoveImplant.class).param(ParamCodec.ofNullable(ParamCodecs.IDENTIFIER)).build(), playerInventory.player);
        this.selectImplantC2S = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "upgrade_manager_select"),
                ChannelFormat.builder(SelectImplant.class).param(ParamCodec.ofNullable(ParamCodecs.IDENTIFIER)).build(), playerInventory.player);

        if (!isClient())
        {
            removeImplantC2S.receiver(this::removeImplant);
            selectImplantC2S.receiver(this::selectImplant);
        }

        createInventory(0, 0, playerInventory);
        createHotbar(0, 0, playerInventory);
    }

    private void selectImplant(@Nullable ResourceLocation id)
    {
        if (id != null)
        {
            EntityImplant implant = getImplantManager().getImplant(id);
            var factory = ImplantConfigHandler.get(id);
            setConfigHandler(factory != null ? factory.createCast(this, implant, playerInventory.player) : null);
        }
        else
        {
            setConfigHandler(null);
        }
    }

    private void removeImplant(ResourceLocation id)
    {
        if (isClient())
            return;

        ItemLike item = manager.removeUpgrade(id);
        if (item != null)
        {
            ItemStack stack = new ItemStack(item);
            if (!playerInventory.add(stack))
            {
                playerInventory.player.drop(stack, false);
            }
        }
    }

    @Nullable
    public ImplantManager getImplantManager()
    {
        return manager.getImplantManager();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        return ItemStack.EMPTY;
    }

    public void setConfigHandler(@Nullable ImplantConfigHandler handler)
    {
        if (configHandler != null)
            configHandler.close();

        configHandler = handler;
    }

    @Override
    public void broadcastChanges()
    {
        super.broadcastChanges();
        if (configHandler != null)
            configHandler.sendUpdates();
    }

    @Override
    public void removed(Player player)
    {
        super.removed(player);
        removeImplantC2S.close();
        selectImplantC2S.close();

        if (configHandler != null)
            configHandler.close();
    }

    public boolean shouldVerifyMip()
    {
        return shouldVerifyMip;
    }

    public void selectClient(@Nullable ResourceLocation id)
    {
        selectImplantC2S.emitter().apply(id);
        var factory = ImplantConfigHandler.get(id);
        EntityImplant implant = getImplantManager().getImplant(id);
        setConfigHandler(factory != null ? factory.createCast(this, implant, playerInventory.player) : null);
    }

    @Nullable
    public ImplantConfigHandler getConfigHandler()
    {
        return configHandler;
    }

    public @Nullable MutateInPlace<?> getMip()
    {
        if (manager instanceof ImplantManagerBlockEntity be)
        {
            return be.getMip();
        }

        return null;
    }

    public interface RemoveImplant
    {
        void apply(ResourceLocation id);
    }

    public interface SelectImplant
    {
        void apply(ResourceLocation id);
    }
}