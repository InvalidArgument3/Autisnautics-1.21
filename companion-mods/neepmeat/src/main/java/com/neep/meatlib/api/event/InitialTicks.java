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

package com.neep.meatlib.api.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Queue;

public final class InitialTicks
{
    private static final HashMap<ServerLevel, InitialTicks> MAP = new HashMap<>(4);
    private final Queue<InitialTickListener> queue = new ArrayDeque<>();
    private final ServerLevel world;

    private InitialTicks(ServerLevel world)
    {
        this.world = world;
    }

    public static InitialTicks getInstance(ServerLevel world)
    {
        return MAP.computeIfAbsent(world, InitialTicks::new);
    }

    public void queue(InitialTickListener listener)
    {
        queue.add(listener);
    }

    public static void init()
    {
//        ServerWorldEvents.LOAD.register((server, world) ->
//        {
//            MAP.put(world, new InitialTicks(world));
//        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server ->
        {
            MAP.clear();
        });

        ServerTickEvents.START_WORLD_TICK.register(world ->
        {
            InitialTicks instance = getInstance(world);
            instance.tick();
        });
    }

    private void tick()
    {
        while (!queue.isEmpty())
        {
            queue.poll().load(world);
        }
    }

    public interface InitialTickListener
    {
        void load(ServerLevel world);
    }
}
