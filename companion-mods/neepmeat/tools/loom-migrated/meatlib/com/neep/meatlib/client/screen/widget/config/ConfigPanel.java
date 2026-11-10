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

package com.neep.meatlib.client.screen.widget.config;

import com.neep.meatlib.client.screen.auto.ScrollVStackPanel;
import com.neep.meatlib.client.screen.widget.ClickableWidget;
import com.neep.meatlib.screen.ConfigWidgetHandler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

///  Like ConfigWidget except it extends Panel
public abstract class ConfigPanel<T> extends ScrollVStackPanel
{
    protected final ConfigWidgetHandler<T> handler;
    protected final List<ClickableWidget> trackedFields = new ObjectArrayList<>();
    protected final Font textRenderer = Minecraft.getInstance().font;
    private boolean initialSynced = false;

    public ConfigPanel(ConfigWidgetHandler<T> handler)
    {
        super(0, 1, false, false);

        this.handler = handler;

        handler.updateParamsS2C.receiver(this::updateParamsInternal);
    }

    protected abstract T createValues();
    protected abstract void updateParams(T values);

    private void updateParamsInternal(T values)
    {
        initialSynced = true;
        updateParams(values);
    }

    protected <W extends ClickableWidget> W addTrackedField(W field)
    {
        trackedFields.add(field);
        return field;
    }

    /**
     * Parses all fields and sends them to the server.
     * Call this in Screen::close
     */
    public void update()
    {
        // Prevent invalid states from being sent to the server.
        // Without this, it's possible for a block to lose data if packets are slightly delayed.
        if (!initialSynced)
            return;

        try
        {
            T values = createValues();
            handler.receiveParamsC2S.emitter().update(values);
            for (var field : trackedFields)
            {
                if (field instanceof ConfigTextField configTextField)
                    configTextField.setDirty(false);
            }
        }
        catch (NumberFormatException e)
        {
            // Don't need to do anything.
        }
    }

    @Override
    public void init()
    {
        // It's a puzzle!
        // We could move all this to the constructor (as VStackPanel retains children)
        // But that would mean that text fields would be added in an overridden method, which means they can't be final in the subclass,
        children.clear();

        for (var field : trackedFields)
        {
            addChild(field);
        }

        super.init();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        // Allow typing scientific notation
        if (isFocused() && keyCode == GLFW.GLFW_KEY_E)
            return true;

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close()
    {
        super.close();
        update();
    }
}
