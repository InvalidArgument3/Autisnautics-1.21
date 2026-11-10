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

import com.neep.neepmeat.init.NMSounds;
import com.neep.neepmeat.item.DrillChassisItem;
import com.neep.neepmeat.meatgun.module.MiningMeatgunModule;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ReciprocatingDrillSoundInstance extends AbstractTickableSoundInstance
{
    @Nullable protected Player player;

    public ReciprocatingDrillSoundInstance(SoundSource soundCategory, RandomSource random)
    {
        super(NMSounds.DRILL_RUNNING, soundCategory, random);
        this.looping = true;
        this.delay = 0;
        this.volume = 0f;
        this.pitch = 1;
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
        if (this.player != null && this.player.isRemoved())
        {
            this.stop();
            return;
        }

        if (player != null)
        {
            boolean canRun = false;
            ItemStack stack = player.getMainHandItem();
            Item item = stack.getItem();
            var head = item instanceof DrillChassisItem chassisItem ? chassisItem.getHead(stack) : null;

            if (head instanceof MiningMeatgunModule mining)
                canRun = mining.canRun();

            if (canRun && DrillChassisItem.Client.using(stack, true))
            {
                this.volume = Mth.lerp(0.4f, this.volume, 0.2f);
                this.pitch = Mth.lerp(0.2f, this.pitch, 1);
            }
            else
            {
                this.volume = Mth.lerp(0.3f, this.volume, 0);
                this.pitch = Mth.lerp(0.2f, this.pitch, 0);
            }
        }
        else
        {
            this.volume = 0;
        }


        this.x = this.player.getX();
        this.y = this.player.getY();
        this.z = this.player.getZ();
    }

    public void setPlayer(Player player)
    {
        this.player = player;
    }
}
