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

package com.neep.neepmeat.neepasm.compiler.parser;

import com.neep.neepmeat.api.plc.instruction.CallInstruction;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.meatlib.util.NeepAsmTokenView;
import com.neep.neepmeat.neepasm.program.Label;
import com.neep.neepmeat.plc.SingletonInstructions;
import org.jetbrains.annotations.Nullable;

public class CallInstructionParser implements InstructionParser
{
    @Override
    public PlcParsedInstruction parse(NeepAsmTokenView view, NeepAsmParser parser, @Nullable String scope) throws NeepASM.ParseException
    {
        view.fastForward();
        String label = view.nextIdentifier();

        if (!label.isEmpty())
        {
            view.fastForward();
            if (!NeepAsmParser.isComment(view) && !view.lineEnded())
                throw new NeepASM.ParseException("unexpected token '" + view.nextBlob() + "'");

            return (world, source, program) ->
            {
                Label label1 = source.findLabel(label);
                if (label1 == null)
                    throw new NeepASM.CompilationException("label '" + label + "' does not exist");

                program.addBack(new CallInstruction(label1));
            };
        }
        else
        {
            return (world, labelLookup, program) -> program.addBack(SingletonInstructions.CALL);
        }
    }
}
