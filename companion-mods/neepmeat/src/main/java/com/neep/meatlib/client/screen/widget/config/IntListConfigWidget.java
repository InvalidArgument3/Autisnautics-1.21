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

package com.neep.meatlib.client.screen.widget.config;

import com.neep.meatlib.client.screen.widget.NumberListConfigWidget;
import com.neep.meatlib.screen.NumberListConfigHandler;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class IntListConfigWidget extends NumberListConfigWidget<Integer, IntList>
{
    public IntListConfigWidget(int x, int y, int w, int h, NumberListConfigHandler<Integer, IntList> handler)
    {
        super(x, y, w, h, handler);
    }

    @Override
    protected IntList createList()
    {
        return new IntArrayList();
    }

    @Override
    protected Integer parseValue(String text)
    {
        try
        {
            return Integer.parseInt(text);
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    @Override
    protected Integer zero()
    {
        return 0;
    }

    @Override
    protected String valueToString(Integer value)
    {
        return Integer.toString(value);
    }

    @Override
    protected boolean validString(String string)
    {
        return string.matches("[+\\-0-9E]*");
    }

    @Override
    protected Integer applyScroll(String text, Double amount)
    {
        return (int) (parseValue(text) + amount * scrollMultiplier());
    }
}
