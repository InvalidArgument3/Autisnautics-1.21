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

package com.neep.neepmeat.client;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.network.ToolTransformPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import static com.neep.meatweapons.client.MWKeys.registerKeyBinding;

@Environment(value= Dist.CLIENT)
public class NMKeys
{
    public static KeyMapping TOOL_TRANSFORM;

    public static void registerKeybindings()
    {
        TOOL_TRANSFORM = registerKeyBinding(NeepMeat.NAMESPACE, "transform", "neepmeat", GLFW.GLFW_KEY_C);

        ClientTickEvents.END_CLIENT_TICK.register(client ->
        {
            while (TOOL_TRANSFORM.consumeClick())
            {
                ToolTransformPacket.Client.send();
            }
        });
    }
}
