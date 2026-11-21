package dev.wyseman.autisupdatersetup;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Writes the Autisnautics updater into the launcher that started this game.
 * It does not download or apply a pack update.
 *
 * <p>Prism Launcher rewrites {@code instance.cfg} from memory when the game
 * process exits, to store play time. A change made during play is lost unless
 * it is written again after that save. CurseForge can do the same with
 * {@code minecraftinstance.json}. The in-game pass writes immediately, the
 * shutdown hook writes once more, and a detached helper waits until this JVM
 * is gone and the launcher's save has gone quiet, then writes again.
 */
public final class Setup {
    static final String LOG_NAME = "updater-setup.log";
    private static final String STATE_NAME = "updater-setup.state";
    private static final Duration PRELAUNCH_WINDOW = Duration.ofMinutes(15);

    /** Tests set this false so a launch does not spawn a helper process. */
    static volatile boolean allowHelper = true;
    /** Tests point this at a fake master store. Production leaves it empty. */
    static volatile java.util.List<java.nio.file.Path> testMasters = java.util.List.of();
    private static volatile boolean skipRestOfThisProcess;

    private Setup() {}

    public enum Phase {
        LAUNCH,
        SHUTDOWN,
        REAPPLY
    }

    public static void main(String[] args) {
        try {
            if (args.length >= 2 && "reapply".equals(args[0])) {
                long pid = args.length >= 3 ? Long.parseLong(args[2]) : -1L;
                reapplyAfterExit(Path.of(args[1]), pid);
                return;
            }
            if (args.length >= 2 && "apply".equals(args[0])) {
                if (args.length >= 3 && "--no-helper".equals(args[2])) {
                    allowHelper = false;
                }
                run(Path.of(args[1]), Phase.LAUNCH);
                return;
            }
            System.err.println("usage: apply <gameDir> | reapply <gameDir> <pid>");
        } catch (Throwable t) {
            System.err.println("autis_updater_setup: " + t);
        }
    }

    public static void run(Path gameDir, Phase phase) {
        try {
            gameDir = gameDir.toAbsolutePath().normalize();
            if (phase == Phase.LAUNCH) {
                skipRestOfThisProcess = false;
            }
            if (phase == Phase.SHUTDOWN && skipRestOfThisProcess) {
                log(gameDir, "shutdown skipped; this launch left the launcher files alone");
                return;
            }
            if (!enabled(gameDir)) {
                log(gameDir, "disabled by config/autis_updater_setup-client.toml");
                if (phase == Phase.LAUNCH) {
                    skipRestOfThisProcess = true;
                }
                return;
            }
            if (phase == Phase.LAUNCH && updaterAlreadyRan(gameDir)) {
                log(gameDir, "updater already ran this launch; leaving launcher files alone");
                skipRestOfThisProcess = true;
                return;
            }
            if (phase == Phase.LAUNCH) {
                reportPreviousWrite(gameDir);
            }
            boolean changed = apply(gameDir, phase);
            if (changed) {
                writeState(gameDir, "pending");
                if (phase == Phase.LAUNCH && allowHelper) {
                    scheduleHelper(gameDir);
                }
            } else if (phase == Phase.LAUNCH && "pending".equals(readState(gameDir))) {
                writeState(gameDir, "idle");
            }
        } catch (Throwable t) {
            try {
                log(gameDir, "error: " + t);
            } catch (Throwable ignored) {
                // never take the game down
            }
        }
    }

    static void reapplyAfterExit(Path gameDir, long pid) {
        try {
            gameDir = gameDir.toAbsolutePath().normalize();
            if (pid >= 0) {
                log(gameDir, "post-exit helper waiting for pid " + pid);
                if (!waitForExit(pid)) {
                    log(gameDir, "post-exit helper gave up; pid " + pid + " still alive");
                    return;
                }
                waitForQuiet(gameDir);
            }
            run(gameDir, Phase.REAPPLY);
        } catch (Throwable t) {
            try {
                log(gameDir, "error: " + t);
            } catch (Throwable ignored) {
                // the game has already exited
            }
        }
    }

