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

package com.neep.neepmeat.machine.pylon;

import com.neep.meatlib.util.ClientComponent;
import com.neep.neepmeat.client.sound.PylonSoundInstance;
import com.neep.neepmeat.init.NMSounds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;

@Environment(value = EnvType.CLIENT)
public class PylonClientComponent implements ClientComponent
{
    private final PylonBlockEntity be;
    private final Minecraft client = Minecraft.getInstance();
    private final PylonSoundInstance mainSound;
    private final PylonSoundInstance runningSound;

    public PylonClientComponent(PylonBlockEntity be)
    {
        this.be = be;
        this.mainSound = new PylonSoundInstance(be, be.getBlockPos(), NMSounds.PYLON_START, SoundSource.BLOCKS);
        this.runningSound = new PylonSoundInstance(be, be.getBlockPos(), NMSounds.PYLON_ACTIVE, SoundSource.BLOCKS);
    }

    @Override
    public void clientTick()
    {
        if (!client.getSoundManager().isActive(mainSound))
        {
            client.getSoundManager().play(mainSound);
        }
        if (be.isRunning() && !client.getSoundManager().isActive(runningSound))
        {
            client.getSoundManager().play(runningSound);
        }
        if (!be.isRunning() && client.getSoundManager().isActive(runningSound))
        {
            client.getSoundManager().stop(runningSound);
        }
    }
}
