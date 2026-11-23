package com.neep.neepspeak.audio;

import com.neep.neepspeak.util.SampleBuffer;

// From https://github.com/hosackm/BiquadFilter/blob/master/Biquad.c
public class BiquadFilter2 implements Filter
{
    private final float fs;

    private final float[] prevInput = new float[2];
    private final float[] prevOutput = new float[2];

    private float b0;
    private float b1;
    private float b2;

    private float a0;
    private float a1;
    private float a2;
    private float gain = 1;

    public BiquadFilter2(float fs)
    {
        this.fs = fs;
    }

    private void normaliseCoefficients()
    {
        a1 /= a0;
        a2 /= a0;
        b0 /= a0;
        b1 /= a0;
        b2 /= a0;
    }

    public void setGain(float gainAbs)
    {
        this.gain = gainAbs;
    }

    public void setPeak(float fc, float q)
    {
        float omega = (float) (2 * Math.PI * fc / fs);
        float sn = (float) Math.sin(omega);
        float cs = (float) Math.cos(omega);
        float alpha = sn / (2 * q);

        b0 = 1 + (alpha * gain);
        b1 = -2 * cs;
        b2 = 1 - (alpha * gain);
        a0 = 1 + (alpha / gain);
        a1 = -2 * cs;
        a2 = 1 - (alpha / gain);

        normaliseCoefficients();
    }

    public void setLowpass(float fc, float q)
    {
        float omega = (float) (2 * Math.PI * fc / fs);
        float sn = (float) Math.sin(omega);
        float cs = (float) Math.cos(omega);
        float alpha = sn / (2 * q);
        float beta = (float) Math.sqrt(gain + gain);

        b0 = (1.0f - cs) / 2.0f;
        b1 = 1.0f - cs;
        b2 = (1.0f - cs) / 2.0f;
        a0 = 1.0f + alpha;
        a1 = -2.0f * cs;
        a2 = 1.0f - alpha;

        normaliseCoefficients();
    }

    @Override
    public void process(SampleBuffer sb)
    {
        for (int i = sb.startSample; i < sb.endSample; i++)
        {
            float input = sb.data[i];

            float output = (b0 * input) +
                    (b1 * prevInput[0]) +
                    (b2 * prevInput[1]) -
                    (a1 * prevOutput[0]) -
                    (a2 * prevOutput[1]);

            prevInput[1] = prevInput[0];
            prevInput[0] = input;
            prevOutput[1] = prevOutput[0];
            prevOutput[0] = output;

            sb.data[i] = output;
        }
    }
}
