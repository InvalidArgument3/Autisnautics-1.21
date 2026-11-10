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

package com.neep.meatlib.larkdown.content;

import com.neep.meatlib.larkdown.LarkdownContent;
import com.neep.meatlib.larkdown.parser.Alignment;
import com.neep.meatlib.larkdown.parser.Format;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record TextLarkdownContent(List<Entry> entries, Alignment alignment) implements LarkdownContent
{
    public TextLarkdownContent(String string, Format format, Alignment alignment)
    {
        this(List.of(new Entry(string, format)), alignment);
    }

    @Override
    public boolean canCombine(LarkdownContent content)
    {
        return content instanceof TextLarkdownContent;
    }

    @Override
    public @Nullable LarkdownContent combine(LarkdownContent content)
    {
        if (content instanceof TextLarkdownContent other)
        {
            var combined = new ObjectArrayList<>(entries);
            combined.addAll(other.entries);
            return new TextLarkdownContent(combined, alignment);
        }

        return null;
    }

    public record Entry(String text, Format format)
    {

    }
}
