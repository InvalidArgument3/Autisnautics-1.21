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

package com.neep.neepmeat.thord.parser;

import com.neep.neepmeat.plc.SingletonInstructions;
import com.neep.neepmeat.plc.instruction.*;
import com.neep.neepmeat.thord.ThordWords;
import com.neep.neepmeat.thord.word.Word;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ThordDictionary
{
    public static Map<String, Word> initBuiltin()
    {
        Map<String, Word> map = new HashMap<>();

        map.put("blank", Word.of((labelLookup, program) -> Instruction.EMPTY));
        map.put("pc", Word.of((labelLookup, program) -> SingletonInstructions.PC));
        map.put("dycall", Word.of(SingletonInstructions.DYCALL));
        map.put("'", Word.of(SingletonInstructions.DYLOC));

        map.put("+", Word.of((labelLookup, program) -> SingletonInstructions.ADD));
        map.put("-", Word.of((labelLookup, program) -> SingletonInstructions.SUB));
        map.put("/", Word.of((labelLookup, program) -> SingletonInstructions.DIV));
        map.put("*", Word.of((labelLookup, program) -> SingletonInstructions.MUL));
        map.put("=", Word.of((labelLookup, program) -> SingletonInstructions.EQ));
        map.put("!=", (acceptor, stack, line) ->
        {
            acceptor.instruction((labelLookup, program) -> SingletonInstructions.EQ, line);
            acceptor.instruction((labelLookup, program) -> SingletonInstructions.NOT, line);
        });
        map.put(">", Word.of((labelLookup, program) -> SingletonInstructions.GT));
        map.put("<", Word.of((labelLookup, program) -> SingletonInstructions.LT));
        map.put(">=", Word.of((labelLookup, program) -> SingletonInstructions.GTEQ));
        map.put("<=", Word.of((labelLookup, program) -> SingletonInstructions.LTEQ));
        map.put("invert", Word.of((labelLookup, program) -> SingletonInstructions.NOT));

        map.put("neg", Word.of((labelLookup, program) -> SingletonInstructions.NEG));
        map.put(".", Word.of((labelLookup, program) -> new SayInstruction('d')));
//        map.put(".S", (acceptor, stack, head, line) ->
//        {
//            acceptor.instruction((labelLookup, program) ->
//        });
        map.put("dup", Word.of((labelLookup, program) -> SingletonInstructions.DUP));
        map.put("2dup", (acceptor, stack, line) ->
        {
            acceptor.instruction((labelLookup, program) -> SingletonInstructions.OVER, line);
            acceptor.instruction((labelLookup, program) -> SingletonInstructions.OVER, line);
        });
        map.put("pick", Word.of((labelLookup, program) -> SingletonInstructions.PICK));
        map.put("swp", Word.of((labelLookup, program) -> SingletonInstructions.SWAP));
        map.put("2swp", (acceptor, stack, line) ->
        {
            acceptor.instruction((labelLookup, program) -> SingletonInstructions.ROT, line);
            acceptor.instruction((labelLookup, program) -> SingletonInstructions.TO_RET, line);
            acceptor.instruction((labelLookup, program) -> SingletonInstructions.ROT, line);
            acceptor.instruction((labelLookup, program) -> SingletonInstructions.FROM_RET, line);
        });
        map.put("over", Word.of((labelLookup, program) -> SingletonInstructions.OVER));
        map.put("2over", (acceptor, stack, line) ->
        {
            acceptor.instruction(new PushInstruction(3), line);
            acceptor.instruction(SingletonInstructions.PICK, line);
            acceptor.instruction(new PushInstruction(3), line);
            acceptor.instruction(SingletonInstructions.PICK, line);
        });
        map.put("rot", Word.of(SingletonInstructions.ROT));
        map.put("drop", Word.of(SingletonInstructions.POP));
        map.put("2drop", (acceptor, stack, line) ->
        {
            acceptor.instruction(SingletonInstructions.POP, line);
            acceptor.instruction(SingletonInstructions.POP, line);
        });
        map.put(">r", Word.of(SingletonInstructions.TO_RET));
        map.put("r>", Word.of(SingletonInstructions.FROM_RET));
        map.put("2>r", (acceptor, stack, line) ->
        {
            acceptor.instruction(SingletonInstructions.SWAP, line);
            acceptor.instruction(SingletonInstructions.TO_RET, line);
            acceptor.instruction(SingletonInstructions.TO_RET, line);
        });
        map.put("2r>", (acceptor, stack, line) ->
        {
            acceptor.instruction(SingletonInstructions.FROM_RET, line);
            acceptor.instruction(SingletonInstructions.FROM_RET, line);
            acceptor.instruction(SingletonInstructions.SWAP, line);
        });
        map.put("r@", Word.of(SingletonInstructions.RET_FETCH));
        map.put("!", Word.of(SingletonInstructions.STORE));
        map.put("+!", ThordWords.PLUS_STORE);
        map.put("@", Word.of(SingletonInstructions.FETCH));
        map.put("?", (acceptor, stack, line) ->
        {
            acceptor.instruction(SingletonInstructions.FETCH, line);
            acceptor.instruction(new SayInstruction(""), line);
        });

        map.put(">str", Word.of(SingletonInstructions.TO_STR));
        map.put(">int", Word.of(SingletonInstructions.TO_INT));

        map.put("execute", Word.of(SingletonInstructions.CALL));
        // Hmmm
//        map.put("allot", Word.of(SingletonInstructions.ALLOT));

        map.put("if", ThordWords.IF);
        map.put("else", ThordWords.ELSE);
        map.put("endif", ThordWords.ENDIF);
        map.put("then", ThordWords.ENDIF);

        map.put("do", ThordWords.DO);
        map.put("for", ThordWords.DO);
        map.put("i", Word.of(SingletonInstructions.RET_FETCH));
        map.put("loop", ThordWords.LOOP);
        map.put("+loop", ThordWords.PLUS_LOOP);

        map.put("begin", ThordWords.BEGIN);
        map.put("until", ThordWords.UNTIL);

        // Compiler instructions
        map.put("cphead", ThordWords.CPHEAD);
        map.put("cpdumpstack", ((acceptor, stack, line) ->
        {
            StringBuilder stackString = new StringBuilder();
            for (int i = stack.dataStack().size() - 1; i >= 0; --i)
            {
                stackString.append(" ");
                stackString.append(stack.dataStack().peek(i).toString());
            }
            stack.say(stackString + " <--");
        }));

        map.put("cpjmp", compiledBranchWord(JumpInstruction::new));
        map.put("cpbif", compiledBranchWord(BIFInstruction::new));
        map.put("cpbit", compiledBranchWord(BITInstruction::new));

        map.put("area", ThordWords.AREA);

        return map;
    }

    // More lambdas
    private static Word compiledBranchWord(Int2ObjectFunction<Instruction> instruction)
    {
        return (acceptor, stack, line) ->
        {
            int jump = stack.popInt(); // n2
            int offset = stack.popInt(); // n1
            int target = acceptor.size() + offset;
            acceptor.instruction(target, (lookup, program) ->
            {
                // Use the target line of the replaced instruction
                int targetLine = program.getDebugLine(target);
                program.setDebugLine(targetLine != -1 ? targetLine : line);
                return instruction.apply(jump);
            }, line);
        };
    }

    private Map<String, Word> dictionary;

    public ThordDictionary()
    {
        reset();
    }

    public void reset()
    {
        dictionary = initBuiltin();
    }

    public void put(String name, Word word)
    {
        dictionary.put(name, word);
    }

    @Nullable
    public Word get(String name)
    {
        return dictionary.get(name);
    }

    public boolean contains(String name)
    {
        return dictionary.containsKey(name);
    }
}
