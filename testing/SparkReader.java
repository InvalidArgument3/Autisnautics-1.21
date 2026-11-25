import me.lucko.spark.proto.SparkHeapProtos;
import me.lucko.spark.proto.SparkSamplerProtos;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * CLI reader for local Spark .sparkprofile and .sparkheap files.
 * Usage: java -cp mods/spark-1.10.124-neoforge.jar testing/SparkReader.java <file> [topN]
 */
public class SparkReader {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: SparkReader <file.sparkprofile|file.sparkheap> [topN]");
            System.exit(1);
        }
        String path = args[0];
        int topN = args.length >= 2 ? Integer.parseInt(args[1]) : 25;

        if (path.endsWith(".sparkheap")) {
            readHeap(path, topN);
        } else if (path.endsWith(".sparkprofile")) {
            readProfile(path, topN);
        } else {
            System.err.println("Unsupported file extension: " + path);
            System.exit(2);
        }
    }

    private static InputStream openMaybeGzip(String path) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
        bis.mark(2);
        int b1 = bis.read();
        int b2 = bis.read();
        bis.reset();
        if (b1 == 0x1f && b2 == 0x8b) {
            return new GZIPInputStream(bis);
        }
        return bis;
    }

    private static void readHeap(String path, int topN) throws Exception {
        SparkHeapProtos.HeapData data;
        try (InputStream in = openMaybeGzip(path)) {
            data = SparkHeapProtos.HeapData.parseFrom(in);
        }
        long totalBytes = 0;
        long totalInstances = 0;
        for (SparkHeapProtos.HeapEntry e : data.getEntriesList()) {
            totalBytes += e.getSize();
            totalInstances += e.getInstances();
        }
        System.out.printf("=== SPARK HEAP SUMMARY (%s) ===%n", path);
        System.out.printf("Entries: %d | Total Size: %.2f MB | Total Instances: %,d%n%n",
                data.getEntriesCount(), totalBytes / (1024.0 * 1024.0), totalInstances);

        List<SparkHeapProtos.HeapEntry> sorted = new ArrayList<>(data.getEntriesList());
        sorted.sort(Comparator.comparingLong(SparkHeapProtos.HeapEntry::getSize).reversed());

        System.out.printf("--- Top %d Classes by Retained/Shallow Bytes ---%n", Math.min(topN, sorted.size()));
        for (int i = 0; i < Math.min(topN, sorted.size()); i++) {
            SparkHeapProtos.HeapEntry e = sorted.get(i);
            double mb = e.getSize() / (1024.0 * 1024.0);
            double pct = totalBytes > 0 ? (e.getSize() * 100.0 / totalBytes) : 0;
            System.out.printf("%2d. %7.2f MB (%5.2f%%) | %,10d instances | %s%n",
                    i + 1, mb, pct, e.getInstances(), e.getType());
        }

        Map<String, long[]> byPkg = new HashMap<>();
        for (SparkHeapProtos.HeapEntry e : sorted) {
            String t = e.getType();
            String pkg = groupKey(t);
            long[] agg = byPkg.computeIfAbsent(pkg, k -> new long[2]);
            agg[0] += e.getSize();
            agg[1] += e.getInstances();
        }
        List<Map.Entry<String, long[]>> pkgList = new ArrayList<>(byPkg.entrySet());
        pkgList.sort((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]));
        System.out.printf("%n--- Top %d Package/Mod Groups by Bytes ---%n", Math.min(topN, pkgList.size()));
        for (int i = 0; i < Math.min(topN, pkgList.size()); i++) {
            Map.Entry<String, long[]> entry = pkgList.get(i);
            double mb = entry.getValue()[0] / (1024.0 * 1024.0);
            double pct = totalBytes > 0 ? (entry.getValue()[0] * 100.0 / totalBytes) : 0;
            System.out.printf("%2d. %7.2f MB (%5.2f%%) | %,10d instances | %s%n",
                    i + 1, mb, pct, entry.getValue()[1], entry.getKey());
        }
    }

    private static void readProfile(String path, int topN) throws Exception {
        SparkSamplerProtos.SamplerData data;
        try (InputStream in = openMaybeGzip(path)) {
            data = SparkSamplerProtos.SamplerData.parseFrom(in);
        }
        SparkSamplerProtos.SamplerMetadata meta = data.getMetadata();
        System.out.printf("=== SPARK PROFILE (%s) ===%n", path);
        System.out.printf("Mode: %s | Engine: %s | Interval: %d us | Ticks: %d | Threads: %d%n",
                meta.getSamplerMode(), meta.getSamplerEngine(), meta.getInterval(),
                meta.getNumberOfTicks(), data.getThreadsCount());

        Map<String, String> classSources = data.getClassSourcesMap();
        List<SparkSamplerProtos.ThreadNode> threads = new ArrayList<>(data.getThreadsList());
        threads.sort((a, b) -> Double.compare(sumTimes(b.getTimesList()), sumTimes(a.getTimesList())));

        System.out.println("\n--- Top Threads ---");
        for (int i = 0; i < Math.min(20, threads.size()); i++) {
            SparkSamplerProtos.ThreadNode t = threads.get(i);
            System.out.printf("%2d. %10.2f ms | %s%n", i + 1, sumTimes(t.getTimesList()), t.getName());
        }

        List<SparkSamplerProtos.ThreadNode> toInspect = new ArrayList<>();
        for (SparkSamplerProtos.ThreadNode t : threads) {
            String tn = t.getName();
            if (tn.equals("Server thread") || tn.equals("Render thread") || tn.startsWith("c2me-worker") || tn.startsWith("Chunk Render")) {
                toInspect.add(t);
            }
        }
        if (toInspect.isEmpty()) {
            for (int ti = 0; ti < Math.min(5, threads.size()); ti++) toInspect.add(threads.get(ti));
        }

        for (SparkSamplerProtos.ThreadNode t : toInspect) {
            double totalMs = sumTimes(t.getTimesList());
            if (totalMs <= 0) continue;

            Map<String, Double> selfByMethod = new HashMap<>();
            Map<String, Double> selfBySource = new HashMap<>();
            List<SparkSamplerProtos.StackTraceNode> pool = t.getChildrenList();
            if (t.getChildrenRefsCount() > 0) {
                for (int idx : t.getChildrenRefsList()) {
                    collectSelf(pool.get(idx), pool, selfByMethod, selfBySource, classSources);
                }
            } else {
                for (SparkSamplerProtos.StackTraceNode child : pool) {
                    collectSelf(child, pool, selfByMethod, selfBySource, classSources);
                }
            }

            double activeMs = 0;
            for (Map.Entry<String, Double> e : selfByMethod.entrySet()) {
                if (!isIdleMethod(e.getKey())) activeMs += e.getValue();
            }

            System.out.printf("%n=== Thread: %s (total %.2f ms, active %.2f ms) ===%n", t.getName(), totalMs, activeMs);
            List<Map.Entry<String, Double>> srcList = new ArrayList<>();
            for (Map.Entry<String, Double> e : selfBySource.entrySet()) {
                if (!e.getKey().equals("jdk.internal.misc") && !e.getKey().equals("java.lang.Object") && !e.getKey().equals("java.lang.Thread")) {
                    srcList.add(e);
                }
            }
            srcList.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
            System.out.println("  [By Mod/Source (active self time)]");
            double denom = activeMs > 0 ? activeMs : totalMs;
            for (int i = 0; i < Math.min(15, srcList.size()); i++) {
                Map.Entry<String, Double> e = srcList.get(i);
                System.out.printf("   %2d. %8.2f ms (%5.2f%%) | %s%n",
                        i + 1, e.getValue(), e.getValue() * 100.0 / denom, e.getKey());
            }

            List<Map.Entry<String, Double>> mList = new ArrayList<>();
            for (Map.Entry<String, Double> e : selfByMethod.entrySet()) {
                if (!isIdleMethod(e.getKey())) mList.add(e);
            }
            mList.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
            System.out.printf("  [Top %d Active Methods (self time)]%n", Math.min(topN, mList.size()));
            for (int i = 0; i < Math.min(topN, mList.size()); i++) {
                Map.Entry<String, Double> e = mList.get(i);
                System.out.printf("   %2d. %8.2f ms (%5.2f%%) | %s%n",
                        i + 1, e.getValue(), e.getValue() * 100.0 / denom, e.getKey());
            }
        }
    }

    private static void collectSelf(
            SparkSamplerProtos.StackTraceNode node,
            List<SparkSamplerProtos.StackTraceNode> pool,
            Map<String, Double> selfByMethod,
            Map<String, Double> selfBySource,
            Map<String, String> classSources
    ) {
        double nodeTime = sumTimes(node.getTimesList());
        double childrenTime = 0;
        for (int idx : node.getChildrenRefsList()) {
            SparkSamplerProtos.StackTraceNode child = pool.get(idx);
            childrenTime += sumTimes(child.getTimesList());
            collectSelf(child, pool, selfByMethod, selfBySource, classSources);
        }
        double self = Math.max(0.0, nodeTime - childrenTime);
        if (self > 0) {
            String cls = node.getClassName();
            String key = cls + "." + node.getMethodName();
            selfByMethod.merge(key, self, Double::sum);
            String src = classSources.getOrDefault(cls, groupKey(cls));
            selfBySource.merge(src, self, Double::sum);
        }
    }

    private static boolean isIdleMethod(String key) {
        return key.equals("jdk.internal.misc.Unsafe.park")
                || key.equals("java.lang.Object.wait0")
                || key.equals("java.lang.Thread.sleepNanos0")
                || key.equals("java.lang.Thread.sleep0")
                || key.equals("sun.nio.ch.WEPoll.wait");
    }

    private static double sumTimes(List<Double> times) {
        double s = 0;
        for (double d : times) s += d;
        return s;
    }

    private static String groupKey(String name) {
        String clean = name.replace("[]", "");
        String[] parts = clean.split("\\.");
        if (parts.length >= 3) {
            return parts[0] + "." + parts[1] + "." + parts[2];
        }
        return clean;
    }
}
