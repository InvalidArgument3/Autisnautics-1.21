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

package com.neep.neepmeat.client.item;

import com.neep.neepmeat.client.sound.ReciprocatingDrillSoundInstance;
import com.neep.neepmeat.init.NMItems;
import com.neep.neepmeat.init.NMSounds;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DrillChassisItemClient
{
    private static final ReciprocatingDrillSoundInstance SOUND = new ReciprocatingDrillSoundInstance(SoundSource.PLAYERS, RandomSource.create());

    public static void init()
    {
        ClientTickEvents.END_CLIENT_TICK.register(client ->
        {
            SoundManager manager = client.getSoundManager();
            Player player = client.player;
            if (player != null)
            {
                ItemStack mainStack = player.getMainHandItem();
                if (mainStack.is(NMItems.DRILL_CHASSIS))
                {
                    if (!manager.isActive(SOUND))
                    {
                        SOUND.setPlayer(player);
                        manager.play(SOUND);
                    }
                }
                else
                {
                    manager.stop(SOUND);
                }
            }

        });
    }
}
