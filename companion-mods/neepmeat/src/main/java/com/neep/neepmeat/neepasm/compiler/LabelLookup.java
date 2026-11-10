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

package com.neep.neepmeat.neepasm.compiler;

import com.neep.neepmeat.neepasm.program.Label;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface LabelLookup
{
    @Nullable Label findLabel(String label);

    @Nullable Label findLabel(String label, int origin, Label.Seek seek);

    static Label findForwards(List<Label> labels, String target, int origin)
    {
        for (Label l : labels)
        {
            if (l.name().equals(target) && l.index() >= origin)
                return l;
        }
        return null;
    }

    static Label findBackwards(List<Label> labels, String target, int origin)
    {
        for (int i = labels.size() - 1; i >= 0; --i)
        {
            Label l = labels.get(i);
            if (l.name().equals(target) && l.index() <= origin)
                return l;
        }
        return null;
    }

    LabelLookup EMPTY = new LabelLookup()
    {
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
    };
}
