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

import com.google.common.collect.Lists;
import com.neep.neepmeat.api.plc.instruction.SimpleInstructionProvider;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.DefaultParsedInstruction;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.meatlib.util.NeepAsmTokenView;
import com.neep.neepmeat.neepasm.program.KeyValue;
import com.neep.neepmeat.plc.instruction.Argument;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DefaultInstructionParser implements InstructionParser
{
    private final InstructionProvider provider;

    public DefaultInstructionParser(InstructionProvider provider)
    {
        this.provider = provider;
    }

    @Override
    public PlcParsedInstruction parse(NeepAsmTokenView view, NeepAsmParser parser, @Nullable String scope) throws NeepASM.ParseException
    {
        if (!(provider instanceof SimpleInstructionProvider))
            throw new NeepASM.ParseException("default parser does not support arbitrary arguments");

        List<Argument> arguments = Lists.newArrayList();
        List<KeyValue> kvs = Lists.newArrayList();

        boolean read = true;
        while (read)
        {
            Argument a;
            KeyValue kv;
            view.fastForward();
            if (NeepAsmParser.isComment(view) || view.lineEnded())
            {
                read = false;
            }
            else if ((a = parser.parseWorldTarget(view)) != null)
            {
                arguments.add(a);
            }
            else if ((kv = parser.parseKV(view)) != null)
            {
                kvs.add(kv);
            }
            else
            {
                throw new NeepASM.ParseException("unexpected token", view.peek());
            }

            if (arguments.size() > ((SimpleInstructionProvider) provider).maxArguments())
                throw new NeepASM.ParseException("too many targets provided");

            view.fastForward();
        }

        return new DefaultParsedInstruction(provider, arguments, kvs);
    }
}
