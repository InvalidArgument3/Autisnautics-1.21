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

package com.neep.neepmeat.enlightenment.event;

import com.neep.meatlib.graphics.GraphicsEffects;
import com.neep.neepmeat.api.enlightenment.EnlightenmentUtil;
import com.neep.neepmeat.api.enlightenment.TickingEnlightenmentEvent;
import com.neep.neepmeat.enlightenment.EnlightenmentManager;
import com.neep.neepmeat.enlightenment.PlayerEnlightenmentManager;
import com.neep.neepmeat.init.NMGraphicsEffects;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ReminaEnlightenmentEvent extends TickingEnlightenmentEvent
{
    private final PlayerEnlightenmentManager manager;
    int maxTime;

    public ReminaEnlightenmentEvent(Level world, ServerPlayer player)
    {
        super(world, player);
        manager = EnlightenmentManager.get(player);
        int multiplier = EnlightenmentUtil.getEnlightenment(player) > EnlightenmentUtil.THRESHOLD_MEDIUM ? 2 : 1;

        maxTime = multiplier * 60 * 20;
    }

    @Override
    public void spawn()
    {
        super.spawn();

        FriendlyByteBuf byteBuf = PacketByteBufs.create();
        GraphicsEffects.CHANNEL.emitter(player).apply(world.dimension(), NMGraphicsEffects.REMINA, Vec3.ZERO, Vec3.ZERO, Vec3.ZERO, 1, 1, byteBuf);
        manager.objectAbove = true;
        manager.sync();
    }

    @Override
    public void tick()
    {
        super.tick();
        if (time > maxTime)
        {
            manager.objectAbove = false;
            manager.sync();
            markRemoved();
        }
        else
        {
            manager.objectAbove = true;
        }
    }

    public static boolean canSpawn(Level world, ServerPlayer player)
    {
//        return EnlightenmentUtil.getEnlightenment(player) > EnlightenmentUtil.THRESHOLD_LOW;
        return player.level().dimension().equals(Level.OVERWORLD);
    }
}
