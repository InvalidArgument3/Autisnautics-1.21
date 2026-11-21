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

package com.neep.meatweapons.client.sound;

import com.neep.meatweapons.item.AssaultDrillItem;
import com.neep.neepmeat.init.NMSounds;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

@OnlyIn(Dist.CLIENT)
public class DrillSoundInstance extends AbstractTickableSoundInstance
{
    protected Player player;

    public DrillSoundInstance()
    {
        super(NMSounds.DRILL_RUNNING.get(), SoundSource.NEUTRAL, RandomSource.create());

        this.looping = true;
        this.delay = 0;
        this.volume = 0.0f;
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
        if (this.player.isRemoved())
        {
            this.stop();
            return;
        }

        this.x = this.player.getX();
        this.y = this.player.getY();
        this.z = this.player.getZ();

        float targetVolume;
        float targetPitch;

        if (AssaultDrillItem.using(player.getMainHandItem()))
        {
            targetVolume = 0.4f;
            targetPitch = (float) (1 + random.triangle(0, 0.2));
        }
        else
        {
            targetVolume = 0;
            targetPitch = 0;
        }

        this.volume = Mth.lerp(0.2f, this.volume, targetVolume);
        this.pitch = Mth.lerp(0.2f, this.pitch, targetPitch);
    }

    public void setPlayer(Player player)
    {
        this.player = player;
    }
}
