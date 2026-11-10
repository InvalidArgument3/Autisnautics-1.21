package com.neep.neepmeat.neepasm.vm;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepbus.util.message.DataVariant;
import com.neep.neepmeat.plc.instruction.Argument;
import com.neep.neepmeat.plc.memory.MemoryEntry;
import com.neep.neepmeat.thord.token.ThordTokenView;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/// Implementations may be mutable
public interface ThordData
{
    ThordData NULL = ThordData.of(0);

    static ThordData getTrue() { return ThordData.of(-1); }
    static ThordData getFalse() { return ThordData.of(0); }

    static ThordData of(String value)
    {
        return new StringImpl(value);
    }

    static ThordData of(int value)
    {
        return new IntImpl(value);
    }

    static ThordData of(boolean value)
    {
        return value ? getTrue() : getFalse();
    }

    /// DataVariant and ThordData have different purposes, so it might not be helpful to unify them.
    static ThordData of(DataVariant dataVariant)
    {
        // Ewww
        if (dataVariant.isNumeric())
        {
            return new IntImpl(dataVariant.getAsInt());
        }
        else if (dataVariant instanceof DataVariant.StringDataVariant sdv && sdv.s().charAt(0) == '@')
        {
            var wt = dataVariant.wtValue();
            if (wt != null)
                return wt;
        }

        return new StringImpl(dataVariant.stringValue());
    }

    static ThordData read(ByteBuf bb)
    {
        byte code = bb.readByte();
        return DataType.fromCode(code).bufDecoder.apply(bb);
    }

    static void write(ThordData data, ByteBuf bb)
    {
        bb.writeByte(data.type().code);
        data.write(bb);
    }

//    static ThordData of(Thord2Token token)
//    {
//        return new TokenImpl(token);
//    }

    DataType type();

    String stringValue();
    int getAsInt() throws NeepASM.RuntimeException;

    default int intValue() {
        try {
            return getAsInt();
        } catch (NeepASM.RuntimeException exception) {
            throw new IllegalStateException(exception);
        }
    }

    default float floatValue() {
        return intValue();
    }
    boolean boolValue();
    int pointerValue() throws NeepASM.RuntimeException;
    default Argument worldTargetValue() throws NeepASM.RuntimeException
    {
        throw new NeepASM.RuntimeException("Cannot convert %s to world target".formatted(type().name()));
    }


    default boolean pointer() { return false; }
//    Thord2Token tokenValue();
//    Thord2Token asLiteral();

    /// ASSUME MUTABLE
    ThordData add(ThordData v2) throws NeepASM.RuntimeException;
    /// ASSUME MUTABLE
    ThordData sub(ThordData v2) throws NeepASM.RuntimeException;
    /// ASSUME MUTABLE
    ThordData mul(ThordData v1) throws NeepASM.RuntimeException;
    /// ASSUME MUTABLE
    ThordData div(ThordData v1) throws NeepASM.RuntimeException;

    boolean eq(ThordData x1) throws NeepASM.RuntimeException;
    boolean gt(ThordData x1) throws NeepASM.RuntimeException;
    boolean lt(ThordData x1) throws NeepASM.RuntimeException;
    default boolean gteq(ThordData x1) throws NeepASM.RuntimeException { return gt(x1) || eq(x1); }
    default boolean lteq(ThordData x1) throws NeepASM.RuntimeException { return lt(x1) || eq(x1); }

    ThordData copy();

    void write(ByteBuf buf);
    Tag asNbt();

    default DataVariant toDataVariant()
    {
        // TODO
        // ugh
        if (this instanceof IntImpl intImpl)
            return DataVariant.of(intImpl.value);
        else
            return DataVariant.of(stringValue());
    }

    default MemoryEntry toMemoryEntry()
    {
        // TODO
        // uuuuugh
        if (this instanceof IntImpl intImpl)
            return new MemoryEntry.IntegerEntry(intImpl.value);
        else return new MemoryEntry.StringEntry(stringValue());
    }

    /// Includes type.
    /// Use this instead {@link ThordData#asNbt}
    static Tag toNbt(ThordData data)
    {
        CompoundTag c = new CompoundTag();
        c.putByte("type", data.type().code);
        c.put("value", data.asNbt());
        return c;
    }

    static ThordData fromNbt(CompoundTag nbt)
    {
        Tag value = nbt.get("value");
        DataType type = DataType.fromCode(nbt.getByte("type"));

        // We can't throw an exception here as it could result in data loss or world corruption.
        if (value == null || value.getType() != type.expectedNbtType)
            return new StringImpl("ERROR");

        return type.nbtDecoder.apply(value);
    }

    /// Note that this class is MUTABLE and will mutate when maths operations are done.
    class IntImpl implements ThordData
    {
        protected int value;

        public IntImpl(int value)
        {
            this.value = value;
        }

        @Override
        public DataType type()
        {
            return DataType.INT;
        }

        @Override
        public String stringValue()
        {
            return Integer.toString(value);
        }

        @Override
        public int getAsInt()
        {
            return value;
        }

        @Override
        public boolean boolValue()
        {
            return value != 0;
        }

        @Override
        public int pointerValue()
        {
            return value;
        }

        @Override
        public ThordData add(ThordData v2) throws NeepASM.RuntimeException
        {
            value += v2.getAsInt();
            return this;
        }

        @Override
        public ThordData sub(ThordData v2) throws NeepASM.RuntimeException
        {
            value -= v2.getAsInt();
            return this;
        }

        @Override
        public ThordData mul(ThordData v1) throws NeepASM.RuntimeException
        {
            value *= v1.getAsInt();
            return this;
        }

