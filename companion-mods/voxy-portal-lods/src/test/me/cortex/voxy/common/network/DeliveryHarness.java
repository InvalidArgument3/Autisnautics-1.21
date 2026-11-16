package me.cortex.voxy.common.network;

import java.util.ArrayList;
import java.util.List;

/**
 * The real reliable queue. A slow acker (one batch every two ticks, the old
 * client apply pace) versus acking every frame the tick it is sent.
 */
public final class DeliveryHarness {
    public static void main(String[] args) {
        double slow = columnsPerSecond(2, 128, 1, 2);
        double fast = columnsPerSecond(48, 1024, 48, 1);
        System.out.println("DeliveryHarness slowColumnsPerSecond=" + slow + " fastColumnsPerSecond=" + fast);
        if (!(fast > slow * 4.0)) {
            throw new AssertionError("widening the window did not raise delivery, slow=" + slow + " fast=" + fast);
        }
        System.out.println("DeliveryHarness passed");
    }

    /** @param ackEvery only ack a frame once it has been in flight this many ticks, and at most ackLimit per tick */
    private static double columnsPerSecond(int window, int ownerEntries, int ackLimit, int ackDelayTicks) {
        ReliableTerrainQueue<String> queue = new ReliableTerrainQueue<String>(2048, 1 << 24, ownerEntries, 1 << 22, window, 1 << 20);
        int next = 0;
        long now = 0L;
        ArrayList<long[]> flight = new ArrayList<long[]>();
        int ticks = 200;
        int[] clock = new int[] {0};
        for (; clock[0] < ticks; clock[0]++) {
            int tick = clock[0];
            while (next < 400) {
                List<ReliableTerrainQueue.Part<String>> parts = new ArrayList<ReliableTerrainQueue.Part<String>>();
                for (int part = 0; part < 6; part++) {
                    parts.add(new ReliableTerrainQueue.Part<String>("column", 80, 8));
                }
                ReliableTerrainQueue.Key key = new ReliableTerrainQueue.Key("player", "minecraft:overworld|overworld|1", next, 0);
                if (!queue.offer(key, next + 1L, parts, now)) {
                    break;
                }
                next++;
            }
            queue.tick(now, key -> true, (sections, bytes) -> true, frame -> flight.add(new long[] {frame.id, clock[0]}), 64);
            int acked = 0;
            for (int i = 0; i < flight.size() && acked < ackLimit; i++) {
                long[] sent = flight.get(i);
                if (tick - sent[1] < ackDelayTicks) {
                    continue;
                }
                queue.ack("player", sent[0], 0, now);
                flight.remove(i);
                i--;
                acked++;
            }
            now += 50L;
        }
        return queue.completedColumns / (ticks * 0.05);
    }
}
