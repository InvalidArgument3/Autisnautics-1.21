package com.neep.meatweapons.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.neep.meatlib.client.AnimationTicks;
import com.neep.meatlib.graphics.GraphicsEffect;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class BoneStealerGraphicsEffect implements GraphicsEffect
{
    // Jank
    public static FabricSpriteProvider PROVIDER = null;

    private final Level world;
    private final int maxAge;
    @Nullable
    private final Entity sourceEntity;
    @Nullable
    private final Entity destEntity;
    private int age = 0;
    private Vec3 fallbackSource;
    private final RandomSource random;
    private final float phaseOffset;

    public BoneStealerGraphicsEffect(Level world, Vec3 start, Vec3 end, Vec3 extra, float scale, int maxAge, FriendlyByteBuf buf)
    {
        this.sourceEntity = world.getEntity(buf.readVarInt()); // Might be null
        this.destEntity = world.getEntity(buf.readVarInt());
        this.fallbackSource = start;

        this.world = world;
        this.maxAge = destEntity != null ? maxAge : 0; // Instantly remove if the destination isn't available

        random = RandomSource.create(world.getGameTime());
        // For some reason the Random created above seems to give the exact same values of the previous one despite the different seed
        phaseOffset = world.getRandom().nextFloat() * Mth.TWO_PI;
    }

    @Override
    public void render(Camera camera, PoseStack matrices, MultiBufferSource consumers, float tickDelta)
    {
//        if (destEntity == null)
//            return;
//
//        Vec3d start = sourceEntity != null ? sourceEntity.getLerpedPos(tickDelta).add(0, sourceEntity.getHeight() / 2, 0)
//                : fallbackSource;
//
//        var end = destEntity.getLerpedPos(tickDelta).add(0, destEntity.getHeight() / 2, 0);
//
//        float radius = 1f;
//        float t = (age + tickDelta) / (float) maxAge;
//        t %= 1.0f;
//        float w = 2 * MathHelper.PI * 4;
//
//        var camPos = camera.getPos();
//        float offset = 1.5f + radius; // offset from origin at which the main beam effect starts
//        Vec3d ray = end.subtract(start);
//
//        float length = (float) ray.length();
//        float pitch = (float) Math.atan2(ray.getX(), ray.getZ());
//        float yaw = (float) Math.asin(-ray.getY() / ray.length());
//
//        float x1 = radius * MathHelper.sin(t * w);
//        float z1 = radius * MathHelper.cos(t * w);
//        float y1 = length * t;
//
//        matrices.push();
//        matrices.translate(start.x - camPos.x, start.y - camPos.y, start.z - camPos.z);
//        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(pitch));
//        matrices.multiply(RotationAxis.POSITIVE_X.rotation(yaw + MathHelper.HALF_PI));
//        var mat = new Matrix4f(matrices.peek().getPositionMatrix());

//        var v = new Vector4f(x1, y1, z1, 1);
//        mat.transform(v); // Move it to world space relative to the camera

//        VertexConsumer consumer = consumers.getBuffer(RenderLayer.getLightning());
//        billboard(consumer, camera, v.x, v.y, v.z);
//        matrices.pop();
    }

    public void billboard(VertexConsumer vertexConsumer, Camera camera, float rx, float ry, float rz)
    {
        Vector3f[] vector3fs = new Vector3f[]{
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F)
        };

        float i = 0.1f;

        for (int j = 0; j < 4; j++)
        {
            Vector3f vector3f = vector3fs[j];
            vector3f.mul(i);
            vector3f.add(rx, ry, rz);
        }

//        float k = this.getMinU();
//        float l = this.getMaxU();
//        float m = this.getMinV();
//        float n = this.getMaxV();

        float r = 1;
        float g = 1;
        float b = 1;
        float a = 1;

//        int o = this.getBrightness(tickDelta);
        vertexConsumer.vertex(vector3fs[0].x(), vector3fs[0].y(), vector3fs[0].z())
//                .texture(l, n)
                .color(r, g, b, a)
//                .light(o)
                .endVertex();
        vertexConsumer.vertex(vector3fs[1].x(), vector3fs[1].y(), vector3fs[1].z())
//                .texture(l, m)
                .color(r, g, b, a)
//                .light(o)
                .endVertex();
        vertexConsumer.vertex(vector3fs[2].x(), vector3fs[2].y(), vector3fs[2].z())
//                .texture(k, m)
                .color(r, g, b, a)
//                .light(o)
                .endVertex();
        vertexConsumer.vertex(vector3fs[3].x(), vector3fs[3].y(), vector3fs[3].z())
//                .texture(k, n)
                .color(r, g, b, a)
//                .light(o)
                .endVertex();
    }

    @Override
    public void tick()
    {
        var client = Minecraft.getInstance();
        if (world.getGameTime() % 2 == 0)
        {
            Vec3 start = sourceEntity != null ? sourceEntity.position().add(0, sourceEntity.getBbHeight() / 2, 0)
                    : fallbackSource;

            Minecraft.getInstance().particleEngine.add(new Particle(client.level, start,
                    () ->
                            destEntity != null ? destEntity.position().add(0, destEntity.getBbHeight() / 2, 0)
                                    : null,
                    (float) random.triangle(0.25, 0.0),
                    (float) random.triangle(1.2, 0.2),
                    (int) random.triangle(20, 15),
                    PROVIDER.get(random)));
        }

        age++;
    }

    @Override
    public boolean isRemoved()
    {
        return age > maxAge;
//        return false;
    }

    @Override
    public Level getWorld()
    {
        return world;
    }

    private class Particle extends SingleQuadParticle
    {
        private final Vec3 start;
        private final Supplier<Vec3> end;
        private final float radius;
        private final float freq;
        private final TextureAtlasSprite sprite;

        protected Particle(ClientLevel clientWorld, Vec3 start, Supplier<Vec3> end, float radius, float freq, int maxAge, TextureAtlasSprite sprite)
        {
            super(clientWorld, start.x, start.y, start.z);
            this.start = start;
            this.end = end;
            this.radius = radius;
            this.freq = freq;
            this.sprite = sprite;

            this.lifetime = maxAge;
            this.quadSize = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;
        }

        @Override
        public void tick()
        {
            float t = (this.age) / (float) lifetime;
            float w = 2 * Mth.PI * freq;

            var end1 = end.get();
            if (end1 != null)
            {
                var ray = end1.subtract(start).toVector3f();

                // Radius is 0 at start and end, and is 8x at the middle
//                float r = radius * (1 + MathHelper.sin(t * 2 * MathHelper.PI * 2));
                float r = radius * (t >= 0.5 ? (1 - t) : t) * 8;

                // Add a constantly increasing phase offset to make it swirlier
                // Add the per-effect offset so that concurrent effects don't use the same path
                float phase = (float) (t * w + AnimationTicks.getRenderTime() / 8) + phaseOffset;

                // Moving upwards and rotating about Y
                float x1 = r * Mth.sin(phase);
                float z1 = r * Mth.cos(phase);
                float y1 = ray.length() * t;

                // Rotate local coordinates so that +Y is facing end
                float pitch = (float) Math.atan2(ray.x, ray.z);
                float yaw = (float) Math.asin(-ray.y / ray.length());
                var q = Axis.YP.rotation(pitch).mul(Axis.XP.rotation(yaw + Mth.HALF_PI));
//            var q = new Quaternionf().lookAlong(ray, new Vector3f(0, 1, 0));

                Vector3f v = new Vector3f(x1, y1, z1);
                v.rotate(q);
                v.add((float) start.x, (float) start.y, (float) start.z);

                // Tick first to set previous position
                this.oRoll = this.roll;
                super.tick();

                this.roll = t * Mth.TWO_PI + phaseOffset;
                this.x = v.x;
                this.y = v.y;
                this.z = v.z;
            }
            else
            {
                remove();
            }
        }

        @Override
        protected float getU0()
        {
            return sprite.getU0();
        }

        @Override
        protected float getU1()
        {
            return sprite.getU1();
        }

        @Override
        protected float getV0()
        {
            return sprite.getV0();
        }

        @Override
        protected float getV1()
        {
            return sprite.getV1();
        }

        @Override
        public ParticleRenderType getRenderType()
        {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }
    }
}