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

package com.neep.neepmeat.transport.block.item_transport.entity;

import com.neep.meatlib.inventory.ImplementedInventory;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.transport.ItemTransport;
import com.neep.neepmeat.transport.api.item_network.ManualRequester;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.block.item_transport.ManualRequesterBlock;
import com.neep.neepmeat.transport.fluid_network.node.NodePos;
import com.neep.neepmeat.transport.item_network.ItemPipeVertexImpl;
import com.neep.neepmeat.transport.screen.ManualRequesterScreenHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class ManualRequesterBlockEntity extends ItemPipeBlockEntity implements ManualRequester, MenuProvider
{
    private final Container returnInventory = ImplementedInventory.ofSize(1, this::handleReturn);
    private final PersistentCraftingInventory craftingInventory = new PersistentCraftingInventory(this::setChanged);

    public ManualRequesterBlockEntity(BlockPos pos, BlockState state)
    {
        this(ItemTransport.ITEM_REQUESTER_BE, pos, state);
    }

    public ManualRequesterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state, Vertex::new);
    }

    @Override
    public Result request(Predicate<ItemVariant> predicate, long amount, NodePos fromPos, RoutingNetwork.RequestContext context, TransactionContext transaction)
    {
        return Result.FAIL;
    }

    @Override
    public long request2(Predicate<ItemVariant> predicate, long amount, NodePos dest, TransactionContext transaction)
    {
        return 0;
    }

    @Override
    public Stream<StorageView<ItemVariant>> getAvailable(TransactionContext transaction)
    {
        return Stream.empty();
    }

    @Override
    public BlockPos getRoutablePipePos()
    {
        return worldPosition;
    }

    @Override
    public @Nullable NodePos requestDestPosDir()
    {
        return null;
    }

    @Override
    public Component getDisplayName()
    {
        return Component.translatable("screen." + NeepMeat.NAMESPACE + "item_requester");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player)
    {
        RoutingNetwork network = cache.getNetwork();

        if (network == null)
            return null;

        return new ManualRequesterScreenHandler(syncId, inv, craftingInventory, returnInventory, network, worldPosition
                , getBlockState().getValue(ManualRequesterBlock.FACING), player);
    }

    private void handleReturn()
    {
        ItemStack stack = returnInventory.getItem(0);
        if (!stack.isEmpty() && cache.getNetwork() != null)
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                Direction facing = getBlockState().getValue(ManualRequesterBlock.FACING);
                long returned = cache.getNetwork().store(ItemVariant.of(stack), stack.getCount(), worldPosition.relative(facing), facing.getOpposite(), transaction);

                if (returned > 0)
                {
                    stack.shrink((int) returned);
                    returnInventory.setItem(0, stack.copy());
                    transaction.commit();
                }
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.put("crafting_inventory", craftingInventory.writeNbt(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        this.craftingInventory.readNbt(tag.getCompound("crafting_inventory"));
    }

    private static class Vertex extends ItemPipeVertexImpl
    {
        public Vertex(BlockEntity parent)
        {
            super(parent);
        }

        @Override
        public boolean canSimplify()
        {
            return false;
        }
    }
}
