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
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.neepmeat.neepasm.compiler.alias.ParsedAlias;
import com.neep.neepmeat.neepasm.compiler.alias.ParsedArgumentAlias;
import com.neep.neepmeat.neepasm.program.Label;
import com.neep.neepmeat.plc.instruction.Instruction;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import com.neep.neepmeat.plc.instruction.PushInstruction;
import com.neep.neepmeat.thord.parser.node.*;
import com.neep.neepmeat.thord.token.*;
import com.neep.neepmeat.thord.word.FunctionWord;
import com.neep.neepmeat.thord.word.VariableWord;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class ThordParser
{
    private final Map<String, InstructionProvider> instructions;
    private final ThordLexer lexer;
    private NeepAsmParser neepAsmParser;

    public ThordParser(ThordLexer lexer, Map<String, InstructionProvider> instructions)
    {
        this.instructions = instructions;
        this.lexer = lexer;
        this.neepAsmParser = new NeepAsmParser(instructions);
    }

    public ThordParsedSource parse(String source, ThordDictionary dictionary) throws NeepASM.ProgramBuildException
    {
        this.neepAsmParser = new NeepAsmParser(instructions);

        ThordTokenView view = new ThordTokenView(source);
        try
        {
            ThordProgramTree tree = new ThordProgramTree(dictionary);
            new TokenVisitorImpl(view,
                    tree,
                    lexer,
                    instructions::get,
                    a -> neepAsmParser.getParsedSource().alias(a)
            ).process();

            ThordParsedSource parsedSource = new ThordParsedSource();
            TreeVisitorImpl visitor = new TreeVisitorImpl(tree, parsedSource, neepAsmParser);
            visitor.process();

            parsedSource.instruction((abelLookup, program) -> Instruction.EMPTY, view.line() + 1);

            for (var function : parsedSource.functions())
            {
                function.expand(parsedSource);
            }

            return parsedSource;
        }
        catch (NeepASM.ParseException e)
        {
            throw new NeepASM.ProgramBuildException(view.line(), view.linePos(), e.getMessage());
        }
    }

    private static class TokenVisitorImpl implements TokenVisitor
    {
        private final LexerContext context;
        private final ThordTokenView view;
        private final ThordProgramTree tree;
        private final ThordLexer lexer;
        private final Function<String, InstructionProvider> lookup;
        private final Consumer<ParsedAlias> aliasConsumer;

        private final Deque<TreeNode> path = new ArrayDeque<>();

        public TokenVisitorImpl(ThordTokenView view, ThordProgramTree tree, ThordLexer lexer, Function<String, InstructionProvider> lookup, Consumer<ParsedAlias> aliasConsumer)
        {
            this.view = view;
            this.tree = tree;
            this.lexer = lexer;
            this.lookup = lookup;
            this.aliasConsumer = aliasConsumer;
            this.context = new LexerContext(this.view);

            path.push(tree.root());
        }

        @Nullable
        private ThordToken nextToken() throws NeepASM.ParseException
        {
            return lexer.nextToken(context);
        }

        public void process() throws NeepASM.ProgramBuildException, NeepASM.ParseException
        {
            int line = 0;

            while (lexer.hasNext(context))
            {
                ThordToken token = nextToken();
                if (token == null)
                    continue;

                line = view.line();

                try
                {
                    token.visit(this);

                    if (path.isEmpty())
                        throw new NeepASM.ParseException("too many ;");
                }
                catch (NeepASM.ParseException e)
                {
                    throw new NeepASM.ProgramBuildException(line, 0, e.getMessage());
                }
            }

            if (path.size() != 1)
                throw new NeepASM.ProgramBuildException(line, 0, "missing ;");
        }

        private TreeNode peek() throws NeepASM.ParseException
        {
            if (path.peek() == null)
                throw new NeepASM.ParseException("too many ;");

            return path.peek();
        }

        private void append(TreeNode node) throws NeepASM.ParseException
        {
            peek().add(node);
        }

        private void pushScope(TreeNode node) throws NeepASM.ParseException
        {
            peek().add(node);
            path.push(node);
        }

        private void popScope()
        {
            path.pop();
        }

        @Override
        public void visit(KeywordToken token) throws NeepASM.ParseException
        {
            switch (token.value())
            {
                case WORD ->
                {
                    ThordToken next = nextToken();
                    if (next instanceof IdentifierToken identifierToken)
                    {
                        String word = identifierToken.value();
                        tree.dictionary().put(word, new FunctionWord(word));
                        pushScope(new WordDefinitionNode(word, token.line()));
                        return;
                    }

                    throw new NeepASM.ParseException("expected word name");
                }
                case WORD_NONAME ->
                {
                    pushScope(new NonameWordDefinitionNode(token.line()));
                }
                case IMMEDIATE_WORD ->
                {
                    ThordToken next = nextToken();
                    if (next instanceof IdentifierToken identifierToken)
                    {
                        String word = identifierToken.value();

                        // Place all immediate word definitions on the root node
                        var node = new ImmediateWordDefinitionNode(word, token.line());
                        tree.root().addPre(node);
                        path.push(node);
                        return;
                    }

                    throw new NeepASM.ParseException("expected word name");
                }
                case LABEL ->
                {
                    ThordToken next = nextToken();
                    if (next instanceof IdentifierToken identifierToken)
                    {
                        String name = identifierToken.value();
                        append(new LabelNode(name, token.line()));

                        int line = token.line();
                        tree.dictionary().put(name, (acceptor, stack, line1) -> acceptor.instruction((labelLookup, program) ->
                        {
                            program.setDebugLine(line1);
                            Label found = labelLookup.findLabel(name);
                            if (found == null)
                                throw new NeepASM.CompilationException("label '" + name + "' does not exist");

                            return new PushInstruction(found.index());
                        }, line));

                        return;
                    }

                    throw new NeepASM.ParseException("expected label name");
                }
                case VARIABLE ->
                {
                    ThordToken next = nextToken();
                    if (next instanceof IdentifierToken identifierToken)
                    {
                        String name = identifierToken.value();
                        append(new VariableDeclareNode(name, token.line()));
                        tree.dictionary().put(name, new VariableWord(name));
                    }
                    else
                    {
                        throw new NeepASM.ParseException("expected variable name");
                    }
                }
                case ARRAY ->
                {
                    if (nextToken() instanceof IdentifierToken identifierToken)
                    {
                        if (nextToken() instanceof IntLiteralToken intLiteralToken)
                        {
                            String name = identifierToken.value();
                            append(new ArrayNode(name, intLiteralToken.value(), token.line()));
                            tree.dictionary().put(name, new VariableWord(name));
                        }
                        else
                        {
                            throw new NeepASM.ParseException("expected array length");
                        }
                    }
                    else
                    {
                        throw new NeepASM.ParseException("expected variable name");
                    }
                }
                case POSTPONE ->
                {
                    pushScope(new PostponeNode(token.line()));
                }
            }
        }

        @Override
        public void visit(IdentifierToken token) throws NeepASM.ParseException
        {
            append(new WordNode(token.value().toLowerCase(Locale.ROOT), token.line()));
        }

        @Override
        public void visit(IntLiteralToken token) throws NeepASM.ParseException
        {
            append(new NumberLiteralNode(token.value(), token.line()));
        }

        @Override
        public void visit(InlineToken token) throws NeepASM.ParseException
        {
            var provider = lookup.apply(token.name().toLowerCase(Locale.ROOT));
            if (provider == null)
                throw new NeepASM.ParseException("unrecognised operation '" + token.name() + "'");

            append(new InlineNode(provider, token.arguments(), token.line()));
        }

        @Override
        public void visit(NewlineToken token)
        {
        }

        @Override
        public void visit(CoordsToken token) throws NeepASM.ParseException
        {
            append(token);
        }

        @Override
        public void visit(WorldTargetAliasToken token)
        {
            aliasConsumer.accept(new ParsedArgumentAlias(token.name(), token.argument()));
        }

        @Override
        public void visit(StringToken token) throws NeepASM.ParseException
        {
            append(token);
        }

        @Override
        public void visit(ScopeEndToken token)
        {
            popScope();
        }
    }
}
