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

package com.neep.neepmeat.machine.advanced_integrator;

import com.neep.meatlib.util.ClientComponent;
import com.neep.neepmeat.client.sound.BlockSoundInstance;
import com.neep.neepmeat.init.NMSounds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;

@Environment(EnvType.CLIENT)
public class AdvancedIntegratorClientComponent implements ClientComponent
{
    private final Minecraft client = Minecraft.getInstance();
    private final BlockSoundInstance sound;

    public AdvancedIntegratorClientComponent(AdvancedIntegratorBlockEntity be)
    {
        this.sound = new BlockSoundInstance(NMSounds.ADVANCED_INTEGRATOR_AMBIENT, SoundSource.BLOCKS, be.getBlockPos().above(3));
    }

    @Override
    public void clientTick()
    {
        if (!client.getSoundManager().isActive(sound))
        {
            client.getSoundManager().play(sound);
        }
    }
}
