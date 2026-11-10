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

package com.neep.neepmeat.machine.separator;

import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.client.screen.auto.BlackBackgroundPanel;
import com.neep.meatlib.client.screen.auto.HashedBackgroundPanel;
import com.neep.meatlib.client.screen.widget.CheckboxWidget;
import com.neep.meatlib.client.screen.widget.IntNumberTextField;
import com.neep.meatlib.client.screen.widget.config.ConfigPanel;
import com.neep.meatlib.screen.ConfigWidgetHandler;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.neepmeat.NeepMeat;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SeparatorScreen extends BetterHandledScreen<SeparatorScreenHandler>
{
    public SeparatorScreen(SeparatorScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
        this.imageWidth = 200;
        this.imageHeight = 100;
    }

    @Override
    protected void init()
    {
        super.init();

        var main = addRenderableWidget(new HashedBackgroundPanel(
                new BlackBackgroundPanel(
                        new Thing(menu.handler))));
        main.setDims(leftPos, topPos, imageWidth, imageHeight);

        ScreenReady.ready();
    }

    static class Thing extends ConfigPanel<SeparatorWidgetHandler.Values>
    {
        private final IntNumberTextField radius;
        private final IntNumberTextField remainder;
        private boolean setTakeBabies;

        public Thing(ConfigWidgetHandler<SeparatorWidgetHandler.Values> handler)
        {
            super(handler);

            this.radius = addTrackedField(new IntNumberTextField(textRenderer, 0 , 0, 100, 18, NeepMeat.translationKey("screen", "separator.radius"),
                    f -> update(), 1, SeparatorBlockEntity.MAX_RADIUS, 1, 1));

            this.remainder = addTrackedField(new IntNumberTextField(textRenderer, 0 , 0, 100, 18, NeepMeat.translationKey("screen", "separator.remainder"),
                    f -> update(), 0, 50, 1, 2));
            remainder.tooltip(NeepMeat.translationKey("screen", "separator.remainder_desc"));

            addTrackedField(new CheckboxWidget(
                    100, 18, () -> setTakeBabies,
                    Component.nullToEmpty("Take babies"),
                    (button, toggled) ->
                    {
                        setTakeBabies = toggled;
                        update();
                    }));
        }

        @Override
        protected SeparatorWidgetHandler.Values createValues()
        {
            return new SeparatorWidgetHandler.Values(
                    radius.parse(),
                    setTakeBabies,
                    remainder.parse()
            );
        }

        @Override
        protected void updateParams(SeparatorWidgetHandler.Values values)
        {
            radius.setNumberText(values.radius());
            setTakeBabies = values.takeBabies();
            remainder.setNumberText(values.remainder());
        }
    }
}
