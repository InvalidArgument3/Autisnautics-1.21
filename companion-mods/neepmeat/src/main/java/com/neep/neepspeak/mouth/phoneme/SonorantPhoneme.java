package com.neep.neepspeak.mouth.phoneme;

import com.neep.neepspeak.audio.SoundData;
import com.neep.neepspeak.audio.SoundDataProvider;
import com.neep.neepspeak.util.SampleBuffer;
import com.neep.neepspeak.util.SampleInfo;

public class SonorantPhoneme implements PlayablePhoneme
{
    private final SoundData data;
    private final SoundDataProvider entry;
    private final float length;
    private boolean crossfade;

    public SonorantPhoneme(SoundDataProvider entry, float length, boolean crossfade)
    {
        this.data = entry.get();
        this.entry = entry;
        this.length = length;
        this.crossfade = crossfade;
    }

    public SonorantPhoneme(SoundDataProvider entry)
    {
        this(entry, 0.1f, true);
    }

    @Override
    public void processBuffer(SampleBuffer sb, PlayContext ctx)
    {
        int offset = ctx.clockN();
        int samplesToFill = Math.min(sb.regionSize(), data.data().length - offset);
        if (samplesToFill > 0)
        {
            sb.fillFrom(data.data(), offset, sb.startSample, samplesToFill);
        }
    }

    @Override
    public float length(PlayContext ctx)
    {
        return length;
    }

    @Override
    public float leadIn()
    {
        return crossfade ? 0.01f : 0;
    }

    @Override
    public float tailOut()
    {
        return 0.01f;
    }

    @Override
    public void init(SampleInfo info)
    {

    }

    @Override
    public String toString()
    {
        return "[" + entry.name() + "]";
    }
}
