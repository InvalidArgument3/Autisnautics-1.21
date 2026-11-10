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

package com.neep.neepmeat.block.entity;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.storage.MeatlibStorageUtil;
import com.neep.neepmeat.api.storage.WritableStackStorage;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.plc.component.MutateInPlace;
import com.neep.neepmeat.screen_handler.DisplayPlateScreenHandler;
import com.neep.neepmeat.transport.block.item_transport.DisplayPlateBlock;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class DisplayPlateBlockEntity extends SyncableBlockEntity implements ExtendedScreenHandlerFactory
{
    protected int capacity = 1;

    protected final WritableStackStorage storage;
    protected final MutateInPlace<ItemStack> mip = new Mutate();
    protected final Delegate delegate = new Delegate();

    public float stackRenderDelta; // Used by the renderer

    public DisplayPlateBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        this.storage = new WritableStackStorage(this::sync, 1)
        {
            @Override
            protected void onFinalCommit()
            {
                super.onFinalCommit();
            }

            protected long getCapacity(ItemVariant variant)
            {
                if (!variant.isBlank())
                {
                    return Math.min(variant.getItem().getMaxStackSize(), DisplayPlateBlockEntity.this.capacity);
                }
                return DisplayPlateBlockEntity.this.capacity;
            }
        };
    }

    public DisplayPlateBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.ITEM_BUFFER_BLOCK_ENTITY, pos, state);
    }

    public WritableStackStorage getStorage(@Nullable Direction direction)
    {
        return storage;
    }

    public MutateInPlace<ItemStack> getMip(Void ctx)
    {
        return mip;
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        this.storage.writeNbt(tag);
        tag.putInt("capacity", capacity);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        this.storage.readNbt(nbt);
        this.capacity = nbt.getInt("capacity");
    }


    public void extractFromItem(ItemEntity itemEntity)
    {
        ItemStack itemStack = itemEntity.getItem();
        if (itemStack.isEmpty())
            return;

        try (Transaction transaction = Transaction.openOuter())
        {
            int transferred = (int) storage.insert(ItemVariant.of(itemStack), itemStack.getCount(), transaction);
            itemStack.shrink(transferred);
            if (itemStack.getCount() <= 0)
            {
                itemEntity.discard();
            }

            transaction.commit();
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
        return new DisplayPlateScreenHandler(playerInventory, syncId, delegate, capacity);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf)
    {
        buf.writeInt(capacity);
    }

    protected class Mutate implements MutateInPlace<ItemStack>
    {
        @Override
        public ItemStack get()
        {
            return storage.getAsStack().copyWithCount(1);
        }

        @Override
        public void set(ItemStack stack)
        {
            storage.setStack(stack);
        }

        public BlockPos getOutputPos()
        {
            return getBlockState().getValue(DisplayPlateBlock.TOP) ? worldPosition.above() : worldPosition;
        }

        public Level getWorld()
        {
            return level;
        }

        public void setResult(int consumed, ItemStack stack)
        {
            // Handle >1 count
            if (!storage.isEmpty())
            {
                if (storage.amount >= consumed)
                    storage.extractDirect(consumed);

                if (storage.amount > 0)
                    MeatlibStorageUtil.scatter(level, worldPosition, storage);
            }

            set(stack);
        }
    }

    private class Delegate implements ContainerData
    {

        @Override
        public int get(int index)
        {
            if (index == 0)
                return capacity;

            return 0;
        }

        @Override
        public void set(int index, int value)
        {
            if (index == 0)
                capacity = Mth.clamp(value, 0, 8);
        }

        @Override
        public int getCount()
        {
            return 1;
        }
    }
}
