package com.neep.neepspeak.util;

import com.neep.neepspeak.mouth.Waveforms;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Util
{
    public static float lerp(float d, float v1, float v2)
    {
        return v1 + d * (v2 - v1);
    }

    public static float max(float[] values)
    {
        float max = 0;
        for (var f : values)
        {
            max = Math.max(f, max);
        }
        return max;
    }

    public static float[] loadFileToFloats(String resourcePath)
    {
        try (AudioInputStream stream = Util.loadResource(resourcePath))
        {
            if (stream != null)
            {
                var format = stream.getFormat();
                byte[] bytes = stream.readAllBytes();

                ByteBuffer bb = ByteBuffer.allocate(bytes.length);

                bb.order(format.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
                int sampleBytes = format.getSampleSizeInBits() / 8;
                int channels = format.getChannels();

                if (channels != 1 || sampleBytes != 2
                        || format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED
                        || format.getSampleRate() != Waveforms.SAMPLE_RATE)
                    throw new IllegalArgumentException("audio file " + resourcePath + " does not match required format");

                bb.put(bytes);
                int numSamples = bytes.length / sampleBytes;

                float[] floats = new float[numSamples];

                for (int i = 0; i < numSamples; i++)
                {
                    short s = bb.getShort(i * sampleBytes);
                    floats[i] = (float) (s / (double) Short.MAX_VALUE);
                }

                return floats;
            }
        }
        catch (UnsupportedAudioFileException | IOException e)
        {
            throw new IllegalArgumentException("Failed to load file %s: %s".formatted(resourcePath, e));
        }

        throw new IllegalArgumentException("Failed to load file %s".formatted(resourcePath));
    }

    public static AudioInputStream loadResource(String resourcePath) throws UnsupportedAudioFileException, IOException
    {
        InputStream stream = Util.class.getClassLoader().getResourceAsStream(resourcePath);

        if (stream != null)
        {
            return AudioSystem.getAudioInputStream(new BufferedInputStream(stream));
        }

        return null;
    }
}
