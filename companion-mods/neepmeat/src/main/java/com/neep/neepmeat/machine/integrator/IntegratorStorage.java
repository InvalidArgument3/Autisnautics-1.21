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

package com.neep.neepmeat.machine.integrator;

import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageView;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.SingleSlotStorage;
import com.neep.meatlib.transfer.SingleVariantStorage;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;

@SuppressWarnings("UnstableApiUsage")
public class IntegratorStorage
{
    protected IntegratorBlockEntity parent;

    public IntegratorStorage(IntegratorBlockEntity parent)
    {
        this.parent = parent;
    }

    protected ImmatureStorage immatureStorage = new ImmatureStorage();

    public Storage<FluidVariant> getFluidStorage(Level world, BlockPos pos, BlockState state, Direction direction)
    {
        if (!parent.isMature)
        {
            return immatureStorage;
        }
        return null;
    }

//    public static final String NBT_VARIANT = "variant";
//    public static final String NBT_AMOUNT = "amount";
    public static final String NBT_IMMATURE_STORAGE = "variant";

    public void writeNbt(CompoundTag nbt)
    {
        CompoundTag immature = new CompoundTag();
        immatureStorage.writeNbt(immature);
        nbt.put(NBT_IMMATURE_STORAGE, immature);
    }

    public void readNbt(CompoundTag nbt)
    {
        CompoundTag immature = nbt.getCompound(NBT_IMMATURE_STORAGE);
        immatureStorage.readNbt(immature);
    }

    public Storage<ItemVariant> itemStorage = new ItemStorage();

    protected class ItemStorage implements Storage<ItemVariant>
    {
        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction)
        {
            Integer inc = IntegratorBlockEntity.DATA_MAP.get(resource.getItem());
            if (inc != null)
            {
                float inserted = parent.insertEnlightenment(inc, transaction);
                if (inserted == inc)
                {
                    transaction.addOuterCloseCallback(result ->
                    {
                        parent.getLevel().playSound(null, parent.getBlockPos(), SoundEvents.HORSE_EAT, SoundSource.BLOCKS, 1, 1);
                    });
                    return 1;
                }
            }
            return 0;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction)
        {
            return 0;
        }

        @Override
        public Iterator<StorageView<ItemVariant>> iterator()
        {
            return Collections.emptyIterator();
        }
    }

    protected static class ImmatureStorage extends SingleVariantStorage<FluidVariant> implements InsertionOnlyStorage<FluidVariant>, SingleSlotStorage<FluidVariant>
    {
        protected long capacity = FluidConstants.BUCKET;

        @Override
        protected FluidVariant getBlankVariant()
        {
            return FluidVariant.BLANK;
        }

        @Override
        protected long getCapacity(FluidVariant variant)
        {
            return capacity;
        }

        public void writeNbt(CompoundTag nbt)
        {
            nbt.putLong("amount", amount);
            nbt.put("resource", variant.toNbt());
        }

        public void readNbt(CompoundTag nbt)
        {
            this.amount = nbt.getLong("amount");
            this.variant = FluidVariant.fromNbt((CompoundTag) nbt.get("resource"));
        }

        @Override
        public boolean supportsExtraction()
        {
            return InsertionOnlyStorage.super.supportsExtraction();
        }

        @Override
        public @NotNull Iterator<StorageView<FluidVariant>> iterator()
        {
            return InsertionOnlyStorage.super.iterator();
        }

        @Override
        protected boolean canInsert(FluidVariant variant)
        {
            return variant.getFluid().is(NMTags.BLOOD);
        }

        @Override
        protected void onFinalCommit()
        {
            // Should something go here?
        }
    }
}
