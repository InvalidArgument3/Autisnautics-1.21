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

package com.neep.neepmeat.thord.word;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.InstructionAcceptor;
import com.neep.neepmeat.thord.compiler.CompilerVM;
import com.neep.neepmeat.thord.parser.ParsedImmediateWord;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntConsumer;

public record ImmediateWord(String name, ParsedImmediateWord words, IntConsumer lineConsumer) implements Word
{
    @Override
    public void expand(InstructionAcceptor acceptor, CompilerVM stack, int line) throws NeepASM.NeepAsmException
    {
        stack.resetCounter();
        while (stack.counter() >= 0 && stack.counter() < words.size())
        {
            int prevCounter = stack.counter();

            var pair = getWord(prevCounter);
            if (pair == null)
                return;

            lineConsumer.accept(pair.valueInt());
            pair.key().expand(acceptor, stack, line);

            // Single increment is assumed if a word does not explicitly change the counter.
            if (stack.counter() <= prevCounter)
            {
                stack.setCounter(prevCounter + 1);
            }
        }
    }

    @Nullable
    private ObjectIntPair<Word> getWord(int index)
    {
        if (index >= 0 && index < words.size())
        {
            return words.getWords().get(index);
        }

        return null;
    }
}
