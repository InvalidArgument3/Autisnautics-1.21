package dev.wyseman.dimstackguard;

import java.lang.reflect.Method;

/**
 * The real mod-loader lookup, kept apart from {@link DimStackGuard} so the
 * decision logic stays testable without FML on the classpath.
 *
 * <p>Reaches the loader by reflection rather than by importing
 * {@code FMLLoader}, so the build needs nothing but Mixin. The lookup runs only
 * while the dimension-stack screen is being built, so reflection costs nothing
 * that matters.
 *
 * <p>Never throws. A failure here means the guard reports the namespace as
 * missing, which makes the GUI label fall back to the namespace text instead of
 * taking the screen down.
 */
public final class ModLookup {
    private static final int UNKNOWN = 0;
    private static final int ABSENT = 1;
    private static final int READY = 2;
    private static final int CLOSED = 3;

    private static int mode = UNKNOWN;
    private static Method getLoadingModList;
    private static Method getModFileById;

    private ModLookup() {}

    /** True when the mod loader knows this id. */
    public static boolean present(String modId) {
        if (mode == UNKNOWN) {
            resolve();
        }
        if (mode != READY) {
            return false;
        }
        try {
            Object list = getLoadingModList.invoke(null);
            if (list == null) {
                return false;
            }
            return getModFileById.invoke(list, modId) != null;
        } catch (Throwable ex) {
            mode = CLOSED;
            return false;
        }
    }

    private static void resolve() {
        try {
            Class<?> loader = Class.forName("net.neoforged.fml.loading.FMLLoader");
            Class<?> listType = Class.forName("net.neoforged.fml.loading.LoadingModList");
            getLoadingModList = loader.getMethod("getLoadingModList");
            getModFileById = listType.getMethod("getModFileById", String.class);
            mode = READY;
        } catch (ClassNotFoundException ex) {
            mode = ABSENT;
        } catch (Throwable ex) {
            mode = CLOSED;
        }
    }
}
