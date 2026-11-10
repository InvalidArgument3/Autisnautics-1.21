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

package com.neep.neepmeat.api.plc.robot;

import com.neep.neepmeat.api.plc.PLC;
import com.neep.meatlib.neepasm.NeepASM;
import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

public class SoundAction implements AtomicAction
{
    private final SoundEvent soundEvent;
    private Supplier<Level> world;

    public SoundAction(Supplier<Level> world, SoundEvent soundEvent)
    {
        this.world = world;
        this.soundEvent = soundEvent;
    }

    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException
    {
        var robot = plc.getActuator();
        world.get().playSeededSound(null, robot.getX(), robot.getY(), robot.getZ(), soundEvent, SoundSource.BLOCKS, 1, 1, 1);
    }
}
