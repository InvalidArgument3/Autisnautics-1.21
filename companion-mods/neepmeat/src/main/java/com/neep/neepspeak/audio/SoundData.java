package com.neep.neepspeak.audio;

import com.neep.neepspeak.util.Util;

public class SoundData
{
    private final float[] data;

    public SoundData(String resourcePath)
    {
        data = Util.loadFileToFloats(resourcePath);
    }

    public float[] data()
    {
        return data;
    }
}
