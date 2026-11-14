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

package com.neep.neepmeat.transport;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.transport.blood_network.BloodNetworkChunkComponent;
import dev.onyxstudios.cca.api.v3.block.BlockComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.block.BlockComponentInitializer;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.minecraft.resources.ResourceLocation;

public class TransportComponents implements ChunkComponentInitializer, BlockComponentInitializer
{
    public static final ComponentKey<BloodNetworkChunkComponent> BLOOD_NETWORK =
            ComponentRegistry.getOrCreate(
                    ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "blood_network_chunk"),
                    BloodNetworkChunkComponent.class);


    @Override
    public void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry)
    {
        registry.register(BLOOD_NETWORK, BloodNetworkChunkComponent.class, BloodNetworkChunkComponent::new);
    }

    @Override
    public void registerBlockComponentFactories(BlockComponentFactoryRegistry registry)
    {

    }
}
