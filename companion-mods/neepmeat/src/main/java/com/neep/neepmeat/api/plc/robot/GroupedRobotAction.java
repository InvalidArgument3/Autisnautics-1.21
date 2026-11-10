/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.api.plc.robot;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.meatlib.util.NbtSerialisable;
import com.neep.neepmeat.api.plc.PLC;
import net.minecraft.nbt.CompoundTag;

import java.util.List;

public class GroupedRobotAction implements RobotAction, NbtSerialisable {
    protected final List<RobotAction> actions;
    protected int index;
    private boolean start;

    public GroupedRobotAction(List<RobotAction> actions) {
        this.actions = actions;
    }

    public static GroupedRobotAction of(RobotAction... actions) {
        return new GroupedRobotAction(List.of(actions));
    }

    @Override
    public boolean finished(PLC plc) {
        return index >= actions.size();
    }

    @Override
    public void start(PLC plc) {
        start = true;
    }

    @Override
    public void tick(PLC plc) throws NeepASM.RuntimeException {
        if (index >= actions.size()) {
            return;
        }
        RobotAction action = actions.get(index);
        if (start) {
            action.start(plc);
            start = false;
        }
        action.tick(plc);
        if (action.finished(plc)) {
            action.end(plc);
            index++;
            start = true;
        }
    }

    @Override
    public void end(PLC plc) {
        index = 0;
        start = true;
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt) {
        nbt.putInt("index", index);
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        this.index = nbt.getInt("index");
    }

    public void setFinished(PLC plc) {
        index = actions.size();
    }
}
