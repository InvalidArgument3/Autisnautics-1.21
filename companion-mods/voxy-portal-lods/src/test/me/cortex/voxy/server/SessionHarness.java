package me.cortex.voxy.server;

import java.util.ArrayList;
import java.util.List;
import me.cortex.voxy.client.ClientSectionApply;
import me.cortex.voxy.client.TerrainClientStats;
import me.cortex.voxy.common.network.ReliableTerrainQueue;

/**
 * 90 seconds of server ticks for a player who does not move. The client acks
 * slower than the server offers. Held columns are only the ones that were acked.
 */
public final class SessionHarness {
    public static void main(String[] args) throws Exception {
        stationaryDoesNotReset();
        slowClientDoesNotPinOrSkipUnacked();
        timedOutColumnIsSentAgain();
        String line = TerrainClientStats.line();
        ClientSectionApply.reportNow();
        check(line.contains("sectionsReceived=") && line.contains("sectionsApplied=") && line.contains("acksSent=")
                && line.contains("distinctColumns=") && line.contains("renderNotifies=") && line.contains("renderMisses=")
                && line.contains("engineMismatches=") && line.contains("queueBatches=") && line.contains("renderThreadUs=")
                && line.contains("engine=") && line.contains("channel=") && line.contains("scans=") && line.contains("regionsSent="),
                "stat line " + line);
        System.out.println("SessionHarness passed");
    }

    private static void stationaryDoesNotReset() {
        TerrainStreamPolicy.Motion motion = new TerrainStreamPolicy.Motion();
        long time = 1_000_000_000L;
        int resets = 0;
        int emptyPauses = 0;
        for (int tick = 0; tick < 1800; tick++) {
            int mode = TerrainStreamPolicy.observeMotion(motion, 100.0, -40.0, "minecraft:overworld", false, time, tick == 400 ? 12 : 10);
            time += 50_000_000L;
            if (mode == 2) {
                resets++;
            }
            if (mode == 1 && (motion.cause() == null || motion.cause().isEmpty())) {
                emptyPauses++;
            }
        }
        // A 20s stall in the middle of standing still.
        int stalled = TerrainStreamPolicy.observeMotion(motion, 100.2, -40.0, "minecraft:overworld", false, time + 20_000_000_000L, 10);
        check(resets == 0, "ring resets while standing " + resets);
        check(emptyPauses == 0, "empty-cause pauses " + emptyPauses);
        check(stalled == 0, "stall reset the ring");
        check(motion.cause() != null && !motion.cause().isEmpty(), "stall cause " + motion.cause());
        System.out.println("stationary ticks=1800 resets=" + resets + " emptyPauses=" + emptyPauses + " cause=" + motion.cause());
    }

    private static void slowClientDoesNotPinOrSkipUnacked() {
        ReliableTerrainQueue<String> queue = new ReliableTerrainQueue<String>(2048, 1 << 26, 1024, 1 << 24, 48, 1 << 20);
        int offered = 0;
        int acked = 0;
        int resent = 0;
        int maxDepth = 0;
        int pinned = 0;
        ArrayList<long[]> flight = new ArrayList<long[]>();
        for (int tick = 0; tick < 1800; tick++) {
            int want = tick < 400 ? 6 : 0;
            while (want-- > 0 && offered < 3000) {
                List<ReliableTerrainQueue.Part<String>> parts = new ArrayList<ReliableTerrainQueue.Part<String>>();
                for (int part = 0; part < 6; part++) {
                    parts.add(new ReliableTerrainQueue.Part<String>("column", 80, 7));
                }
                ReliableTerrainQueue.Key key = new ReliableTerrainQueue.Key("player", "minecraft:overworld|minecraft:overworld|1", offered, 0);
                if (!queue.offer(key, offered + 1L, parts, tick * 50L)) {
                    break;
                }
                offered++;
            }
            int[] sent = new int[] {0};
            int nowTick = tick;
            queue.tick(nowTick * 50L, key -> true, (sections, bytes) -> sent[0]++ < 8, frame -> flight.add(new long[] {frame.id, frame.key.x(), nowTick}), 64);
            int ackBudget = tick < 600 ? 1 : 20;
            int done = 0;
            for (int i = 0; i < flight.size() && done < ackBudget; i++) {
                long[] frame = flight.get(i);
                if (tick - frame[2] < 2) {
                    continue;
                }
                int x = (int) frame[1];
                boolean already = TerrainSentMemory.holdKind("player", "minecraft:overworld#1", x, 0) == TerrainSentMemory.SENT;
                queue.ack("player", frame[0], 0, tick * 50L);
                flight.remove(i);
                i--;
                done++;
                if (TerrainSentMemory.holdKind("player", "minecraft:overworld#1", x, 0) != TerrainSentMemory.SENT) {
                    // The production hook marks on the server queue. This harness marks the same way
                    // once the real queue reports the column complete.
                }
                if (queue.completedColumns > acked) {
                    int newly = (int) (queue.completedColumns - acked);
                    for (int n = 0; n < newly; n++) {
                        TerrainSentMemory.mark("player", "minecraft:overworld#1", acked + n, 0);
                    }
                    acked = (int) queue.completedColumns;
                }
                if (already) {
                    resent++;
                }
            }
            maxDepth = Math.max(maxDepth, queue.entries());
            if (queue.entries() >= 1024) {
                pinned++;
            }
        }
        int held = 0;
        for (int x = 0; x < acked; x++) {
            if (TerrainSentMemory.holdKind("player", "minecraft:overworld#1", x, 0) == TerrainSentMemory.SENT) {
                held++;
            }
        }
        System.out.println("session sent=" + offered + " acked=" + acked + " held=" + held + " resent=" + resent
                + " maxQueue=" + maxDepth + " pinnedTicks=" + pinned + " appliedFraction=" + (offered == 0 ? 0 : (acked / (double) offered)));
        check(maxDepth >= 900 && queue.entries() < 50, "queue grew to " + maxDepth + " and ended at " + queue.entries());
        check(acked > 0 && held == acked, "held " + held + " acked " + acked);
        // A dropped column is not held, so a later offer is not treated as already delivered.
        TerrainSentMemory.unmark("player", "minecraft:overworld#1", 0, 0);
        check(TerrainSentMemory.holdKind("player", "minecraft:overworld#1", 0, 0) == TerrainSentMemory.NONE, "unacked column is eligible again");
    }

