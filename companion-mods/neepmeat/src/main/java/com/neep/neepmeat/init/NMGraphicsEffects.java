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

package com.neep.neepmeat.init;

import com.neep.meatlib.graphics.GraphicsEffectType;
import com.neep.meatlib.graphics.GraphicsEffects;
import com.neep.neepmeat.NeepMeat;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class NMGraphicsEffects
{
    public static final GraphicsEffectType REMINA = GraphicsEffects.register(NeepMeat.NAMESPACE, "remina", new GraphicsEffectType());
    public static final GraphicsEffectType PHAGE_RAY = GraphicsEffects.register(NeepMeat.NAMESPACE, "phage_ray", new GraphicsEffectType());

    public static void init()
    {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
        {
//            ServerPlayNetworking.send(handler.getPlayer(),GraphicsEffects.CHANNEL_ID, GraphicsEffects.createPacket(REMINA, handler.getPlayer().getWorld()));
        });
    }

}
