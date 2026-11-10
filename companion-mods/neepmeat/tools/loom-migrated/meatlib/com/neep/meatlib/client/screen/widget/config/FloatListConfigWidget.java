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
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public abstract class FloatListConfigWidget extends NumberListConfigWidget<Float, FloatList>
{
    public FloatListConfigWidget(int x, int y, int w, int h, NumberListConfigHandler<Float, FloatList> handler)
    {
        super(x, y, w, h, handler);
    }

    @Override
    protected FloatList createList()
    {
        return new FloatArrayList();
    }

    @Override
    protected Float parseValue(String text)
    {
        try
        {
            return Float.parseFloat(text);
        }
        catch (NumberFormatException e)
        {
            return 0f;
        }
    }

    @Override
    protected Float zero()
    {
        return 0f;
    }

    @Override
    protected String valueToString(Float value)
    {
        return Float.toString(value);
    }

    @Override
    protected boolean validString(String string)
    {
        return string.matches("[+\\-.0-9E]*");
    }

    @Override
    protected Float applyScroll(String text, Double amount)
    {
        return (float) (parseValue(text) + amount * scrollMultiplier());
    }
}
