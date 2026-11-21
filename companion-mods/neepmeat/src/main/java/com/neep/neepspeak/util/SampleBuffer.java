package com.neep.neepspeak.util;

import java.util.Arrays;

public class SampleBuffer
{
    public final float[] data;

    public int startSample;
    public int endSample;

    public SampleBuffer(float[] backing, int startSample, int endSampleExc)
    {
        this.data = backing;
        this.startSample = startSample;
        this.endSample = endSampleExc;
    }

    public SampleBuffer(int size)
    {
        this(new float[size], 0, size);
    }

    public void put(int n, float value)
    {
        data[n] = value;
    }

    public float[] array()
    {
        return data;
    }

    public int regionSize()
    {
        return endSample - startSample;
    }

    public void copyInto(SampleBuffer other, int size)
    {
        System.arraycopy(data, startSample, other.data, other.startSample, size);
    }

    public void addInto(SampleBuffer other, int size)
    {
        for (int i = 0; i < size; i++)
        {
            other.data[other.startSample + i] += data[startSample + i];
        }
    }

    public void addInto(SampleBuffer other, int sourceStart, int destStart, int size)
    {
        for (int i = 0; i < size; i++)
        {
            other.data[destStart + i] += data[sourceStart + i];
        }
    }

    public void clearRegion()
    {
        Arrays.fill(data, startSample, endSample, 0);
    }

    public void fillFrom(float[] data, int sourceStart, int destStart, int size)
    {
        System.arraycopy(data, sourceStart, this.data, destStart, size);
    }

    public void applyRamp(float a0, float a1, int start, int endExc)
    {
        float a = a0;
        float step = (a1 - a0) / (endExc - start);
        for (int i = start; i < endExc; ++i)
        {
            data[i] *= a;
            a += step;
        }
    }
}
