package com.neep.meatweapons.client.particle;

import com.neep.meatweapons.particle.MWParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class LongBoiHitParticle extends RisingParticle
{
    protected LongBoiHitParticle(ClientLevel clientWorld, double d, double e, double f, double g, double h, double i)
    {
        super(clientWorld, d, e, f, g, h, i);
    }

    @Override
    public void tick()
    {
        super.tick();
        yd -= 0.1f;

        if (random.nextBoolean())
        {
//            this.world.addParticle(MWParticles.PLASMA_PARTICLE, this.x, this.y, this.z, this.velocityX, this.velocityY, this.velocityZ);
            this.level.addParticle(MWParticles.BLOODTHROWER_SPLASH, this.x, this.y, this.z, 0, 0, 0);
        }
    }

    @Override
    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider)
        {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType parameters, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ)
        {
            var particle = new LongBoiHitParticle(world, x, y, z, velocityX, velocityY, velocityZ);
            particle.pickSprite(spriteProvider);
            return particle;
        }
    }
}
