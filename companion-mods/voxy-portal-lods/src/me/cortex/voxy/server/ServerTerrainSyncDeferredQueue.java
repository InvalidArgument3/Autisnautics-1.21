package me.cortex.voxy.server;
/** Compatibility facade: v2 transport owns the only bounded queue and tick listener. */
public final class ServerTerrainSyncDeferredQueue {
 private ServerTerrainSyncDeferredQueue(){}
 public static void drainForTick(){}
 public static void clear(){ServerTerrainTransport.clear();}
}
