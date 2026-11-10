package com.neep.neepspeak.mouth;

import com.neep.neepspeak.util.Util;

public class ASR
{
    private final float a;
    private final float s;
    private final float r;

    public ASR(float a, float s, float r)
    {
        this.a = a;
        this.s = s;
        this.r = r;
    }

    public float sample(int n, float fs)
    {
        float nS = n / fs;

        if (nS < a)
            return Util.lerp(nS / a, 0, 1);

        if (nS < a + s)
            return 1;

        return Math.max(0, Util.lerp((nS - (a + s)) / r, 1, 0));
    }
}
