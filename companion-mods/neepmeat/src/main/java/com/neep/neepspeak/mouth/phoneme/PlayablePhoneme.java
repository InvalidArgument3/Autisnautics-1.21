package com.neep.neepspeak.mouth.phoneme;

import com.neep.neepspeak.audio.AudioComponent;
import com.neep.neepspeak.util.SampleBuffer;

public interface PlayablePhoneme extends AudioComponent
{
    void processBuffer(SampleBuffer sb, PlayContext ctx);

    float length(PlayContext ctx);

    float leadIn();

    float tailOut();

    interface PlayContext
    {
        int clockN();
    }

    record SimplePlayContext(int clockN) implements PlayContext { }
}
