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

package com.neep.meatlib.client.screen.widget;

import com.neep.meatlib.MeatLib;
import com.neep.meatlib.screen.NumberListConfigHandler;
import com.neep.meatlib.client.screen.util.Background;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public abstract class NumberListConfigWidget<T extends Number, L extends List<T>> extends ParentWidget
{
    protected final NumberListConfigHandler<T, L> handler;
    protected final List<TextField> textFields = new ObjectArrayList<>();

    public NumberListConfigWidget(int x, int y, int w, int h, NumberListConfigHandler<T, L> handler)
    {
        super(x, y, w, h);
        this.handler = handler;

        handler.updateParamsS2C.receiver(this::updateParams);

        createTextFields();
    }

    protected abstract void createTextFields();

    protected abstract L createList();

    protected abstract T zero();

    protected abstract T parseValue(String text);

    protected abstract String valueToString(T value);

    protected abstract boolean validString(String string);

    protected abstract T applyScroll(String text, Double amount);

    protected float scrollMultiplier()
    {
        return Screen.hasShiftDown() ? 1 : 10;
    }

    protected <W extends TextField> W addTextField(W field)
    {
        textFields.add(field);
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
        try
        {
            L values = createList();
            for (var field : textFields)
            {
                String text = field.text();
                T parsed = !text.isEmpty() ? parseValue(text) : zero();
                values.add(parsed);
            }

            handler.receiveParamsC2S.emitter().update(values);
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

        for (var field : textFields)
        {
            addChild(field);
            field.setDims(x, yOff, nw(), field.nh());
//            field.setWidth(w());
//            field.setPos(x, yOff);
            field.drawFancyBackground(false);
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

    private void updateParams(L values)
    {
        if (values.size() != textFields.size())
        {
            MeatLib.LOG.error("RangeConfigWidget: Incorrect number of values received");
            return; // Something has gone horribly wrong and I don't want to think about it.
        }

        for (int i = 0; i < values.size(); ++i)
        {
            var field = textFields.get(i);
            field.setNumberText(values.get(i));
            field.dirty = false;
        }
    }

    protected class TextField extends NumberTextField<T>
    {
        public TextField(Font textRenderer, int x, int y, int width, int height, Component prefix)
        {
            super(textRenderer,
                    x, y, width, height,
                    prefix,
                    NumberListConfigWidget.this::update,
                    NumberListConfigWidget.this::validString,
                    NumberListConfigWidget.this::valueToString,
                    NumberListConfigWidget.this::applyScroll
            );
        }
    }
}
