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
import com.neep.meatlib.api.network.ParamCodecs;
import com.neep.meatlib.network.ChannelManager;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.implant.entity.PlayerImplantManager;
import com.neep.neepmeat.implant.entity.SpiderLegsImplant;
import com.neep.neepmeat.screen_handler.ImplantManagerScreenHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class HexapodImplantConfigHandler implements ImplantConfigHandler
{
    public final ChannelManager<SkinInstall> skinInstallC2S;
    private final Player player;
    private final ImplantManagerScreenHandler handler;

    public HexapodImplantConfigHandler(ImplantManagerScreenHandler handler, Object implant, Player player)
    {
        this.handler = handler;
        skinInstallC2S = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "hexapod_implant_install"),
                ChannelFormat.builder(SkinInstall.class).param(ParamCodecs.IDENTIFIER).build(),
                player);
        this.player = player;

        if (!handler.isClient())
        {
            skinInstallC2S.receiver(this::setSkin);
        }
    }

    private void setSkin(ResourceLocation identifier)
    {
        PlayerImplantManager manager = PlayerImplantManager.get(player);
        SpiderLegsImplant implant = manager.getImplant(SpiderLegsImplant.ID);
        if (implant != null)
        {
            implant.setModelId(identifier);
        }

        manager.sync();
    }

    @Override
    public void sendUpdates()
    {

    }

    @Override
    public void close()
    {
        skinInstallC2S.close();
    }

    public interface SkinInstall
    {
        void apply(ResourceLocation identifier);
    }
}
