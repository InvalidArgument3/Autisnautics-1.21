/*
 * Copyright (c) MeatWheeze 2026.
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
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.block.item_transport.entity.RequesterBlockEntity;
import net.minecraft.world.entity.player.Player;

public class RequesterConfigWidgetHandler extends ConfigWidgetHandler<RequesterConfigWidgetHandler.Values>
{
    private final RequesterBlockEntity be;

    public RequesterConfigWidgetHandler(RequesterBlockEntity be, Player player)
    {
        super(player, Values.PARAM_CODEC);
        this.be = be;
    }

    @Override
    protected Values getValues()
    {
        return new Values(be.getAmount(), be.getRequestType());
    }

    @Override
    protected void setValues(Values values)
    {
        be.setValues(values.amount, values.type);
    }

    public record Values(int amount, RoutingNetwork.RequestType type)
    {
        public static ParamCodec<Values> PARAM_CODEC = RecordParamCodec.builder(Values.class)
                .param(ParamCodec.INT)
                .param(ParamCodec.ofEnum(RoutingNetwork.RequestType.class))
                .build();
    }
}
