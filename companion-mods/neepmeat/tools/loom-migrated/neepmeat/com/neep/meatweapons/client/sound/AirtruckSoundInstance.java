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

import com.neep.meatweapons.entity.AirtruckEntity;
import com.neep.neepmeat.init.NMSounds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

@Environment(value= EnvType.CLIENT)
public class AirtruckSoundInstance extends AbstractTickableSoundInstance
{
    protected final AirtruckEntity entity;
    private float distance = 0.0f;
    public float maxVol = 1;

    public AirtruckSoundInstance(AirtruckEntity entity)
    {
        super(NMSounds.AIRTRUCK_RUNNING, SoundSource.NEUTRAL, RandomSource.create());
//        super(SoundEvents.ENTITY_MINECART_RIDING, SoundCategory.NEUTRAL);
        this.entity = entity;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0f;
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
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
        if (this.entity.isRemoved())
        {
            this.stop();
            return;
        }

        this.x = this.entity.getX();
        this.y = this.entity.getY();
        this.z = this.entity.getZ();
        float f = (float) this.entity.getDeltaMovement().horizontalDistance();

        if (f >= 0.01f)
        {
            this.distance = Mth.clamp(this.distance + 0.0025f, 0.0f, 1.0f);
            this.volume = Mth.lerp(Mth.clamp(f, 0.0f, 1f), 0.0f, 3f);
            this.pitch = Mth.lerp(Mth.clamp(f, 0.0f, 0.5f), 0.7f, 1.4f);
        } else
        {
            this.distance = 0.0f;
            this.volume = 0.0f;
        }

    }

    public static void initEvent()
    {
        ClientEntityEvents.ENTITY_LOAD.register((entity1, world) ->
        {
            // If Mojang is allowed to get away with the abomination that is ClientPlayNetworkHandler then I can do this.
            if (entity1 instanceof AirtruckEntity airtruck)
            {
                Minecraft.getInstance().getSoundManager().play(new AirtruckSoundInstance(airtruck));
            }
        });
    }
}
