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

package com.neep.neepmeat.transport.screen.widget;

import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.RecordParamCodec;
import com.neep.meatlib.screen.ConfigWidgetHandler;
import com.neep.neepmeat.transport.block.energy_transport.entity.vsc.VSCBlockEntity;
import com.neep.neepmeat.transport.block.energy_transport.entity.vsc.VSCMode;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class VSCConfigWidgetHandler extends ConfigWidgetHandler<VSCConfigWidgetHandler.Values>
{
    @Nullable
    private final VSCBlockEntity be;

    public VSCConfigWidgetHandler(@Nullable VSCBlockEntity be, Player player)
    {
        super(player, Values.PARAM_CODEC);
        this.be = be;
    }

    @Override
    protected Values getValues()
    {
        if (be != null)
        {
            return new Values(be.getDesiredPower(), be.getPriority(), be.cutOff(), be.mode());
        }
        return new Values(0, 0, true, VSCMode.NORMAL);
    }

    @Override
    protected void setValues(Values values)
    {
        if (be != null)
        {
            be.setProperties(values.power, values.priority, values.cutOff, values.mode);
        }
    }

    public record Values(int power, int priority, boolean cutOff, VSCMode mode)
    {
        public static ParamCodec<Values> PARAM_CODEC = RecordParamCodec.builder(Values.class).param(ParamCodec.INT).param(ParamCodec.INT).param(ParamCodec.BOOLEAN).param(VSCMode.PARAM_CODEC).build();
    }
}
