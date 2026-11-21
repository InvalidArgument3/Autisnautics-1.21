package dev.wyseman.autisupdatersetup;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

/**
 * Prism keeps instance.cfg in memory and writes that copy when the game JVM
 * exits. This starts the real helper from the packaged jar, then does that
 * write from the original bytes, and checks the helper puts the hook back.
 */
public final class ExitRewriteHarness {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("usage: ExitRewriteHarness <mod.jar>");
        }
        Path jar = Path.of(args[0]).toAbsolutePath().normalize();
        Path root = Files.createTempDirectory("autis-prism-exit");
        Path mc = root.resolve("minecraft");
        Path cfg = root.resolve("instance.cfg");
        Files.createDirectories(mc);
        byte[] original = "[General]\nInstanceType=OneSix\nname=Exit rewrite\n".getBytes();
        Files.write(cfg, original);

        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        Process game = new ProcessBuilder(
                java, "-cp", jar.toString(),
                "dev.wyseman.autisupdatersetup.Setup", "apply", mc.toString())
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .start();
        int code = game.waitFor();
        if (code != 0) {
            throw new AssertionError("game exited " + code);
        }
        // Let the helper notice the exit and snapshot the patched file, then
        // write the pre-launch bytes the way Prism does from memory.
        Instant armed = Instant.now().plus(Duration.ofSeconds(5));
        String started = "";
        while (Instant.now().isBefore(armed)) {
            started = Files.exists(mc.resolve("autis/updater-setup.log"))
                    ? Files.readString(mc.resolve("autis/updater-setup.log")) : "";
            if (started.contains("post-exit helper waiting")) {
                break;
            }
            Thread.sleep(20L);
        }
        Thread.sleep(400L);
        Files.write(cfg, original);
        if (Files.readString(cfg).contains("autis/updater.jar")) {
            throw new AssertionError("exit rewrite did not remove the hook");
        }

        Instant deadline = Instant.now().plus(Duration.ofSeconds(25));
        String log = "";
        String text = "";
        while (Instant.now().isBefore(deadline)) {
            Thread.sleep(100L);
            text = Files.readString(cfg);
            log = Files.exists(mc.resolve("autis/updater-setup.log"))
                    ? Files.readString(mc.resolve("autis/updater-setup.log")) : "";
            if (text.contains("autis/updater.jar") && text.contains("OverrideCommands=true")
                    && log.contains("post-exit rewrite verified")) {
                break;
            }
        }
        if (!text.contains("autis/updater.jar") || !log.contains("post-exit rewrite verified")) {
            String err = Files.exists(mc.resolve("autis/updater-setup-helper.err"))
                    ? Files.readString(mc.resolve("autis/updater-setup-helper.err")) : "";
            throw new AssertionError("helper did not restore the hook\ncfg:\n" + text + "\nlog:\n" + log + "\nerr:\n" + err);
        }
        if (!log.contains("scheduled post-exit rewrite")) {
            throw new AssertionError("helper was not scheduled\n" + log);
        }
        if (!log.contains("launcher rewrote a watched file after exit")) {
            throw new AssertionError("helper did not see Prism's exit rewrite\n" + log);
        }

        Setup.allowHelper = false;
        Setup.run(mc, Setup.Phase.LAUNCH);
        log = Files.readString(mc.resolve("autis/updater-setup.log"));
        if (!log.contains("previous write stuck")) {
            throw new AssertionError("next launch did not report the pending write\n" + log);
        }
        if (!Files.readString(mc.resolve("autis/updater-setup.state")).trim().equals("idle")) {
            throw new AssertionError("pending state was not cleared");
        }
        String latest = Files.readString(mc.resolve("logs/latest.log"));
        if (!latest.contains("[autis_updater_setup] patched prism")
                || !latest.contains("[autis_updater_setup] previous write stuck")) {
            throw new AssertionError("latest.log missing the info line\n" + latest);
        }
        System.out.println("exit rewrite ok");
    }
}
