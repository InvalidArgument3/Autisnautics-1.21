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

package com.neep.neepbus.client.screen;

import com.neep.meatlib.client.screen.widget.FloatNumberTextField;
import com.neep.meatlib.client.screen.widget.ParentWidget;
import com.neep.neepbus.screen.RangeConfigHandler;
import com.neep.neepmeat.NeepMeat;
import com.neep.meatlib.client.screen.util.Background;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class RangeConfigWidget extends ParentWidget
{
    // For delaying C2S updates
//    private int counter = -1;

    private final RangeConfigHandler handler;

    private final List<TextField> textFields = new ObjectArrayList<>();

    public RangeConfigWidget(int x, int y, int w, int h, RangeConfigHandler handler)
    {
        super(x, y, w, h);

        this.handler = handler;
        handler.updateParamsS2C.receiver(this::updateParams);

        addTextField(new TextField(textRenderer, x, y, w, 8, Component.nullToEmpty("Value: ")));
        addTextField(new TextField(textRenderer, x, y, w, 8, Component.nullToEmpty("Min: ")));
        addTextField(new TextField(textRenderer, x, y, w, 8, Component.nullToEmpty("Max: ")));
    }

    protected <T extends TextField> T addTextField(T t)
    {
        textFields.add(t);
        return t;
    }

    void updateParams(List<Float> integers)
    {
        if (integers.size() != textFields.size())
        {
            NeepMeat.LOGGER.error("RangeConfigWidget: Incorrect number of integers received");
            return; // Something has gone horribly wrong and I don't want to think about it.
        }

        for (int i = 0; i < integers.size(); ++i)
        {
            var field = textFields.get(i);
            field.setNumberText(integers.get(i));
            field.dirty = false;
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
    public void tick()
    {
    }

    /**
     * Parses all fields and sends them to the server.
     * Call this in Screen::close
     */
    public void update()
    {
        try
        {
            FloatList list = new FloatArrayList(textFields.size());
            for (var field : textFields)
            {
                float parsed = field.parse();
                list.add(parsed);
            }

            handler.receiveParamsC2S.emitter().update(list);
//            updateC2S.accept(intList);
        }
        catch (NumberFormatException e)
        {
            // Don't need to do anything.
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput builder)
    {

    }

    @Override
    public NarrationPriority narrationPriority()
    {
        return NarrationPriority.NONE;
    }

    public void setW(int w)
    {
        this.w = w;
    }

    protected class TextField extends FloatNumberTextField
    {
        public TextField(Font textRenderer, int x, int y, int width, int height, Component prefix)
        {
            super(textRenderer, x, y, width, height, prefix, c -> RangeConfigWidget.this.update(),
                    -Float.MAX_VALUE, Float.MAX_VALUE, 1, 10);
//            setChangedListener(this::onChanged);
        }

//        public void setIntText(int value)
//        {
//            ignoreChange = true;
//            String newText = Integer.toString(value);
//            setText(newText);
//            prevText = newText;
//            ignoreChange = false;
//        }

        // Can't be bothered to add another AW entry and restart the IDE and generate sources again.
//        private void onChanged(String text)
//        {
//            if (!ignoreChange && !Objects.equals(prevText, text))
//                dirty = true;
//        }
    }
}
