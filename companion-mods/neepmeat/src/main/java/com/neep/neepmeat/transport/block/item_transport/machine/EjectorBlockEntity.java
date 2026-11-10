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

package com.neep.neepmeat.transport.block.item_transport.machine;
import com.neep.neepmeat.machine.transport.ConnectedPipeBlock;
import com.neep.neepmeat.machine.transport.DeferredRequest;
import com.neep.neepmeat.machine.transport.FilterEntry;
import com.neep.neepmeat.machine.transport.FilterPipeMenu;
import com.neep.neepmeat.machine.transport.NetworkCrafterBlockEntity;
import com.neep.neepmeat.machine.transport.StorageBusBlock;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.block.fluid_transport.FluidPipeBlock;
import com.neep.neepmeat.transport.block.fluid_transport.machine.FluidPipeBlockEntity;
import com.neep.neepmeat.transport.block.fluid_transport.machine.PumpBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.FilterItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.ItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.MergePipeBlock;
import com.neep.neepmeat.transport.block.item_transport.PipeDriverBlock;
import com.neep.neepmeat.transport.block.item_transport.entity.ItemPipeBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.entity.PipeDriverBlockEntity;
import com.neep.neepmeat.transport.item_network.PipeRouteFinder;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.storage.MeatlibStorageUtil;
import com.neep.meatlib.storage.ResourcePredicate;
import com.neep.neepmeat.api.storage.DynamicLazyBlockApiCache;
import com.neep.neepmeat.api.storage.LazyBlockApiCache;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.plc.instruction.PlcInstruction;
import com.neep.neepmeat.transport.block.fluid_transport.PumpBlock;
import com.neep.neepmeat.transport.item_network.ItemInPipe;
import com.neep.neepmeat.transport.util.ItemPipeUtil;
import com.neep.meatlib.util.MiscUtil;
import com.neep.meatlib.transfer.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.ResourceAmount;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class EjectorBlockEntity extends SyncableBlockEntity
{
    public static final String NBT_COOLDOWN = "cooldown";

    protected final LazyBlockApiCache<Storage<ItemVariant>, Direction> insertionCache =
            addListener(DynamicLazyBlockApiCache.facing(ItemStorage.SIDED, this, () -> getBlockState().getValue(EjectorBlock.FACING)));
    protected final LazyBlockApiCache<Storage<ItemVariant>, Direction> extractionCache =
            addListener(DynamicLazyBlockApiCache.facing(ItemStorage.SIDED, this, () -> getBlockState().getValue(EjectorBlock.FACING).getOpposite()));

    public int cooldown;
    @Nullable protected ResourceAmount<ItemVariant> stored;

    public int shuttle;
    protected boolean activeWithoutRedstone = false;

    // Client only
    public double offset;

    private final PipeItemMemory rememberer = new PipeItemMemory();

    public EjectorBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.EJECTOR, pos, state);
    }

    public EjectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void fromClientTag(CompoundTag tag)
    {
        this.shuttle = tag.getInt("shuttle_ticks");
    }

    @Override
    public void toClientTag(CompoundTag tag)
    {
        tag.putInt("shuttle_ticks", shuttle);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        tag.putInt(NBT_COOLDOWN, cooldown);
        tag.put("stored", PlcInstruction.writeItem(stored));
        tag.putBoolean("active_without_redstone", activeWithoutRedstone);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        this.cooldown = nbt.getInt(NBT_COOLDOWN);
        this.stored = PlcInstruction.readItem(nbt.getCompound("stored"));
        this.activeWithoutRedstone = nbt.getBoolean("active_without_redstone");
    }

    public void serverTick()
    {
        cooldown = Math.max(cooldown - 1, 0);

        if (shuttle > 0)
        {
            --shuttle;
            if (shuttle == 0)
                sync();
        }

        eject();

        if (cooldown == 0 && getBlockState().getValue(EjectorBlock.ACTIVE) && stored == null)
        {
            cooldown = 10;
            tryTransfer();
        }
    }