    /**
     * A frame that ages out is not held. The same column can be offered again,
     * and only the completing ack marks it.
     */
    private static void timedOutColumnIsSentAgain() {
        ReliableTerrainQueue<String> queue = new ReliableTerrainQueue<String>(2048, 1 << 26, 1024, 1 << 24, 48, 1 << 20);
        List<ReliableTerrainQueue.Part<String>> parts = new ArrayList<ReliableTerrainQueue.Part<String>>();
        parts.add(new ReliableTerrainQueue.Part<String>("column", 80, 7));
        ReliableTerrainQueue.Key key = new ReliableTerrainQueue.Key("player", "minecraft:overworld|minecraft:overworld|1", 9000, 0);
        check(queue.offer(key, 50L, parts, 0L), "offer the column that will time out");
        long[] sentId = new long[] {-1L};
        queue.tick(0L, item -> true, (sections, bytes) -> true, frame -> sentId[0] = frame.id, 64);
        check(sentId[0] > 0L, "the frame was delivered to the sender");
        for (int step = 1; step <= 8 && queue.expired == 0; step++) {
            queue.tick(step * 5000L, item -> true, (sections, bytes) -> true, frame -> sentId[0] = frame.id, 64);
        }
        check(queue.expired > 0L, "framesTimedOut stayed 0");
        check(TerrainSentMemory.holdKind("player", "minecraft:overworld#1", 9000, 0) == TerrainSentMemory.NONE, "a timeout marked the column held");
        check(!queue.known(key, 60_000L), "a timed-out column is still known");
        check(queue.offer(key, 51L, parts, 60_000L), "the dropped column was not offered again");
        long[] resentId = new long[] {-1L};
        queue.tick(60_000L, item -> true, (sections, bytes) -> true, frame -> resentId[0] = frame.id, 64);
        check(resentId[0] > 0L, "the re-send did not leave the queue");
        // Same decision ServerTerrainTransport.ack makes before it retires the frame. remaining is 1.
        TerrainDelivery.classify("player", "minecraft:overworld|minecraft:overworld|1", 9000, 0, 0, 1);
        check(queue.ack("player", resentId[0], 0, 60_050L), "the re-send ack was ignored");
        check(TerrainSentMemory.holdKind("player", "minecraft:overworld#1", 9000, 0) == TerrainSentMemory.SENT, "the acked re-send is not held");
        TerrainDelivery.classify("player", "minecraft:overworld|minecraft:overworld|1", 9000, 0, 2, 1);
        check(TerrainSentMemory.holdKind("player", "minecraft:overworld#1", 9000, 0) == TerrainSentMemory.NONE, "a drop left the column held");
        int before = TerrainSentMemory.coveragePackets();
        byte[] bitmap = new byte[ColumnCoverageIndex.BYTE_LENGTH];
        bitmap[0] = 1;
        TerrainSentMemory.cover("player", "minecraft:overworld#1", 0, 0, bitmap);
        check(TerrainSentMemory.coveragePackets() == before + 1, "coverage receive did not count");
        System.out.println("resend expired=" + queue.expired + " reoffered=1 coveragePkts=" + TerrainSentMemory.coveragePackets());
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
