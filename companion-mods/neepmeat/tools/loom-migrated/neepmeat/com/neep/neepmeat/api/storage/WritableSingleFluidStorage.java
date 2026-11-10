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

package com.neep.neepmeat.api.storage;

import com.neep.neepmeat.fluid.MixableFluid;
import com.neep.neepmeat.implant.entity.ImplantManager;
import com.neep.neepmeat.implant.item.LivingToolImplant;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.item.LivingToolImplantItem;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.awt.event.ContainerAdapter;

@SuppressWarnings("UnstableApiUsage")
public class WritableSingleFluidStorage extends SingleVariantStorage<FluidVariant> implements StorageView<FluidVariant>
{
    public static final String KEY_RESOURCE = "resource";
    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_CAPACITY = "capacity";

    protected long capacity;

    public float renderLevel;
    public Runnable finalCallback;

    public WritableSingleFluidStorage(long capacity, Runnable finalCallback)
    {
        this(capacity);
        this.finalCallback = finalCallback;
    }

    public WritableSingleFluidStorage(long capacity)
    {
        this.capacity = capacity;
        this.finalCallback = () -> {};
    }
    
    @Nullable
    public static Storage<FluidVariant> findFluidStorage(ItemStack stack, Player player, InteractionHand hand)
    {
        ContainerItemContext cic = ContainerItemContext.ofPlayerHand(player, hand);
        Storage<FluidVariant> storage = FluidStorage.ITEM.find(stack, cic);
        
        if (storage == null)
        {
            ImplantManager manager = NMComponents.IMPLANT_MANAGER.getNullable(stack);
            if (manager != null && manager.isInstalled(LivingToolImplant.ID))
            {
                storage = LivingToolImplantItem.getStorage(stack, cic);
                if (storage != null)
                    return storage;
            }
        }
        
        return storage;
    }

    public static boolean handleInteract(Storage<FluidVariant> buffer, Level world, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        Storage<FluidVariant> storage = findFluidStorage(stack, player, hand);

        if (world.isClientSide())
            return true;

        if (storage != null)
        {
            if (player.isCreative())
            {
                try (Transaction transaction = Transaction.openOuter())
                {
                    var it = storage.iterator();
                    if (it.hasNext())
                    {
                        StorageView<FluidVariant> view = it.next();
                        if (!view.isResourceBlank())
                        {
                            SoundEvent bucketFillSound = FluidVariantAttributes.getEmptySound(view.getResource());
                            world.playSound(null, player.blockPosition(), bucketFillSound, SoundSource.BLOCKS, 1f, 1.5f);
                            buffer.insert(view.getResource(), view.getAmount(), transaction);
                            transaction.commit();
                            return true;
                        }
                        transaction.abort();
                    }
                }
            }

            try (Transaction transaction = Transaction.openOuter())
            {
                // Get empty sound before resource can be extracted from the item.
                FluidVariant stored = StorageUtil.findStoredResource(storage);
                SoundEvent empty = stored != null ? FluidVariantAttributes.getEmptySound(stored) : SoundEvents.BUCKET_EMPTY;

                try (Transaction inner = transaction.openNested())
                {
                    if (StorageUtil.move(storage, buffer, variant -> true, Long.MAX_VALUE, inner) > 0)
                    {
                        world.playSound(null, player.blockPosition(), empty, SoundSource.BLOCKS, 1f, 1.5f);
                        inner.commit();
                        transaction.commit();
                        return true;
                    }
                    inner.abort();
                }

                // Jaaaank
                SoundEvent fill = buffer instanceof StorageView<?> view ?
                        FluidVariantAttributes.getFillSound((FluidVariant) view.getResource()) : SoundEvents.BUCKET_FILL;

                try (Transaction inner = transaction.openNested())
                {
                    if (StorageUtil.move(buffer, storage, variant -> true, Long.MAX_VALUE, inner) > 0)
                    {
                        world.playSound(null, player.blockPosition(), fill, SoundSource.BLOCKS, 1f, 1.5f);
                        inner.commit();
                        transaction.commit();
                        return true;
                    }
                    inner.abort();
                }
                transaction.abort();
            }
        }
        return false;
    }

    @Override
    protected FluidVariant getBlankVariant()
    {
        return FluidVariant.blank();
    }

    @Override
    protected long getCapacity(FluidVariant variant)
    {
        return capacity;
    }

    protected boolean variantsCompatible(FluidVariant insertedVariant)
    {
        return insertedVariant.equals(variant)
                || MixableFluid.canVariantsMix(variant, insertedVariant) && insertedVariant.isOf(variant.getFluid());
    }

    @Override
    public long insert(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction)
    {
        StoragePreconditions.notBlankNotNegative(insertedVariant, maxAmount);

        if ((variantsCompatible(insertedVariant) || variant.isBlank()) && canInsert(insertedVariant))
        {
            long insertedAmount = Math.min(maxAmount, getCapacity(insertedVariant) - amount);

            if (insertedAmount > 0)
            {
                updateSnapshots(transaction);

                if (variant.isBlank())
                {
                    variant = insertedVariant;
                    amount = insertedAmount;
                }
                else
                {
                    if (MixableFluid.canVariantsMix(variant, insertedVariant))
                    {
                       variant = ((MixableFluid) variant.getFluid()).mixNbt(variant, amount, insertedVariant, insertedAmount);
                    }

                    amount += insertedAmount;
                }

                return insertedAmount;
            }
        }
        return 0;
    }

    protected void onFinalCommit()
    {
        if (finalCallback != null)
            finalCallback.run();
    }

    public static FluidVariant readFluidVariant(CompoundTag nbt)
    {
        return FluidVariant.fromNbt(nbt.getCompound(WritableSingleFluidStorage.KEY_RESOURCE));
    }

    public static long readAmount(CompoundTag nbt)
    {
        return nbt.getLong(KEY_AMOUNT);
    }

    public static long readCapacity(CompoundTag nbt)
    {
        return nbt.getLong(KEY_CAPACITY);
    }

    public CompoundTag toNbt(CompoundTag nbt)
    {
        writeNbt(nbt);
        return nbt;
    }

    @Override
    public void writeNbt(CompoundTag nbt)
    {
        nbt.put(KEY_RESOURCE, getResource().toNbt());
        nbt.putLong(KEY_AMOUNT, amount);
        nbt.putLong(KEY_CAPACITY, capacity);
    }

    public CompoundTag readNbt(CompoundTag nbt) // TODO: make this return void
    {
        this.variant = readFluidVariant(nbt);
        this.amount = readAmount(nbt);
//        this.capacity = readCapacity(nbt); // No changing capacity through NBT
        return nbt;
    }
}
