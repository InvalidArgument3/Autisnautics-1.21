package com.neep.neepspeak.mouth.phoneme;

import com.neep.neepspeak.audio.SoundData;
import com.neep.neepspeak.audio.SoundDataProvider;
import com.neep.neepspeak.util.SampleBuffer;
import com.neep.neepspeak.util.SampleInfo;

public class ObstruentPhoneme implements PlayablePhoneme
{
    private final SoundData data;
    private final SoundDataProvider entry;
    private final float length;

    public ObstruentPhoneme(SoundDataProvider entry, float length)
    {
        this.data = entry.get();
        this.entry = entry;
        this.length = length;
    }

    public ObstruentPhoneme(SoundDataProvider entry)
    {
        this(entry, 0.1f);
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
        return "[" + entry.name() + "]";
    }
}
