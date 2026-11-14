package com.neep.neepmeat.client.screen.tablet.graph;

import com.neep.neepmeat.util.NMMaths;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.util.Mth;

public class GraphData
{
    public static float[] range(float start, float end, int samples)
    {
        float[] result = new float[samples + 1];
        for (int i = 0; i <= samples; i++)
        {
            result[i] = Mth.lerp(i / (float) samples, start, end);
        }
        return result;
    }

    public static float[] logRange(float start, float end, int samples)
    {
        double logStart = Math.log10(start);
        double logEnd = Math.log10(end);
        float[] result = new float[samples + 1];
        for (int i = 0; i <= samples; i++)
        {
            result[i] = (float) Math.pow(10, Mth.lerp(i / (float) samples, logStart, logEnd));
        }
        return result;
    }

    public static float[][] join(float[][]... sections)
    {
        // Reallocation can extend the wrapped array
        ObjectArrayList<float[]> result = ObjectArrayList.wrap(new float[][]{});

        int offset = 0;
        for (var sec : sections)
        {
            result.addElements(offset, sec);
            offset += sec.length;
        }

        float[][] resultArray = new float[result.size()][];
        System.arraycopy(result.elements(), 0, resultArray, 0, result.size());
        return resultArray;
    }

    public static float[][] sample(float[] x, Float2FloatFunction x1, Float2FloatFunction y)
    {
        float[][] result = new float[x.length][2];
        for (int i = 0; i < x.length; i++)
        {
            result[i][0] = x1.apply(x[i]);
            result[i][1] = y.apply(x[i]);
        }

        return result;
    }

    public record Range(float start, float end)
    {
        // Takes an 'actual' value and maps it to [0 1]
        public double map(boolean log, float value)
        {
            if (!log)
            {
                return (value - start) / (end - start);
            }
            else
            {
                float logStart = start == 0 ? 0 : NMMaths.log10(start); // Not sure if this is sound
                float logValue = value == 0 ? 0 : NMMaths.log10(value);
                float logEnd = NMMaths.log10(end);
                return (logValue - logStart) / (logEnd - logStart);
            }
        }

        public double unMap(boolean log, float value)
        {
            if (!log)
            {
                return value * (end - start);
            }
            else
            {
                float logStart = start == 0 ? 0 : NMMaths.log10(start); // Not sure if this is sound
                float logEnd = NMMaths.log10(end);

                float logValue = value * (logEnd - logStart) + logStart;

                return Math.pow(10, logValue);
            }
        }
    }

    public record Point(float x, float y) {}

    public record Tick(float value, boolean fill, String label)
    {
        public Tick(float value, boolean fill)
        {
            this(value, fill, "");
        }
    }

    private String
            xTitle = "",
            yTitle = "";

    private Range
            xRange = new Range(0, 1),
            yRange = new Range(0, 1);

    private List<Point> points = new ObjectArrayList<>();
    private List<Tick> xTicks =  new ObjectArrayList<>();
    private List<Tick> yTicks =  new ObjectArrayList<>();

    private String title = "";

    public boolean semiLogX;

    public GraphData()
    {
    }

    public float sample(float x)
    {
        for (int i = 0; i < points.size() - 1; i++)
        {
            var point = points.get(i);
            var point1 = points.get(i + 1);

            if (x > point.x && x <= point1.x)
            {
                // Using a stupid linear approximation
                float value = Mth.lerp((x - point.x) / (point1.x - point.x), point.y, point1.y);

                return value;
            }
        }

        return 0;
    }

    public GraphData setXRange(Range range)
    {
        this.xRange = range;
        return this;
    }

    public GraphData setYRange(Range range)
    {
        this.yRange = range;
        return this;
    }

    public GraphData setPoints(List<Point> points)
    {
        this.points = points;
        return this;
    }

    public GraphData setPoints(float[][] points)
    {
        this.points = Arrays.stream(points).map(p -> new Point(p[0], p[1])).toList();
        return this;
    }

    public GraphData setXTicks(List<Tick> ticks)
    {
        this.xTicks = ticks;
        return this;
    }

    public GraphData setXTicks(double[] ticks)
    {
        this.xTicks = Arrays.stream(ticks).mapToObj(v -> new Tick((float) v, false)).toList();
        return this;
    }

    public GraphData setYTicks(List<Tick> ticks)
    {
        this.yTicks = ticks;
        return this;
    }

    public GraphData setYTicks(double[] ticks)
    {
        this.yTicks = Arrays.stream(ticks).mapToObj(v -> new Tick((float) v, false)).toList();
        return this;
    }

    public GraphData setXTitle(String xTitle, String unit)
    {
        this.xTitle = xTitle + (unit.isEmpty() ? "" : " (" + unit + ")");
        return this;
    }

    public GraphData setYTitle(String yTitle, String unit)
    {
        this.yTitle = yTitle + (unit.isEmpty() ? "" : " (" + unit + ")");
        return this;
    }

    public GraphData setTitle(String title)
    {
        this.title = title;
        return this;
    }

    public GraphData semiLogX(boolean value)
    {
        this.semiLogX = value;
        return this;
    }

    public List<Point> points()
    {
        return points;
    }

    public Range xRange()
    {
        return xRange;
    }

    public Range yRange()
    {
        return yRange;
    }

    public String xLabel()
    {
        return xTitle;
    }

    public String yLabel()
    {
        return yTitle;
    }

    public List<Tick> yTicks()
    {
        return yTicks;
    }

    public List<Tick> xTicks()
    {
        return xTicks;
    }

    public String title()
    {
        return title;
    }
}
