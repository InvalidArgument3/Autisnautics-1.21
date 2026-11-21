package com.neep.neepmeat.neepasm.vm;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.plc.instruction.Argument;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record WorldTargetImpl(BlockPos pos, Direction face) implements ThordData
{
    @Override
    public DataType type()
    {
        return DataType.WORLD_TARGET;
    }

    @Override
    public String stringValue()
    {
        return "@(%s %s %s %s)".formatted(pos.getX(), pos.getY(), pos.getZ(), face);
    }

    @Override
    public Argument worldTargetValue() throws NeepASM.RuntimeException
    {
        return new Argument(pos, face);
    }

    @Override
    public int getAsInt() throws NeepASM.RuntimeException
    {
        throw new NeepASM.RuntimeException("Cannot convert %s to integer".formatted(type().name()));
    }

    @Override
    public boolean boolValue()
    {
        return true;
    }

    @Override
    public int pointerValue() throws NeepASM.RuntimeException
    {
        throw new NeepASM.RuntimeException("Cannot convert %s to pointer".formatted(type().name()));
    }

    public static WorldTargetImpl read(ByteBuf buf)
    {
        return new WorldTargetImpl(new BlockPos(buf.readInt(), buf.readInt(), buf.readInt()),
                Direction.values()[buf.readInt()]);
    }

    @Override
    public ThordData add(ThordData v2) throws NeepASM.RuntimeException
    {
        return new WorldTargetImpl(v2.worldTargetValue().pos().offset(pos), face);
    }

    @Override
    public ThordData sub(ThordData v2) throws NeepASM.RuntimeException
    {
        return new WorldTargetImpl(pos.subtract(v2.worldTargetValue().pos()), face);
    }

    @Override
    public ThordData mul(ThordData v1) throws NeepASM.RuntimeException
    {
        return new WorldTargetImpl(pos.multiply(v1.getAsInt()), face);
    }

    @Override
    public ThordData div(ThordData v1) throws NeepASM.RuntimeException
    {
        return new WorldTargetImpl(pos.multiply(Math.round(1 / (float) v1.getAsInt())), face);
    }

    @Override
    public boolean eq(ThordData x1) throws NeepASM.RuntimeException
    {
        var p1 = x1.worldTargetValue();
        return pos.equals(p1.pos()) && face == p1.face();
    }

    @Override
    public boolean gt(ThordData x1) throws NeepASM.RuntimeException
    {
        return false;
    }

    @Override
    public boolean lt(ThordData x1) throws NeepASM.RuntimeException
    {
        return false;
    }

    @Override
    public ThordData copy()
    {
        // The object is immutable. Nobody will know any different.
        return this;
    }

    @Override
    public void write(ByteBuf buf)
    {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeInt(face.ordinal());
    }

    @Override
    public Tag asNbt()
    {
        var c = new CompoundTag();
        c.putInt("x", pos.getX());
        c.putInt("y", pos.getY());
        c.putInt("z", pos.getZ());
        c.putInt("face", face.ordinal());
        return c;
    }

    public static WorldTargetImpl readNbt(Tag nbt)
    {
        var c = (CompoundTag) nbt;
        return new WorldTargetImpl(new BlockPos(c.getInt("x"), c.getInt("y"), c.getInt("z")),
                Direction.values()[c.getInt("face")]);
    }
}
