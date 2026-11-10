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

package com.neep.neepbus.util;

import java.util.Locale;
import net.minecraft.network.chat.Component;

public class SimpleEntry implements ConfigEntry
{
    private final String name;
    private String address;
    private final Component tooltip;

    public SimpleEntry(String name, Component tooltip)
    {
        this.name = name;
        this.address = name.toLowerCase(Locale.ROOT);
        this.tooltip = tooltip;
    }

    public SimpleEntry(String name, String tooltip)
    {
        this(name, Component.literal(tooltip));
    }

    public SimpleEntry(String name)
    {
        this(name, "");
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getAddress()
    {
        return address;
    }

    @Override
    public void setAddress(String address)
    {
        this.address = address;
    }

    @Override
    public Component getTooltip()
    {
        return tooltip;
    }
}
