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

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

public abstract class AbstractTreeNode implements TreeNode
{
    private final int line;
    protected List<TreeNode> children = new ObjectArrayList<>();
    private int preSize = 0;

    public AbstractTreeNode(int line)
    {
        this.line = line;
    }

    @Override
    public void add(TreeNode node)
    {
        children.add(node);
    }

    @Override
    public void addPre(TreeNode node)
    {
        children.add(preSize, node);
        preSize++;
    }

    @Override
    public List<TreeNode> children()
    {
        return children;
    }

    @Override
    public int line()
    {
        return line;
    }
}
