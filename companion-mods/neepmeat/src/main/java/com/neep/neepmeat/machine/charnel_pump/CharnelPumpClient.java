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

package com.neep.neepmeat.machine.charnel_pump;

import com.neep.meatlib.util.ClientComponent;
import com.neep.neepmeat.client.sound.BlockSoundInstance;
import com.neep.neepmeat.client.sound.LoopingSoundInstance;
import com.neep.neepmeat.init.NMSounds;
import com.neep.neepmeat.machine.live_machine.process.CharnelPumpProcess;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

@OnlyIn(Dist.CLIENT)
public class CharnelPumpClient implements ClientComponent
{
    private final CharnelPumpBlockEntity be;
    private final SoundManager soundManager;
    private final SoundInstance upSound;
    private final SoundInstance topSound;
    private final SoundInstance downSound;
    private final SoundInstance glugSound;
    private final SoundInstance idleSound;

    public CharnelPumpClient(CharnelPumpBlockEntity be)
    {
        this.be = be;
        this.soundManager = Minecraft.getInstance().getSoundManager();

        this.upSound = new BlockSoundInstance(NMSounds.CHARNEL_PUMP_UP.get(), SoundSource.BLOCKS, be.getBlockPos().above(3));
        this.topSound = new BlockSoundInstance(NMSounds.CHARNEL_PUMP_TOP.get(), SoundSource.BLOCKS, be.getBlockPos().above(3));
        this.downSound = new BlockSoundInstance(NMSounds.CHARNEL_PUMP_DOWN.get(), SoundSource.BLOCKS, be.getBlockPos().above(3));
        this.glugSound = new BlockSoundInstance(NMSounds.CHARNEL_PUMP_GLUG.get(), SoundSource.BLOCKS, be.getBlockPos().above(3));
        this.idleSound = new LoopingSoundInstance(NMSounds.CHARNEL_PUMP_IDLE.get(), SoundSource.BLOCKS, 1, 1, RandomSource.create(), be.getBlockPos().above(3));
    }

    @Override
    public void clientTick()
    {
        float increment = be.progressIncrement();

        if (be.getLevel().getGameTime() % 4 == 0)
        {
            if (be.hasAir && !CharnelPumpProcess.canRun(increment, be))
            {
                if (!soundManager.isActive(idleSound))
                    soundManager.play(idleSound);
            }
            else
            {
                soundManager.stop(idleSound);
            }
        }

        // Fractional animation ticks allows the animation to run at different speeds.
        // Unfortunately, executing events at certain ticks now requires more mathematical jank.
        if (matches(be.animationTicks, 100, increment))
        {
            soundManager.stop(glugSound);
            soundManager.stop(upSound);
        }
        else if (matches(be.animationTicks, 99, increment))
        {
            soundManager.play(upSound);
        }
        else if (matches(be.animationTicks, 45, increment))
        {
            soundManager.play(topSound);
        }
        else if (matches(be.animationTicks, 40, increment))
        {
            soundManager.stop(upSound);

            soundManager.play(be.hasFluidCurrentCycle ? glugSound : downSound);
        }
    }

    private static boolean matches(float ticks, int value, float increment)
    {
        return Math.round(ticks) == value && Math.abs(ticks - value) < Math.min(1, increment);
    }
}
