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

package com.neep.neepmeat.transport.client.screen.widget;

import com.neep.meatlib.client.screen.widget.EnumSelectorWidget;
import com.neep.meatlib.client.screen.widget.IntNumberTextField;
import com.neep.meatlib.client.screen.widget.config.ConfigPanel;
import com.neep.meatlib.screen.ConfigWidgetHandler;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.screen.widget.RequesterConfigWidgetHandler;

public class RequesterConfigWidget extends ConfigPanel<RequesterConfigWidgetHandler.Values>
{
    private final IntNumberTextField amount;
    private final EnumSelectorWidget<RoutingNetwork.RequestType> requestType;
    private RoutingNetwork.RequestType currentRequestType = RoutingNetwork.RequestType.EXACT_AMOUNT;

    public RequesterConfigWidget(ConfigWidgetHandler<RequesterConfigWidgetHandler.Values> handler)
    {
        super(handler);

        amount = (IntNumberTextField) addTrackedField(new IntNumberTextField(textRenderer, 0, 0, 60, textRenderer.lineHeight + 4,
                NeepMeat.translationKey("screen", "advanced_ejector.amount"),
                a -> update(), 0, 64, 1, 16).drawFancyBackground(false));

        requestType = addTrackedField(new EnumSelectorWidget<>(0, 0, 60, textRenderer.lineHeight + 4, RoutingNetwork.RequestType.values(),
                NeepMeat.translationKey("screen", "requester.request_type"), () -> currentRequestType, (b, t) -> currentRequestType = t));
    }

    @Override
    protected RequesterConfigWidgetHandler.Values createValues()
    {
        return new RequesterConfigWidgetHandler.Values(amount.parse(), currentRequestType);
    }

    @Override
    protected void updateParams(RequesterConfigWidgetHandler.Values values)
    {
        amount.setNumberText(values.amount());
        currentRequestType = values.type();
    }
}