    private static boolean apply(Path gameDir, Phase phase) throws IOException {
        boolean changed = false;
        boolean known = false;
        Path prism = prismCfg(gameDir);
        if (prism != null) {
            known = true;
            changed |= writeText(prism, PrismPatch.patch(read(prism)), false, gameDir,
                    phase == Phase.REAPPLY ? "post-exit rewrite prism" : "patched prism",
                    "prism already hooked");
        }
        Path curse = curseInstance(gameDir);
        if (curse != null) {
            known = true;
            Path dir = curse.getParent();
            changed |= writeText(curse, CursePatch.patchInstance(read(curse)), true, gameDir,
                    phase == Phase.REAPPLY ? "post-exit rewrite curseforge instance" : "patched curseforge instance",
                    "curseforge instance already hooked");
            changed |= patchMasters(gameDir, dir == null ? "" : dir.toString(), phase);
        }
        if (prism == null && curse == null) {
            int modrinth = applyModrinth(gameDir, phase);
            if (modrinth < 0 && !known) {
                log(gameDir, "unsupported launcher");
            }
            changed |= modrinth > 0;
        }
        return changed;
    }

    /** @return 1 if a file changed, 0 if Modrinth was recognized and left as-is, -1 otherwise */
    private static int applyModrinth(Path gameDir, Phase phase) throws IOException {
        Path profile = profileJson(gameDir);
        if (profile != null) {
            String text = read(profile);
            if (ModrinthPatch.looksLikeProfile(text)) {
                String patched = ModrinthPatch.patch(text);
                if (patched == null) {
                    log(gameDir, "unsupported launcher; modrinth profile.json has no hook or jvm args field");
                    return 0;
                }
                return writeText(profile, patched, true, gameDir,
                        phase == Phase.REAPPLY ? "post-exit rewrite modrinth" : "patched modrinth",
                        "modrinth already hooked") ? 1 : 0;
            }
        }
        if (ModrinthPatch.looksLikeAppPath(gameDir.toString())) {
            log(gameDir, "unsupported launcher; modrinth app keeps override_hook_pre_launch in its database");
            return 0;
        }
        return -1;
    }

    private static boolean patchMasters(Path gameDir, String instanceDir, Phase phase) throws IOException {
        boolean any = false;
        boolean found = false;
        for (Path store : masters()) {
            if (!Files.isRegularFile(store)) {
                continue;
            }
            found = true;
            try {
                String text = read(store);
                if (!CursePatch.masterHasMatch(text, instanceDir)) {
                    log(gameDir, "curseforge master store has no matching installPath: " + store);
                    continue;
                }
                any |= writeText(store, CursePatch.patchMaster(text, instanceDir), true, gameDir,
                        phase == Phase.REAPPLY ? "post-exit rewrite curseforge master " + store : "patched curseforge master " + store,
                        "curseforge master already hooked " + store);
            } catch (Throwable t) {
                log(gameDir, "error: curseforge master " + store + ": " + t.getMessage());
            }
        }
        if (!found) {
            log(gameDir, "curseforge master store not found");
        }
        return any;
    }

    private static boolean writeText(Path file, String next, boolean backup, Path gameDir, String changedMsg, String sameMsg)
            throws IOException {
        String current = read(file);
        if (current.equals(next)) {
            log(gameDir, sameMsg);
            return false;
        }
        if (backup) {
            AtomicFiles.backupOnce(file);
        }
        AtomicFiles.write(file, next.getBytes(StandardCharsets.UTF_8));
        log(gameDir, changedMsg);
        return true;
    }

    static Path prismCfg(Path gameDir) {
        Path parent = gameDir.getParent();
        if (parent != null && Files.isRegularFile(parent.resolve("instance.cfg"))) {
            return parent.resolve("instance.cfg");
        }
        Path here = gameDir.resolve("instance.cfg");
        return Files.isRegularFile(here) ? here : null;
    }

    static Path curseInstance(Path gameDir) {
        Path here = gameDir.resolve("minecraftinstance.json");
        if (Files.isRegularFile(here)) {
            return here;
        }
        Path parent = gameDir.getParent();
        if (parent != null && Files.isRegularFile(parent.resolve("minecraftinstance.json"))) {
            return parent.resolve("minecraftinstance.json");
        }
        return null;
    }

    static Path profileJson(Path gameDir) {
        Path here = gameDir.resolve("profile.json");
        if (Files.isRegularFile(here)) {
            return here;
        }
        Path parent = gameDir.getParent();
        if (parent != null && Files.isRegularFile(parent.resolve("profile.json"))) {
            return parent.resolve("profile.json");
        }
        return null;
    }

