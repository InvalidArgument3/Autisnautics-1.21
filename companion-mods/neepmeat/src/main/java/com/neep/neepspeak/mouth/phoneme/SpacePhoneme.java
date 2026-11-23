package com.neep.neepspeak.mouth.phoneme;

import com.neep.neepspeak.util.SampleBuffer;
import com.neep.neepspeak.util.SampleInfo;

public class SpacePhoneme implements PlayablePhoneme
{
    private final float length;

    public SpacePhoneme(float length)
    {
        this.length = length;
    }

    @Override
    public void processBuffer(SampleBuffer sb, PlayContext ctx)
    {

    }

    @Override
    public float length(PlayContext ctx)
    {
        return length;
    }

    @Override
    public float leadIn()
    {
        return 0;
    }

    @Override
    public float tailOut()
    {
        return 0;
    }

    @Override
    public void init(SampleInfo info)
    {

    }

    @Override
    public String toString()
    {
        return "[SPACE]";
    }
}
