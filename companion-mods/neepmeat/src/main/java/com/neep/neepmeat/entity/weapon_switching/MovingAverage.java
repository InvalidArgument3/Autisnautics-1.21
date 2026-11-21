package com.neep.neepmeat.entity.weapon_switching;

import java.util.Arrays;
import net.minecraft.util.Mth;

public class MovingAverage
{
    private final int size;
    private int head = 0;
    private final boolean[] hits;

    public MovingAverage(int size)
    {
        this.size = size;
        hits = new boolean[size];
        Arrays.fill(hits, true);
    }

    public void add(boolean hit)
    {
        head = (head + 1) % size;
        hits[head] = hit;
    }

    public float get()
    {
        float avg = 0;
        for (int i = 0; i < size; i++)
        {
            int idx = Mth.positiveModulo(i - size - head, size);
            avg += hits[idx] ? 1 : 0;
        }

        return avg / size;
    }

    public void reset()
    {
        for (int i = 0; i < size; i++)
        {
            hits[i] = true;
        }
    }
}