    static boolean enabled(Path gameDir) {
        Path cfg = gameDir.resolve("config").resolve("autis_updater_setup-client.toml");
        if (!Files.isRegularFile(cfg)) {
            return true;
        }
        try {
            for (String line : Files.readAllLines(cfg, StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                if (trimmed.startsWith("enabled")) {
                    int eq = trimmed.indexOf('=');
                    if (eq < 0) {
                        return true;
                    }
                    return !trimmed.substring(eq + 1).trim().equals("false");
                }
            }
        } catch (Throwable t) {
            log(gameDir, "config unreadable, leaving the setup enabled: " + t);
        }
        return true;
    }

    static boolean updaterAlreadyRan(Path gameDir) {
        Path log = gameDir.resolve("autis").resolve("updater.log");
        if (!Files.isRegularFile(log)) {
            return false;
        }
        String last;
        try {
            last = lastLine(Files.readString(log, StandardCharsets.UTF_8));
        } catch (IOException e) {
            log(gameDir, "error: cannot read updater.log: " + e.getMessage());
            return false;
        }
        if (last.isEmpty()) {
            return false;
        }
        boolean agent = last.contains("mode=agent");
        boolean prelaunch = last.contains("mode=prelaunch");
        if (!agent && !prelaunch) {
            return false;
        }
        Instant stamped = leadingInstant(last);
        if (stamped == null) {
            return false;
        }
        long when = stamped.toEpochMilli();
        long start = ManagementFactory.getRuntimeMXBean().getStartTime();
        long now = System.currentTimeMillis();
        if (agent) {
            return when >= start - 5_000L && when <= now + 5_000L;
        }
        return when >= start - PRELAUNCH_WINDOW.toMillis() && when <= start + 60_000L;
    }

    private static void reportPreviousWrite(Path gameDir) {
        if (!"pending".equals(readState(gameDir))) {
            return;
        }
        if (hookPresent(gameDir)) {
            log(gameDir, "previous write stuck");
        } else {
            log(gameDir, "previous write was overwritten");
        }
    }

    private static boolean hookPresent(Path gameDir) {
        try {
            Path prism = prismCfg(gameDir);
            if (prism != null) {
                return read(prism).contains("autis/updater.jar") && read(prism).contains("OverrideCommands=true");
            }
            Path curse = curseInstance(gameDir);
            if (curse != null) {
                return CursePatch.hasAgent(read(curse));
            }
            Path profile = profileJson(gameDir);
            if (profile != null) {
                return read(profile).contains("autis/updater.jar");
            }
        } catch (IOException e) {
            log(gameDir, "error: " + e.getMessage());
        }
        return false;
    }

    private static void scheduleHelper(Path gameDir) {
        Path jar = selfJar();
        if (jar == null) {
            log(gameDir, "no jar path, skipping the post-exit rewrite");
            return;
        }
        String java = javaBinary();
        long pid = ProcessHandle.current().pid();
        List<String> cmd = new ArrayList<>();
        if (!isWindows() && Files.isExecutable(Path.of("/usr/bin/setsid"))) {
            cmd.add("/usr/bin/setsid");
        }
        cmd.add(java);
        cmd.add("-cp");
        cmd.add(jar.toString());
        cmd.add("dev.wyseman.autisupdatersetup.Setup");
        cmd.add("reapply");
        cmd.add(gameDir.toString());
        cmd.add(Long.toString(pid));
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            pb.redirectError(ProcessBuilder.Redirect.DISCARD);
            pb.redirectInput(ProcessBuilder.Redirect.PIPE);
            pb.start();
            log(gameDir, "scheduled post-exit rewrite");
        } catch (IOException e) {
            log(gameDir, "error: could not schedule post-exit rewrite: " + e.getMessage());
        }
    }

    static Path selfJar() {
        try {
            var url = Setup.class.getProtectionDomain().getCodeSource().getLocation();
            if (url == null) {
                return null;
            }
            Path path = Path.of(url.toURI());
            if (Files.isRegularFile(path) && path.getFileName().toString().endsWith(".jar")) {
                return path;
            }
        } catch (Throwable ignored) {
            return null;
        }
        return null;
    }

    private static List<Path> masters() {
        if (testMasters.isEmpty()) {
            return CursePatch.masterStores();
        }
        List<Path> all = new ArrayList<>(CursePatch.masterStores());
        all.addAll(testMasters);
        return all;
    }

