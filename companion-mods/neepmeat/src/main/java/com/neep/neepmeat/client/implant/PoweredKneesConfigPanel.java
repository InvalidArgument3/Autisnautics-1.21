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

package com.neep.neepmeat.client.implant;

import com.neep.meatlib.client.screen.auto.ScrollVStackPanel;
import com.neep.meatlib.client.screen.widget.FloatNumberTextField;
import com.neep.neepmeat.client.screen.ImplantManagerScreen;
import com.neep.neepmeat.implant.config.PoweredKneesConfigHandler;
import com.neep.neepmeat.screen_handler.ImplantManagerScreenHandler;
import net.minecraft.network.chat.Component;

public class PoweredKneesConfigPanel extends ImplantConfigPanel<PoweredKneesConfigHandler>
{
    private final FloatNumberTextField powerField;

    public PoweredKneesConfigPanel(ImplantManagerScreenHandler handler, ImplantManagerScreen screen)
    {
        super(handler, PoweredKneesConfigHandler.class);
        powerField = new FloatNumberTextField(textRenderer, 0, 0, 1, 18, Component.nullToEmpty("Added jump power"), c -> sendPower(), 0, 0.2f, 0.05f, 0.01f);

        configHandler.jumpPowerC2S.receiver(this::update);
    }

    private void update(float power)
    {
        powerField.setNumberText(power);
    }

    @Override
    public void onInit()
    {
        ScrollVStackPanel buttons = new ScrollVStackPanel(1, 1, true, false);
        main = buttons;
        buttons.addChild(powerField);
    }

    private void sendPower()
    {
        configHandler.jumpPowerC2S.emitter().apply(powerField.parse());
    }
}
