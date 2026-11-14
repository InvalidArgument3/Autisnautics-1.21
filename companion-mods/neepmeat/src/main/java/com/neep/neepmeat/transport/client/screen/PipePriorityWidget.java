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

package com.neep.neepmeat.transport.client.screen;

import com.neep.meatlib.client.screen.widget.config.IntListConfigWidget;
import com.neep.meatlib.screen.NumberListConfigHandler;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.NeepMeat;
import it.unimi.dsi.fastutil.ints.IntList;

public class PipePriorityWidget extends IntListConfigWidget
{
    public PipePriorityWidget(int x, int y, int w, int h, NumberListConfigHandler<Integer, IntList> handler)
    {
        super(x, y, w, h, handler);
    }

    @Override
    protected void createTextFields()
    {
        addTextField(new TextField(textRenderer, x, y, w, PLCCols.ENTRY_H, NeepMeat.translationKey("screen", "pipe_priority.output_priority")))
                .tooltip(NeepMeat.translationKey("screen", "pipe_priority.output_priority.desc"));
        addTextField(new TextField(textRenderer, x, y, w, PLCCols.ENTRY_H, NeepMeat.translationKey("screen", "pipe_priority.input_priority")))
                .tooltip(NeepMeat.translationKey("screen", "pipe_priority.input_priority.desc"));
    }
}
