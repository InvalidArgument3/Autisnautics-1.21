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

import com.neep.meatlib.util.NbtSerialisable;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.meatlib.neepasm.NeepASM;
import java.util.List;
import net.minecraft.nbt.CompoundTag;

public class GroupedRobotAction implements RobotAction, NbtSerialisable
{
    protected final List<RobotAction> actions;
    protected int index = 0;
    private boolean start;

    public GroupedRobotAction(List<RobotAction> actions)
    {
        this.actions = actions;
    }

    public static GroupedRobotAction of(RobotAction... actions)
    {
        return new GroupedRobotAction(List.of(actions));
    }

    @Override
    public boolean finished(PLC plc)
    {
        return index >= actions.size();
    }

    @Override
    public void start(PLC plc)
    {
        start = true;

//        if (!justLoaded)
//        {
//            // The action must be reset to its initial previous state.
//            index = 0;
//        }
//        else
//        {
//            // If the action has been loaded from NBT, use the stored index.
//            justLoaded = false;
//        }
    }

    @Override
    public void tick(PLC plc) throws NeepASM.RuntimeException
    {
        if (index >= actions.size())
            return;

        var action = actions.get(index);

        if (start)
        {
            action.start(plc);
            start = false;
        }

        action.tick(plc);

        if (action.finished(plc))
        {
            action.end(plc);
            index++;
            start = true;
        }
    }

    @Override
    public void end(PLC plc)
    {
        index = 0;
        start = true;
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.putInt("index", index);
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        this.index = nbt.getInt("index");
    }

    public void setFinished(PLC plc)
    {
        index = actions.size();
    }
}
