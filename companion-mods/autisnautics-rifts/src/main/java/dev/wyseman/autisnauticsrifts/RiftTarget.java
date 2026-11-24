package dev.wyseman.autisnauticsrifts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record RiftTarget(String dimension, int targetX, int targetY, int targetZ, boolean found, boolean ceilingMode, String boundary) {
    public static final RiftTarget EMPTY = new RiftTarget("", 0, 0, 0, false, false, "");

    public static final Codec<RiftTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("dimension").forGetter(RiftTarget::dimension),
        Codec.INT.fieldOf("target_x").forGetter(RiftTarget::targetX),
        Codec.INT.fieldOf("target_y").forGetter(RiftTarget::targetY),
        Codec.INT.fieldOf("target_z").forGetter(RiftTarget::targetZ),
        Codec.BOOL.fieldOf("found").forGetter(RiftTarget::found),
        Codec.BOOL.fieldOf("ceiling_mode").forGetter(RiftTarget::ceilingMode),
        Codec.STRING.fieldOf("boundary").forGetter(RiftTarget::boundary)
    ).apply(instance, RiftTarget::new));

    public static final StreamCodec<ByteBuf, RiftTarget> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public RiftTarget decode(ByteBuf buf) {
            String dim = ByteBufCodecs.STRING_UTF8.decode(buf);
            int x = ByteBufCodecs.VAR_INT.decode(buf);
            int y = ByteBufCodecs.VAR_INT.decode(buf);
            int z = ByteBufCodecs.VAR_INT.decode(buf);
            boolean found = ByteBufCodecs.BOOL.decode(buf);
            boolean ceilingMode = ByteBufCodecs.BOOL.decode(buf);
            String boundary = ByteBufCodecs.STRING_UTF8.decode(buf);
            return new RiftTarget(dim, x, y, z, found, ceilingMode, boundary);
        }

        @Override
        public void encode(ByteBuf buf, RiftTarget val) {
            ByteBufCodecs.STRING_UTF8.encode(buf, val.dimension());
            ByteBufCodecs.VAR_INT.encode(buf, val.targetX());
            ByteBufCodecs.VAR_INT.encode(buf, val.targetY());
            ByteBufCodecs.VAR_INT.encode(buf, val.targetZ());
            ByteBufCodecs.BOOL.encode(buf, val.found());
            ByteBufCodecs.BOOL.encode(buf, val.ceilingMode());
            ByteBufCodecs.STRING_UTF8.encode(buf, val.boundary());
        }
    };

    public BlockPos blockPos() {
        return new BlockPos(targetX, targetY, targetZ);
    }

    public RiftTarget withMode(boolean newCeilingMode) {
        return new RiftTarget(dimension, targetX, targetY, targetZ, false, newCeilingMode, boundary);
    }
}
