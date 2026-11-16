package net.minecraft.world.level;

/** Just enough for WalkCursor.next to run outside a game. */
public final class ChunkPos {
    private ChunkPos() {
    }

    public static long asLong(int x, int z) {
        return (long) x & 0xFFFFFFFFL | ((long) z & 0xFFFFFFFFL) << 32;
    }
}
