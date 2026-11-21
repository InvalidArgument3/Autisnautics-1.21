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

import com.neep.meatlib.client.screen.auto.Panel;
import com.neep.meatlib.client.screen.widget.ClickableWidget;
import com.neep.neepmeat.implant.config.ImplantConfigHandler;
import com.neep.neepmeat.screen_handler.ImplantManagerScreenHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;

public abstract class ImplantConfigPanel<T extends ImplantConfigHandler> extends Panel
{
    protected final ImplantManagerScreenHandler screenHandler;
    private final Class<T> handlerClass;
    @Nullable protected ClickableWidget main;
    protected final T configHandler;

    protected Font textRenderer = Minecraft.getInstance().font;

    protected ImplantConfigPanel(ImplantManagerScreenHandler screenHandler, Class<T> handlerClass)
    {
        this.screenHandler = screenHandler;
        this.handlerClass = handlerClass;
        this.configHandler = (T) screenHandler.getConfigHandler();
//        if (handler != null && handlerClass.isAssignableFrom(handler.getClass()))
//        {
//            return Optional.of((T) handler);
//        }
    }

    @Override
    public void init()
    {
        onInit();

        if (main != null)
        {
            int padding = 1;
            setDims(main, x + padding, y + padding, w - 2 * padding, h - 2 * padding);
        }
    }

    protected abstract void onInit();

    @Override
    protected List<Renderable> drawables()
    {
        return main != null ? List.of(main) : List.of();
    }

    @Override
    public List<GuiEventListener> children()
    {
        return main != null ? List.of(main) : List.of();
    }
}
