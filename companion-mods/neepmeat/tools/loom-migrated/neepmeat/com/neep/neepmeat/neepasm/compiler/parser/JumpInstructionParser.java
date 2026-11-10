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

import com.neep.meatlib.util.NeepAsmTokenView;
import com.neep.neepmeat.api.plc.program.MutableProgram;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.LabelLookup;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.neepmeat.neepasm.compiler.ParsedSource;
import com.neep.neepmeat.neepasm.program.Label;
import com.neep.neepmeat.plc.instruction.Instruction;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import org.jetbrains.annotations.Nullable;

public class JumpInstructionParser implements InstructionParser
{
    private final Int2ObjectFunction<Instruction> constructor;

    public JumpInstructionParser(Int2ObjectFunction<Instruction> constructor)
    {
        this.constructor = constructor;
    }

    @Override
    public ParsedInstruction parse(NeepAsmTokenView view, NeepAsmParser parser, @Nullable String scope) throws NeepASM.ParseException
    {
        view.fastForward();

        String label = view.nextIdentifier();

        if (!label.isEmpty())
        {
            label = ParsedSource.mangleLabel(label, scope);

            view.fastForward();

            char c = view.peek();
            if (c == 'f')
            {
                view.next();
                return new ParsedJumpInstruction(constructor, label, Label.Seek.FORWARDS);
            }
            else if (c == 'b')
            {
                view.next();
                return new ParsedJumpInstruction(constructor, label, Label.Seek.BACKWARDS);
            }

            parser.assureLineEnd(view);

            return new ParsedJumpInstruction(constructor, label, Label.Seek.ABSOLUTE);
        }

        if (NeepAsmTokenView.isDigit(view.peek()))
        {
            int jump = view.nextInteger();

            parser.assureLineEnd(view);

            return new ParsedRelJumpInstruction(constructor, jump);
        }
        throw new NeepASM.ParseException("expected label or relative jump");
    }

    public static class ParsedJumpInstruction implements ParsedInstruction
    {
        private final Int2ObjectFunction<Instruction> constructor;
        private final String label;
        private final Label.Seek seek;

        public ParsedJumpInstruction(Int2ObjectFunction<Instruction> constructor, String label, Label.Seek seek)
        {
            this.constructor = constructor;
            this.label = label;
            this.seek = seek;
        }

        @Override
        public Instruction build(LabelLookup labelLookup, MutableProgram program) throws NeepASM.CompilationException
        {
            Label l = labelLookup.findLabel(label, program.size(), seek);
            if (l == null)
                throw new NeepASM.CompilationException("label '" + label + "' does not exist");

            return constructor.apply(l.index() - program.size());
        }
    }

    public static class ParsedRelJumpInstruction implements ParsedInstruction
    {
        private final Int2ObjectFunction<Instruction> constructor;
        private final int jump;

        public ParsedRelJumpInstruction(Int2ObjectFunction<Instruction> constructor, int jump)
        {
            this.constructor = constructor;
            this.jump = jump;
        }

        @Override
        public Instruction build(LabelLookup labelLookup, MutableProgram program) throws NeepASM.CompilationException
        {
            return constructor.apply(jump);
        }
    }
}
