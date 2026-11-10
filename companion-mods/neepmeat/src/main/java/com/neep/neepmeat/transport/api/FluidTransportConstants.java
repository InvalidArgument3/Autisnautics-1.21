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

package com.neep.neepmeat.transport.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.neep.neepmeat.NeepMeat;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import com.neep.meatlib.transfer.FluidConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class FluidTransportConstants implements SimpleSynchronousResourceReloadListener
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "pipe_constants");

    public static final FluidTransportConstants INSTANCE = new FluidTransportConstants();

    public static final long DEFAULT_MAX_TRANSFER = FluidConstants.BUCKET / 8;
    public static final float DEFAULT_PUMP_HEIGHT = 20; // It's similar to hydraulic head, but... different.

    private long maxTransfer = DEFAULT_MAX_TRANSFER;
    private float basicPumpHeight = DEFAULT_PUMP_HEIGHT;
    private boolean pipeGravityEnabled = false;
    private boolean nodeGravityEnabled = true;

    private FluidTransportConstants() {}

    public long maxTransfer()
    {
        return maxTransfer;
    }

    public boolean pipeGravityEnabled()
    {
        return pipeGravityEnabled;
    }

    public boolean nodeGravityEnabled()
    {
        return nodeGravityEnabled;
    }

    public float basicPumpHeight()
    {
        return basicPumpHeight;
    }

    public ResourceLocation getFabricId()
    {
        return ID;
    }

    public void onResourceManagerReload(ResourceManager manager)
    {
        for (ResourceLocation id : manager.listResources("transport", path -> path.getPath().endsWith("transport/fluid_transport.json")).keySet())
        {
            if (manager.getResource(id).isPresent())
            {
                try (InputStream stream = manager.getResource(id).get().open())
                {
                    Reader reader = new InputStreamReader(stream);
                    JsonElement rootElement = JsonParser.parseReader(reader);
                    JsonObject rootObject = (JsonObject) rootElement;

                    // Disallow negative values
                    maxTransfer = Math.max(0, GsonHelper.getAsLong(rootObject, "basic_pipe_throughput"));
                    basicPumpHeight = Math.max(0, GsonHelper.getAsFloat(rootObject, "basic_pump_height"));

                    nodeGravityEnabled = GsonHelper.getAsBoolean(rootObject, "enable_node_gravity");
                    pipeGravityEnabled = GsonHelper.getAsBoolean(rootObject, "enable_pipe_gravity");

                }
                catch (Exception e)
                {
                    NeepMeat.LOGGER.error("Error while reading fluid transport constants {}", id.toString(), e);
//                    maxTransfer = DEFAULT_MAX_TRANSFER;
//                    basicPumpHeight = DEFAULT_PUMP_HEIGHT;
                }
            }
        }
    }
}
