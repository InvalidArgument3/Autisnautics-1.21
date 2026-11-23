package com.neep.neepspeak.audio;

import com.neep.neepspeak.util.SampleBuffer;

public interface Filter
{
    void process(SampleBuffer sb);
}
