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

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.network.ChannelManager;
import com.neep.meatlib.screen.ScreenReadyListener;
import com.neep.neepbus.NeepBusScreenHandlers;
import com.neep.neepbus.part.Slider;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.screen_handler.BasicScreenHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public class SliderScreenHandler extends BasicScreenHandler implements ScreenReadyListener
{
    private boolean ready;

    public final ChannelManager<MouseScroll> incrementC2S;
    public final SliderConfigHandler sliderConfig;

    private final Slider slider;

    protected SliderScreenHandler(@Nullable MenuType<?> type, Inventory playerInventory, int syncId, Slider slider)
    {
        super(type, playerInventory, null, syncId, null);

        incrementC2S = ChannelManager.create(new ResourceLocation(NeepMeat.NAMESPACE, "increment"),
                ChannelFormat.builder(MouseScroll.class).param(ParamCodec.DOUBLE).param(ParamCodec.BOOLEAN).build(),
                playerInventory.player);

        sliderConfig = new SliderConfigHandler(playerInventory.player, slider);

        this.slider = slider;

        incrementC2S.receiver(this::increment);
    }

    public SliderScreenHandler(int syncId, Inventory playerInventory)
    {
        this(NeepBusScreenHandlers.SLIDER, playerInventory, syncId, Slider.EMPTY);
    }

    public SliderScreenHandler(int syncId, Inventory playerInventory, Slider slider)
    {
        this(NeepBusScreenHandlers.SLIDER, playerInventory, syncId, slider);
    }

    private void increment(double amount, boolean large)
    {
        slider.increment(amount, large);
        sliderConfig.markNeedsUpdate();
    }

    @Override
    public void broadcastChanges()
    {
        super.broadcastChanges();

        if (ready)
            sliderConfig.sendUpdates();
    }

    @Override
    public void removed(Player player)
    {
        super.removed(player);
        incrementC2S.close();

        sliderConfig.close();
    }

    @Override
    public void onScreenReady()
    {
        ready = true;
    }

    @FunctionalInterface
    public interface MouseScroll
    {
        void scroll(double amount, boolean large);
    }

    public interface UpdateParams
    {
//        void update(int value, int min, int max, int divisions);
        void update(List<Float> ints);
    }
}
