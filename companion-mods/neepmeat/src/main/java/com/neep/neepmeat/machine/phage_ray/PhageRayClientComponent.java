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

package com.neep.neepmeat.machine.phage_ray;

import com.neep.meatlib.client.event.KeyboardEvents;
import com.neep.meatlib.client.event.UseAttackCallback;
import com.neep.meatlib.util.ClientComponent;
import com.neep.neepmeat.init.NMSounds;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

@OnlyIn(Dist.CLIENT)
public class PhageRayClientComponent implements ClientComponent
{
    private static boolean prevUse;

    private final TrackingSoundInstance runningInstance;
    private final PhageRayEntity parent;

    public PhageRayClientComponent(PhageRayEntity parent)
    {
        runningInstance = new TrackingSoundInstance(
                NMSounds.PHAGE_RAY_RUNNING.get(), SoundSource.BLOCKS,
                2, 1,
                parent);
        this.parent = parent;
    }

    public static void init()
    {
        KeyboardEvents.POST_INPUT.register((window, key, scancode, action, modifiers) ->
        {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null)
                return false;

            if (client.player.getVehicle() instanceof PhageRayEntity phageRay)
            {
                if (client.options.keyUse.isDown())
                {
                    if (!prevUse)
                    {
                        phageRay.setPlayerTrigger(true);
                        sendPacket(client.player, true);
                        prevUse = true;
                    }
                }
                else
                {
                    if (prevUse)
                    {
                        phageRay.setPlayerTrigger(false);
                        sendPacket(client.player, false);
                        prevUse = false;
                    }
                }
            }

            return false;
        });

        UseAttackCallback.DO_USE.register((client, player) ->
        {
            if (player.getVehicle() instanceof PhageRayEntity phageRay)
            {
                return false;
            }
            return true;
        });
    }

    private static void sendPacket(Player player, boolean trigger)
    {
        PhageRayEntity.TRIGGER_C2S.emitter(player).accept(trigger);
    }

    @Override
    public void clientTick()
    {
        SoundManager manager = Minecraft.getInstance().getSoundManager();
//        if (parent.isRunning() && parent.trigger && parent.triggerTicks == 0)
//        {
//            manager.play(new EntityTrackingSoundInstance(NMSounds.PHAGE_RAY_CHARGE.get(), SoundCategory.BLOCKS, 16, 1, parent, 0));
//        }

        if (parent.isRunning() && parent.trigger())
        {
            if (!manager.isActive(runningInstance))
            {
                manager.play(runningInstance);
                runningInstance.start();
            }
        }
        else
        {
            if (manager.isActive(runningInstance))
            {
                manager.stop(runningInstance);
                runningInstance.end();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static class TrackingSoundInstance extends EntityBoundSoundInstance
    {
        private float targetPitch = 0;
        private float targetVolume = 0;

        public TrackingSoundInstance(SoundEvent sound, SoundSource category, float volume, float pitch, Entity entity)
        {
            super(sound, category, volume, pitch, entity, 0);
            this.looping = true;
            this.delay = 0;
        }

        void start()
        {
            targetPitch = 1;
            targetVolume = 1;
        }

        void end()
        {
            targetPitch = 0;
            targetVolume = 0;
        }

        @Override
        public void tick()
        {
            super.tick();
            pitch = Mth.lerp(0.5f, pitch, targetPitch);
            volume = Mth.lerp(0.1f, volume, targetVolume);
        }
    }
}
