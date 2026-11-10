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
import com.neep.meatlib.network.ChannelManager;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.implant.entity.PlayerImplantManager;
import com.neep.neepmeat.implant.entity.PoweredKneesImplant;
import com.neep.neepmeat.screen_handler.ImplantManagerScreenHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class PoweredKneesConfigHandler implements ImplantConfigHandler
{
    public final ChannelManager<SetJumpPower> jumpPowerC2S;
    private final Player player;
    private final ImplantManagerScreenHandler handler;
    private boolean updateToClient = true;

    public PoweredKneesConfigHandler(ImplantManagerScreenHandler handler, Object implant, Player player)
    {
        this.handler = handler;
        jumpPowerC2S = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "sync_jump_power"),
                ChannelFormat.builder(SetJumpPower.class).param(ParamCodec.FLOAT).build(),
                player);
        this.player = player;

        if (!handler.isClient())
        {
            jumpPowerC2S.receiver(this::setJumpPower);
        }
    }

    private void setJumpPower(float power)
    {
        PlayerImplantManager manager = PlayerImplantManager.get(player);
        PoweredKneesImplant implant = manager.getImplant(PoweredKneesImplant.ID);
        if (implant != null)
        {
            implant.setJumpPower(power);
        }

        manager.sync();

        updateToClient = true;
    }

    @Override
    public void sendUpdates()
    {
        if (updateToClient)
        {
            PlayerImplantManager manager = PlayerImplantManager.get(player);
            PoweredKneesImplant implant = manager.getImplant(PoweredKneesImplant.ID);
            if (implant != null)
            {
                jumpPowerC2S.emitter().apply(implant.getJumpPower());
            }

            updateToClient = false;
        }
    }

    @Override
    public void close()
    {
        jumpPowerC2S.close();
    }

    public interface SetJumpPower
    {
        void apply(float power);
    }
}
