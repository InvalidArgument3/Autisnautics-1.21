/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat.client.sound;

import com.neep.neepbus.block.entity.MouthBlockEntity;
import com.neep.neepspeak.NEEPSpeak;
import com.neep.neepspeak.NEEPSpeakException;
import com.neep.neepspeak.PhonemeList;
import com.neep.neepspeak.mouth.Mouth;
import com.neep.neepspeak.parser.RuleParser;
import com.neep.neepspeak.phoneme.PhonemeSet;
import com.neep.neepspeak.util.SampleBuffer;
import com.neep.neepspeak.util.SampleInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sounds.AudioStream;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.nio.ByteBuffer;

@Environment(EnvType.CLIENT)
public class NEEPSpeakAudioStream implements AudioStream
{
    public static final AudioFormat FORMAT = NEEPSpeak.initOutputFormat(44100,2);

    private final Mouth mouth;

    public NEEPSpeakAudioStream(PhonemeList phonemeList)
    {
        mouth = new Mouth(phonemeList);
    }

    public NEEPSpeakAudioStream(String text, boolean phonetic) throws NEEPSpeakException
    {
        this(phonetic ?
            new PhonemeList(RuleParser.parseSamPhonetic(text, PhonemeSet.NORMAL))
            : MouthBlockEntity.PHONEMISER.process(text));
    }

    @Override
    public AudioFormat getFormat()
    {
        return FORMAT;
    }

    @Override
    public ByteBuffer read(int size) throws IOException
    {
        // Assuming size is the minimum length of the requested buffer in bytes

        if (mouth.isDone())
            return null;

        int bytes = FORMAT.getSampleSizeInBits() / 8;

        int samples = size / bytes;
        SampleBuffer sb = new SampleBuffer(samples);
        mouth.init(new SampleInfo(FORMAT.getSampleRate(), samples));
        mouth.processBuffer(sb);
        ByteBuffer result = NEEPSpeak.floatsToOutput(sb.data, bytes, FORMAT.isBigEndian(), BufferUtils::createByteBuffer);
        result.rewind();


        // Debug sine wave kept here in case everything stops working and I have to debug from first principles again
//        ByteBuffer result = ByteBuffer.allocateDirect(size);
//        result.order(ByteOrder.nativeOrder());
//
//        float fs = FORMAT.getSampleRate();
//        float f0 = 440;
//
//        int samples = size / bytes;
//        for (int i = 0; i < samples; ++i)
//        {
//            float thing = fs / f0;
//            double angle = (i / thing) * 2 * Math.PI;
//
//            double value = Math.sin(angle);
//
//            short s = (short) (value * Short.MAX_VALUE);
//
//            result.putShort(i * 2, s);
//        }

        return result;
    }

    @Override
    public void close() throws IOException
    {

    }

    public boolean isDone()
    {
        return mouth.isDone();
    }
}