    private static boolean waitForExit(long pid) throws InterruptedException {
        long deadline = System.nanoTime() + Duration.ofHours(12).toNanos();
        while (System.nanoTime() < deadline) {
            Optional<ProcessHandle> handle = ProcessHandle.of(pid);
            if (handle.isEmpty() || !handle.get().isAlive()) {
                return true;
            }
            Thread.sleep(500L);
        }
        Optional<ProcessHandle> handle = ProcessHandle.of(pid);
        return handle.isEmpty() || !handle.get().isAlive();
    }

    /**
     * After this process dies, Prism or CurseForge may still be writing the
     * file from the copy it held in memory. Wait until those files stop
     * changing, then let the caller write.
     */
    private static void waitForQuiet(Path gameDir) throws InterruptedException {
        List<Path> files = watched(gameDir);
        long death = System.nanoTime();
        long deadline = death + Duration.ofSeconds(8).toNanos();
        long stableSince = death;
        String sig = signature(files);
        while (System.nanoTime() < deadline) {
            Thread.sleep(250L);
            String now = signature(files);
            if (!now.equals(sig)) {
                sig = now;
                stableSince = System.nanoTime();
                log(gameDir, "launcher rewrote a watched file after exit");
            } else if (System.nanoTime() - stableSince >= Duration.ofSeconds(1).toNanos()
                    && System.nanoTime() - death >= Duration.ofSeconds(1).toNanos()) {
                return;
            }
        }
    }

    private static List<Path> watched(Path gameDir) {
        List<Path> files = new ArrayList<>();
        Path prism = prismCfg(gameDir);
        if (prism != null) {
            files.add(prism);
        }
        Path curse = curseInstance(gameDir);
        if (curse != null) {
            files.add(curse);
            String dir = curse.getParent() == null ? "" : curse.getParent().toString();
            for (Path store : masters()) {
                if (Files.isRegularFile(store)) {
                    try {
                        if (CursePatch.masterHasMatch(read(store), dir)) {
                            files.add(store);
                        }
                    } catch (IOException ignored) {
                        // a store we cannot read is not worth waiting on
                    }
                }
            }
        }
        Path profile = profileJson(gameDir);
        if (profile != null) {
            files.add(profile);
        }
        return files;
    }

    private static String signature(List<Path> files) {
        StringBuilder out = new StringBuilder();
        for (Path file : files) {
            try {
                out.append(file).append('=').append(Files.getLastModifiedTime(file).toMillis())
                        .append(':').append(Files.size(file)).append(';');
            } catch (IOException e) {
                out.append(file).append("=missing;");
            }
        }
        return out.toString();
    }

    private static void writeState(Path gameDir, String value) {
        try {
            Path file = gameDir.resolve("autis").resolve(STATE_NAME);
            Files.createDirectories(file.getParent());
            AtomicFiles.write(file, value.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            log(gameDir, "error: cannot write state: " + e.getMessage());
        }
    }

    private static String readState(Path gameDir) {
        Path file = gameDir.resolve("autis").resolve(STATE_NAME);
        try {
            if (!Files.isRegularFile(file)) {
                return "";
            }
            return Files.readString(file, StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            return "";
        }
    }

    private static String read(Path file) throws IOException {
        return Files.readString(file, StandardCharsets.UTF_8);
    }

    static void log(Path gameDir, String message) {
        String line = Instant.now().truncatedTo(ChronoUnit.SECONDS) + " " + message + System.lineSeparator();
        try {
            Path file = gameDir.resolve("autis").resolve(LOG_NAME);
            Files.createDirectories(file.getParent());
            Files.writeString(file, line, StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (Throwable ignored) {
            // logging must not throw into the game
        }
    }

    private static String lastLine(String text) {
        String last = "";
        for (String line : text.split("\\R")) {
            if (!line.isBlank()) {
                last = line.trim();
            }
        }
        return last;
    }

    private static Instant leadingInstant(String line) {
        int space = line.indexOf(' ');
        String token = space < 0 ? line : line.substring(0, space);
        try {
            return Instant.parse(token);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String javaBinary() {
        String name = isWindows() ? "java.exe" : "java";
        return Path.of(System.getProperty("java.home"), "bin", name).toString();
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("win");
    }
}
