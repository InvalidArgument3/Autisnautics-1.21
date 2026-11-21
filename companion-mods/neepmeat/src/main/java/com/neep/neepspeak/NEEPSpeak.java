package com.neep.neepspeak;

import com.neep.neepspeak.audio.BiquadFilter2;
import com.neep.neepspeak.audio.SoundFiles;
import com.neep.neepspeak.mouth.Mouth;
import com.neep.neepspeak.mouth.Waveforms;
import com.neep.neepspeak.parser.Phonemiser;
import com.neep.neepspeak.parser.RuleParser;
import com.neep.neepspeak.phoneme.PhonemeFileSet;
import com.neep.neepspeak.phoneme.PhonemeSet;
import com.neep.neepspeak.util.SampleBuffer;
import com.neep.neepspeak.util.SampleInfo;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.IntStream;

public class NEEPSpeak
{
    public static AudioFormat initOutputFormat(float fs, int bytes)
    {
        int channels = 1;
        int sampleSizeInBits = bytes * 8;
        int frameSize = (sampleSizeInBits + 7) / 8 * channels;

        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                fs,
                sampleSizeInBits,
                channels,
                frameSize,
                fs,
                false);
    }

    public static void main(String[] args) throws LineUnavailableException, NEEPSpeakException
    {
        float fs = Waveforms.SAMPLE_RATE;
        int bytes = 2;
        var af = initOutputFormat(fs, bytes);

        SoundFiles.preload();

        SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
        sdl.open();
        sdl.start();

//        int longFrameSize = (int) (fs * 2); // Use a single frame for sanity check
        int longFrameSize = 1024;
        int lenSamples = (int) ((fs * 4) / longFrameSize) * longFrameSize;
        SampleBuffer mainSb = new SampleBuffer(new float[lenSamples], 0, lenSamples);

        var phonemiser = new Phonemiser(PhonemeSet.NORMAL);
        phonemiser.setupDefaultRules();
        var phonemes = phonemiser.process("whole whatever wherever");
//        var phonemes = new PhonemeList(RuleParser.parseSamPhonetic("EYD", phonemiser.getPhonemeSet()));
        Mouth mouth = new Mouth(phonemes);
        mouth.init(new SampleInfo(fs, mainSb.regionSize()));

        int frames = lenSamples / longFrameSize;
        for (int i = 0; i < frames; ++i)
        {
            SampleBuffer sb = new SampleBuffer(mainSb.data, i * longFrameSize, (i + 1) * longFrameSize);
            mouth.processBuffer(sb);
        }

        var convert = floatsToOutput(mainSb.data, bytes, af.isBigEndian(), ByteBuffer::allocate);
        sdl.write(convert.array(), 0, lenSamples * bytes);

        sdl.drain();
        sdl.stop();

//        JavaPlot jp = new JavaPlot(false);
////        jp.setTerminal(new ImageTerminal());
//        JavaPlot.getDebugger().setLevel(Debug.ERROR);
////        var plot = new DataSetPlot(new FloatDataSet(fRange, sb.data));
//        var plot = new DataSetPlot(new FloatDataSet(linearRange(0, lenSamples, lenSamples), mainSb.data));
//        plot.getPlotStyle().setStyle(Style.LINES);
//        jp.getAxis("x").setLabel("Frequency / Hz");
//        jp.getAxis("y").setLabel("Amplitude");
//        jp.getAxis("x").setBoundaries(0.35 * fs, 0.65 * fs);
//        jp.getAxis("y").setBoundaries(-1, 1);
//        jp.addPlot(plot);
//        jp.plot();

//        try
//        {
//            File outfFile = new File("output.wav");
//            AudioFormat outFormat = new AudioFormat(fs, 16, 1, true, false);
//            ByteArrayInputStream byteStream = new ByteArrayInputStream(convert.array());
//            AudioInputStream audioStream = new AudioInputStream(byteStream, outFormat, convert.capacity());
//            AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, outfFile);
//            audioStream.close();
//        }
//        catch (IOException e)
//        {
//        }
    }

    public static void main1(String[] args) throws LineUnavailableException
    {
        float fs = Waveforms.SAMPLE_RATE;
//        int lenSamples = 44100 / 2;
//        int lenSamples = 300;
        int lenSamples = (int) fs * 2;

        boolean play = false;

        var af = initOutputFormat(fs, 4);
        SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
        if (play)
        {
            sdl.open();
            sdl.start();
        }

        // Main buffer
        SampleBuffer sb = new SampleBuffer(new float[lenSamples], 0, lenSamples);

        // Waveform test
//        Waveforms.sine(sb, fs, 4000, 0.5f);
//        var fRange = linearRange(0, 2000, sb.regionSize());

//        float f1 = 6000;
//        int buckets = 100;
//        var fRange = linearRange(0, f1, buckets, sb.regionSize());
//        Waveforms.sineSweep(sb, fs, fRange, 0.5f);

//        Waveforms.sineSweep(sb, fs, 0, f1, buckets, 0.5f);
//        Waveforms.sawtooth(sb, fs, 440, 0.5f);
//        Waveforms.whiteNoise(sb);
//        Waveforms.vowelAa(sb, fs);
//        Waveforms.whiteNoise(sb);

//        var biquad = new BiquadFilter(fs);
//        biquad.setLowpass(1900, 1);
//        biquad.process(sb);
//        biquad.setResonant(500, 1, false);

        var biquad2 = new BiquadFilter2(fs);
        biquad2.setLowpass(2000, 2);
//        biquad2.process(sb);

        SampleInfo info = new SampleInfo(Waveforms.SAMPLE_RATE, sb.regionSize());

//        var vowel = new VowelA();
//        vowel.init(info);
//        FrequencySweep sweep = new FrequencySweep(fs, 1, 8000, 200, info.frameSize());
//        sweep.apply(b -> vowel.processBuffer(b, new PlayablePhoneme.SimplePlayContext(0)))
//                .plot();

//        Mouth mouth = new Mouth(new PhonemeList(List.of(
//                new FricativeTest(),
//                new VowelA(),
//                new VowelA(),
//                new VowelA(),
//                new VowelA(),
//                new VowelA(),
//                new VowelA(),
////                new VowelI(),
//                new SpacePhoneme()
//        )));
//        mouth.init(info);
//        mouth.processBuffer(sb);

        if (play)
        {
            int bytes = 4;
            // Convert the buffer for the SDL

            var convert = floatsToOutput(sb.data, bytes, true, ByteBuffer::allocate);
            sdl.write(convert.array(), 0, lenSamples * bytes);

            sdl.drain();
            sdl.stop();
        }

//        JavaPlot jp = new JavaPlot(false);
////        JavaPlot.getDebugger().setLevel(Debug.VERBOSE);
////        var plot = new DataSetPlot(new FloatDataSet(fRange, sb.data));
//        var plot = new DataSetPlot(new FloatDataSet(linearRange(0, f1, lenSamples), sb.data));
//        plot.getPlotStyle().setStyle(Style.LINES);
//        jp.getAxis("x").setLabel("Frequency (Hz)");
//        jp.getAxis("y").setLabel("Amplitude");
//        jp.getAxis("y").setBoundaries(-1, 1);
//        jp.addPlot(plot);
//        jp.plot();
    }

    public static ByteBuffer floatsToOutput(float[] floats, int bytes, boolean bigEndian, Function<Integer, ByteBuffer> allocate)
    {
//        ByteBuffer convert = ByteBuffer.allocate(floats.length * bytes);
        ByteBuffer convert = allocate.apply(floats.length * bytes);
        convert.order(bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);

        if (bytes == 2)
        {
            for (int i = 0; i < floats.length; i++)
            {
                convert.putShort(i * bytes, (short) (((double) floats[i]) * Short.MAX_VALUE));
            }
        }
        else if (bytes == 4)
        {
            for (int i = 0; i < floats.length; i++)
            {
                convert.putInt(i * bytes, (int) (((double) floats[i]) * Integer.MAX_VALUE));
            }
        }

        return convert;
    }

    public static float[] linearRange(float f0, float f1, int size)
    {
        float step = (f1 - f0) / size;
        float[] res = new float[size];
        float f = f0;

        for (int i = 0; i < size; i++)
        {
            res[i] = f;
            f += step;
        }

        return res;
    }

    public static float[] linearRange(float f0, float f1, int buckets, int size)
    {
        float step = (f1 - f0) / buckets;

        float[] res = new float[size];
        float f;

        for (int i = 0; i < size; i++)
        {
            int bucket = (int) Math.floor((double) buckets * i / size);
            f = bucket * step;

            res[i] = f;
        }

        return res;
    }

