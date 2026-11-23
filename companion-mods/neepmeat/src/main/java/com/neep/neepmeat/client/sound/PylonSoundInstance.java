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

package com.neep.neepmeat.client.sound;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.machine.pylon.PylonBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

@OnlyIn(Dist.CLIENT)
public class PylonSoundInstance extends AbstractSoundInstance implements TickableSoundInstance
{
    protected final ResourceLocation soundId;
    private boolean done;
    private final PylonBlockEntity blockEntity;
    private final Minecraft client = Minecraft.getInstance();

    public PylonSoundInstance(PylonBlockEntity be, BlockPos pos, SoundEvent sound, SoundSource category)
    {
        super(sound, category, RandomSource.create());
        this.blockEntity = be;

        this.looping = true;
        this.delay = 0;
        this.volume = 1.0f;
        this.x = pos.getX() + 0.5;
        this.y = pos.getY() + 0.5;
        this.z = pos.getZ() + 0.5;
        this.soundId = sound.getLocation();
//        this.runningId = sound.getId();
    }

//    public boolean isRunning()
//    {
//        return blockEntity.getSpeed() >= PylonBlockEntity.RUNNING_SPEED;
//    }

    @Override
    public ResourceLocation getLocation()
    {
        return this.soundId;
    }

    @Override
    public Sound getSound()
    {
        return sound;
    }

    @Override
    public boolean isStopped()
    {
        return done;
    }

    @Override
    public float getVolume()
    {
        return this.volume * getSound().getVolume().sample(random);
    }

    @Override
    public float getPitch()
    {
        return this.pitch * getSound().getPitch().sample(random);
    }

    @Override
    public boolean canPlaySound()
    {
        return true;
    }

    @Override
    public boolean canStartSilent()
    {
        return true;
    }

    @Override
    public void tick()
    {
        float runningSpeed = NeepMeat.CONFIG.pylon.runningSpeed.getRealValue();
        this.pitch = Mth.lerp(0.2f, this.pitch, Mth.lerpInt(Mth.clamp(blockEntity.getSpeed(), 0, runningSpeed) / runningSpeed, 0, 1));
        this.volume = pitch;

        if (blockEntity.isUnstable())
        {
            this.pitch += 0.6f * Mth.sin(client.getFrameTimeNs());
        }

        if (this.blockEntity.isRemoved())
        {
            this.done = true;
            return;
        }

    }
}
