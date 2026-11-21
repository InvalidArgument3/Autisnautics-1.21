//package com.neep.meatweapons.particle;
//
//import com.mojang.brigadier.StringReader;
//import com.mojang.brigadier.exceptions.CommandSyntaxException;
//import com.mojang.serialization.Codec;
//import com.mojang.serialization.codecs.RecordCodecBuilder;
//import net.minecraft.network.PacketByteBuf;
//import net.minecraft.particle.ParticleEffect;
//import net.minecraft.particle.ParticleType;
//import net.minecraft.registry.Registries;
//
//public class BloodthrowerParticleType extends ParticleType<BloodthrowerParticleType.BloodthrowerParticleEffect>
//{
//    public static Codec<BloodthrowerParticleEffect> createCodec(ParticleType<BloodthrowerParticleEffect> type)
//    {
//        return RecordCodecBuilder.create(
//                instance -> instance.group(
//                                Codec.DOUBLE.fieldOf("dx").forGetter(e -> e.dx),
//                                Codec.DOUBLE.fieldOf("dy").forGetter(e -> e.dy),
//                                Codec.DOUBLE.fieldOf("dz").forGetter(e -> e.dz),
//                                Codec.FLOAT.fieldOf("scale").forGetter(e -> e.scale),
//                                Codec.INT.fieldOf("max_age").forGetter(e -> e.maxAge))
//                        .apply(instance, (aDouble, aDouble2, aDouble3, aFloat, maxAge) ->
//                                new BloodthrowerParticleEffect(type, aDouble, aDouble2, aDouble3, aFloat, maxAge))
//        );
//    }
//
//    private final Codec<BloodthrowerParticleEffect> codec;
//
//    public static final ParticleEffect.Factory<BloodthrowerParticleEffect> PARAMETER_FACTORY = new ParticleEffect.Factory<>()
//    {
//        @Override
//        public BloodthrowerParticleEffect read(ParticleType<BloodthrowerParticleEffect> type, StringReader reader) throws CommandSyntaxException
//        {
//            reader.expect(' ');
//            double dx = 0;
//            double dy = 0;
//            double dz = 0;
//            float scale = 1;
//            return new BloodthrowerParticleEffect(MWParticles.BLOODTHROWER_SPLASH, dx, dy, dz, scale, 1);
//        }
//
//        @Override
//        public BloodthrowerParticleEffect read(ParticleType<BloodthrowerParticleEffect> type, PacketByteBuf buf)
//        {
//            ParticleType<BloodthrowerParticleEffect> type1 = (ParticleType<BloodthrowerParticleEffect>) buf.readRegistryValue(Registries.PARTICLE_TYPE);
//            return new BloodthrowerParticleEffect(type1, buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat(), buf.readInt());
//        }
//    };
//
//    protected BloodthrowerParticleType(boolean alwaysShow, Codec<BloodthrowerParticleEffect> codec)
//    {
//        super(alwaysShow, PARAMETER_FACTORY);
//        this.codec = codec;
//    }
//
////    public static BloodthrowerParticleEffect create()
////    {
////
////    }
//
//    @Override
//    public Codec<BloodthrowerParticleEffect> getCodec()
//    {
//        return codec;
//    }
//
//    public record BloodthrowerParticleEffect(ParticleType<BloodthrowerParticleEffect> type,
//                                             double dx, double dy, double dz, float scale, int maxAge) implements ParticleEffect
//    {
//        @Override
//        public ParticleType<BloodthrowerParticleEffect> getType()
//        {
//            return type();
//        }
//
//        @Override
//        public void write(PacketByteBuf buf)
//        {
//            buf.writeDouble(dx());
//            buf.writeDouble(dy());
//            buf.writeDouble(dz());
//            buf.writeFloat(scale());
//            buf.writeInt(maxAge());
//        }
//
//        @Override
//        public String asString()
//        {
//            return Registries.PARTICLE_TYPE.getId(type()).toString();
//        }
//    }
//}
