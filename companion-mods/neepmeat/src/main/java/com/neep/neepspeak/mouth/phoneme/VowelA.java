package com.neep.neepspeak.mouth.phoneme;

import com.neep.neepspeak.audio.AudioComponent;
import com.neep.neepspeak.audio.BiquadFilter2;
import com.neep.neepspeak.mouth.Waveforms;
import com.neep.neepspeak.util.SampleBuffer;
import com.neep.neepspeak.util.SampleInfo;

public class VowelA implements PlayablePhoneme, AudioComponent
{
    // OH NO! WE CAN'T DO THIS! ALL FIELDS MUST BE IMMUTABLE DURING PLAYBACK!
    private SampleInfo info;
    private BiquadFilter2 f1;
    private BiquadFilter2 f2;
    private BiquadFilter2 f3;

    private SampleBuffer b1;
    private SampleBuffer b2;
    private SampleBuffer b3;

    public VowelA()
    {
    }

    public void init(SampleInfo info)
    {
        this.info = info;
        this.f1 = new BiquadFilter2(info.fs());
        this.f2 = new BiquadFilter2(info.fs());
        this.f3 = new BiquadFilter2(info.fs());

        float q = 2;
        f1.setLowpass(700, q);
        f2.setLowpass(1220, q);
        f3.setLowpass(2600, q);

        this.b1 = new SampleBuffer(info.frameSize());
        this.b2 = new SampleBuffer(info.frameSize());
        this.b3 = new SampleBuffer(info.frameSize());
    }

    @Override
    public void processBuffer(SampleBuffer sb, PlayContext context)
    {
//        sb.clearRegion();
        float f0 = 140;
        int nharm = (int) ((info.fs() / 2) / f0);
        Waveforms.impulseTrain(sb, info.fs(), f0, nharm, 0.5f);

        int size = sb.regionSize();
        // This is highly suboptimal
        sb.copyInto(b1, size);
        sb.copyInto(b2, size);
        sb.copyInto(b3, size);

        f1.process(b1);
        f2.process(b2);
        f3.process(b3);

        sb.clearRegion();
        b1.addInto(sb, size);
        b2.addInto(sb, size);
        b3.addInto(sb, size);
    }

    @Override
    public float length(PlayContext ctx)
    {
        return 0.2f;
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
}
