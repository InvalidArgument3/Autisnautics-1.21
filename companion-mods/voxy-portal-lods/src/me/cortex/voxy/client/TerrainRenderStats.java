package me.cortex.voxy.client;

import java.lang.reflect.Field;
import me.cortex.voxy.common.world.WorldEngine;

/**
 * Counts what the renderer actually does with a section after the apply
 * callback. {@code renderNotifies} only means the dirty callback ran.
 */
public final class TerrainRenderStats {
    private static long meshRequested;
    private static long meshCompleted;
    private static long gpuUploads;
    private static long gpuUploadBytes;
    private static long trackerInserts;
    private static long nodesDrawn;
    private static int farthestChunks;

    private TerrainRenderStats() {
    }

    public static void requested(long position) {
        meshRequested++;
        note(position);
    }

    public static void meshCompleted(Object section) {
        meshCompleted++;
        note(position(section));
    }

    public static void uploaded(Object section) {
        gpuUploads++;
        gpuUploadBytes += bytes(section);
        nodesDrawn++;
        note(position(section));
    }

    public static void tracker(long position) {
        trackerInserts++;
        note(position);
    }

    public static void removed() {
        if (nodesDrawn > 0) {
            nodesDrawn--;
        }
    }

    public static String line() {
        return "meshRequested=" + meshRequested
                + " meshCompleted=" + meshCompleted
                + " gpuUploads=" + gpuUploads
                + " gpuUploadBytes=" + gpuUploadBytes
                + " trackerInserts=" + trackerInserts
                + " nodesDrawn=" + nodesDrawn
                + " farthestRing=" + farthestChunks;
    }

    private static void note(long position) {
        if (position == 0L) {
            return;
        }
        int level = WorldEngine.getLevel(position);
        int scale = 2 << level;
        int chunks = Math.max(Math.abs(WorldEngine.getX(position)), Math.abs(WorldEngine.getZ(position))) * scale;
        if (chunks > farthestChunks) {
            farthestChunks = chunks;
        }
    }

    private static long position(Object section) {
        try {
            Field field = section.getClass().getField("position");
            return field.getLong(section);
        } catch (Throwable ignored) {
            return 0L;
        }
    }

    private static long bytes(Object section) {
        try {
            Object buffer = section.getClass().getField("geometryBuffer").get(section);
            if (buffer == null) {
                return 0L;
            }
            return buffer.getClass().getField("size").getLong(buffer);
        } catch (Throwable ignored) {
            return 0L;
        }
    }
}
