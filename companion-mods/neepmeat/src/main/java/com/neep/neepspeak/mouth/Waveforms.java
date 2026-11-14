package com.neep.neepspeak.mouth;

import com.neep.neepspeak.util.SampleBuffer;

public class Waveforms
{
    public static final int MAX_AMPLITUDE = Integer.MAX_VALUE / 2;
    public static final float SAMPLE_RATE = 44100;

    public static void vowelAa(SampleBuffer sb, float fs)
    {
        float f0 = 700;
        float f1 = 1220;
        float f2 = 2600;


        for (int i = sb.startSample; i < sb.endSample; i++)
        {
            float thing = fs / 440;
            double angle = (i / thing) % 1;
            sb.put(i, (float) (angle));
        }
    }

    public static void sawtooth(SampleBuffer sb, float fs, float f0, float amp)
    {
        for (int i = sb.startSample; i < sb.endSample; i++)
        {
            float thing = fs / f0;
            double angle = (i / thing) % 1;
            sb.put(i, (float) ((angle - 0.5) * 2 * amp));
        }
    }

    public static void impulseTrain(SampleBuffer sb, float fs, float f0, int harmonics, float gain)
    {
        for (int i = 0; i < harmonics; ++i)
        {
            float w0T = (float) (2 * Math.PI * f0 / fs);
            for (int n = sb.startSample; n < sb.endSample; n++)
            {
                double angle = n * i * w0T;
                sb.data[n] += (float) (Math.cos(angle) * gain) / harmonics;
            }
        }
    }

    public static void sine(SampleBuffer sb, float fs, float f0, float gain)
    {
        for (int i = sb.startSample; i < sb.endSample; i++)
        {
            float thing = fs / f0;
            double angle = (i / thing) * 2 * Math.PI;
            sb.put(i, (float) (Math.sin(angle) * gain));
        }
    }

    public static void sineSweep(SampleBuffer sb, float fs, float f0, float f1, float gain)
    {
        float f = f0;
        float step = (f1 - f0) / sb.regionSize();

        for (int i = sb.startSample; i < sb.endSample; i++)
        {
            float thing = fs / f;
            double angle = (i / thing) * 2 * Math.PI;
            sb.put(i, (float) (Math.sin(angle) * gain));

            f += step;
        }
    }

    public static void sineSweep(SampleBuffer sb, float fs, float f0, float f1, int buckets, float gain)
    {
        float step = (f1 - f0) / buckets;
        int size = sb.regionSize();

        int bucketSamples = size / buckets;
        float bucketTime = (float) bucketSamples / fs;
        float rise = 0.1f * bucketTime;
        ASR env = new ASR(rise, bucketTime - 2 * rise, rise);

        for (int i = sb.startSample; i < sb.endSample; i++)
        {
            float amp = gain * env.sample((i - sb.startSample) % bucketSamples, fs);

            int bucket = (int) (Math.floor((double) buckets * i / size));
            float f = bucket * step;

            float thing = fs / f;
            double angle = (i / thing) * 2 * Math.PI;
            sb.put(i, (float) (Math.sin(angle) * amp));
        }
    }

    public static void sineSweep(SampleBuffer sb, float fs, float[] fRange, float gain)
    {
        for (int i = 0; i < sb.regionSize(); ++i)
        {
            float thing = fs / fRange[i];
            double angle = (i / thing) * 2 * Math.PI;
            sb.put(sb.startSample + i, (float) (Math.sin(angle) * gain));
        }
    }

    public static void whiteNoise(SampleBuffer sb)
    {
        for (int i = sb.startSample; i < sb.endSample; i++)
        {
            sb.put(i, (float) (2 * (Math.random() - 0.5)));
        }
    }
}
