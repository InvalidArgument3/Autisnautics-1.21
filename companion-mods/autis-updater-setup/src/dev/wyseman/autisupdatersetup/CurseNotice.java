package dev.wyseman.autisupdatersetup;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;

/**
 * A small client notice. The mod is compiled without Minecraft on the
 * classpath, so the toast is reached by reflection and a missing client is
 * a skipped notice, not a crash.
 */
final class CurseNotice {
    private CurseNotice() {}

    static boolean show(Path gameDir, String version) {
        Class<?> minecraft = findMinecraft();
        if (minecraft == null) {
            return false;
        }
        try {
            Object mc = null;
            for (int i = 0; i < 20; i++) {
                mc = minecraft.getMethod("getInstance").invoke(null);
                if (mc != null) {
                    break;
                }
                Thread.sleep(500L);
            }
            if (mc == null) {
                return false;
            }
            Object client = mc;
            Runnable task = () -> display(gameDir, client, version);
            try {
                minecraft.getMethod("execute", Runnable.class).invoke(mc, task);
            } catch (NoSuchMethodException e) {
                minecraft.getMethod("tell", Runnable.class).invoke(mc, task);
            }
            return true;
        } catch (Throwable t) {
            Setup.log(gameDir, "curseforge mode: in-game notice failed: " + t.getMessage());
            return false;
        }
    }

    private static void display(Path gameDir, Object mc, String version) {
        String line = version + " will be installed when the game closes";
        try {
            if (toast(mc, "Autisnautics update", line)) {
                Setup.log(gameDir, "curseforge mode: in-game notice shown");
                Setup.gameInfo(gameDir, "curseforge mode: in-game notice shown");
                return;
            }
        } catch (Throwable t) {
            Setup.log(gameDir, "curseforge mode: toast failed: " + t.getMessage());
        }
        try {
            if (chat(mc, "Autisnautics update: " + line)) {
                Setup.log(gameDir, "curseforge mode: in-game notice shown");
                Setup.gameInfo(gameDir, "curseforge mode: in-game notice shown");
                return;
            }
        } catch (Throwable t) {
            Setup.log(gameDir, "curseforge mode: chat notice failed: " + t.getMessage());
        }
        Setup.log(gameDir, "curseforge mode: in-game notice skipped");
        Setup.gameInfo(gameDir, "curseforge mode: in-game notice skipped");
    }

    private static Class<?> findMinecraft() {
        ClassLoader[] loaders = {
                Thread.currentThread().getContextClassLoader(),
                CurseNotice.class.getClassLoader(),
                ClassLoader.getSystemClassLoader()
        };
        for (ClassLoader loader : loaders) {
            if (loader == null) {
                continue;
            }
            try {
                return Class.forName("net.minecraft.client.Minecraft", false, loader);
            } catch (ClassNotFoundException ignored) {
                // try the next loader
            }
        }
        return null;
    }

    private static boolean toast(Object mc, String title, String message) throws ReflectiveOperationException {
        ClassLoader loader = mc.getClass().getClassLoader();
        Class<?> component = Class.forName("net.minecraft.network.chat.Component", false, loader);
        Class<?> toast = Class.forName("net.minecraft.client.gui.components.toasts.SystemToast", false, loader);
        Object titleText = component.getMethod("literal", String.class).invoke(null, title);
        Object bodyText = component.getMethod("literal", String.class).invoke(null, message);
        Object toasts = mc.getClass().getMethod("getToasts").invoke(mc);
        Class<?> idClass = null;
        Object id = null;
        for (Class<?> nested : toast.getDeclaredClasses()) {
            if (!nested.getSimpleName().equals("SystemToastId")) {
                continue;
            }
            idClass = nested;
            for (Field field : nested.getFields()) {
                if (Modifier.isStatic(field.getModifiers()) && nested.isAssignableFrom(field.getType())) {
                    id = field.get(null);
                    if (id != null) {
                        break;
                    }
                }
            }
            break;
        }
        if (id == null && idClass != null) {
            try {
                id = idClass.getConstructor().newInstance();
            } catch (NoSuchMethodException e) {
                id = idClass.getConstructor(int.class).newInstance(1);
            }
        }
        if (id == null || toasts == null) {
            return false;
        }
        Method add = null;
        for (Method method : toast.getMethods()) {
            if (method.getName().equals("add") && Modifier.isStatic(method.getModifiers())
                    && method.getParameterCount() == 4) {
                add = method;
                break;
            }
        }
        if (add == null) {
            return false;
        }
        add.invoke(null, toasts, id, titleText, bodyText);
        return true;
    }

    private static boolean chat(Object mc, String message) throws ReflectiveOperationException {
        Object player;
        try {
            player = mc.getClass().getField("player").get(mc);
        } catch (NoSuchFieldException e) {
            player = mc.getClass().getMethod("player").invoke(mc);
        }
        if (player == null) {
            return false;
        }
        ClassLoader loader = mc.getClass().getClassLoader();
        Class<?> component = Class.forName("net.minecraft.network.chat.Component", false, loader);
        Object text = component.getMethod("literal", String.class).invoke(null, message);
        player.getClass().getMethod("displayClientMessage", component, boolean.class).invoke(player, text, false);
        return true;
    }
}
