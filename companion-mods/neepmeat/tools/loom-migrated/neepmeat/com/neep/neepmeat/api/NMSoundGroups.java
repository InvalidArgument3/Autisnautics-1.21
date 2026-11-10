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

package com.neep.neepmeat.api;

import com.neep.neepmeat.init.NMSounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;

public class NMSoundGroups
{
    public static final SoundType METAL = new SoundType(1f, 1f, SoundEvents.NETHERITE_BLOCK_BREAK, SoundEvents.NETHERITE_BLOCK_STEP, SoundEvents.NETHERITE_BLOCK_PLACE, SoundEvents.NETHERITE_BLOCK_HIT, SoundEvents.NETHERITE_BLOCK_FALL);
    public static final SoundType MECHANICAL_MACHINE = new SoundType(1f, 1f, NMSounds.MECHANICAL_MACHINE_BREAK, SoundEvents.NETHERITE_BLOCK_STEP, NMSounds.MECHANICAL_MACHINE_PLACE, SoundEvents.NETHERITE_BLOCK_HIT, SoundEvents.NETHERITE_BLOCK_FALL);
    public static final SoundType TANK = new SoundType(1f, 1f, NMSounds.MECHANICAL_MACHINE_BREAK, SoundEvents.NETHERITE_BLOCK_STEP, NMSounds.TANK_PLACE, SoundEvents.NETHERITE_BLOCK_HIT, SoundEvents.NETHERITE_BLOCK_FALL);
    public static final SoundType FLESH_MACHINE = new SoundType(1f, 1f, NMSounds.MECHANICAL_MACHINE_BREAK, SoundEvents.NETHERITE_BLOCK_STEP, NMSounds.FLESH_MACHINE_PLACE, SoundEvents.NETHERITE_BLOCK_HIT, SoundEvents.NETHERITE_BLOCK_FALL);
    public static final SoundType TRANSDUCER = new SoundType(1f, 1f, NMSounds.MECHANICAL_MACHINE_BREAK, SoundEvents.NETHERITE_BLOCK_STEP, NMSounds.TRANSDUCER_PLACE, SoundEvents.NETHERITE_BLOCK_HIT, SoundEvents.NETHERITE_BLOCK_FALL);
    public static final SoundType VASCULAR_CONDUIT = new SoundType(1f, 1f, SoundEvents.NETHERITE_BLOCK_BREAK, SoundEvents.NETHERITE_BLOCK_STEP, SoundEvents.NETHERITE_BLOCK_PLACE, SoundEvents.NETHERITE_BLOCK_HIT, SoundEvents.NETHERITE_BLOCK_FALL);
}