//    public static float[] fExp(float f0, float f1, int size)
//    {
//        float sr = (float) Math.log(f1 / f0);
//
//        float[] res = new float[size];
//        for (int i = 0; i < size; i++)
//        {
//            res[i] = (float) (f0 * size / sr * (Math.exp(i * sr / size) - 1));
//            res[i] = (float) f0 + ((float) i / size) * (f1 - f0);
//        }
//        return res;
//    }

    public static int[] concat(byte[] b)
    {
        int[] result = new int[b.length / 2];
        for (int i = 0; i < result.length; i++)
        {
            // How to convert two unsigned bytes (big endian) that represent a signed short to a signed int:

            // Mask the shifted bytes because java thinks they're signed and converts them to signed ints when bitshifting
            // Interpret the result as a short because the bytes make up a signed short
            // Cast the short to an int to correctly pad it according to the sign
            // argh
            result[i] = (int)
            (short) ((((b[2 * i]) << 8) & 0xFF00)
                    | (((int) b[2 * i + 1]) & 0x00FF))
            ;
        }
        return result;
    }

    public static int[] range(int size)
    {
        return IntStream.range(0, size).toArray();
    }

//    public static class IntDataSet implements DataSet
//    {
//        private final String[][] points;
//
//        public IntDataSet(int[] x, int[] y)
//        {
//            points = new String[x.length][2];
//            for (int i = 0; i < x.length; i++)
//            {
//                points[i][0] = Integer.toString(x[i]);
//                points[i][1] = Integer.toString(y[i]);
//            }
//        }
//
//        public IntDataSet(int[] x, short[] y)
//        {
//            points = new String[x.length][2];
//            for (int i = 0; i < x.length; i++)
//            {
//                points[i][0] = Integer.toString(x[i]);
//                points[i][1] = Integer.toString(y[i]);
//            }
//        }
//
//        @Override
//        public int size()
//        {
//            return points.length;
//        }
//
//        @Override
//        public int getDimensions()
//        {
//            return 2;
//        }
//
//        @Override
//        public String getPointValue(int point, int dim)
//        {
//            return points[point][dim];
//        }
//    }
}
