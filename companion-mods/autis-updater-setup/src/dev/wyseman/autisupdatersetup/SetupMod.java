package dev.wyseman.autisupdatersetup;

import net.neoforged.fml.common.Mod;

/**
 * Client entry. The toml marks the mod client-only, so a dedicated server
 * does not construct it. The dist check is a second gate.
 */
@Mod(SetupMod.ID)
public final class SetupMod {
    public static final String ID = "autis_updater_setup";

    public SetupMod() {
        try {
            if (!client()) {
                return;
            }
            java.nio.file.Path game = gameDir();
            Setup.run(game, Setup.Phase.LAUNCH);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Setup.run(game, Setup.Phase.SHUTDOWN);
                } catch (Throwable ignored) {
                    // the hook must not fail the exit
                }
            }, "autis-updater-setup"));
        } catch (Throwable ignored) {
            // a launcher we do not understand is not a crash
        }
    }

    private static boolean client() {
        try {
            Class<?> env = Class.forName("net.neoforged.fml.loading.FMLEnvironment");
            Object dist = env.getField("dist").get(null);
            return dist != null && "CLIENT".equals(String.valueOf(dist));
        } catch (Throwable t) {
            return false;
        }
    }

    private static java.nio.file.Path gameDir() {
        try {
            Class<?> paths = Class.forName("net.neoforged.fml.loading.FMLPaths");
            Object gamedir = paths.getField("GAMEDIR").get(null);
            Object path = gamedir.getClass().getMethod("get").invoke(gamedir);
            if (path instanceof java.nio.file.Path p) {
                return p;
            }
        } catch (Throwable ignored) {
            // fall through to the working directory, which these launchers set to the instance
        }
        return java.nio.file.Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
    }
}
