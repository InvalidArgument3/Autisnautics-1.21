package dev.wyseman.endflashguard;

import java.lang.reflect.Method;

/** Runtime half of {@link EndFlashPolicy}. The switched-world flag is read from Immersive Portals. */
public final class EndFlashGuard {
    public static final EndFlashPolicy POLICY = new EndFlashPolicy();

    private static final int UNKNOWN = 0;
    private static final int ABSENT = 1;
    private static final int READY = 2;
    private static final int CLOSED = 3;

    private static int mode = UNKNOWN;
    private static Method switched;

    private EndFlashGuard() {}

    /**
     * True while {@code ClientWorldLoader.withSwitchedWorld} is inside its runnable.
     * Missing Immersive Portals means nothing swaps the client level. A present class
     * whose flag cannot be read fails closed, so a background End cannot play the sound.
     */
    public static boolean worldSwitched() {
        if (mode == UNKNOWN) {
            resolve();
        }
        if (mode == ABSENT) {
            return false;
        }
        if (mode == CLOSED) {
            return true;
        }
        try {
            return Boolean.TRUE.equals(switched.invoke(null));
        } catch (Throwable ex) {
            mode = CLOSED;
            return true;
        }
    }

    private static void resolve() {
        try {
            Class<?> type = Class.forName("qouteall.imm_ptl.core.ClientWorldLoader");
            switched = type.getMethod("getIsWorldSwitched");
            mode = READY;
        } catch (ClassNotFoundException ex) {
            mode = ABSENT;
        } catch (Throwable ex) {
            mode = CLOSED;
        }
    }
}
