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

package com.neep.neepmeat.thord;

import com.neep.neepmeat.api.plc.instruction.SimplerInstructionProvider;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.NeepAsmPLCCompiler;
import com.neep.neepmeat.neepasm.program.Program;
import com.neep.neepmeat.plc.instruction.Instruction;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import com.neep.neepmeat.thord.parser.ThordDictionary;
import com.neep.neepmeat.thord.parser.ThordParser;
import com.neep.neepmeat.thord.token.ThordLexer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ThordTest
{
    public static void main(String[] args)
    {
//        String source = """
//                -123
//                123
//                +
//                name func
//                    1 2 +
//                ;
//
//                /move @(1 2 3 U) @(4 5 7 D)
//
//                label handler
//
//                1 dup . drop
//
//                func
//                """;

//        String source = """
//                123
//                if
//                    1 -
//                    .
//                else
//                    drop 333 .
//                ;
//
//                0
//                if
//                    .
//                ;
//                """;

//        String source = """
//                :i if1 c-pc .end ; ;
//
//                :i then
//                    c-pc c-sub
//                    c-dup c-neg c-bif
//                ;
//
//                1 1 = if1 123 . then
//                """;

        String source = """
                10 0 do
                1 .
                loop
                """;

        ThordDictionary dictionary = new ThordDictionary();
        ThordLexer lexer = new ThordLexer(dictionary, new HashSet<>());

        try
        {
            var tokens = lexer.tokenise(source);

            System.out.println("Tokens:");
            for (var token : tokens)
            {
                System.out.println(token);
            }

            Map<String, InstructionProvider> map = new HashMap<>();
            map.put("end", new SimplerInstructionProvider((w, n) -> Instruction.end(), (view, parser, scope) -> (l, program) -> Instruction.end(), "END)"));
            ThordParser parser = new ThordParser(lexer, map);

            var parsedSource = parser.parse(source, dictionary);

            NeepAsmPLCCompiler compiler = new NeepAsmPLCCompiler(() -> null);
            Program program = compiler.compile(parsedSource);

            System.out.println("\nProgram: ");
            for (int i = 0; i < program.size(); ++i)
            {
                System.out.println(i + " " + program.get(i));
            }

        }
        catch (NeepASM.ProgramBuildException e)
        {
            throw new RuntimeException(e);
        }
    }
}
