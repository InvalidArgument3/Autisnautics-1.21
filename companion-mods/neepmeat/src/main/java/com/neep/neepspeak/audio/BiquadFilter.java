package com.neep.neepspeak.audio;

import com.neep.neepspeak.util.SampleBuffer;

// Features borrowed from STK
public class BiquadFilter implements Filter
{
    private final float fs;

    private final float[] prevX = new float[3];
    private final float[] prevY = new float[3];

    private float fc;

    private float a0;
    private float a1;
    private float a2;

    private float b0;
    private float b1;
    private float b2;

    public BiquadFilter(float fs)
    {
        this.fs = fs;
    }

    public void setLowpass(float fc, float q)
    {
        float k = (float) Math.tan(Math.PI * fc / fs);
        float kk = k * k;
        float denom = 1 / (kk * q + k + q);

        a1 = 2 * q * (kk - 1) * denom;
        a2 = (kk * q - k + q) * denom;

        b0 = kk * q * denom;
        b1 = 2 * b0;
        b2 = b0;
    }

    public void setResonant(float fc, float radius, boolean normalize)
    {
        a2 = radius * radius;
        a1 = (float) (-2.f * radius * Math.cos(2 * Math.PI * fc / fs));

        if (normalize)
        {
            // Use zeros at +- 1 and normalize the filter peak gain.
            b0 = 0.5f - 0.5f * a2;
            b1 = 0.0f;
            b2 = -b0;
        }
        else
        {
            b0 = 1.0f;
            b1 = 0.0f;
            b2 = 0.0f;
        }
    }

    public void process(SampleBuffer sb)
    {
        float g = 1;

        for (int i = sb.startSample; i < sb.endSample; i++)
        {
            prevX[0] = g * sb.data[i];
            float res = b0 * prevX[0] + b1 * prevX[1] + b2 * prevX[2];
            res -= a2 * prevY[2] + a1 * prevY[1];
            prevX[2] = prevX[1];
            prevX[1] = prevX[0];
            prevY[2] = prevY[1];
            prevY[1] = res;

            sb.data[i] = res;
        }
    }
}
