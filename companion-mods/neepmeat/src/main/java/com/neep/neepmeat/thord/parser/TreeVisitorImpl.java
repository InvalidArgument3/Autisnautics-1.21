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

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.meatlib.util.NeepAsmTokenView;
import com.neep.neepmeat.api.plc.program.MutableProgram;
import com.neep.neepmeat.neepasm.compiler.InstructionAcceptor;
import com.neep.neepmeat.neepasm.compiler.LabelLookup;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.neepmeat.neepasm.compiler.ParsedFunction;
import com.neep.neepmeat.neepasm.compiler.parser.ParsedInstruction;
import com.neep.neepmeat.neepasm.program.Label;
import com.neep.neepmeat.neepasm.vm.DataStack;
import com.neep.neepmeat.neepasm.vm.ThordData;
import com.neep.neepmeat.neepasm.vm.WorldTargetImpl;
import com.neep.neepmeat.plc.SingletonInstructions;
import com.neep.neepmeat.plc.instruction.Argument;
import com.neep.neepmeat.plc.instruction.PushInstruction;
import com.neep.neepmeat.plc.processor.VariableStack;
import com.neep.neepmeat.thord.compiler.CompilerVM;
import com.neep.neepmeat.thord.parser.node.*;
import com.neep.neepmeat.thord.token.CoordsToken;
import com.neep.neepmeat.thord.token.StringToken;
import com.neep.neepmeat.thord.word.ImmediateWord;
import com.neep.neepmeat.thord.word.Word;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TreeVisitorImpl implements ThordTreeVisitor
{
    private final ThordProgramTree tree;
    private final NeepAsmParser neepAsmParser;
    private final Stack<InstructionAcceptor> acceptors = new ObjectArrayList<>();

    private int memoryHead = 1; // Address of next memory element that can be allocated. Start at 1 as 0 is NULL.
    private int nonameWords = 0; // Number of NONAME words
    private final DataStack compilerStack = new VariableStack(32, "compile stack");
    private final CompilerVM machine = new CompilerVM(compilerStack);

    // Top item is the parent of the current node
    private final Stack<TreeNode> route = new ObjectArrayList<>();

    private int line;

    public TreeVisitorImpl(ThordProgramTree tree, ThordParsedSource rootSource, NeepAsmParser neepAsmParser)
    {
        this.tree = tree;
        this.neepAsmParser = neepAsmParser;
        acceptors.push(rootSource);
    }

    @Nullable
    private TreeNode lookAhead(TreeNode child)
    {
        if (!route.isEmpty())
        {
            TreeNode top = route.peek(0);
            List<TreeNode> children = top.children();
            int index = children.indexOf(child);
            if (index != -1 && children.size() > index + 1)
            {
                return children.get(index + 1);
            }
        }

        return null;
    }

    private InstructionAcceptor peek()
    {
        return acceptors.peek(0);
    }

    private void instruction(ParsedInstruction parsedInstruction, int line)
    {
        peek().instruction(parsedInstruction, line);
    }

    private void instruction(int position, ParsedInstruction parsedInstruction, int line) throws NeepASM.CompilationException
    {
        peek().instruction(position, parsedInstruction, line);
    }

    private void label(Label label)
    {
        peek().label(label);
    }

    public void process() throws NeepASM.ProgramBuildException
    {
        TreeNode node = tree.root();

        try
        {
            node.visit(this);

            if (!compilerStack.isEmpty())
            {
                throw new NeepASM.ParseException("compiler stack not empty - probably unterminated construct");
            }
        }
        catch (NeepASM.NeepAsmException e)
        {
            throw new NeepASM.ProgramBuildException(line, 0, e.getMessage());
        }
    }

    private void visitChildren(TreeNode node) throws NeepASM.NeepAsmException
    {
        route.push(node);
        for (var child : node.children())
        {
            line = child.line();
            child.visit(this);
        }
        route.pop();
    }

    @Override
    public void visit(ThordProgramTree.RootNode node) throws NeepASM.NeepAsmException
    {
        visitChildren(node);
    }

    @Override
    public void visit(WordNode node) throws NeepASM.NeepAsmException
    {
        Word word = tree.dictionary().get(node.name());
        if (word == null)
            throw new NeepASM.CompilationException("undefined word " + node.name());

        word.expand(peek(), machine, node.line());
    }

    @Override
    public void visit(VariableDeclareNode node)
    {
        // Allocate a cell at the value of memoryHead and initialise it
        instruction((labelLookup, program) -> new PushInstruction(0), node.line());
        final int address = memoryHead; // Copy
        instruction((labelLookup, program) -> new PushInstruction(address), node.line());
        instruction((labelLookup, program) -> SingletonInstructions.STORE, node.line());

        // Expose the variable's address at build time using a label.
        label(new Label(node.name(), memoryHead));

        // Advance to next empty cell
        memoryHead++;
    }

    @Override
    public void visit(ArrayNode node)
    {
//        final int address = memoryHead;
        instruction((labelLookup, program) -> new PushInstruction(node.size()), node.line());
        instruction((labelLookup, program) -> SingletonInstructions.ALLOT, node.line());

        // Fill the array with 0
//        final int address = memoryHead; // Copy
//        instruction((labelLookup, program) -> new PushInstruction(address), node.line());
        instruction((labelLookup, program) -> new PushInstruction(node.size()), node.line());
        instruction((labelLookup, program) -> new PushInstruction(0), node.line());
        instruction((labelLookup, program) -> SingletonInstructions.FILL, node.line());

        // No runtime dictionary addition, so we'll just have to hope that the allocated address is the same as memoryHead
        label(new Label(node.name(), memoryHead));
        memoryHead += node.size();
    }

    @Override
    public void visit(CoordsToken node)
    {
        int line = node.line();
        Argument argument = node.argument();
        instruction((labelLookup, program) ->
                new PushInstruction(new WorldTargetImpl(argument.pos(), argument.face())), line);
    }

    @Override
    public void visit(StringToken node)
    {
        instruction(((labelLookup, program) -> new PushInstruction(ThordData.of(node.value()))), node.line());
    }

    @Override
    public void visit(WordDefinitionNode node) throws NeepASM.NeepAsmException
    {
        ParsedFunction function = new ParsedFunction(node.name(), s -> null);
        peek().function(function);
        acceptors.push(function);
        visitChildren(node);
        acceptors.pop();
    }

    @Override
    public void visit(NonameWordDefinitionNode node) throws NeepASM.NeepAsmException
    {
        String name = "noname" + nonameWords;
        ParsedFunction function = new ParsedFunction(name, s -> null);
        peek().function(function);
        acceptors.push(function);
        visitChildren(node);
        acceptors.pop();

        instruction((labelLookup, program) ->
        {
            Label label = labelLookup.findLabel(name);
            if (label == null)
                throw new NeepASM.CompilationException("unknown label " + name);

            return new PushInstruction(label.index());
        }, node.line());

        nonameWords++;
    }

    @Override
    public void visit(ImmediateWordDefinitionNode node) throws NeepASM.NeepAsmException
    {
        String name = node.name();

        ParsedImmediateWord collector = new ParsedImmediateWord(machine);
//        ExecAcceptor execAcceptor = new ExecAcceptor(collector);
        acceptors.push(collector);

        // The abomination
        route.push(node);
        for (var child : node.children())
        {
            line = child.line();
            if (child instanceof NonameWordDefinitionNode nonameWordDefinitionNode)
            {
                nonameWordDefinitionNode.visit(this);
            }
            else if (child instanceof PostponeNode postpone)
            {
                // Jank
                // TODO: It would be useful to have an interface for accepting words as well as just parsed instructions
                for (var postponeChild : postpone.children())
                {
                    if (postponeChild instanceof WordNode wordNode)
                    {
                        Word word1 = tree.dictionary().get(wordNode.name());
                        if (word1 == null)
                            throw new NeepASM.CompilationException("undefined word " + wordNode.name());

                        collector.word(word1, wordNode.line());
                    }
                    else if (postponeChild instanceof NumberLiteralNode numberNode)
                    {
                        // Oh, god
                        collector.word((acceptor, stack, line1) ->
                                acceptor.instruction((labelLookup, program) ->
                                        new PushInstruction(numberNode.value()), numberNode.line()), numberNode.line());
                    }
                    else if (postponeChild instanceof InlineNode inline)
                    {
                        try
                        {
                            // WHAT DOES THIS EVEN DO?
                            // I WROTE THIS WITHOUT KNOWING HOW THIS WORKS BUT IT DOES
                            // I think... we add an immediate word to the new immediate word that emits the parsed inline instruction when run by the compiler...
                            var parsed = inline.provider().getParser().parse(new NeepAsmTokenView(inline.arguments()), neepAsmParser, null);
                            collector.word((acceptor, stack, line1) ->
                                    acceptor.instruction(parsed, node.line()), node.line());
                        }
                        catch (NeepASM.ParseException e)
                        {
                            throw new NeepASM.CompilationException(e.getMessage());
                        }
                    }
                }
            }
            else
            {
                child.visit(this);
            }
        }

        acceptors.pop();
        tree.dictionary().put(name, new ImmediateWord(name, collector, i -> this.line = i));
    }

    @Override
    public void visit(ExecuteNode node) throws NeepASM.NeepAsmException
    {
        MutableProgram program = new ExecutionProgram();
        ParsedExecute collector = new ParsedExecute();

        acceptors.push(collector);
        visitChildren(node);
        acceptors.pop();

        for (var pair : collector.parsedInstructions())
        {
            ParsedInstruction parsedInstruction = pair.key();

            if (parsedInstruction.supportsCompiler())
            {
                program.addBack(parsedInstruction.build(LabelLookup.EMPTY, program));
            }
            else
            {
                throw new NeepASM.CompilationException("operation not supported at compile time");
            }
        }

        machine.resetCounter();
        while (machine.counter() < program.size())
        {
            program.get(machine.counter()).start(machine);
        }
    }

    @Override
    public void visit(PostponeNode node) throws NeepASM.NeepAsmException
    {
//        acceptors.push(new PostponeAcceptor());
//        visitChildren(node);
//        acceptors.pop();
    }

    @Override
    public void visit(LabelNode node)
    {
        label(new Label(node.name(), peek().size()));
    }

    @Override
    public void visit(NumberLiteralNode node)
    {
        instruction((labelLookup, program) -> new PushInstruction(node.value()), node.line());
    }

    @Override
    public void visit(InlineNode node) throws NeepASM.CompilationException
    {
        try
        {
            instruction(node.provider().getParser().parse(new NeepAsmTokenView(node.arguments()), neepAsmParser, null), node.line());
        }
        catch (NeepASM.ParseException e)
        {
            throw new NeepASM.CompilationException(e.getMessage());
        }
    }
}
