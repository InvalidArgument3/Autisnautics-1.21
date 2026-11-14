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

package com.neep.neepmeat.api.live_machine.metrics;

import com.neep.meatlib.api.network.ParamCodec;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.live_machine.LivingMachineBlockEntity;
import com.neep.neepmeat.api.live_machine.LivingMachineCols;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import java.util.Arrays;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public class DataLog
{
    private long timeStart = -1;

    private final LongDataBuffer time;
    private final FloatDataBuffer efficiency;
    private final FloatDataBuffer health;

    private final int sampleInterval;
    private final int capacity;

    public static final ParamCodec<DataView> PARAM_CODEC = ParamCodec.of(DataView.class, DataView::write, DataView::read);

    public DataLog(int capacity, int sampleInterval)
    {
        this.capacity = capacity;
        this.sampleInterval = sampleInterval;

        this.time = new LongDataBuffer(capacity);
        this.efficiency = new FloatDataBuffer(capacity);
        this.health = new FloatDataBuffer(capacity);
    }

    public void log(long time, LivingMachineBlockEntity be)
    {
        if (be.getAge() % sampleInterval != 0)
            return;

        if (timeStart == -1)
        {
            timeStart = time;
        }

        time -= timeStart;

        this.time.enqueueCircular(time);

        efficiency.enqueueCircular(be.getEfficiency());
        health.enqueueCircular(be.getHealth());
    }

    public DataView asView()
    {
        return new DataView(capacity, efficiency.size(),
                time.encode(),
                efficiency.toDoubleArray(),
                health.toDoubleArray()
        );
    }

    private static final List<ObjectIntPair<Component>> LEGEND = List.of(
            ObjectIntPair.of(NeepMeat.translationKey("screen", "living_machine.efficiency"), LivingMachineCols.EFFICIENCY),
            ObjectIntPair.of(NeepMeat.translationKey("screen", "living_machine.health"), LivingMachineCols.HEALTH)
    );

    public static record DataView(int capacity, int size,
                                  long[] time,
                                  double[] efficiency,
                                  double[] health)
    {
        public static final DataView EMPTY = new DataView(0, 0, new long[0],
                new double[0],
                new double[0]
        );

        public List<ObjectIntPair<Component>> getLegend()
        {
            return LEGEND;
        }

        public void write(FriendlyByteBuf buf)
        {
            buf.writeInt(capacity);
            buf.writeInt(efficiency.length);

            buf.writeLongArray(time);
            // Casting to float should be fine as
            buf.writeVarIntArray(Arrays.stream(efficiency).mapToInt(d -> Float.floatToIntBits((float) d)).toArray());
            buf.writeVarIntArray(Arrays.stream(health).mapToInt(d -> Float.floatToIntBits((float) d)).toArray());
        }

        public static DataView read(FriendlyByteBuf buf)
        {
            int capacity = buf.readInt();
            int size = buf.readInt();

            long[] time = buf.readLongArray();

            // There's no FloatStream in Java for some mysterious reason
            double[] efficiency = Arrays.stream(buf.readVarIntArray()).mapToDouble(Float::intBitsToFloat).toArray();
            double[] health = Arrays.stream(buf.readVarIntArray()).mapToDouble(Float::intBitsToFloat).toArray();

            return new DataView(capacity, size, time,
                    efficiency,
                    health);
        }
    }
}
