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

package com.neep.neepmeat.api.data;

import com.neep.neepmeat.NeepMeat;
import java.text.DecimalFormat;
import net.minecraft.network.chat.Component;

public class DataUtil
{
    public static final DecimalFormat GIEB_FORMAT = new DecimalFormat("###.#");
    public static final DecimalFormat EB_FORMAT = new DecimalFormat("###");

    // One GieB (gibi-esoteric byte)
    public static final long GIEB = 1024;

    public static Component formatData(long data)
    {
        if (data > GIEB)
        {
            return Component.translatable("text." + NeepMeat.NAMESPACE + ".data.gieb_unit", GIEB_FORMAT.format(data / (double) GIEB));
        }
        return Component.translatable("text." + NeepMeat.NAMESPACE + ".data.eb_unit", EB_FORMAT.format(data));
    }
}
