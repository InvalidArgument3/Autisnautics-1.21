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

package com.neep.neepmeat.client.screen.button;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.client.screen.plc.PLCScreenButton;
import com.neep.neepmeat.guide.GuideReloadListener;
import com.neep.neepmeat.item.ProjectorItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class OpenGuideButton extends PLCScreenButton
{
    private final String article;

    public OpenGuideButton(String article)
    {
        super(0, 0, Component.empty());
        this.article = article;
        tooltip(NeepMeat.translationKey("screen", "open_guide_entry"));
    }

    @Override
    protected int getU()
    {
        return 256 - 16;
    }

    @Override
    public void onClick(double mouseX, double mouseY)
    {
        super.onClick(mouseX, mouseY);
        var nodes = GuideReloadListener.getInstance().getPath(article);
        if (nodes != null)
        {
            var screen = ProjectorItem.Client.openScreen();
            for (var node : nodes)
            {
                node.visitScreen(screen);
            }
        }
    }
};
