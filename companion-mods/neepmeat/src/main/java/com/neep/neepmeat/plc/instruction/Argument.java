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

package com.neep.neepmeat.plc.instruction;

import com.neep.meatlib.api.network.ParamCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record Argument(BlockPos pos, Direction face)
{
    public static final ParamCodec<Argument> PARAM_CODEC = ParamCodec.of(Argument.class, Argument::writeBuf, Argument::fromBuf);

    public void writeBuf(FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
        buf.writeInt(face.ordinal());
    }

    public static Argument fromBuf(FriendlyByteBuf buf)
    {
        BlockPos pos = buf.readBlockPos();
        Direction face = Direction.values()[buf.readInt()];
        return new Argument(pos, face);
    }

    public CompoundTag toNbt()
    {
        CompoundTag nbt = new CompoundTag();

        nbt.put("pos", NbtUtils.writeBlockPos(pos));
        nbt.putInt("face", face.ordinal());
        return nbt;
    }

    public static Argument fromNbt(CompoundTag nbt)
    {
        return new Argument(NbtUtils.readBlockPos(nbt, "pos").orElseThrow(),
                Direction.values()[nbt.getInt("face")]);
    }

    @Override
    public String toString()
    {
        return String.format("@(%s %s %s %s)", pos.getX(), pos.getY(), pos.getZ(), face);
    }
}
