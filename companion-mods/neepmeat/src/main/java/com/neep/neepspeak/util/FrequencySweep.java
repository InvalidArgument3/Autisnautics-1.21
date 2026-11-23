package com.neep.neepspeak.util;

import com.neep.neepspeak.mouth.Waveforms;

import java.util.function.Consumer;

// Swept frequency response to test direct form filters
public class FrequencySweep
{
    private final float fs;
    private final float f0;
    private final float f1;
    private final int steps;
    private final int frameSize;

    public FrequencySweep(float fs, float f0, float f1, int steps, int frameSize)
    {
        this.fs = fs;
        this.f0 = f0;
        this.f1 = f1;
        this.steps = steps;
        this.frameSize = frameSize;
    }

    public FrequencyResponse apply(Consumer<SampleBuffer> system)
    {
        float[] h = new float[steps];
        float[] f = new float[steps];

        int cycles = 30;
        for (int step = 0; step < steps; step++)
        {
            float stepF = f0 + (f1 - f0) * ((float) step / steps);

            float samplesPerCycle = fs / stepF;

            int size = (int) (cycles * samplesPerCycle);

            int frames = 1 + (int) Math.ceil((double) size / frameSize);

            float max = 0;
            for (int frame = 0; frame < frames; frame++)
            {
                SampleBuffer sb = new SampleBuffer(new float[frameSize], 0, frameSize);

                Waveforms.sine(sb, fs, stepF, 1);
                system.accept(sb);

                // Skip the first frame to allow the filter to settle
                if (frame > 0)
                    max = Math.max(max, Util.max(sb.data));
            }

            h[step] = max;
            f[step] = stepF;
        }

        return new FrequencyResponse(f, h);
    }

    public record FrequencyResponse(float[] f, float[] h)
    {
//        public void plot()
//        {
//            JavaPlot jp = new JavaPlot(false);
//
//            DataSetPlot plot = new DataSetPlot(new FloatDataSet(f, h));
//            plot.getPlotStyle().setStyle(Style.LINES);
//
//            jp.addPlot(plot);
//            jp.getAxis("x").setLabel("Frequency / Hz");
//            jp.getAxis("y").setLabel("Magnitude");
//            jp.plot();
//        }
    }
}
