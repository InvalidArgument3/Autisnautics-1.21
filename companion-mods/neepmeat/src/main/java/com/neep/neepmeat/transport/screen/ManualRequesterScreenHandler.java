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

package com.neep.neepmeat.transport.screen;

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.ParamCodecs;
import com.neep.meatlib.network.ChannelManager;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.screen_handler.BasicScreenHandler;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.block.item_transport.entity.PersistentCraftingInventory;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.ResourceAmount;
import com.neep.meatlib.transfer.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class ManualRequesterScreenHandler extends BasicScreenHandler
{
    public final List<Slot> playerSlots;
    public final List<Slot> returnSlots;
    public final List<Slot> craftingSlots;
    public final ResultSlot craftingResultSlot;
    public final ChannelManager<SyncItems> syncItemsS2C;
    public final ChannelManager<Consumer<ResourceAmount<ItemVariant>>> requestC2S;

    protected final Player player;
    // Routing network
    protected final List<RoutingNetwork.Entry> items = new ArrayList<>(20);
    // Inventories
    private final CraftingContainer craftingInventory;
    private final Container returnInventory;
    private final ResultContainer craftingResultInventory = new ResultContainer();
    protected RoutingNetwork routingNetwork;
    protected final BlockPos pos;
    protected final Direction outDirection;

    // Sync
    private boolean dirty = true;

    // Client
    public ManualRequesterScreenHandler(int syncId, Inventory playerInventory)
    {
        this(syncId, playerInventory, new PersistentCraftingInventory.Simple(9), new SimpleContainer(1), RoutingNetwork.DEFAULT, BlockPos.ZERO, Direction.UP, playerInventory.player);
    }

    // Server
    public ManualRequesterScreenHandler(int syncId, Inventory playerInventory, CraftingContainer craftingInventory, Container returnInventory, RoutingNetwork network, BlockPos pos, Direction outDirection, Player player)
    {
        super(TransportScreenHandlers.ITEM_REQUESTER_HANDLER, playerInventory, returnInventory, syncId, null);
        this.craftingInventory = new PersistentCraftingInventory.Delegate(craftingInventory, this);
        this.returnInventory = returnInventory;
        this.routingNetwork = network;
        this.pos = pos;
        this.outDirection = outDirection;
        this.player = player;

        returnSlots = createSlotBlock(0, 0, 1, 1, returnInventory, 0, net.minecraft.world.inventory.Slot::new);
        craftingSlots = createSlotBlock(0, 0, 3, 3, this.craftingInventory, 0, net.minecraft.world.inventory.Slot::new);
        playerSlots = createPlayerSlots(0, 0, playerInventory);
        craftingResultSlot = new ResultSlot(player, this.craftingInventory, craftingResultInventory, slots.size(), 0, 0);
        addSlot(craftingResultSlot);

        syncItemsS2C = addClosable(ChannelManager.create(
                ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "requester_screen/sync_items"), ChannelFormat.builder(SyncItems.class)
                        .param(ParamCodec.list(RoutingNetwork.Entry.PARAM_CODEC)).build(), player));

        requestC2S = addClosable(ChannelManager.create(
                ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "requester_screen/request"), ChannelFormat.builder(ParamCodecs.<Consumer<ResourceAmount<ItemVariant>>>as(Consumer.class))
                        .param(ParamCodecs.resourceAmount(ItemVariant::fromPacket)).build(), player));

        if (!player.level().isClientSide())
            requestC2S.receiver(this::receiveRequest);
    }

    protected static void sortItems(List<RoutingNetwork.Entry> items)
    {
        items.sort((s1, s2) -> s1.resource().toStack().isEmpty() ? 1 : s1.resource().toStack().getHoverName().getString(256).compareTo(s2.resource().toStack().getHoverName().getString(256)));
    }

    protected void updateCraftingResult(Level world)
    {
        if (player instanceof ServerPlayer serverPlayer)
        {
            ItemStack itemStack = ItemStack.EMPTY;
            Optional<CraftingRecipe> optional = world.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftingInventory, world);
            if (optional.isPresent())
            {
                CraftingRecipe craftingRecipe = optional.get();
                if (craftingResultInventory.setRecipeUsed(world, serverPlayer, craftingRecipe))
                {
                    ItemStack itemStack2 = craftingRecipe.assemble(craftingInventory, world.registryAccess());
                    if (itemStack2.isItemEnabled(world.enabledFeatures()))
                    {
                        itemStack = itemStack2;
                    }
                }
            }

            int resultId = craftingResultSlot.getContainerSlot();
            craftingResultInventory.setItem(0, itemStack);
            setRemoteSlot(resultId, itemStack);
            serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), resultId, itemStack));
        }
    }

    @Override
    public void slotsChanged(Container inventory)
    {
        super.slotsChanged(inventory);

        if (!player.level().isClientSide())
            updateCraftingResult(player.level());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        if (index == craftingResultSlot.index)
        {
            Slot slot2 = getSlot(index);
            ItemStack itemStack2 = slot2.getItem();
            ItemStack itemStack = itemStack2.copy();

            if (!player.level().isClientSide())
                itemStack2.getItem().onCraftedBy(itemStack2, player.level(), player);

            if (!this.moveItemStackTo(itemStack2, 10, 46, true))
                return ItemStack.EMPTY;

            slot2.onQuickCraft(itemStack2, itemStack);

            if (itemStack2.isEmpty())
                slot2.setByPlayer(ItemStack.EMPTY);
            else
                slot2.setChanged();

            if (itemStack2.getCount() == itemStack.getCount())
                return ItemStack.EMPTY;

            slot2.onTake(player, itemStack2);

            if (index == 0)
                player.drop(itemStack2, false);

            return itemStack;
        }
        else if (index >= craftingSlots.get(0).index && index <= craftingSlots.get(craftingSlots.size() - 1).index)
        {
            // Move into inventory instead of export slot
            ItemStack originalCopy = ItemStack.EMPTY;
            Slot slot = this.slots.get(index);
            if (slot.hasItem())
            {
                ItemStack mutableStack = slot.getItem();
                originalCopy = mutableStack.copy();
                if (!this.moveItemStackTo(mutableStack, playerSlots.get(0).index, playerSlots.get(0).index + playerSlots.size(), false))
                {
                    return ItemStack.EMPTY;
                }

                if (mutableStack.isEmpty())
                {
                    slot.setByPlayer(ItemStack.EMPTY);
                }
                else
                {
                    slot.setChanged();
                }
            }
            return originalCopy;
        }
        else
        {
            return super.quickMoveStack(player, index);
        }
    }

    @Override
    public void broadcastChanges()
    {
        super.broadcastChanges();

        if (ready && (dirty || player.level().getGameTime() % 10 == 0))
        {
            syncToPacket(items, routingNetwork);
            dirty = false;

            updateCraftingResult(player.level());
        }
    }

    private void syncToPacket(List<RoutingNetwork.Entry> items, RoutingNetwork network)
    {
        try (Transaction transaction = Transaction.openOuter())
        {
            items.clear();
            var available = network.listAllAvailable(v -> !v.isBlank(), transaction);
            items.addAll(available);
            transaction.commit();
        }
        sortItems(items);

        syncItemsS2C.emitter().apply(items);
    }

    public void receiveItems(List<RoutingNetwork.Entry> items)
    {
        this.items.clear();
        this.items.addAll(items);
    }

    public List<RoutingNetwork.Entry> getItems()
    {
        return items;
    }

    public void receiveRequest(ResourceAmount<ItemVariant> ra)
    {
        try (Transaction transaction = Transaction.openOuter())
        {

            if (routingNetwork.request(ra, pos, outDirection, RoutingNetwork.RequestType.ANY_AMOUNT, new RoutingNetwork.RequestContext(), transaction))
                transaction.commit();
        }

        dirty = true;
    }

    @Override
    public void removed(Player player)
    {
        super.removed(player);
        clearContainer(player, returnInventory);
    }

    public interface SyncItems
    {
        void apply(List<RoutingNetwork.Entry> list);
    }
}