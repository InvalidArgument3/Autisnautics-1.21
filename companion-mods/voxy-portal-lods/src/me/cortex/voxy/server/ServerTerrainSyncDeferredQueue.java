package me.cortex.voxy.server;

/** Called at the start of every dedicated-server tick. The profile rides this hook. */
public final class ServerTerrainSyncDeferredQueue {
    private ServerTerrainSyncDeferredQueue() {
    }

    public static void drainForTick() {
        TerrainWorkers.drain();
        ColumnProfile.onTick();
    }

    public static void clear() {
        ServerTerrainTransport.clear();
    }
}
