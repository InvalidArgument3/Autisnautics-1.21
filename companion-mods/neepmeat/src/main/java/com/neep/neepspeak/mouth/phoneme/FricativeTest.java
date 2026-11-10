package com.neep.neepspeak.mouth.phoneme;

import com.neep.neepspeak.audio.BiquadFilter2;
import com.neep.neepspeak.mouth.ASR;
import com.neep.neepspeak.mouth.Waveforms;
import com.neep.neepspeak.util.SampleBuffer;
import com.neep.neepspeak.util.SampleInfo;

public class FricativeTest implements PlayablePhoneme
{
    private final ASR asr = new ASR(0, 0.00f, 0.01f);
    private final BiquadFilter2 filter = new BiquadFilter2(Waveforms.SAMPLE_RATE);

    public FricativeTest()
    {
        filter.setPeak(1000, 5);
    }

    @Override
    public void processBuffer(SampleBuffer sb, PlayContext context)
    {
        Waveforms.whiteNoise(sb);

        filter.process(sb);

        for (int i = 0; i < sb.regionSize(); i++)
        {
            int n = context.clockN() + i;
            float a = asr.sample(n, Waveforms.SAMPLE_RATE);

            sb.data[sb.startSample + i] = sb.data[sb.startSample + i] * a;
        }
    }

    @Override
    public float length(PlayContext ctx)
    {
        return 0.01f;
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
}
