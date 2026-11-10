package com.neep.meatweapons.client.sound;

import com.neep.neepmeat.init.NMSounds;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public class LongBoiSoundInstance extends EntityBoundSoundInstance
{
    private int age = 0;
    public LongBoiSoundInstance(SoundSource category, Entity entity)
    {
        super(NMSounds.LONG_BOI_CHARGE, category, 1, 1, entity, 0);
    }

    @Override
    public void tick()
    {
        super.tick();

        age = Math.min(130, age + 1);
    }

    @Override
    public Sound getSound()
    {
        return super.getSound();
    }

    @Override
    public float getPitch()
    {
        return pitch;
//        return  (age / 130f) * this.sound.getPitch().get(this.random);
    }
}
