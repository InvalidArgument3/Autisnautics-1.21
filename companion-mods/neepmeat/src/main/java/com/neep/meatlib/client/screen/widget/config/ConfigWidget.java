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

import net.minecraft.client.gui.narration.NarratableEntry;

import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.meatlib.client.screen.widget.ClickableWidget;
import com.neep.meatlib.client.screen.widget.ParentWidget;
import com.neep.meatlib.screen.ConfigWidgetHandler;
import com.neep.meatlib.client.screen.util.Background;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public abstract class ConfigWidget<T> extends ParentWidget
{
    protected final ConfigWidgetHandler<T> handler;
    protected final List<ClickableWidget> trackedFields = new ObjectArrayList<>();
    private boolean initialSynced = false;

    public ConfigWidget(int x, int y, int w, int h, ConfigWidgetHandler<T> handler)
    {
        super(x, y, w, h);
        this.handler = handler;

        handler.updateParamsS2C.receiver(this::updateParamsInternal);
    }

    protected abstract T createValues();

    protected float scrollMultiplier()
    {
        return Screen.hasShiftDown() ? 1 : 10;
    }

    protected <W extends ClickableWidget> W addTrackedField(W field)
    {
        trackedFields.add(field);
        return field;
    }

    @Override
    public void close()
    {
        super.close();
        update();
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
        super.init();

        Background background = new Background(x, y, w, h, 6);
        addChild(background);

        int yOff = y;

        for (var field : trackedFields)
        {
            addChild(field);

            if (field instanceof Rectangle.Mutable mutable)
                mutable.setDims(x, yOff, nw(), field.nh());
            else
                field.setPos(x, yOff);

            yOff += field.nh() + 1;
        }

        h = yOff - y;
        background.setH(h);
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
    public void tick()
    {
//        if (counter != -1)
//        {
//            counter = Math.min(counter + 1, 10);
//
//            if (counter == 10)
//            {
////                update();
//                counter = -1;
//            }
//        }
    }

    @Override
    public void updateNarration(NarrationElementOutput builder)
    {

    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority()
    {
        return NarratableEntry.NarrationPriority.NONE;
    }

    public void setW(int w)
    {
        this.w = w;
    }

    private void updateParamsInternal(T values)
    {
        initialSynced = true;
        updateParams(values);
    }

    protected abstract void updateParams(T values);

    //    {
//        if (values.size() != textFields.size())
//        {
//            NeepMeat.LOGGER.error("RangeConfigWidget: Incorrect number of values received");
//            return; // Something has gone horribly wrong and I don't want to think about it.
//        }
//
//        for (int i = 0; i < values.size(); ++i)
//        {
//            var field = textFields.get(i);
//            field.setNumberText(values.get(i));
//            field.dirty = false;
//        }
//    }

//    protected class TextField extends NMTextField
//    {
//        public TextField(TextRenderer textRenderer, int x, int y, int width, int height, @Nullable NMTextField copyFrom, Text text)
//        {
//            super(textRenderer, x, y, width, height, copyFrom, text);
//        }
//    }
//        public TextField(TextRenderer textRenderer, int x, int y, int width, int height, Text prefix)
//        {
//            super(textRenderer,
//                    x, y, width, height,
//                    prefix,
//                    ConfigWidget.this::update,
//                    ConfigWidget.this::validString,
//                    ConfigWidget.this::valueToString,
//                    ConfigWidget.this::applyScroll
//            );
//        }
//    }
}
