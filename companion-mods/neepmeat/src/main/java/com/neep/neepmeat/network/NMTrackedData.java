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

package com.neep.neepmeat.network;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.level.material.Fluid;

public class NMTrackedData
{
    public static final EntityDataSerializer<Double> DOUBLE = new EntityDataSerializer<>()
    {

        public void write(FriendlyByteBuf packetByteBuf, Double d)
        {
            packetByteBuf.writeDouble(d);
        }

        public Double read(FriendlyByteBuf packetByteBuf)
        {
            return (double) packetByteBuf.readFloat();
        }

        @Override
        public Double copy(Double d)
        {
            return d;
        }
    };
    public static final EntityDataSerializer<Fluid> FLUID = new EntityDataSerializer<Fluid>()
    {
        public void write(FriendlyByteBuf buf, Fluid value)
        {
            buf.writeById(BuiltInRegistries.FLUID::getId, value);
        }

        public Fluid read(FriendlyByteBuf buf)
        {
            return buf.readById(BuiltInRegistries.FLUID::byId);
        }

        @Override
        public Fluid copy(Fluid value)
        {
            return value;
        }
    };

    public static void init()
    {
        EntityDataSerializers.registerSerializer(DOUBLE);
        EntityDataSerializers.registerSerializer(FLUID);
    }
}
