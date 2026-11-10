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

package com.neep.neepmeat.client.screen.tablet;

import com.neep.meatlib.client.screen.auto.GeneralPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public abstract class ContentPane extends GeneralPanel implements Renderable, NarratableEntry, GuiEventListener, ContainerEventHandler
{
    protected int screenOffsetX = 5;
    protected int screenOffsetY = 5;

    protected final Font textRenderer = Minecraft.getInstance().font;

    protected final GuideScreen parent;

    protected ContentPane(GuideScreen parent)
    {
        this.parent = parent;
    }

    @Override
    public NarrationPriority narrationPriority()
    {
//        if (this.focused)
//        {
//            return Selectable.SelectionType.FOCUSED;
//        }
//        if (this.hovered)
//        {
//            return Selectable.SelectionType.HOVERED;
//        }
//        return Selectable.SelectionType.NONE;
        return NarrationPriority.FOCUSED;
    }

    public void tick()
    {

    }
}