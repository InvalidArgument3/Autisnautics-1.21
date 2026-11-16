package me.cortex.voxy.client;

import net.minecraft.client.multiplayer.ClientLevel;

/**
 * The level a server terrain packet should be applied against. Immersive Portals
 * swaps {@code Minecraft.level} for a background dimension; the player's own
 * level stays the world they are standing in. Callers invoke {@code ClientLevel}
 * methods, so the value has to be a client level, not the {@code Level} declared
 * by {@code Entity.level()}.
 */
public final class TerrainReceive {
    public static final int APPLY = 0;
    public static final int DROP = 2;

    private TerrainReceive() {
    }

    public static ClientLevel acceptLevel(Object minecraft) {
        if (minecraft == null) {
            return null;
        }
        try {
            Object player = minecraft.getClass().getField("player").get(minecraft);
            if (player != null) {
                Object level = player.getClass().getMethod("level").invoke(player);
                if (level instanceof ClientLevel) {
                    return (ClientLevel) level;
                }
            }
        } catch (Throwable ignored) {
            // Fall through to the render level.
        }
        try {
            Object level = minecraft.getClass().getField("level").get(minecraft);
            if (level instanceof ClientLevel) {
                return (ClientLevel) level;
            }
        } catch (Throwable ignored) {
            return null;
        }
        return null;
    }

    /**
     * A packet for the player's world is applied even while the render level
     * is a different dimension. A packet for neither world is dropped.
     */
    public static int disposition(boolean matchesPlayerWorld, boolean matchesRenderLevel) {
        if (matchesPlayerWorld) {
            return APPLY;
        }
        return matchesRenderLevel ? APPLY : DROP;
    }
}
