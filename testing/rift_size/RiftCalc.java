// Measures the Abyssal Stack bounded rift discs for seed 1313966032424028775.
//
// The masks call lithostitched:cellular (grid 2236, jitter 0.9, salt 810457).
// That function places one point per cell with Minecraft's WorldgenRandom
// (LegacyRandomSource + setLargeFeatureWithSalt) and Mth.length. This program
// calls those same classes, then checks the placed discs against
// CellularDensityFunction.sample2D over ±20000 at a step of 8.
//
// Run from a machine that has the Mojmap 1.21.1 server jar, its libraries,
// and lithostitched-1.8.1:
//   javac --release 21 -proc:none -cp "server-mojmap.jar:libs/*:lithostitched.jar" RiftCalc.java
//   java -cp ".:server-mojmap.jar:libs/*:lithostitched.jar" RiftCalc results.txt

import dev.worldgen.lithostitched.impl.LithostitchedInternalHooks;
import dev.worldgen.lithostitched.impl.worldgen.densityfunction.CellularDensityFunction;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class RiftCalc {
    static final long SEED = 1313966032424028775L;
    static final int CELL = 2236;
    static final float JITTER = 0.9f;
    static final int SALT = 810457;
    static final int M = (int) ((float) CELL * JITTER);
    static final int SAMPLE = 20000;
    static final int STEP = 8;
    static final int WIDE = 100000;

    static final String[] BOUNDARY = {
        "boundary 1  mask_0_1  overworld floor / infinite_abyss:first_layer ceiling",
        "boundary 2  mask_1_2  infinite_abyss:first_layer floor / deeperdarker:otherside ceiling",
        "boundary 3  mask_2_3  deeperdarker:otherside floor / oceanworld:deepsea ceiling",
        "boundary 4  mask_3_4  oceanworld:deepsea floor / dimension_of_caves:cave ceiling",
        "boundary 5  mask_4_5  dimension_of_caves:cave floor / infernalcross:dimension ceiling",
        "boundary 6  mask_5_6  infernalcross:dimension floor / minecraft:the_nether ceiling",
        "boundary 7  mask_6_7  minecraft:the_nether floor / undergarden:undergarden ceiling",
        "boundary 8  mask_7_8  undergarden:undergarden floor / minecraft:the_end ceiling"
    };
    static final String[] YBAND = {
        "floor overworld y=-53..-28, ceiling infinite_abyss:first_layer y=103..127",
        "floor infinite_abyss:first_layer y=0..24, ceiling deeperdarker:otherside y=103..127",
        "floor deeperdarker:otherside y=0..24, ceiling oceanworld:deepsea y=275..319",
        "floor oceanworld:deepsea y=-64..-40, ceiling dimension_of_caves:cave y=103..127",
        "floor dimension_of_caves:cave y=-48..-24, ceiling infernalcross:dimension y=39..63",
        "floor infernalcross:dimension y=-48..-24, ceiling minecraft:the_nether y=231..255",
        "floor minecraft:the_nether y=0..24, ceiling undergarden:undergarden y=103..127",
        "floor undergarden:undergarden y=-64..-40, ceiling minecraft:the_end y=359..383"
    };

    static CellularDensityFunction distanceFn;
    static CellularDensityFunction valueFn;

    static final class Rift {
        final int gx, gz, tx, tz, wx, wz;
        final float cellValue;
        final double radius;
        final int width;

        Rift(int gx, int gz, int tx, int tz, int ox, int oz, float cellValue) {
            this.gx = gx;
            this.gz = gz;
            this.tx = tx;
            this.tz = tz;
            this.wx = tx - ox;
            this.wz = tz - oz;
            this.cellValue = cellValue;
            this.radius = 50.0 + 59.0 * cellValue;
            int half = 0;
            while ((float) Mth.length(half + 1, 0) <= radius) half++;
            this.width = half * 2 + 1;
        }

        double distOrigin() {
            return Math.hypot(wx, wz);
        }
    }

    static Rift place(int gx, int gz, int ox, int oz) {
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureWithSalt(SEED, gx, gz, SALT);
        int tx = gx * CELL + random.nextIntBetweenInclusive(0, M);
        int tz = gz * CELL + random.nextIntBetweenInclusive(0, M);
        float cellValue = random.nextFloat();
        return new Rift(gx, gz, tx, tz, ox, oz, cellValue);
    }

    static long key(int gx, int gz) {
        return ((long) gx << 32) ^ (gz & 0xffffffffL);
    }

    static HashMap<Long, Rift> enumerate(int boundary, int reach) {
        int ox = 4096 * boundary;
        int oz = 7193 * boundary;
        int minG = Math.floorDiv(-reach + ox, CELL) - 3;
        int maxG = Math.floorDiv(reach + ox, CELL) + 3;
        int minH = Math.floorDiv(-reach + oz, CELL) - 3;
        int maxH = Math.floorDiv(reach + oz, CELL) + 3;
        HashMap<Long, Rift> map = new HashMap<>();
        for (int gx = minG; gx <= maxG; gx++) {
            for (int gz = minH; gz <= maxH; gz++) {
                Rift rift = place(gx, gz, ox, oz);
                map.put(key(gx, gz), rift);
            }
        }
        return map;
    }

    static float distTo(Rift rift, int sx, int sz) {
        return (float) Mth.length((double) (rift.tx - sx), (double) (rift.tz - sz));
    }

    static Rift nearest(HashMap<Long, Rift> map, int sx, int sz) {
        int gx = Math.floorDiv(sx, CELL);
        int gz = Math.floorDiv(sz, CELL);
        Rift best = null;
        float bestD = Float.POSITIVE_INFINITY;
        for (int xo = -1; xo <= 1; xo++) {
            for (int zo = -1; zo <= 1; zo++) {
                Rift rift = map.get(key(gx + xo, gz + zo));
                if (rift == null) continue;
                float d = distTo(rift, sx, sz);
                if (d < bestD) {
                    bestD = d;
                    best = rift;
                }
            }
        }
        return best;
    }

    static void checkPlaced(HashMap<Long, Rift> map, StringBuilder errors) {
        for (Rift rift : map.values()) {
            double atCenter = distanceFn.sample2D(rift.tx, rift.tz);
            double atStep = distanceFn.sample2D(rift.tx + 1, rift.tz);
            double value = valueFn.sample2D(rift.tx, rift.tz);
            double valueStep = valueFn.sample2D(rift.tx + 1, rift.tz);
            if (atCenter != 0.0 || atStep != 1.0 || value != (double) rift.cellValue || valueStep != (double) rift.cellValue) {
                errors.append(String.format(
                        "place mismatch cell %d,%d centerDist %.6f stepDist %.6f value %.8f/%.8f%n",
                        rift.gx, rift.gz, atCenter, atStep, value, rift.cellValue));
                return;
            }
            int half = (rift.width - 1) / 2;
            double edge = distanceFn.sample2D(rift.tx + half, rift.tz);
            double outside = distanceFn.sample2D(rift.tx + half + 1, rift.tz);
            if (!(edge <= rift.radius) || !(outside > rift.radius)) {
                errors.append(String.format(
                        "edge mismatch world %d,%d half %d edge %.6f outside %.6f radius %.6f%n",
                        rift.wx, rift.wz, half, edge, outside, rift.radius));
                return;
            }
        }
    }

    static final class Sample {
        long points;
        long openPoints;
        long mismatches;
        int distinctOpen;
        String firstMismatch = "";
    }

    static Sample raster(HashMap<Long, Rift> map, int ox, int oz) throws Exception {
        int minX = -SAMPLE;
        int maxX = SAMPLE;
        int rows = (maxX - minX) / STEP + 1;
        int threads = Math.max(1, Runtime.getRuntime().availableProcessors());
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        Set<Long> seen = ConcurrentHashMap.newKeySet();
        List<Future<Sample>> jobs = new ArrayList<>();
        int chunk = (rows + threads - 1) / threads;
        for (int t = 0; t < threads; t++) {
            int start = t * chunk;
            int end = Math.min(rows, start + chunk);
            jobs.add(pool.submit((Callable<Sample>) () -> {
                Sample local = new Sample();
                for (int row = start; row < end; row++) {
                    int x = minX + row * STEP;
                    int sx0 = x + ox;
                    for (int z = minX; z <= maxX; z += STEP) {
                        int sz = z + oz;
                        local.points++;
                        Rift rift = nearest(map, sx0, sz);
                        if (rift == null) {
                            local.mismatches++;
                            if (local.firstMismatch.isEmpty()) local.firstMismatch = "no cell for x " + x + " z " + z;
                            continue;
                        }
                        float expect = distTo(rift, sx0, sz);
                        double got = distanceFn.sample2D(sx0, sz);
                        boolean geomOpen = expect <= rift.radius;
                        if (got != (double) expect) {
                            local.mismatches++;
                            if (local.firstMismatch.isEmpty()) {
                                local.firstMismatch = String.format(
                                        "x %d z %d got %.8f expect %.8f", x, z, got, expect);
                            }
                        }
                        if (geomOpen) {
                            local.openPoints++;
                            seen.add(key(rift.gx, rift.gz));
                        }
                    }
                }
                return local;
            }));
        }
        Sample total = new Sample();
        for (Future<Sample> job : jobs) {
            Sample local = job.get();
            total.points += local.points;
            total.openPoints += local.openPoints;
            total.mismatches += local.mismatches;
            if (total.firstMismatch.isEmpty()) total.firstMismatch = local.firstMismatch;
        }
        total.distinctOpen = seen.size();
        pool.shutdown();
        return total;
    }

    static boolean inside(Rift rift, int reach) {
        return rift.wx >= -reach && rift.wx <= reach && rift.wz >= -reach && rift.wz <= reach;
    }

    static int countInside(HashMap<Long, Rift> map, int reach) {
        int n = 0;
        for (Rift rift : map.values()) if (inside(rift, reach)) n++;
        return n;
    }

    static double minGap(HashMap<Long, Rift> map) {
        int[][] neighbors = {{1, 0}, {0, 1}, {1, 1}, {1, -1}, {2, 0}, {0, 2}};
        double min = Double.POSITIVE_INFINITY;
        for (Rift rift : map.values()) {
            for (int[] step : neighbors) {
                Rift other = map.get(key(rift.gx + step[0], rift.gz + step[1]));
                if (other == null) continue;
                double sep = Math.hypot(rift.tx - other.tx, rift.tz - other.tz);
                min = Math.min(min, sep - rift.radius - other.radius);
            }
        }
        return min;
    }

    static int maxWidth(HashMap<Long, Rift> map, int reach) {
        int max = 0;
        for (Rift rift : map.values()) if (inside(rift, reach)) max = Math.max(max, rift.width);
        return max;
    }

    static int minWidth(HashMap<Long, Rift> map, int reach) {
        int min = Integer.MAX_VALUE;
        for (Rift rift : map.values()) if (inside(rift, reach)) min = Math.min(min, rift.width);
        return min;
    }

    static List<Rift> nearest(HashMap<Long, Rift> map, int n) {
        ArrayList<Rift> list = new ArrayList<>();
        for (Rift rift : map.values()) if (inside(rift, SAMPLE)) list.add(rift);
        list.sort((a, b) -> Double.compare(a.distOrigin(), b.distOrigin()));
        return list.subList(0, Math.min(n, list.size()));
    }

    static String line(String fmt, Object... args) {
        return String.format(fmt, args) + "\n";
    }

    public static void main(String[] args) throws Exception {
        if (M != 2012) throw new IllegalStateException("jitter span changed: M=" + M);
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        LithostitchedInternalHooks.WORLD_SEED = SEED;
        var grid = new CellularDensityFunction.GridDimensions(CELL, 0);
        distanceFn = new CellularDensityFunction(Optional.empty(), grid, JITTER, CellularDensityFunction.ReturnType.DISTANCE_1, SALT);
        valueFn = new CellularDensityFunction(Optional.empty(), grid, JITTER, CellularDensityFunction.ReturnType.CELL_VALUE, SALT);

        StringBuilder report = new StringBuilder();
        StringBuilder errors = new StringBuilder();
        long side = SAMPLE * 2L + 1;
        double sampleArea = (double) side * side;
        long wideSide = WIDE * 2L + 1;
        double wideArea = (double) wideSide * wideSide;
        report.append(line("seed %d", SEED));
        report.append(line("cell %d  jitter %.1f  jitter_span %d  salt %d", CELL, JITTER, M, SALT));
        report.append(line("cell area %d  structural density %.6f rifts per 5,000,000 blocks", CELL * CELL, 5_000_000.0 / (CELL * (double) CELL)));
        report.append(line("opening radius = 50 + 59*cellValue in [50, 109)  so block width is 101..217"));
        report.append(line("sample square x,z in [-%d, %d] inclusive, step %d, area %.0f", SAMPLE, SAMPLE, STEP, sampleArea));
        report.append(line("wide square [-%d, %d] is center enumeration only", WIDE, WIDE));

        int globalMax = 0;
        int globalMin = Integer.MAX_VALUE;
        double globalGap = Double.POSITIVE_INFINITY;
        long sampleCenters = 0;
        long wideCenters = 0;
        long rasterPoints = 0;
        long rasterOpen = 0;
        long rasterMismatch = 0;

        for (int b = 0; b < 8; b++) {
            HashMap<Long, Rift> map = enumerate(b, WIDE);
            checkPlaced(map, errors);
            if (!errors.isEmpty()) break;
            Sample sample = raster(map, 4096 * b, 7193 * b);
            int inSample = countInside(map, SAMPLE);
            int inWide = countInside(map, WIDE);
            int maxW = maxWidth(map, WIDE);
            int minW = minWidth(map, WIDE);
            double gap = minGap(map);
            sampleCenters += inSample;
            wideCenters += inWide;
            rasterPoints += sample.points;
            rasterOpen += sample.openPoints;
            rasterMismatch += sample.mismatches;
            globalMax = Math.max(globalMax, maxW);
            globalMin = Math.min(globalMin, minW);
            globalGap = Math.min(globalGap, gap);
            report.append(line(""));
            report.append(line(BOUNDARY[b]));
            report.append(line("  shift x %d z %d", 4096 * b, 7193 * b));
            report.append(line("  Y %s", YBAND[b]));
            report.append(line("  centers in ±%d: %d   per 5,000,000 blocks: %.4f", SAMPLE, inSample, inSample / sampleArea * 5_000_000.0));
            report.append(line("  centers in ±%d: %d   per 5,000,000 blocks: %.4f", WIDE, inWide, inWide / wideArea * 5_000_000.0));
            report.append(line("  width min %d max %d   min edge gap %.2f blocks", minW, maxW, gap));
            report.append(line("  raster points %d open %d distinct rifts hit %d distance mismatches %d",
                    sample.points, sample.openPoints, sample.distinctOpen, sample.mismatches));
            if (!sample.firstMismatch.isEmpty()) report.append(line("  first mismatch %s", sample.firstMismatch));
            if (b < 2) {
                List<Rift> near = nearest(map, 3);
                for (int i = 0; i < near.size(); i++) {
                    Rift rift = near.get(i);
                    report.append(line(
                            "  nearest %d center %d %d radius %.3f width %d dist_origin %.1f rim_dist %.1f Y %s",
                            i + 1, rift.wx, rift.wz, rift.radius, rift.width, rift.distOrigin(),
                            rift.distOrigin() - rift.radius, YBAND[b]));
                }
            }
            System.out.println("finished boundary " + (b + 1));
        }

        report.append(line(""));
        report.append(line("ALL boundaries"));
        report.append(line("  observed width min %d max %d (hard cap 256)", globalMin, globalMax));
        report.append(line("  minimum disc-edge gap %.2f blocks", globalGap));
        report.append(line("  ±%d centers %d over 8 boundaries, mean per boundary %.2f, mean per 5,000,000 blocks %.4f",
                SAMPLE, sampleCenters, sampleCenters / 8.0, (sampleCenters / 8.0) / sampleArea * 5_000_000.0));
        report.append(line("  ±%d centers %d over 8 boundaries, mean per boundary %.2f, mean per 5,000,000 blocks %.4f",
                WIDE, wideCenters, wideCenters / 8.0, (wideCenters / 8.0) / wideArea * 5_000_000.0));
        report.append(line("  raster points %d open %d distance mismatches %d", rasterPoints, rasterOpen, rasterMismatch));
        if (!errors.isEmpty()) report.append(line("ERRORS%n%s", errors));

        String text = report.toString();
        System.out.println("=== RIFT REPORT ===");
        System.out.print(text);
        if (args.length > 0) Files.writeString(Path.of(args[0]), text);
        if (!errors.isEmpty() || rasterMismatch != 0 || globalMax > 256 || globalGap <= 0) {
            System.exit(1);
        }
    }
}
