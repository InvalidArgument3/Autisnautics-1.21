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

package com.neep.neepmeat.thord.token;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.thord.parser.node.ThordTreeVisitor;
import com.neep.neepmeat.thord.parser.node.TreeNode;

public record StringToken(int line, String value) implements ThordToken, TreeNode
{
    @Override
    public void visit(TokenVisitor visitor) throws NeepASM.ParseException
    {
        visitor.visit(this);
    }

    @Override
    public void visit(ThordTreeVisitor visitor) throws NeepASM.NeepAsmException
    {
        visitor.visit(this);
    }
}
