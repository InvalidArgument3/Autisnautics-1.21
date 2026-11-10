package com.neep.neepspeak.mouth.phoneme;

import com.neep.neepspeak.util.SampleBuffer;
import com.neep.neepspeak.util.SampleInfo;

// In the future this might do some special things to the phonemes
// For now it's a dirty hack
public class DiphthongPhoneme implements PlayablePhoneme
{
    private final PlayablePhoneme p1;
    private final PlayablePhoneme p2;
    private SampleInfo info;

    public DiphthongPhoneme(PlayablePhoneme p1, PlayablePhoneme p2)
    {
        this.p1 = p1;
        this.p2 = p2;
    }

    public PlayablePhoneme getP1()
    {
        return p1;
    }

    public PlayablePhoneme getP2()
    {
        return p2;
    }

    @Override
    public void processBuffer(SampleBuffer sb, PlayContext ctx)
    {
//        float offsetS = ctx.clockN() / info.fs();
//
//        int p1LengthN = (int) (p1.length(ctx) / info.fs());
//        int p2LengthN = (int) (p2.length(ctx) / info.fs());
//
//        int samplesRemaining = sb.regionSize();
//
//        while (samplesRemaining > 0)
//        {
//            int samplesThisTime =
//        }
//
//        if (offsetS < p1.length(ctx))
//        {
//            p1.processBuffer(sb, ctx);
//        }
//        else
//        {
//            p2.processBuffer(sb, ctx);
//        }
    }

    @Override
    public float length(PlayContext ctx)
    {
        return p1.length(ctx) + p2.length(ctx);
    }

    @Override
    public float leadIn()
    {
        return p1.leadIn();
    }

    @Override
    public float tailOut()
    {
        return p2.tailOut();
    }

    @Override
    public void init(SampleInfo info)
    {
        this.info = info;
    }

    @Override
    public String toString()
    {
        return p1.toString() + p2.toString();
    }
}
