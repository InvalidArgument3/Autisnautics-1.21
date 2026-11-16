package me.cortex.voxy.server;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Marks a column held only when the client acks its last frame. A timeout or
 * a drop leaves it eligible for another send.
 */
public final class TerrainDelivery {
    private static long ackedColumns;
    private static Field queueField;
    private static Field flightField;
    private static Field columnsField;

    private TerrainDelivery() {
    }

    public static void beforeAck(Object player, long id, int status) {
        try {
            ensure();
            Object queue = queueField.get(null);
            Map<?, ?> flight = (Map<?, ?>) flightField.get(queue);
            Object frame = flight.get(Long.valueOf(id));
            if (frame == null) {
                return;
            }
            Object key = frame.getClass().getField("key").get(frame);
            String owner = (String) key.getClass().getMethod("owner").invoke(key);
            String world = walkerWorld((String) key.getClass().getMethod("world").invoke(key));
            int x = ((Integer) key.getClass().getMethod("x").invoke(key)).intValue();
            int z = ((Integer) key.getClass().getMethod("z").invoke(key)).intValue();
            Map<?, ?> columns = (Map<?, ?>) columnsField.get(queue);
            Object column = columns.get(key);
            int remaining = column == null ? 1 : column.getClass().getField("remaining").getInt(column);
            classify(owner, world, x, z, status, remaining);
        } catch (Throwable ignored) {
            // An ack we cannot classify must not break the send loop.
        }
    }

    /** Held only on the ack that finishes the column. Status 2 drops a premature mark. */
    static void classify(String owner, String queueWorld, int x, int z, int status, int remaining) {
        String world = walkerWorld(queueWorld);
        if (status == 2) {
            TerrainSentMemory.unmark(owner, world, x, z);
            return;
        }
        if (status == 0 && remaining <= 1) {
            TerrainSentMemory.mark(owner, world, x, z);
            ackedColumns++;
        }
    }

    /** Game-world key the ring walker uses, from the queue's dimension|type|seed string. */
    public static String walkerWorld(String queueWorld) {
        if (queueWorld == null) {
            return "";
        }
        int first = queueWorld.indexOf('|');
        int last = queueWorld.lastIndexOf('|');
        if (first < 0 || last <= first) {
            return queueWorld;
        }
        return queueWorld.substring(0, first) + "#" + queueWorld.substring(last + 1);
    }

    public static long acked() {
        return Math.max(ackedColumns, counter("completedColumns"));
    }

    public static long timedOut() {
        return counter("expired");
    }

    public static long dropped() {
        return counter("cancelled");
    }

    private static long counter(String name) {
        try {
            ensure();
            Object queue = queueField.get(null);
            return queue.getClass().getField(name).getLong(queue);
        } catch (Throwable ignored) {
            return 0L;
        }
    }

    private static void ensure() throws ReflectiveOperationException {
        if (queueField != null) {
            return;
        }
        Class<?> transport = Class.forName("me.cortex.voxy.server.ServerTerrainTransport");
        queueField = transport.getDeclaredField("QUEUE");
        queueField.setAccessible(true);
        Class<?> queue = Class.forName("me.cortex.voxy.common.network.ReliableTerrainQueue");
        flightField = queue.getDeclaredField("flight");
        flightField.setAccessible(true);
        columnsField = queue.getDeclaredField("columns");
        columnsField.setAccessible(true);
    }
}
