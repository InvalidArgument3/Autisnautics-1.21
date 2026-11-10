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

package com.neep.neepmeat.transport.fluid_network.node;

import com.google.common.base.MoreObjects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.math.*;
import net.minecraft.world.level.ChunkPos;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static net.minecraft.core.BlockPos.*;

public final class NodePos
{
    private static final int SIZE_BITS_X;
    private static final int SIZE_BITS_Z;
    public static final int SIZE_BITS_Y;
    public static final int SIZE_BITS_D;
    private static final long BITS_X;
    private static final long BITS_Y;
    private static final long BITS_Z;
    private static final long BITS_D;
    private static final int BIT_SHIFT_Z;
    private static final int BIT_SHIFT_X;
    private static final int BIT_SHIFT_D;
    private final BlockPos pos;
    private final Direction face;

    public NodePos(BlockPos pos, Direction face)
    {
        this.pos = pos.immutable();
        this.face = face;
    }

    public NodePos(int x, int y, int z, Direction face)
    {
        this.pos = new BlockPos(x, y, z);
        this.face = face;
    }

    public CompoundTag toNbt(CompoundTag nbt)
    {
        nbt.putLong("pos", pos.asLong());
        nbt.putInt("face", face.get3DDataValue());
        return nbt;
    }

    public static NodePos fromNbt(CompoundTag nbt)
    {
        BlockPos pos = BlockPos.of(nbt.getLong("pos"));
        Direction face = Direction.from3DDataValue(nbt.getInt("face"));
        return new NodePos(pos, face);
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this).add("x", pos.getX()).add("y", pos.getY()).add("z", pos.getZ()).add("face", face).toString();
    }

    @Override
    public boolean equals(Object object)
    {
        if (!(object instanceof NodePos nodePos)) {
            return false;
        }
        return pos.equals(nodePos.pos) && nodePos.face == face;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(3, 19)
                .append(pos.hashCode())
                .append(face)
                .toHashCode();
    }

    public static long toLong(NodePos pos)
    {
        long l = 0L;
        l |= ((long) pos.pos().getX() & BITS_X) << BIT_SHIFT_X;
        l |= ((long) pos.pos().getY() & BITS_Y) << 0;
        l |= ((long) pos.pos().getZ() & BITS_Z) << BIT_SHIFT_Z;
        l |= ((long) pos.face().ordinal() & BITS_D) << BIT_SHIFT_D;
        return l;
    }

    public static Direction unpackLongFace(long packedPos)
    {
        int dir = (int) (packedPos << 64 - BIT_SHIFT_D - SIZE_BITS_D >> 64 - SIZE_BITS_D);
        return Direction.values()[dir];
    }

    public static BlockPos fromLong(long packedPos)
    {
        return new BlockPos(getX(packedPos), getY(packedPos), getZ(packedPos));
    }

    public ChunkPos toChunkPos()
    {
        return SectionPos.of(pos).chunk();
    }

    public BlockPos facingBlock()
    {
        return pos.relative(face);
    }

    public BlockPos pos()
    {
        return pos;
    }

    public Direction face()
    {
        return face;
    }

    static {
        SIZE_BITS_Z = SIZE_BITS_X = 1 + Mth.log2(Mth.smallestEncompassingPowerOfTwo(30000000));
        SIZE_BITS_Y = 64 - SIZE_BITS_X - SIZE_BITS_Z;
        SIZE_BITS_D = 4;
        BITS_X = (1L << SIZE_BITS_X) - 1L;
        BITS_Y = (1L << SIZE_BITS_Y) - 1L;
        BITS_Z = (1L << SIZE_BITS_Z) - 1L;
        BITS_D = (1L << SIZE_BITS_Z) - 1L;
        BIT_SHIFT_Z = SIZE_BITS_Y;
        BIT_SHIFT_X = SIZE_BITS_Y + SIZE_BITS_Z;
        BIT_SHIFT_D = SIZE_BITS_Y + SIZE_BITS_Z + SIZE_BITS_X;
    }
}
