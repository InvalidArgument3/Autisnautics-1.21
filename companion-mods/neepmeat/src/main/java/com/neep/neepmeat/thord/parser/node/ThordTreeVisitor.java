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

package com.neep.neepmeat.thord.parser.node;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.thord.parser.ThordProgramTree;
import com.neep.neepmeat.thord.token.CoordsToken;
import com.neep.neepmeat.thord.token.StringToken;

public interface ThordTreeVisitor
{
    void visit(ThordProgramTree.RootNode node) throws NeepASM.NeepAsmException;

    void visit(WordNode node) throws NeepASM.NeepAsmException;

    void visit(WordDefinitionNode node) throws NeepASM.NeepAsmException;

    void visit(NonameWordDefinitionNode node) throws NeepASM.NeepAsmException;

    void visit(ImmediateWordDefinitionNode node) throws NeepASM.NeepAsmException;

    void visit(LabelNode node);

    void visit(NumberLiteralNode node);

    void visit(InlineNode node) throws NeepASM.NeepAsmException;

    void visit(ExecuteNode node) throws NeepASM.NeepAsmException;

    void visit(PostponeNode node) throws NeepASM.NeepAsmException;

    void visit(VariableDeclareNode node);

    void visit(ArrayNode node);

    void visit(CoordsToken node);

    void visit(StringToken node);
}
