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
import com.neep.neepmeat.client.NMImplantsClient;
import com.neep.neepmeat.client.screen.ImplantManagerScreen;
import com.neep.neepmeat.client.screen.button.TextToggleButtonWidget;
import com.neep.neepmeat.implant.config.HexapodImplantConfigHandler;
import com.neep.neepmeat.screen_handler.ImplantManagerScreenHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class HexapodConfigPanel extends ImplantConfigPanel<HexapodImplantConfigHandler>
{
    public HexapodConfigPanel(ImplantManagerScreenHandler handler, ImplantManagerScreen screen)
    {
        super(handler, HexapodImplantConfigHandler.class);
    }

    @Override
    public void onInit()
    {
        int entryHeight = 18;
        ScrollVStackPanel buttons = new ScrollVStackPanel(1, 1, true, false);
        main = buttons;

        for (var entry : NMImplantsClient.SPIDER_LEGS_MODELS.entrySet())
        {
            buttons.addChild(new TextToggleButtonWidget(0, 0, 100, entryHeight, () -> false,
                    Component.translatable(entry.getKey().toLanguageKey("hexapod_skin")),
                    (button, toggled) ->
                        sendSkin(entry.getKey())).showBackground(false)
            );
        }
    }

    private void sendSkin(ResourceLocation id)
    {
        configHandler.skinInstallC2S.emitter().apply(id);
    }
}