//    private <T> ResourceAmount<T> fuckOff(Storage<T> storage, TransactionContext transaction)
//    {
//        for (var view :  storage)
//        {
//            if (!view.isResourceBlank())
//            {
//                view.extract(view.getResource(),
//            }
//        }
//
//        return null;
//    }

    protected void tryTransfer()
    {
        Storage<ItemVariant> storage;
        if ((storage = extractionCache.find()) != null)
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                Storage<ItemVariant> facingStorage = insertionCache.find();

                ResourcePredicate<ItemVariant> predicate = MeatlibStorageUtil.insertionPredicate(facingStorage);

                ResourceAmount<ItemVariant> ra = MeatlibStorageUtil.findAndExtractContent(storage, 1, transaction, predicate);

                if (ra == null || ra.amount() != 1)
                {
                    transaction.abort();
                    return;
                }

                long canInsert = ra.amount();
                if (facingStorage != null)
                {
                    canInsert = Math.min(canInsert, MeatlibStorageUtil.simulateInsert(facingStorage, ra.resource(), canInsert, transaction));
                }

                if (canInsert <= 0)
                {
                    transaction.abort();
                    return;
                }

                long extracted = ra.amount();

                if (extracted == canInsert)
                {
                    succeed();
                    stored = new ResourceAmount<>(ra.resource(), extracted);
                    transaction.commit();
                    return;
                }
                transaction.abort();
            }
        }
    }

    protected void eject()
    {
        if (stored == null)
            return;

        try (Transaction transaction = Transaction.openOuter())
        {
            long forwarded = forwardItem(new ResourceAmount<>(stored.resource(), Math.min(16, stored.amount())), transaction);
            if (forwarded == stored.amount())
            {
                stored = null;
            }
            else
            {
                stored = new ResourceAmount<>(stored.resource(), stored.amount() - forwarded);
            }
            transaction.commit();
        }
    }

    public long forwardItem(ResourceAmount<ItemVariant> amount, TransactionContext transaction)
    {
        return forwardItem(new ItemInPipe(amount, level.getGameTime()), transaction);
    }

    public long forwardItem(ItemInPipe item, TransactionContext transaction)
    {
        Direction facing = getBlockState().getValue(ItemPumpBlock.FACING);

        Storage<ItemVariant> storage;
        if (insertionCache != null && (storage = insertionCache.find()) != null)
        {
            Transaction nested = transaction.openNested();
            long transferred = storage.insert(item.resource(), item.amount(), nested);
            nested.commit();
            return transferred;
        }

        return ItemPipeUtil.EjectOperation.of(level, getBlockPos(), facing, rememberer)
                .useRouting(true)
                .pipeToAny(item, transaction)
                .amount();
    }

    public long forwardToEntity(ResourceAmount<ItemVariant> resource, Transaction transaction)
    {
        BlockPos pos1 = getBlockPos().relative(getBlockState().getValue(EjectorBlock.FACING));
        AABB toBox = AABB.ofSize(Vec3.atCenterOf(pos1), 1, 1, 1);
        Vec3 centre = Vec3.atCenterOf(pos1);
        List<AbstractMinecartContainer> toMinecarts = level.getEntities(EntityTypeTest.forClass(AbstractMinecartContainer.class), toBox, (entity -> true));
        AbstractMinecartContainer minecart = MiscUtil.closestEntity(toMinecarts, centre);
        if (minecart != null)
        {
            Storage<ItemVariant> storage = InventoryStorage.of(minecart, null);
            return storage.insert(resource.resource(), resource.amount(), transaction);
        }
        return 0;
    }

    public void succeed()
    {
        this.shuttle = 3;
        sync();
    }

    public void changeMode()
    {
        activeWithoutRedstone = !activeWithoutRedstone;
        updatePowered(getLevel().hasNeighborSignal(worldPosition));
        setChanged();
    }

    public void updatePowered(boolean powered)
    {
        boolean active = powered != activeWithoutRedstone;
        BlockState newState = getBlockState().setValue(PumpBlock.ACTIVE, active);
        level.setBlockAndUpdate(worldPosition, newState);
    }
}
