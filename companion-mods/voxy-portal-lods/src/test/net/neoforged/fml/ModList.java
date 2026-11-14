package net.neoforged.fml;

/** Test classpath stand-in so Voxy can initialise outside a NeoForge runtime. */
public final class ModList {
    public static ModList get() {
        return null;
    }

    private ModList() {
    }
}
