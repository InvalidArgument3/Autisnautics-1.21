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

package com.neep.neepmeat.implant.config;

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.ParamCodecs;
import com.neep.meatlib.network.ChannelManager;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.implant.entity.CatEarsImplant;
import com.neep.neepmeat.screen_handler.ImplantManagerScreenHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class CatEarsConfigHandler implements ImplantConfigHandler
{
    private final ImplantManagerScreenHandler screenHandler;
    private final CatEarsImplant implant;

    public final ChannelManager<UpdateSkin> updateAppearanceC2S;
    public final ChannelManager<UpdateCol> updateColC2S;
    public final ChannelManager<UpdateCol> updateInnerColC2S;

    public CatEarsConfigHandler(ImplantManagerScreenHandler screenHandler, CatEarsImplant implant, Player player)
    {
        this.screenHandler = screenHandler;
        this.implant = implant;
        updateAppearanceC2S = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "cat_ears_appearance"),
                ChannelFormat.builder(UpdateSkin.class).param(ParamCodecs.IDENTIFIER).build(),
                player);

        updateColC2S = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "cat_ears_col"),
                ChannelFormat.builder(UpdateCol.class).param(ParamCodec.INT).build(),
                player);

        updateInnerColC2S = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "cat_ears_inner_col"),
                ChannelFormat.builder(UpdateCol.class).param(ParamCodec.INT).build(),
                player);

        updateAppearanceC2S.receiver(this::receiveAppearance);
        updateColC2S.receiver(this::receiveCol);
        updateInnerColC2S.receiver(this::receiveInnerCol);
    }

    private void receiveAppearance(ResourceLocation skin)
    {
        implant.setAppearance(skin, implant.getCol(), implant.getInnerCol());
    }

    private void receiveCol(int i)
    {
        implant.setAppearance(implant.getSkin(), i, implant.getInnerCol());
    }

    private void receiveInnerCol(int i)
    {
        implant.setAppearance(implant.getSkin(), implant.getCol(), i);
    }

    @Override
    public void sendUpdates()
    {
    }

    @Override
    public void close()
    {
        updateAppearanceC2S.close();
        updateColC2S.close();
        updateInnerColC2S.close();

        var manager = screenHandler.getImplantManager();
        if (manager != null)
            manager.sync();
    }

    public CatEarsImplant getImplant()
    {
        return implant;
    }

    public interface UpdateSkin
    {
        void apply(ResourceLocation skin);
    }

    public interface UpdateCol
    {
        void apply(int col);
    }
}
