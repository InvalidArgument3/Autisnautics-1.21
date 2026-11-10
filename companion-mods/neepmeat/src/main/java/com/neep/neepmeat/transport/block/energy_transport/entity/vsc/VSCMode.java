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

package com.neep.neepmeat.transport.block.energy_transport.entity.vsc;

import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.neepmeat.NeepMeat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum VSCMode implements StringRepresentable
{
    NORMAL(NeepMeat.translationKey("screen", "vsc.normal.desc"), "normal"),
    BALANCE(NeepMeat.translationKey("screen", "vsc.balance.desc"), "balance");

    public final Component text;

    public static final ParamCodec<VSCMode> PARAM_CODEC = ParamCodec.ofEnum(VSCMode.class);
    private final String stringName;

    VSCMode(Component text, String stringName)
    {
        this.text = text;
        this.stringName = stringName;
    }

    public List<Component> wrappedText()
    {
        return TooltipSupplier.wrapLines(new ObjectArrayList<>(), text);
    }

    @Override
    public String getSerializedName()
    {
        return stringName;
    }
}
