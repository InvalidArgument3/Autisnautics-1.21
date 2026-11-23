/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.plc.instruction.block;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.block.entity.BuilderBlockEntity;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import com.neep.neepmeat.plc.instruction.PlcInstruction;
import com.neep.neepmeat.plc.robot.RobotMoveToAction;
import org.jetbrains.annotations.NotNull;

public class PlaceBlockInstruction implements PlcInstruction {
    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException {
        var target = plc.dataStack().last(0).worldTargetValue();
        if (plc.getActuator() instanceof BuilderBlockEntity) {
            plc.addRobotAction(new RobotMoveToAction(target.pos()), this::complete);
        } else {
            throw NeepASM.RuntimeException.notSupported(getOpcode().getParseName());
        }
    }

    private void complete(PLC plc) throws NeepASM.RuntimeException {
        var target = plc.dataStack().pop().worldTargetValue();
        boolean success = plc.controlActuator1(BuilderBlockEntity.class, builder -> builder.placeSelected(target.pos(), target.face()));
        plc.dataStack().push(success);
        plc.advanceCounter();
    }

    @Override
    public @NotNull InstructionProvider getOpcode() {
        return Instructions.BPLACE;
    }
}
