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
import com.neep.neepmeat.neepasm.compiler.LabelLookup;
import com.neep.neepmeat.neepasm.program.Label;
import com.neep.neepmeat.thord.parser.node.AbstractTreeNode;
import com.neep.neepmeat.thord.parser.node.ThordTreeVisitor;
import com.neep.neepmeat.thord.parser.node.TreeNode;
import org.jetbrains.annotations.Nullable;

public class ThordProgramTree implements LabelLookup
{
    private final TreeNode root;
    private final ThordDictionary dictionary;

    public ThordProgramTree(ThordDictionary dictionary)
    {
        this.dictionary = dictionary;
        root = new RootNode();
    }

    public TreeNode root()
    {
        return root;
    }

    public ThordDictionary dictionary()
    {
        return dictionary;
    }

    @Override
    public @Nullable Label findLabel(String label)
    {
        return null;
    }

    @Override
    public @Nullable Label findLabel(String label, int origin, Label.Seek seek)
    {
        return null;
    }

    public static class RootNode extends AbstractTreeNode
    {
        public RootNode()
        {
            super(0);
        }

        @Override
        public void visit(ThordTreeVisitor visitor) throws NeepASM.NeepAsmException
        {
            visitor.visit(this);
        }
    }
}
