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

package com.neep.neepmeat.entity.skyworm;

import com.neep.neepmeat.entity.goal.Action;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.init.NMParticles;
import com.neep.neepmeat.init.NMSounds;
import javax.swing.*;
import net.minecraft.world.level.Level;

public class SkywormAction<T extends Action.Simple<T>> extends Action.Simple<T>
{
    protected final SkywormEntity mob;

    protected double targetY;

    public SkywormAction(SkywormEntity mob)
    {
        this.mob = mob;
    }

    public double getX() { return mob.getX(); }
    public double getY() { return mob.getY(); }
    public double getZ() { return mob.getZ(); }

    protected Level getWorld()
    {
        return mob.level();
    }
}
