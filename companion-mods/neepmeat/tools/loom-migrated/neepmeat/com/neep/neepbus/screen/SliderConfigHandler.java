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

package com.neep.neepbus.screen;

import com.neep.neepbus.part.Slider;
import java.util.List;
import net.minecraft.world.entity.player.Player;

public class SliderConfigHandler extends RangeConfigHandler
{
    private final Slider slider;

    public SliderConfigHandler(Player player, Slider ranged)
    {
        super(player, ranged);
        this.slider = ranged;
    }

    @Override
    protected void receiveParams(List<Float> ints)
    {
        super.receiveParams(ints);

//        slider.setValue(ints.get(0));
        slider.setInterval(ints.get(3));
    }

    @Override
    public void sendUpdates()
    {
        if (updateToClient)
        {
            // Send the updated (and possibly sanitised) values back to the client.
            updateParamsS2C.emitter().update(List.of(slider.getValue(), slider.getMinValue(), slider.getMaxValue(), slider.getInterval()));
            updateToClient = false;
        }
    }
}
