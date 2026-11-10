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

package com.neep.meatlib.util;

import com.neep.meatlib.api.network.ParamCodec;
import net.minecraft.util.StringRepresentable;

public enum RedstoneMode implements StringRepresentable
{
    ACTIVE_WITH,
    ACTIVE_WITHOUT;

    public boolean active(boolean receivingRedstone)
    {
        if (this == ACTIVE_WITH)
            return receivingRedstone;
        else
            return !receivingRedstone;
    }

    public static final ParamCodec<RedstoneMode> PARAM_CODEC = ParamCodec.ofEnum(RedstoneMode.class);

    @Override
    public String getSerializedName()
    {
        return switch (this)
        {
            case ACTIVE_WITH -> "active_with";
            case ACTIVE_WITHOUT -> "active_without";
        };
    }
}
