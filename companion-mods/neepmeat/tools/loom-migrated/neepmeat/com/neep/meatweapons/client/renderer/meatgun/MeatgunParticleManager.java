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

package com.neep.meatweapons.client.renderer.meatgun;

import com.neep.meatweapons.client.particle.MeatgunParticle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class MeatgunParticleManager
{
    private static final List<MeatgunParticle> PARTICLES = new ArrayList<>();

    public static void add(MeatgunParticle particle)
    {
        PARTICLES.add(particle);
    }

    public static void init()
    {
        ClientTickEvents.END_CLIENT_TICK.register(client -> PARTICLES.removeIf(MeatgunParticle::isParticleRemoved));
        ClientPlayConnectionEvents.DISCONNECT.register((clientPlayNetworkHandler, minecraftClient) -> PARTICLES.clear());
    }

    public static Iterable<MeatgunParticle> getParticles()
    {
        return PARTICLES;
    }
}
