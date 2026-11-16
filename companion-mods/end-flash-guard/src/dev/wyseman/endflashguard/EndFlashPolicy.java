package dev.wyseman.endflashguard;

/**
 * Decides whether VanillaBackport may advance or display an end flash.
 *
 * <p>The player level is remembered from a tick that is not inside
 * {@code ClientWorldLoader.withSwitchedWorld}. Immersive Portals points
 * {@code Minecraft.level} at the background world for that whole tick, so
 * comparing the ticking level with {@code Minecraft.level} is true for the
 * hidden End as well. Visuals use the remembered level because a portal
 * render also overwrites {@code Minecraft.level} and does not set the
 * switched flag.
 */
public final class EndFlashPolicy {
    private Object playerLevel;

    public void observe(Object minecraftLevel, boolean worldSwitched) {
        if (!worldSwitched && minecraftLevel != null) {
            this.playerLevel = minecraftLevel;
        }
    }

    public boolean allowsTick(Object tickingLevel, boolean worldSwitched) {
        return tickingLevel != null && tickingLevel == this.playerLevel && !worldSwitched;
    }

    public boolean allowsVisual(Object minecraftLevel) {
        return minecraftLevel != null && minecraftLevel == this.playerLevel;
    }

    public Object playerLevel() {
        return this.playerLevel;
    }
}
