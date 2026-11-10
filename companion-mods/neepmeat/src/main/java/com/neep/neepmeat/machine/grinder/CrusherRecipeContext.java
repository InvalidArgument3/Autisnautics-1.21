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

package com.neep.neepmeat.machine.grinder;

import com.neep.meatlib.util.NbtSerialisable;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.nbt.CompoundTag;

public interface CrusherRecipeContext
{
    Storage<ItemVariant> getInputStorage();

    Storage<ItemVariant> getOutputStorage();

    Storage<ItemVariant> getExtraStorage();

    XpStorage getXpStorage();

    float getChanceMod();

    class XpStorage extends SnapshotParticipant<Float> implements NbtSerialisable
    {
        private float xp;

        public float insert(float maxAmount, TransactionContext transaction)
        {
            if (maxAmount > 0)
            {
                updateSnapshots(transaction);
                xp += maxAmount;
                return maxAmount;
            }
            return 0;
        }

        public float extract(float maxAmount, TransactionContext transaction)
        {
            if (maxAmount > 0)
            {
                updateSnapshots(transaction);
                float extracted = Math.min(xp, maxAmount);
                xp -= extracted;
                return extracted;
            }
            return 0;
        }

        public float getAmount()
        {
            return xp;
        }

        public CompoundTag writeNbt(CompoundTag nbt)
        {
            nbt.putFloat("amount", xp);
            return nbt;
        }

        public void readNbt(CompoundTag nbt)
        {
            this.xp = nbt.getFloat("amount");
        }

        protected Float createSnapshot()
        {
            return xp;
        }

        protected void readSnapshot(Float snapshot)
        {
            this.xp = snapshot;
        }
    }
}