        @Override
        public ThordData div(ThordData v1) throws NeepASM.RuntimeException
        {
            try
            {
                value /= v1.getAsInt();
            }
            catch (ArithmeticException e)
            {
                throw new NeepASM.RuntimeException("divide by zero");
            }

            return this;
        }

        @Override
        public boolean eq(ThordData x1) throws NeepASM.RuntimeException
        {
            return x1.type() == type() && value == x1.getAsInt();
        }

        @Override
        public boolean gt(ThordData x1) throws NeepASM.RuntimeException
        {
            return value > x1.getAsInt();
        }

        @Override
        public boolean lt(ThordData x1) throws NeepASM.RuntimeException
        {
            return value < x1.getAsInt();
        }

        @Override
        public ThordData copy()
        {
            return new IntImpl(value);
        }

        @Override
        public void write(ByteBuf buf)
        {
            buf.writeInt(value);
        }

        @Override
        public Tag asNbt()
        {
            return IntTag.valueOf(value);
        }

        public int value()
        {
            return value;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (IntImpl) obj;
            return this.value == that.value;
        }

        @Override
        public String toString()
        {
            return "Int[" + value + ']';
        }

        public static ThordData read(ByteBuf buf)
        {
            return new IntImpl(buf.readInt());
        }

        public static IntImpl readNbt(Tag nbtElement)
        {
            return new IntImpl(((IntTag) nbtElement).getAsInt());
        }
    }

    class PointerImpl extends IntImpl
    {
        public PointerImpl(int value)
        {
            super(value);
        }

        @Override
        public int pointerValue()
        {
            return value;
        }

        @Override
        public boolean pointer()
        {
            return true;
        }

        @Override
        public Tag asNbt()
        {
            return IntTag.valueOf(value);
        }

        @Override
        public String toString()
        {
            return "Pointer[" + value + ']';
        }

        public static PointerImpl read(ByteBuf buf)
        {
            return new PointerImpl(buf.readInt());
        }
    }

    record StringImpl(String value) implements ThordData
    {
        @Override
        public DataType type()
        {
            return DataType.STRING;
        }

        @Override
        public String stringValue()
        {
            return value;
        }

        @Override
        public int getAsInt() throws NeepASM.RuntimeException
        {
            try
            {
                return ThordTokenView.parseInteger(value);
            }
            catch (NeepASM.ParseException e)
            {
                throw new NeepASM.RuntimeException("cannot convert %s to %s".formatted(value, DataType.INT.name()));
            }
        }

        @Override
        public boolean boolValue()
        {
            return !value.isEmpty();
        }

        @Override
        public int pointerValue() throws NeepASM.RuntimeException
        {
            throw new NeepASM.RuntimeException("cannot convert %s to address".formatted(type().name()));
        }

        @Override
        public ThordData add(ThordData v2)
        {
            return new StringImpl(value + v2.stringValue());
        }

        @Override
        public ThordData sub(ThordData v2) throws NeepASM.RuntimeException
        {
            throw new NeepASM.RuntimeException("cannot subtract from a string");
        }

        @Override
        public ThordData mul(ThordData v1) throws NeepASM.RuntimeException
        {
            throw new NeepASM.RuntimeException("cannot multiply a string");
        }

        @Override
        public ThordData div(ThordData v1) throws NeepASM.RuntimeException
        {
            throw new NeepASM.RuntimeException("cannot multiply a string");
        }

        @Override
        public boolean eq(ThordData x1)
        {
            return value.equals(x1.stringValue());
        }

        @Override
        public boolean gt(ThordData x1) throws NeepASM.RuntimeException
        {
            throw new NeepASM.RuntimeException("operation invalid for string");
        }

        @Override
        public boolean lt(ThordData x1) throws NeepASM.RuntimeException
        {
            throw new NeepASM.RuntimeException("operation invalid for string");
        }

        @Override
        public ThordData copy()
        {
            return new StringImpl(value);
        }

        @Override
        public void write(ByteBuf buf)
        {
            buf.writeInt(value.length());
            buf.writeCharSequence(value, StandardCharsets.ISO_8859_1);
        }

        @Override
        public Tag asNbt()
        {
            return StringTag.valueOf(value);
        }

        public static StringImpl read(ByteBuf buf)
        {
            int length = buf.readInt();
            return new StringImpl(buf.readCharSequence(length, StandardCharsets.ISO_8859_1).toString());
        }

        public static ThordData readNbt(Tag nbtElement)
        {
            return new StringImpl(nbtElement.getAsString());
        }
    }

    interface BufDecoder extends Function<ByteBuf, ThordData> {}
    interface NbtDecoder extends Function<Tag, ThordData> {}

    enum DataType
    {
        INT(0, IntImpl::read, IntImpl::readNbt, IntTag.TYPE),
        ADDRESS(1, PointerImpl::read, PointerImpl::readNbt, IntTag.TYPE),
        STRING(2, StringImpl::read, StringImpl::readNbt, StringTag.TYPE),
        WORLD_TARGET(3, WorldTargetImpl::read, WorldTargetImpl::readNbt, CompoundTag.TYPE)
//        FLOAT,
        ;

        public final byte code;
        public final BufDecoder bufDecoder;
        public final NbtDecoder nbtDecoder;
        public final TagType<?> expectedNbtType;

        DataType(int code, BufDecoder bufDecoder, NbtDecoder nbtDecoder, TagType<?> expectedNbtType)
        {
            this.code = (byte) code;
            this.bufDecoder = bufDecoder;
            this.nbtDecoder = nbtDecoder;
            this.expectedNbtType = expectedNbtType;
        }

        public static DataType fromCode(byte code)
        {
            return switch (code)
            {
                case 0 -> INT;
                case 1 -> ADDRESS;
                case 2 -> STRING;
                case 3 -> WORLD_TARGET;
                default -> INT;
            };
        }
    }
}
