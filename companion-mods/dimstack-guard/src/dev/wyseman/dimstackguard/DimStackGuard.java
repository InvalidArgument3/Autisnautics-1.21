package dev.wyseman.dimstackguard;

import java.util.function.Predicate;

/**
 * Decides whether a dimension namespace has a mod behind it.
 *
 * <p>Immersive Portals builds the dimension-stack GUI label with
 * {@code McHelper.getDimensionName}, which falls back to
 * {@code O_O.getModName(namespace)} when the dimension has no translation key.
 * {@code getModName} is:
 *
 * <pre>
 * FMLLoader.getLoadingModList().getModFileById(modId).getMods()...
 * </pre>
 *
 * <p>with no null check, so any namespace that is not a mod id throws
 * {@code NullPointerException} out of {@code getModFileById}. Datapack
 * dimensions do exactly that: this pack's stack contains {@code oceanworld},
 * {@code dimension_of_caves}, {@code infinite_abyss}, {@code infernalcross} and
 * {@code autisnautics}, none of which are mods. The GUI then dies while
 * building its first entry widget and the client reports a failed packet.
 *
 * <p>{@code getDimensionName} already handles a null return —
 * {@code modName != null ? modName : namespace} — so the guard only has to stop
 * the call from throwing. Both {@code getModName} and {@code getDimensionName}
 * are display-only: {@code getModName} is called by nothing but
 * {@code McHelper}, and {@code getDimensionName} by nothing but
 * {@code DimEntryWidget}.
 *
 * <p>The lookup is injected so the decision can be tested without a mod loader.
 * A lookup that throws is treated as "missing", so an unexpected failure shows
 * the namespace in the label instead of crashing the screen.
 */
public final class DimStackGuard {
    private DimStackGuard() {}

    /** True when {@code getModName} must not be called for this namespace. */
    public static boolean isMissingMod(String modId, Predicate<String> modPresent) {
        if (modId == null || modId.isEmpty()) {
            return true;
        }
        try {
            return !modPresent.test(modId);
        } catch (Throwable ex) {
            return true;
        }
    }
}
