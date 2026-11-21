package dev.wyseman.autisupdatersetup;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Writes the Autisnautics updater into the launcher that started this game.
 * It does not download or apply a pack update.
 *
 * <p>Prism Launcher keeps {@code instance.cfg} in memory and writes that copy
 * when the game process exits, which removes a hook written during play. The
 * shutdown hook runs before that save, so it cannot win. A detached helper
 * waits until this JVM is gone and the launcher file has stopped changing,
 * then writes the hook again. The helper is a plain copy of this mod at
 * {@code autis/updater-setup-helper.jar}, because NeoForge's jar filesystem
 * hides the mod's CodeSource from the game.
 */
public final class Setup {
    static final String LOG_NAME = "updater-setup.log";
    private static final String STATE_NAME = "updater-setup.state";
    private static final String HELPER_NAME = "updater-setup-helper.jar";
    private static final Duration PRELAUNCH_WINDOW = Duration.ofMinutes(15);
    /** Prism's exit save lands within about 50 ms. Keep watching longer than that. */
    private static final Duration QUIET_MAX = Duration.ofSeconds(30);
    private static final Duration QUIET_STABLE = Duration.ofSeconds(1);
    private static final long QUIET_POLL_MS = 50L;

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
                // Pending means the previous session's write was not confirmed.
                // Log it before any skip, or a launch that leaves the files alone
                // never says whether Prism kept the hook.
                reportPreviousWrite(gameDir);
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
            if (watched(gameDir).isEmpty()) {
                return;
            }
            if (hookPresent(gameDir)) {
                log(gameDir, "post-exit rewrite verified");
            } else {
                log(gameDir, "post-exit rewrite did not stick");
            }
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
                gameInfo(gameDir, "unsupported launcher");
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
                    gameInfo(gameDir, "unsupported launcher");
                    return 0;
                }
                return writeText(profile, patched, true, gameDir,
                        phase == Phase.REAPPLY ? "post-exit rewrite modrinth" : "patched modrinth",
                        "modrinth already hooked") ? 1 : 0;
            }
        }
        if (ModrinthPatch.looksLikeAppPath(gameDir.toString())) {
            log(gameDir, "unsupported launcher; modrinth app keeps override_hook_pre_launch in its database");
            gameInfo(gameDir, "unsupported launcher");
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
            gameInfo(gameDir, sameMsg);
            return false;
        }
        if (backup) {
            AtomicFiles.backupOnce(file);
        }
        AtomicFiles.write(file, next.getBytes(StandardCharsets.UTF_8));
        log(gameDir, changedMsg);
        gameInfo(gameDir, changedMsg);
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
            gameInfo(gameDir, "previous write stuck");
        } else {
            log(gameDir, "previous write was overwritten");
            gameInfo(gameDir, "previous write was overwritten");
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
        Path jar = materializeHelper(gameDir);
        if (jar == null) {
            log(gameDir, "no jar path, skipping the post-exit rewrite");
            gameInfo(gameDir, "no jar path, skipping the post-exit rewrite");
            return;
        }
        String java = javaCommand();
        long pid = ProcessHandle.current().pid();
        List<String> tail = List.of(
                "-cp", jar.toString(),
                "dev.wyseman.autisupdatersetup.Setup",
                "reapply", gameDir.toString(), Long.toString(pid));
        List<String> cmd = new ArrayList<>();
        if (isWindows()) {
            // start is a cmd builtin. One command line, so a Java path with spaces stays quoted.
            cmd.add("cmd.exe");
            cmd.add("/c");
            cmd.add("start \"autis-updater-setup\" /b " + quote(java) + " " + joinQuoted(tail));
        } else {
            Path setsid = executable("/usr/bin/setsid", "/bin/setsid");
            Path nohup = executable("/usr/bin/nohup", "/bin/nohup");
            if (setsid != null) {
                cmd.add(setsid.toString());
            }
            if (nohup != null) {
                cmd.add(nohup.toString());
            }
            cmd.add(java);
            cmd.addAll(tail);
        }
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            Path err = gameDir.resolve("autis").resolve("updater-setup-helper.err");
            Files.createDirectories(err.getParent());
            if (!Files.exists(err)) {
                Files.writeString(err, "");
            }
            Path devNull = isWindows() ? Path.of("NUL") : Path.of("/dev/null");
            pb.redirectInput(ProcessBuilder.Redirect.from(devNull.toFile()));
            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(err.toFile()));
            pb.redirectError(ProcessBuilder.Redirect.appendTo(err.toFile()));
            pb.start();
            String where = Files.isRegularFile(Path.of("/.flatpak-info"))
                    ? " (flatpak sandbox, detached inside it)" : "";
            log(gameDir, "scheduled post-exit rewrite" + where + " java=" + java);
            gameInfo(gameDir, "scheduled post-exit rewrite");
        } catch (IOException e) {
            log(gameDir, "error: could not schedule post-exit rewrite: " + e.getMessage());
            gameInfo(gameDir, "could not schedule post-exit rewrite");
        }
    }

    /**
     * A plain jar the helper JVM can open. NeoForge serves this mod from a
     * union filesystem, so {@code CodeSource} is not a file path.
     */
    static Path materializeHelper(Path gameDir) {
        try {
            Path dir = gameDir.resolve("autis");
            Files.createDirectories(dir);
            Path dest = dir.resolve(HELPER_NAME);
            Path source = locateModJar(gameDir);
            if (source != null) {
                copyJar(source, dest);
                log(gameDir, "helper jar copied from " + source);
                return dest;
            }
            if (writeHelperFromClasses(dest)) {
                log(gameDir, "helper jar built from loaded classes");
                return dest;
            }
        } catch (IOException e) {
            log(gameDir, "error: cannot write helper jar: " + e.getMessage());
            try {
                Path alt = gameDir.resolve("autis").resolve(
                        "updater-setup-helper-" + ProcessHandle.current().pid() + ".jar");
                Path source = locateModJar(gameDir);
                if (source != null) {
                    copyJar(source, alt);
                    return alt;
                }
                if (writeHelperFromClasses(alt)) {
                    return alt;
                }
            } catch (IOException ignored) {
                // logged above
            }
        }
        return null;
    }

    static Path locateModJar(Path gameDir) {
        Path fromResource = jarFromOwnClass();
        if (isJarFile(fromResource)) {
            return fromResource;
        }
        Path fromCode = codeSourceJar();
        if (isJarFile(fromCode)) {
            return fromCode;
        }
        Path fromModList = modListJar();
        if (isJarFile(fromModList)) {
            return fromModList;
        }
        return scanModsDir(gameDir);
    }

    private static Path jarFromOwnClass() {
        URL url = Setup.class.getResource("Setup.class");
        if (url == null) {
            return null;
        }
        String text = url.toString();
        if (!text.startsWith("jar:")) {
            return null;
        }
        int bang = text.indexOf("!/");
        if (bang < 0) {
            return null;
        }
        try {
            return Path.of(URI.create(text.substring("jar:".length(), bang)));
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Path codeSourceJar() {
        try {
            var source = Setup.class.getProtectionDomain().getCodeSource();
            if (source == null || source.getLocation() == null) {
                return null;
            }
            return Path.of(source.getLocation().toURI());
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** {@code ModList.get().getModFileById(id)} then {@code getFile().getFilePath()}. */
    private static Path modListJar() {
        try {
            Class<?> modList = Class.forName("net.neoforged.fml.ModList");
            Object list = modList.getMethod("get").invoke(null);
            Object info = modList.getMethod("getModFileById", String.class).invoke(list, "autis_updater_setup");
            return findPath(info, 0);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Path findPath(Object value, int depth) {
        if (value == null || depth > 4) {
            return null;
        }
        if (value instanceof Path path) {
            return path;
        }
        for (String name : List.of("getFilePath", "getFile", "getSecureJar", "getPrimaryPath", "toPath")) {
            try {
                Object next = value.getClass().getMethod(name).invoke(value);
                Path found = findPath(next, depth + 1);
                if (isJarFile(found)) {
                    return found;
                }
            } catch (ReflectiveOperationException ignored) {
                // this object does not have that method
            }
        }
        return null;
    }

    private static Path scanModsDir(Path gameDir) {
        List<Path> dirs = new ArrayList<>();
        try {
            Class<?> paths = Class.forName("net.neoforged.fml.loading.FMLPaths");
            Object mods = paths.getField("MODSDIR").get(null);
            Object path = mods.getClass().getMethod("get").invoke(mods);
            if (path instanceof Path p) {
                dirs.add(p);
            }
        } catch (Throwable ignored) {
            // the game dir's mods folder is the same place for these launchers
        }
        dirs.add(gameDir.resolve("mods"));
        for (Path dir : dirs) {
            if (!Files.isDirectory(dir)) {
                continue;
            }
            try (var stream = Files.list(dir)) {
                Optional<Path> hit = stream
                        .filter(p -> {
                            String name = p.getFileName().toString();
                            return name.startsWith("autis_updater_setup") && name.endsWith(".jar");
                        })
                        .filter(Files::isRegularFile)
                        .sorted()
                        .findFirst();
                if (hit.isPresent()) {
                    return hit.get();
                }
            } catch (IOException ignored) {
                // try the next directory
            }
        }
        return null;
    }

    private static boolean isJarFile(Path path) {
        return path != null && Files.isRegularFile(path) && path.getFileName().toString().endsWith(".jar");
    }

    private static void copyJar(Path source, Path dest) throws IOException {
        if (source.toAbsolutePath().normalize().equals(dest.toAbsolutePath().normalize())) {
            return;
        }
        Files.createDirectories(dest.getParent());
        Files.copy(source, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    private static boolean writeHelperFromClasses(Path dest) throws IOException {
        String[] names = {
                "Setup.class", "Setup$Phase.class",
                "PrismPatch.class", "CursePatch.class", "ModrinthPatch.class",
                "AtomicFiles.class", "JsonText.class", "JsonText$Span.class", "JsonText$Cursor.class"
        };
        byte[][] bytes = new byte[names.length][];
        for (int i = 0; i < names.length; i++) {
            try (InputStream in = Setup.class.getResourceAsStream(names[i])) {
                if (in == null) {
                    return false;
                }
                bytes[i] = in.readAllBytes();
            }
        }
        Files.createDirectories(dest.getParent());
        Path tmp = dest.resolveSibling(dest.getFileName().toString() + ".autis-tmp");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(tmp))) {
            for (int i = 0; i < names.length; i++) {
                zip.putNextEntry(new ZipEntry("dev/wyseman/autisupdatersetup/" + names[i]));
                zip.write(bytes[i]);
                zip.closeEntry();
            }
        }
        try {
            Files.move(tmp, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            Files.move(tmp, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return true;
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
            Thread.sleep(100L);
        }
        Optional<ProcessHandle> handle = ProcessHandle.of(pid);
        return handle.isEmpty() || !handle.get().isAlive();
    }

    /**
     * After this process dies, Prism or CurseForge may still be writing the
     * file from the copy it held in memory. Prism's save is about 50 ms after
     * exit. Wait until those files stop changing, up to 30 seconds.
     */
    private static void waitForQuiet(Path gameDir) throws InterruptedException {
        List<Path> files = watched(gameDir);
        long death = System.nanoTime();
        long deadline = death + QUIET_MAX.toNanos();
        long stableSince = death;
        String sig = signature(files);
        while (System.nanoTime() < deadline) {
            Thread.sleep(QUIET_POLL_MS);
            String now = signature(files);
            if (!now.equals(sig)) {
                sig = now;
                stableSince = System.nanoTime();
                log(gameDir, "launcher rewrote a watched file after exit");
            } else if (System.nanoTime() - stableSince >= QUIET_STABLE.toNanos()) {
                return;
            }
        }
        log(gameDir, "launcher files were still changing after " + QUIET_MAX.toSeconds() + "s; patching anyway");
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
                long size = Files.size(file);
                out.append(file).append('=').append(Files.getLastModifiedTime(file).toMillis())
                        .append(':').append(size);
                // instance.cfg is small, and a same-second rewrite can keep the old mtime.
                if (size <= 1_000_000L) {
                    out.append(':').append(Arrays.hashCode(Files.readAllBytes(file)));
                }
                out.append(';');
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

    /** The Java that launched the game, so the helper uses the same runtime Flatpak exposed. */
    private static String javaCommand() {
        try {
            Optional<String> command = ProcessHandle.current().info().command();
            if (command.isPresent() && !command.get().isBlank()) {
                return command.get();
            }
        } catch (Throwable ignored) {
            // fall through to java.home
        }
        return javaBinary();
    }

    private static String javaBinary() {
        String name = isWindows() ? "java.exe" : "java";
        return Path.of(System.getProperty("java.home"), "bin", name).toString();
    }

    private static Path executable(String... candidates) {
        for (String candidate : candidates) {
            Path path = Path.of(candidate);
            if (Files.isExecutable(path)) {
                return path;
            }
        }
        return null;
    }

    private static String joinQuoted(List<String> args) {
        StringBuilder out = new StringBuilder();
        for (String arg : args) {
            if (!out.isEmpty()) {
                out.append(' ');
            }
            out.append(quote(arg));
        }
        return out.toString();
    }

    private static String quote(String value) {
        if (value.indexOf(' ') < 0 && value.indexOf('"') < 0) {
            return value;
        }
        return "\"" + value.replace("\"", "\\\"") + "\"";
    }

    private static void gameInfo(Path gameDir, String message) {
        String line = "[autis_updater_setup] " + message;
        if (log4jInfo(line)) {
            return;
        }
        try {
            Path latest = gameDir.resolve("logs").resolve("latest.log");
            Files.createDirectories(latest.getParent());
            String row = Instant.now().truncatedTo(ChronoUnit.SECONDS) + " INFO " + line + System.lineSeparator();
            Files.writeString(latest, row, StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (Throwable ignored) {
            // the setup log already has the line
        }
    }

    private static boolean log4jInfo(String line) {
        try {
            Class<?> manager = Class.forName("org.apache.logging.log4j.LogManager");
            Object logger = manager.getMethod("getLogger", String.class).invoke(null, "autis_updater_setup");
            logger.getClass().getMethod("info", String.class).invoke(logger, line);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("win");
    }
}
