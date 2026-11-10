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

import com.neep.neepspeak.PhonemeList;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import java.util.concurrent.CompletableFuture;

@OnlyIn(Dist.CLIENT)
public class NEEPSpeakSoundInstance extends AbstractSoundInstance
{
    private final NEEPSpeakAudioStream stream;

    public NEEPSpeakSoundInstance(PhonemeList phonemes, Vec3 pos, boolean attenuate)
    {
        super(ResourceLocation.fromNamespaceAndPath("neepmeat", "neepspeak"), SoundSource.MASTER, SoundInstance.createUnseededRandom());
        stream = new NEEPSpeakAudioStream(phonemes);
        this.x = pos.x + 0.5;
        this.y = pos.y + 0.5;
        this.z = pos.z + 0.5;
        attenuation = attenuate ? Attenuation.LINEAR : Attenuation.NONE;
    }

    public NEEPSpeakSoundInstance(PhonemeList phonemeList, BlockPos pos)
    {
        this(phonemeList, Vec3.atCenterOf(pos), true);
    }

    @Override
    public Attenuation getAttenuation()
    {
        return Attenuation.LINEAR;
    }

    public CompletableFuture<AudioStream> getAudioStream(SoundBufferLibrary loader, ResourceLocation id, boolean repeatInstantly)
    {
        return CompletableFuture.completedFuture(stream);
    }

    @Override
    public boolean canPlaySound()
    {
        return !stream.isDone();
    }
}
