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

package com.neep.neepmeat.transport.worm;

import com.neep.neepmeat.machine.live_machine.block.entity.DistributorPointBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public record PointHandle(BlockPos pos, String address, DistributorPointBlockEntity.Mode mode, boolean powered) {
    public static PointHandle fromNbt(CompoundTag nbt) {
        return new PointHandle(BlockPos.of(nbt.getLong("pos")), nbt.getString("address"),
                DistributorPointBlockEntity.Mode.fromNbt(nbt.getInt("mode")), nbt.getBoolean("powered"));
    }

    public Tag toNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putLong("pos", pos.asLong());
        nbt.putString("address", address);
        nbt.putInt("mode", mode.ordinal());
        nbt.putBoolean("powered", powered);
        return nbt;
    }

    public boolean matches(String address) {
        return !this.address.isEmpty() && this.address.equals(address);
    }
}
