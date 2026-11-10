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

import com.neep.neepmeat.plc.SingletonInstructions;
import com.neep.neepmeat.plc.instruction.*;
import com.neep.neepmeat.thord.word.Word;

public class ThordWords
{
    public static final Word CPHEAD = (acceptor, stack, line) -> stack.push(acceptor.size());

    public static final Word AREA = (acceptor, stack, line) ->
    {
        acceptor.instruction(SingletonInstructions.DUP, line);
    };

    public static final Word PLUS_STORE = (acceptor, stack, line) ->
    {
        acceptor.instruction(SingletonInstructions.DUP, line);
        acceptor.instruction(SingletonInstructions.FETCH, line);
        acceptor.instruction(SingletonInstructions.ROT, line);
        acceptor.instruction(SingletonInstructions.ADD, line);
        acceptor.instruction(SingletonInstructions.SWAP, line);
        acceptor.instruction(SingletonInstructions.STORE, line);
    };

    public static final Word IF = (acceptor, stack, line) ->
    {
        // BIF placeholder
        stack.push(acceptor.size());
        acceptor.instruction(Instruction.EMPTY, line);
    };

    public static final Word ELSE = (acceptor, stack, line) ->
    {
        // Backpatch BIF
        int bifPosition = stack.popInt();
        int head = acceptor.size();
        acceptor.instruction(bifPosition, (labelLookup, program) -> new BIFInstruction(head - bifPosition + 1), line);

        // JMP placeholder
        stack.push(acceptor.size());
        acceptor.instruction(Instruction.EMPTY, line);

        stack.push(-1);
    };

    public static final Word ENDIF = (acceptor, stack, line) ->
    {
        boolean isElse = stack.dataStack().peek(0).intValue() == -1;

        if (!isElse)
        {
            // if
            int head = acceptor.size();
            int bifPosition = stack.popInt();
            acceptor.instruction(bifPosition, (labelLookup, program) -> new BIFInstruction(head - bifPosition), line);
        }
        else
        {
            // else
            stack.popInt(); // Remove the -1

            // Backpatch JMP
            int head = acceptor.size();
            int jmpPosition = stack.popInt();
            acceptor.instruction(jmpPosition, (labelLookup, program) -> new JumpInstruction(head - jmpPosition), line);
        }
    };

    public static final Word DO = (acceptor, stack, line) ->
    {
        acceptor.instruction(SingletonInstructions.OVER, line);
        acceptor.instruction(SingletonInstructions.OVER, line);
        acceptor.instruction(SingletonInstructions.EQ, line);

        // BIT target
        stack.push(acceptor.size());
        acceptor.instruction((labelLookup, program) -> Instruction.EMPTY, line);

        // Copy to control stack
        acceptor.instruction(SingletonInstructions.SWAP, line);
        acceptor.instruction(SingletonInstructions.TO_RET, line);
        acceptor.instruction(SingletonInstructions.TO_RET, line);

        // Loop begin
        stack.push(acceptor.size());
    };

    public static final Word LOOP = (acceptor, stack, line) ->
    {
        int bifPosition = stack.popInt();

        int bitPosition = stack.popInt();

        acceptor.instruction(new PushInstruction(1), line);
        acceptor.instruction(SingletonInstructions.RET_ADD, line);
        acceptor.instruction(SingletonInstructions.RET_EQ, line);

        int bifJump = bifPosition - acceptor.size();

        acceptor.instruction(new BIFInstruction(bifJump), line);

        // Clean up
        acceptor.instruction(SingletonInstructions.FROM_RET, line);
        acceptor.instruction(SingletonInstructions.FROM_RET, line);

        // Backpatch initial EQ jump
        int jump = acceptor.size() - bitPosition;
        acceptor.instruction(bitPosition, (labelLookup, program) -> new BITInstruction(jump), line);

        acceptor.instruction(SingletonInstructions.POP, line);
        acceptor.instruction(SingletonInstructions.POP, line);
    };

    public static final Word PLUS_LOOP = (acceptor, stack, line) ->
    {
        int bifPosition = stack.popInt();

        int bitPosition = stack.popInt();

        acceptor.instruction(SingletonInstructions.RET_ADD, line);
        acceptor.instruction(SingletonInstructions.RET_EQ, line);

        int bifJump = bifPosition - acceptor.size();

        acceptor.instruction(new BIFInstruction(bifJump), line);

        // Clean up
        acceptor.instruction(SingletonInstructions.FROM_RET, line);
        acceptor.instruction(SingletonInstructions.FROM_RET, line);

        // Backpatch initial EQ jump
        int jump = acceptor.size() - bitPosition;
        acceptor.instruction(bitPosition, (labelLookup, program) -> new BITInstruction(jump), line);

        acceptor.instruction(SingletonInstructions.POP, line);
        acceptor.instruction(SingletonInstructions.POP, line);
    };

    public static final Word BEGIN = (acceptor, stack, line) ->
    {
        stack.push(acceptor.size());
    };

    public static final Word UNTIL = (acceptor, stack, line) ->
    {
        int position = stack.popInt();

        acceptor.instruction(new BITInstruction(position - acceptor.size()), line);
    };
}
