import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

/**
 * Autisnautics pack sync.
 *
 * {@code java -jar} is the Prism pre-launch command and is unchanged: it prompts,
 * then downloads the client zip. Loaded as {@code -javaagent}, {@link #premain}
 * runs that same sync on the calling thread and returns, so NeoForge scans mods
 * only after the files are in place. The agent never calls {@code System.exit}
 * and never throws out of {@code premain}.
 */
public class Updater {
    static final String REPO = "InvalidArgument3/Autisnautics-1.21";
    static final String RELEASES_LATEST = "https://github.com/InvalidArgument3/Autisnautics-1.21/releases/latest";
    static final String DEFAULT_ASSET = "Autisnautics-1.21-{version}-client.zip";
    /**
     * Embedded updater generation. Must match {@code Updater-Version} in the
     * manifest and {@code updater-version.txt} in the jar. A release copy
     * older than this, or with no readable version, is never installed.
     */
    static final int UPDATER_VERSION = 3;
    /**
     * Whole agent sync, including applying the zip after the download.
     * A dead connect still fails at the 20s connect timeout. The download
     * request itself is capped separately so a stalled transfer cannot sit
     * until this budget.
     */
    static final Duration AGENT_BUDGET = Duration.ofMinutes(20);
    static final Duration AGENT_DOWNLOAD_CAP = Duration.ofMinutes(15);
    static boolean verbose = false;
    static volatile boolean agentMode = false;
    static volatile boolean agentAbort = false;
    static final HttpClient HTTP = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(20))
            .build();
    /** Prism instance files that sit beside the game directory and must not be copied into it. */
    static final Set<String> ROOT_SKIP = Set.of("instance.cfg", "mmc-pack.json", "icon.png", "a2.png");
    /**
     * These names are never taken from the pack zip. {@code autis/} stays so an
     * installer can leave README.txt, updater.log, and java-hook.json beside the
     * updater. The only file under {@code autis/} the sync may replace is
     * {@code autis/updater.jar}, and only when that copy is an acceptable agent.
     * The sync removes extra {@code mods/*.jar} files and trims
     * {@code .autis/backups} to two directories. It does not delete other folders.
     */
    static final Set<String> PROTECTED_TOP = Set.of(
            ".autis", "autis", "saves", "logs", "screenshots", "crash-reports",
            "options.txt", "servers.dat", "servers.dat_old", "usercache.json",
            "usernamecache.json", "realms_persistence.json");
    static final CountDownLatch guiExit = new CountDownLatch(1);
    static volatile boolean outcomeLogged = false;
    static volatile boolean updateApplied = false;
    static String seenInstalled = "unknown";
    static String seenLatest = "unknown";
    static Path activeGameDir;
    static String activeMode = "prelaunch";

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        agentMode = true;
        activeMode = "agent";
        try {
            runAgent(agentArgs);
        } catch (Throwable t) {
            log("agent error: " + t);
            try {
                note(activeGameDir, "error: " + t.getMessage());
            } catch (Throwable ignored) {
                // the game still starts
            }
        }
    }

    public static void main(String[] args) {
        String mode = "gui";
        boolean auto = false;
        boolean allowDev = false;
        boolean force = false;
        Path gameDir = null;
        int timeout = 90;
        String asset = DEFAULT_ASSET;
        try {
            for (int i = 0; i < args.length; i++) {
                String opt = args[i];
                switch (opt) {
                    case "--prelaunch":
                    case "--gui":
                        mode = "gui";
                        break;
                    case "--prompt":
                        auto = false;
                        break;
                    case "--auto":
                        auto = true;
                        break;
                    case "--check":
                        mode = "check";
                        break;
                    case "--apply":
                        mode = "apply";
                        break;
                    case "--force":
                        force = true;
                        break;
                    case "--allow-dev":
                        allowDev = true;
                        break;
                    case "--verbose":
                    case "-v":
                        verbose = true;
                        break;
                    case "--game-dir":
                        gameDir = Paths.get(args[++i]);
                        break;
                    case "--timeout":
                        timeout = Integer.parseInt(args[++i]);
                        break;
                    case "--asset":
                        asset = args[++i];
                        break;
                    case "--help":
                    case "-h":
                        usage();
                        System.exit(0);
                        return;
                    default:
                        if (opt.startsWith("-")) {
                            log("unknown option " + opt);
                        }
                        break;
                }
            }
        } catch (Exception e) {
            usage();
            System.exit(2);
            return;
        }
        activeMode = "prelaunch";
        gameDir = resolveGameDir(gameDir);
        activeGameDir = gameDir;
        if (gameDir == null) {
            log("ERROR: cannot locate the game directory (pass --game-dir)");
            exitFor(mode, 1);
            return;
        }
        if (!allowDev && isPackSource(gameDir)) {
            String installedSource = readTrimmed(gameDir.resolve(".autis/version.txt"));
            if (installedSource != null) {
                seenInstalled = installedSource;
            }
            log("pack source detected in " + gameDir + " — not syncing (pass --allow-dev to override)");
            note(gameDir, "skipped-pack-source");
            exitFor(mode, 0);
            return;
        }
        finishPendingSwap(gameDir);
        Path dot = gameDir.resolve(".autis");
        try {
            Files.createDirectories(dot);
        } catch (IOException e) {
            log("WARN: cannot create " + dot + ": " + e.getMessage());
            dot = gameDir;
        }
        String installed = readTrimmed(dot.resolve("version.txt"));
        seenInstalled = nz(installed);
        String latest;
        try {
            latest = latestTag();
        } catch (Exception e) {
            log("ERROR: cannot reach GitHub: " + e.getMessage());
            note(gameDir, "error: " + e.getMessage());
            if (mode.equals("gui")) {
                swingError("Could not reach GitHub.\n\n" + e.getMessage()
                        + "\n\nThe game will start with the installed pack (" + nz(installed) + ").", timeout);
                System.exit(0);
            }
            System.exit(1);
            return;
        }
        if (latest == null) {
            log("ERROR: no release found");
            note(gameDir, "error: no release found");
            exitFor(mode, 1);
            return;
        }
        seenLatest = latest;
        boolean current = installed != null && compareVersions(latest, installed) <= 0;
        if (current && !force) {
            if (mode.equals("check")) {
                System.out.println("up to date (" + installed + ")");
            }
            note(gameDir, "up-to-date");
            System.exit(0);
            return;
        }
        Asset chosen = resolveAsset(latest, asset);
        String url = chosen.url;
        long size = contentLength(url);
        if (mode.equals("check")) {
            System.out.println("update available: " + nz(installed) + " -> " + latest
                    + " (" + (size > 0 ? human(size) : "size unknown") + ")");
            note(gameDir, "update-available");
            System.exit(3);
            return;
        }
        if (mode.equals("apply")) {
            try {
                download(url, dot, size, (a, b) -> { });
                applyZip(dot.resolve("tmp").resolve("pack.zip"), gameDir, dot, latest, (a, b) -> { });
                System.out.println("updated to " + latest);
                note(gameDir, "updated");
                System.exit(0);
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
                note(gameDir, "error: " + e.getMessage());
                System.exit(1);
            }
            return;
        }
        if (GraphicsEnvironment.isHeadless()) {
            note(gameDir, "skipped-headless");
            System.exit(0);
            return;
        }
        if (auto) {
            swingAuto(gameDir, dot, latest, installed, url, size);
        } else {
            swingPrompt(gameDir, dot, latest, installed, url, size, timeout, fetchChangelog(latest));
        }
        try {
            guiExit.await();
        } catch (InterruptedException ignored) {
            // the window closed
        }
        if (!outcomeLogged) {
            note(gameDir, updateApplied ? "updated" : "dismissed");
        }
        System.exit(0);
    }

    /**
     * Agent entry. Resolves the game directory, then blocks until the sync
     * finishes, fails, or the budget runs out. Always returns normally.
     */
    static void runAgent(String agentArgs) {
        Path gameDir;
        try {
            gameDir = resolveAgentGameDir(agentArgs);
        } catch (Throwable t) {
            log("agent: cannot resolve game dir: " + t.getMessage());
            return;
        }
        activeGameDir = gameDir;
        if (gameDir == null || !Files.isDirectory(gameDir)) {
            log("agent: game dir is not a directory (" + gameDir + ")");
            return;
        }
        log("agent: syncing " + gameDir);
        AgentWindow window = AgentWindow.tryOpen();
        ExecutorService pool = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "autis-updater-agent");
            t.setDaemon(true);
            return t;
        });
        Future<String> task = pool.submit(() -> syncBlocking(gameDir, window));
        String result;
        try {
            result = task.get(AGENT_BUDGET.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            agentAbort = true;
            task.cancel(true);
            result = "timed-out";
            log("agent: timed out after " + AGENT_BUDGET.toMinutes() + " min; starting the game anyway");
        } catch (Throwable t) {
            agentAbort = true;
            task.cancel(true);
            Throwable cause = t.getCause() == null ? t : t.getCause();
            result = "error: " + cause.getMessage();
            log("agent: " + result);
        } finally {
            pool.shutdownNow();
            if (window != null) {
                window.close();
            }
        }
        note(gameDir, result);
    }

    static String syncBlocking(Path gameDir, AgentWindow window) throws Exception {
        if (isPackSource(gameDir)) {
            String installedSource = readTrimmed(gameDir.resolve(".autis/version.txt"));
            if (installedSource != null) {
                seenInstalled = installedSource;
            }
            log("pack source detected in " + gameDir + " — not syncing (pass --allow-dev to override)");
            return "skipped-pack-source";
        }
        finishPendingSwap(gameDir);
        Path dot = gameDir.resolve(".autis");
        Files.createDirectories(dot);
        String installed = readTrimmed(dot.resolve("version.txt"));
        seenInstalled = nz(installed);
        if (window != null) {
            window.status("Checking for a pack update…");
        }
        String latest = latestTag();
        if (latest == null) {
            return "error: no release found";
        }
        seenLatest = latest;
        if (installed != null && compareVersions(latest, installed) <= 0) {
            log("up to date (" + installed + ")");
            return "up-to-date";
        }
        Asset chosen = resolveAsset(latest, DEFAULT_ASSET);
        String url = chosen.url;
        long size = contentLength(url);
        if (agentAbort || Thread.currentThread().isInterrupted()) {
            return "timed-out";
        }
        if (window != null) {
            window.status("Downloading " + latest + (size > 0 ? " (" + human(size) + ")" : "") + "…");
        }
        download(url, dot, size, (done, total) -> {
            if (window != null) {
                window.progress(done, total, "download");
            }
        });
        if (agentAbort || Thread.currentThread().isInterrupted()) {
            return "timed-out";
        }
        if (window != null) {
            window.status("Applying " + latest + "…");
        }
        applyZip(dot.resolve("tmp").resolve("pack.zip"), gameDir, dot, latest, (done, total) -> {
            if (window != null) {
                window.progress(done, total, "files");
            }
        });
        log("agent: updated to " + latest);
        return "updated";
    }

    static Path resolveAgentGameDir(String agentArgs) {
        String fromArgs = agentPath(agentArgs);
        if (fromArgs != null) {
            return Paths.get(fromArgs).toAbsolutePath().normalize();
        }
        String fromCommand = gameDirFromJavaCommand(System.getProperty("sun.java.command", ""));
        if (fromCommand != null) {
            return Paths.get(fromCommand).toAbsolutePath().normalize();
        }
        return Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
    }

    /** {@code gameDir=...} or a bare path. Spaces are part of the path. */
    static String agentPath(String agentArgs) {
        if (agentArgs == null) {
            return null;
        }
        String s = stripQuotes(agentArgs.trim());
        if (s.isEmpty()) {
            return null;
        }
        if (s.startsWith("gameDir=")) {
            s = stripQuotes(s.substring("gameDir=".length()).trim());
        }
        return s.isEmpty() ? null : s;
    }

    static String stripQuotes(String s) {
        if (s.length() >= 2) {
            char a = s.charAt(0);
            char b = s.charAt(s.length() - 1);
            if ((a == '"' && b == '"') || (a == '\'' && b == '\'')) {
                return s.substring(1, s.length() - 1);
            }
        }
        return s;
    }

    /**
     * Pull {@code --gameDir} (or {@code --game-dir}) out of {@code sun.java.command}.
     * Quoted paths keep their spaces. An unquoted value runs until the next
     * {@code --} option.
     */
    static String gameDirFromJavaCommand(String command) {
        if (command == null || command.isBlank()) {
            return null;
        }
        List<String> tokens = tokenize(command);
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (token.equals("--gameDir") || token.equals("--game-dir")) {
                if (i + 1 < tokens.size()) {
                    return tokens.get(i + 1);
                }
                return null;
            }
            if (token.startsWith("--gameDir=") || token.startsWith("--game-dir=")) {
                return token.substring(token.indexOf('=') + 1);
            }
        }
        return null;
    }

    static List<String> tokenize(String s) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') {
                quoted = !quoted;
                continue;
            }
            if (!quoted && Character.isWhitespace(c)) {
                if (cur.length() > 0) {
                    out.add(cur.toString());
                    cur.setLength(0);
                }
            } else {
                cur.append(c);
            }
        }
        if (cur.length() > 0) {
            out.add(cur.toString());
        }
        return out;
    }

    static boolean isPackSource(Path gameDir) {
        return Files.exists(gameDir.resolve("pakku.json")) || Files.exists(gameDir.resolve("pakku-lock.json"));
    }

    static void note(Path gameDir, String result) {
        if (outcomeLogged || gameDir == null) {
            return;
        }
        outcomeLogged = true;
        String line = Instant.now().truncatedTo(ChronoUnit.SECONDS)
                + " mode=" + activeMode
                + " installed=" + oneLine(seenInstalled)
                + " latest=" + oneLine(seenLatest)
                + " result=" + oneLine(result);
        log(line);
        try {
            Path dir = gameDir.resolve("autis");
            Files.createDirectories(dir);
            Files.writeString(dir.resolve("updater.log"), line + System.lineSeparator(),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {
            log("WARN: cannot write updater.log: " + e.getMessage());
        }
    }

    static String oneLine(String s) {
        if (s == null || s.isBlank()) {
            return "unknown";
        }
        return s.replace('\r', ' ').replace('\n', ' ').trim();
    }

    static void exitFor(String mode, int code) {
        System.exit(mode.equals("gui") ? 0 : code);
    }

    static void usage() {
        System.out.println("autis-updater - Autisnautics pack sync\n"
                + "  (no args)             prompt, then download the release and apply it\n"
                + "  --auto                same, without asking\n"
                + "  --check               exit 0 up to date, 3 update available, 1 error\n"
                + "  --apply               download and apply, no window\n"
                + "  --game-dir <path>     instance minecraft/ dir (default: $INST_MC_DIR or cwd)\n"
                + "  --timeout <sec>       prompt auto-dismiss, 0 = wait (default 90)\n"
                + "  --asset <name>        release asset to use (default: Autisnautics-1.21-{version}-client.zip)\n"
                + "  --force               apply even when versions match\n"
                + "  --allow-dev           operate on a directory that holds the pack source\n"
                + "  --verbose             more logging\n");
    }

    static Path resolveGameDir(Path path) {
        List<Path> candidates = new ArrayList<>();
        if (path != null) {
            candidates.add(path);
        }
        String env = System.getenv("INST_MC_DIR");
        if (env != null && !env.isBlank()) {
            candidates.add(Paths.get(env));
        }
        candidates.add(Paths.get("").toAbsolutePath());
        for (Path candidate : candidates) {
            Path abs = candidate.toAbsolutePath().normalize();
            if (Files.isDirectory(abs.resolve("mods")) || Files.isDirectory(abs.resolve(".autis"))) {
                return abs;
            }
        }
        for (Path candidate : candidates) {
            Path abs = candidate.toAbsolutePath().normalize();
            if (Files.isDirectory(abs)) {
                return abs;
            }
        }
        return null;
    }

    static String latestTag() throws Exception {
        HttpResponse<Void> response = HTTP.send(HttpRequest.newBuilder(URI.create(RELEASES_LATEST))
                .timeout(requestTimeout(Duration.ofSeconds(20)))
                .header("User-Agent", "autis-updater/2.0")
                .build(), HttpResponse.BodyHandlers.discarding());
        String path = response.uri().getPath();
        int n = path.indexOf("/tag/");
        return n < 0 ? null : path.substring(n + 5);
    }

    static Duration requestTimeout(Duration normal) {
        if (!agentMode || normal.compareTo(AGENT_DOWNLOAD_CAP) <= 0) {
            return normal;
        }
        return AGENT_DOWNLOAD_CAP;
    }

    static String fetchChangelog(String version) {
        try {
            HttpResponse<byte[]> response = HTTP.send(HttpRequest.newBuilder(
                    URI.create("https://api.github.com/repos/" + REPO + "/releases/latest"))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "autis-updater/2.0")
                    .header("Accept", "application/vnd.github+json")
                    .build(), HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() / 100 != 2) {
                return "";
            }
            String body = jsonField(new String(response.body(), StandardCharsets.UTF_8), "body");
            return body == null ? "" : body.trim();
        } catch (Exception e) {
            if (verbose) {
                log("changelog fetch failed: " + e.getMessage());
            }
            return "";
        }
    }

    static String jsonField(String json, String field) {
        String key = "\"" + field + "\":";
        int n = json.indexOf(key);
        if (n < 0) {
            return null;
        }
        n += key.length();
        while (n < json.length() && Character.isWhitespace(json.charAt(n))) {
            n++;
        }
        if (n >= json.length() || json.charAt(n) != '"') {
            return null;
        }
        n++;
        StringBuilder out = new StringBuilder();
        while (n < json.length()) {
            char c = json.charAt(n++);
            if (c == '"') {
                break;
            }
            if (c != '\\') {
                out.append(c);
                continue;
            }
            char e = json.charAt(n++);
            switch (e) {
                case 'n': out.append('\n'); break;
                case 't': out.append('\t'); break;
                case 'r': out.append('\r'); break;
                case 'b': out.append('\b'); break;
                case 'f': out.append('\f'); break;
                case 'u':
                    out.append((char) Integer.parseInt(json.substring(n, n + 4), 16));
                    n += 4;
                    break;
                default:
                    out.append(e);
                    break;
            }
        }
        return out.toString();
    }

    /**
     * The client zip is the pack the game runs. A release that only published
     * the Prism instance falls back to that zip, which stores the game under
     * {@code .minecraft/}.
     */
    static Asset resolveAsset(String version, String template) {
        String name = template.replace("{version}", version);
        String url = "https://github.com/" + REPO + "/releases/download/" + version + "/" + name;
        String prismName = "Autisnautics-1.21-" + version + "-prism.zip";
        String prismUrl = "https://github.com/" + REPO + "/releases/download/" + version + "/" + prismName;
        if (!headOk(url) && headOk(prismUrl)) {
            if (verbose) {
                log("asset " + name + " missing, using " + prismName);
            }
            return new Asset(prismName, prismUrl);
        }
        return new Asset(name, url);
    }

    static boolean headOk(String url) {
        try {
            HttpResponse<Void> response = HTTP.send(HttpRequest.newBuilder(URI.create(url))
                    .timeout(requestTimeout(Duration.ofSeconds(20)))
                    .header("User-Agent", "autis-updater/2.0")
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build(), HttpResponse.BodyHandlers.discarding());
            return response.statusCode() / 100 == 2;
        } catch (Exception e) {
            return false;
        }
    }

    static long contentLength(String url) {
        try {
            HttpResponse<Void> response = HTTP.send(HttpRequest.newBuilder(URI.create(url))
                    .timeout(requestTimeout(Duration.ofSeconds(20)))
                    .header("User-Agent", "autis-updater/2.0")
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build(), HttpResponse.BodyHandlers.discarding());
            return response.headers().firstValueAsLong("content-length").orElse(-1L);
        } catch (Exception e) {
            if (verbose) {
                log("HEAD failed: " + e.getMessage());
            }
            return -1L;
        }
    }

    static void download(String url, Path dot, long expected, Progress progress) throws Exception {
        Path tmp = dot.resolve("tmp");
        Files.createDirectories(tmp);
        Path zip = tmp.resolve("pack.zip");
        Files.deleteIfExists(zip);
        HttpResponse<Path> response = HTTP.send(HttpRequest.newBuilder(URI.create(url))
                .timeout(requestTimeout(Duration.ofMinutes(30)))
                .header("User-Agent", "autis-updater/2.0")
                .build(), HttpResponse.BodyHandlers.ofFile(zip));
        if (agentAbort) {
            Files.deleteIfExists(zip);
            throw new InterruptedException("timed-out");
        }
        if (response.statusCode() / 100 != 2) {
            Files.deleteIfExists(zip);
            throw new IOException("HTTP " + response.statusCode() + " for " + url);
        }
        long got = Files.size(zip);
        if (expected > 0 && got != expected) {
            throw new IOException("short download: " + got + " of " + expected + " bytes");
        }
        progress.tick(got, Math.max(got, 1L));
    }

    static void applyZip(Path zip, Path gameDir, Path dot, String version, Progress progress) throws Exception {
        Path stage = dot.resolve("tmp").resolve("stage");
        deleteTree(stage);
        Files.createDirectories(stage);
        int entries = 0;
        try (ZipFile archive = new ZipFile(zip.toFile())) {
            var it = archive.entries();
            while (it.hasMoreElements()) {
                ZipEntry entry = it.nextElement();
                String name = entry.getName().replace('\\', '/');
                if (entry.isDirectory()) {
                    continue;
                }
                if (name.startsWith("overrides/")) {
                    name = name.substring("overrides/".length());
                } else if (name.startsWith(".minecraft/")) {
                    name = name.substring(".minecraft/".length());
                } else if (ROOT_SKIP.contains(name)) {
                    continue;
                }
                String top = name.contains("/") ? name.substring(0, name.indexOf('/')) : name;
                boolean updaterJar = name.equals("autis/updater.jar");
                if ((PROTECTED_TOP.contains(top) && !updaterJar)
                        || name.equals("manifest.json")
                        || name.equals("modlist.html")
                        || name.endsWith(".jar.disabled")) {
                    continue;
                }
                Path dest = stage.resolve(name).normalize();
                if (!dest.startsWith(stage)) {
                    throw new IOException("zip entry escapes the instance: " + name);
                }
                Files.createDirectories(dest.getParent());
                try (InputStream in = archive.getInputStream(entry)) {
                    Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
                }
                entries++;
            }
        }
        Path stagedMods = stage.resolve("mods");
        if (!Files.isDirectory(stagedMods)) {
            deleteTree(stage);
            throw new IOException("release asset has no mods/ — it is not a self-contained client pack");
        }
        stageUpdaterJar(stage.resolve("autis/updater.jar"), gameDir.resolve("autis/updater.jar"));
        Path backup = dot.resolve("backups").resolve(version + "-" + System.currentTimeMillis() / 1000L);
        Path conflicts = backup.resolve("conflicts");
        Files.createDirectories(backup);
        Map<String, String> applied = loadApplied(dot);
        Map<String, String> next = new HashMap<>();
        boolean first = applied.isEmpty();
        Set<String> keepJars = new HashSet<>();
        int written = 0;
        int kept = 0;
        long tick = 0;
        long total = Math.max(1, entries);
        try (DirectoryStream<Path> mods = Files.newDirectoryStream(stagedMods)) {
            for (Path mod : mods) {
                if (!Files.isRegularFile(mod)) {
                    continue;
                }
                String fileName = mod.getFileName().toString();
                keepJars.add(fileName);
                Path dest = gameDir.resolve("mods").resolve(fileName);
                Files.createDirectories(dest.getParent());
                if (!sameContent(mod, dest)) {
                    if (Files.exists(dest)) {
                        backup(backup, gameDir, dest);
                    }
                    Files.copy(mod, dest, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                }
                next.put("mods/" + fileName, sha1Hex(mod));
                progress.tick(++tick, total);
            }
        }
        Path liveMods = gameDir.resolve("mods");
        if (Files.isDirectory(liveMods)) {
            try (DirectoryStream<Path> jars = Files.newDirectoryStream(liveMods, "*.jar")) {
                for (Path jar : jars) {
                    String fileName = jar.getFileName().toString();
                    if (keepJars.contains(fileName)) {
                        continue;
                    }
                    backup(backup, gameDir, jar);
                    Files.deleteIfExists(jar);
                    if (verbose) {
                        log("removed " + fileName);
                    }
                }
            }
        }
        List<Path> files = new ArrayList<>();
        try (var walk = Files.walk(stage)) {
            walk.filter(Files::isRegularFile).forEach(files::add);
        }
        for (Path file : files) {
            String rel = stage.relativize(file).toString().replace('\\', '/');
            if (rel.startsWith("mods/")) {
                continue;
            }
            Path dest = gameDir.resolve(rel);
            // The updater jar is installed only by stageUpdaterJar, which refuses
            // a copy with no Premain-Class or an older embedded version.
            if (rel.equals("autis/updater.jar")) {
                continue;
            }
            Files.createDirectories(dest.getParent());
            String incoming = sha1Hex(file);
            if (Files.isRegularFile(dest)) {
                String have = sha1Hex(dest);
                if (have.equalsIgnoreCase(incoming)) {
                    next.put(rel, incoming);
                    continue;
                }
                String recorded = applied.get(rel);
                boolean localEdit = !first && (recorded == null || !have.equalsIgnoreCase(recorded));
                if (localEdit) {
                    Path conflict = conflicts.resolve(rel);
                    Files.createDirectories(conflict.getParent());
                    Files.copy(file, conflict, StandardCopyOption.REPLACE_EXISTING);
                    kept++;
                    if (verbose) {
                        log("kept local " + rel);
                    }
                    continue;
                }
                backup(backup, gameDir, dest);
            }
            Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            next.put(rel, incoming);
            written++;
            progress.tick(++tick, total);
        }
        Files.writeString(dot.resolve("version.txt"), version + "\n", StandardCharsets.UTF_8);
        saveApplied(dot, next);
        deleteTree(stage);
        Files.deleteIfExists(dot.resolve("tmp").resolve("pack.zip"));
        deleteTree(dot.resolve("tmp"));
        pruneBackups(dot.resolve("backups"), 2);
        updateApplied = true;
        log("applied " + version + " — " + keepJars.size() + " mods, " + written + " files written, " + kept + " local edits kept");
    }

    static void stageUpdaterJar(Path staged, Path installed) throws IOException {
        if (!Files.isRegularFile(staged) || (Files.isRegularFile(installed) && sameContent(staged, installed))) {
            return;
        }
        if (!acceptIncomingUpdater(staged, installed)) {
            Files.deleteIfExists(staged);
            return;
        }
        Files.createDirectories(installed.getParent());
        Path pending = installed.resolveSibling("updater.pending.jar");
        Files.copy(staged, pending, StandardCopyOption.REPLACE_EXISTING);
        if (agentMode && isLoadedJar(installed)) {
            scheduleSwapWhenFree(pending, installed);
            return;
        }
        replaceSelf(pending, installed);
    }

    static void replaceSelf(Path pending, Path installed) {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (!os.contains("win")) {
            try {
                Files.move(pending, installed, StandardCopyOption.REPLACE_EXISTING);
                log("updater jar replaced");
            } catch (IOException e) {
                log("WARN: cannot replace updater jar yet: " + e.getMessage());
            }
            return;
        }
        try {
            String script = "ping -n 3 127.0.0.1 >nul & move /y \"" + pending + "\" \"" + installed + "\"";
            new ProcessBuilder("cmd", "/c", script)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
            log("updater jar queued for replacement after exit");
        } catch (IOException e) {
            log("WARN: cannot queue updater replacement: " + e.getMessage());
        }
    }

    /**
     * The loaded agent jar stays where it is until this process exits. A
     * detached helper then moves the pending file into place, which is the
     * swap the next launch picks up. Windows cannot replace the file while
     * this JVM has it open.
     */
    static void scheduleSwapWhenFree(Path pending, Path installed) {
        try {
            String p = pending.toAbsolutePath().toString();
            String d = installed.toAbsolutePath().toString();
            long pid = ProcessHandle.current().pid();
            ProcessBuilder pb;
            if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
                String script = "for /L %i in (1,1,1440) do @(if not exist \"" + p + "\" exit /b 0"
                        + " & move /Y \"" + p + "\" \"" + d + "\" >nul 2>&1 && exit /b 0"
                        + " & ping -n 31 127.0.0.1 >nul)";
                pb = new ProcessBuilder("cmd", "/c", script);
            } else {
                String script = "while kill -0 \"$3\" 2>/dev/null; do sleep 5; done; "
                        + "[ -f \"$1\" ] || exit 0; mv -f \"$1\" \"$2\"";
                pb = new ProcessBuilder("sh", "-c", script, "sh", p, d, Long.toString(pid));
            }
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            pb.redirectError(ProcessBuilder.Redirect.DISCARD);
            pb.start();
            log("updater jar staged; it replaces autis/updater.jar after this process releases the file");
        } catch (Exception e) {
            log("WARN: cannot stage updater swap: " + e.getMessage());
        }
    }

    static void finishPendingSwap(Path gameDir) {
        Path pending = gameDir.resolve("autis/updater.pending.jar");
        if (!Files.isRegularFile(pending)) {
            return;
        }
        Path installed = gameDir.resolve("autis/updater.jar");
        if (!acceptIncomingUpdater(pending, installed)) {
            try {
                Files.deleteIfExists(pending);
            } catch (IOException e) {
                log("WARN: cannot discard updater.pending.jar: " + e.getMessage());
            }
            log("discarded updater.pending.jar; keeping the current updater jar");
            return;
        }
        try {
            if (Files.isRegularFile(installed) && sameContent(pending, installed)) {
                Files.deleteIfExists(pending);
                return;
            }
            if (agentMode && isLoadedJar(installed)) {
                scheduleSwapWhenFree(pending, installed);
                return;
            }
            Files.move(pending, installed, StandardCopyOption.REPLACE_EXISTING);
            log("updater jar replaced (pending swap)");
        } catch (IOException e) {
            if (agentMode && isLoadedJar(installed)) {
                scheduleSwapWhenFree(pending, installed);
            } else if (verbose) {
                log("pending swap still blocked: " + e.getMessage());
            }
        }
    }

    /**
     * A candidate replaces the running updater only when it is a Java agent and
     * its embedded version is at least the running one. Anything unreadable,
     * versionless, or older is refused and the current jar stays.
     */
    static boolean acceptIncomingUpdater(Path candidate, Path installed) {
        try {
            if (candidate == null || !Files.isRegularFile(candidate)) {
                return false;
            }
            String premain = manifestMain(candidate, "Premain-Class");
            if (premain == null || premain.isBlank()) {
                log("keeping the current updater jar: release copy has no Premain-Class");
                return false;
            }
            Integer incoming = embeddedVersion(candidate);
            if (incoming == null) {
                log("keeping the current updater jar: release copy has no usable Updater-Version");
                return false;
            }
            int bar = UPDATER_VERSION;
            if (installed != null && Files.isRegularFile(installed)) {
                Integer have = embeddedVersion(installed);
                if (have != null) {
                    bar = Math.max(bar, have);
                }
            }
            if (incoming < bar) {
                log("keeping the current updater jar: release copy is version " + incoming + ", running is " + bar);
                return false;
            }
            return true;
        } catch (Exception e) {
            log("keeping the current updater jar: " + e.getMessage());
            return false;
        }
    }

    static String manifestMain(Path jar, String name) throws IOException {
        try (java.util.jar.JarFile file = new java.util.jar.JarFile(jar.toFile())) {
            if (file.getManifest() == null) {
                return null;
            }
            return file.getManifest().getMainAttributes().getValue(name);
        }
    }

    /** Manifest {@code Updater-Version} and {@code updater-version.txt} must agree. */
    static Integer embeddedVersion(Path jar) throws IOException {
        Integer manifest = parseVersion(manifestMain(jar, "Updater-Version"));
        Integer resource = null;
        try (java.util.jar.JarFile file = new java.util.jar.JarFile(jar.toFile())) {
            ZipEntry entry = file.getEntry("updater-version.txt");
            if (entry != null) {
                try (InputStream in = file.getInputStream(entry)) {
                    resource = parseVersion(new String(in.readAllBytes(), StandardCharsets.UTF_8));
                }
            }
        }
        if (manifest == null || resource == null || !manifest.equals(resource)) {
            return null;
        }
        return manifest;
    }

    static Integer parseVersion(String raw) {
        if (raw == null) {
            return null;
        }
        String text = raw.trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            int value = Integer.parseInt(text);
            return value < 0 ? null : value;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static boolean isLoadedJar(Path path) {
        try {
            var source = Updater.class.getProtectionDomain().getCodeSource();
            if (source == null || source.getLocation() == null) {
                return false;
            }
            Path loaded = Paths.get(source.getLocation().toURI()).toAbsolutePath().normalize();
            return loaded.equals(path.toAbsolutePath().normalize());
        } catch (Exception e) {
            return false;
        }
    }

    static Map<String, String> loadApplied(Path dot) {
        Map<String, String> map = new HashMap<>();
        Path file = dot.resolve("applied.txt");
        if (!Files.isRegularFile(file)) {
            return map;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                int tab = line.indexOf('\t');
                if (tab > 0) {
                    map.put(line.substring(0, tab), line.substring(tab + 1).trim());
                }
            }
        } catch (IOException e) {
            log("WARN: unreadable applied.txt: " + e.getMessage());
        }
        return map;
    }

    static void saveApplied(Path dot, Map<String, String> map) {
        StringBuilder out = new StringBuilder();
        map.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEach(e -> out.append(e.getKey()).append('\t').append(e.getValue()).append('\n'));
        try {
            Files.writeString(dot.resolve("applied.txt"), out.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log("WARN: cannot write applied.txt: " + e.getMessage());
        }
    }

    static void pruneBackups(Path backups, int keep) {
        if (!Files.isDirectory(backups)) {
            return;
        }
        try (DirectoryStream<Path> kids = Files.newDirectoryStream(backups)) {
            List<Path> list = new ArrayList<>();
            for (Path kid : kids) {
                list.add(kid);
            }
            list.sort(Comparator.comparing(p -> p.getFileName().toString()));
            while (list.size() > keep) {
                deleteTree(list.remove(0));
            }
        } catch (IOException ignored) {
            // a leftover backup is harmless
        }
    }

    static int compareVersions(String a, String b) {
        String[] left = a.replaceAll("[^0-9.]", "").split("[.]");
        String[] right = b.replaceAll("[^0-9.]", "").split("[.]");
        int n = Math.max(left.length, right.length);
        for (int i = 0; i < n; i++) {
            int x = i < left.length && !left[i].isEmpty() ? Integer.parseInt(left[i]) : 0;
            int y = i < right.length && !right[i].isEmpty() ? Integer.parseInt(right[i]) : 0;
            if (x != y) {
                return Integer.compare(x, y);
            }
        }
        return 0;
    }

    static boolean sameContent(Path a, Path b) throws IOException {
        if (!Files.isRegularFile(b)) {
            return false;
        }
        if (Files.size(a) != Files.size(b)) {
            return false;
        }
        return sha1Hex(a).equalsIgnoreCase(sha1Hex(b));
    }

    static void backup(Path backupRoot, Path gameDir, Path file) throws IOException {
        Path rel = gameDir.toAbsolutePath().normalize().relativize(file.toAbsolutePath().normalize());
        Path dest = backupRoot.resolve(rel.toString());
        Files.createDirectories(dest.getParent());
        Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
    }

    static void deleteTree(Path root) {
        if (root == null || !Files.exists(root)) {
            return;
        }
        try (var walk = Files.walk(root)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                    // best effort
                }
            });
        } catch (IOException ignored) {
            // best effort
        }
    }

    static String sha1Hex(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] buf = new byte[65536];
            int n;
            while ((n = in.read(buf)) > 0) {
                digest.update(buf, 0, n);
            }
            StringBuilder hex = new StringBuilder();
            for (byte b : digest.digest()) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
    }

    static String readTrimmed(Path file) {
        try {
            return Files.exists(file) ? Files.readString(file, StandardCharsets.UTF_8).trim() : null;
        } catch (IOException e) {
            return null;
        }
    }

    static String nz(String s) {
        return s == null || s.isBlank() ? "unknown" : s;
    }

    static String human(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format("%.0f KB", kb);
        }
        return String.format("%.0f MB", kb / 1024.0);
    }

    static void log(String message) {
        System.err.println("[autis-updater] " + message);
    }

    static void swingAuto(Path gameDir, Path dot, String latest, String installed, String url, long size) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Autisnautics 1.21 — pack update");
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(new EmptyBorder(16, 18, 14, 18));
            panel.add(new JLabel("Downloading " + latest + (size > 0 ? " (" + human(size) + ")" : "") + "…"), BorderLayout.NORTH);
            frame.setContentPane(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    guiExit.countDown();
                }
            });
            frame.setVisible(true);
            runUpdate(frame, gameDir, dot, latest, installed, url, size);
        });
    }

    static void swingPrompt(Path gameDir, Path dot, String latest, String installed, String url, long size, int timeout, String changelog) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Autisnautics 1.21 — pack update");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setResizable(false);
            JPanel root = new JPanel(new BorderLayout(0, 10));
            root.setBorder(new EmptyBorder(16, 18, 14, 18));
            JLabel title = new JLabel("Pack update available");
            title.setFont(title.getFont().deriveFont(Font.BOLD, title.getFont().getSize2D() + 4f));
            JLabel versions = new JLabel("Installed " + nz(installed) + "  →  " + latest
                    + (size > 0 ? "     (one download, " + human(size) + ")" : ""));
            String notes = changelog == null || changelog.isBlank()
                    ? "No release notes for " + latest + "."
                    : changelog.replace("**", "").replace("`", "").trim();
            JTextArea area = new JTextArea(notes);
            area.setEditable(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            area.setCaretPosition(0);
            JScrollPane scroll = new JScrollPane(area);
            scroll.setPreferredSize(new Dimension(620, 300));
            JLabel hint = new JLabel("<html>mods/ is replaced and configs are merged &mdash; your edited configs are kept, the pack's version goes to .autis/backups/conflicts. saves, options.txt and servers.dat are never touched.</html>");
            hint.setFont(hint.getFont().deriveFont(Font.PLAIN, 11f));
            JButton update = new JButton("Update now");
            JButton later = new JButton("Not now");
            JLabel countdown = new JLabel(" ");
            countdown.setFont(countdown.getFont().deriveFont(Font.PLAIN, 11f));
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            buttons.add(countdown);
            buttons.add(later);
            buttons.add(update);
            JPanel heading = new JPanel();
            heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            versions.setAlignmentX(Component.LEFT_ALIGNMENT);
            heading.add(title);
            heading.add(Box.createVerticalStrut(4));
            heading.add(versions);
            JPanel middle = new JPanel(new BorderLayout(0, 6));
            middle.add(scroll, BorderLayout.CENTER);
            middle.add(hint, BorderLayout.SOUTH);
            root.add(heading, BorderLayout.NORTH);
            root.add(middle, BorderLayout.CENTER);
            root.add(buttons, BorderLayout.SOUTH);
            frame.setContentPane(root);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (!outcomeLogged) {
                        note(gameDir, updateApplied ? "updated" : "dismissed");
                    }
                    guiExit.countDown();
                }
            });
            Timer[] ticker = new Timer[1];
            if (timeout > 0) {
                int[] left = new int[] {timeout};
                ticker[0] = new Timer(1000, ev -> {
                    left[0]--;
                    countdown.setText("starting in " + left[0] + "s   ");
                    if (left[0] <= 0) {
                        ((Timer) ev.getSource()).stop();
                        frame.dispose();
                    }
                });
                countdown.setText("starting in " + left[0] + "s   ");
                ticker[0].start();
            }
            update.addActionListener(ev -> {
                if (ticker[0] != null) {
                    ticker[0].stop();
                }
                runUpdate(frame, gameDir, dot, latest, installed, url, size);
            });
            later.addActionListener(ev -> {
                if (ticker[0] != null) {
                    ticker[0].stop();
                }
                frame.dispose();
            });
            frame.setVisible(true);
            update.requestFocusInWindow();
            String autotest = System.getenv("AUTIS_UPDATER_AUTOTEST");
            if (autotest != null && !autotest.isBlank()) {
                Timer click = new Timer(1200, ev -> {
                    if (autotest.equals("update")) {
                        update.doClick();
                    } else {
                        later.doClick();
                    }
                });
                click.setRepeats(false);
                click.start();
            }
        });
    }

    static void runUpdate(JFrame frame, Path gameDir, Path dot, String latest, String installed, String url, long size) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(new EmptyBorder(16, 18, 14, 18));
        JLabel title = new JLabel("Downloading " + latest + "…");
        title.setFont(title.getFont().deriveFont(Font.BOLD, title.getFont().getSize2D() + 3f));
        JProgressBar bar = new JProgressBar(0, 1000);
        bar.setStringPainted(true);
        bar.setString("0%");
        JLabel detail = new JLabel(size > 0 ? human(size) : "");
        panel.add(title, BorderLayout.NORTH);
        panel.add(bar, BorderLayout.CENTER);
        panel.add(detail, BorderLayout.SOUTH);
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.revalidate();
        frame.repaint();
        new Thread(() -> {
            try {
                download(url, dot, size, (done, total) -> {
                    int pct = (int) Math.min(100L, done * 100L / Math.max(1L, total));
                    SwingUtilities.invokeLater(() -> {
                        bar.setValue(pct * 10);
                        bar.setString(pct + "%");
                        detail.setText(human(done) + " / " + human(total) + "  (download)");
                    });
                });
                SwingUtilities.invokeLater(() -> {
                    title.setText("Applying " + latest + "…");
                    bar.setValue(0);
                    bar.setString("0%");
                    detail.setText("extracting");
                });
                applyZip(dot.resolve("tmp").resolve("pack.zip"), gameDir, dot, latest, (done, total) -> {
                    int pct = (int) Math.min(100L, done * 100L / Math.max(1L, total));
                    SwingUtilities.invokeLater(() -> {
                        bar.setValue(pct * 10);
                        bar.setString(pct + "%");
                        detail.setText(done + " / " + total + " files");
                    });
                });
                SwingUtilities.invokeLater(() -> {
                    bar.setValue(1000);
                    bar.setString("100%");
                    title.setText("Updated to " + latest + ". Starting Minecraft…");
                    detail.setText("replaced files are in .autis/backups");
                    note(gameDir, "updated");
                    Timer close = new Timer(1200, ev -> frame.dispose());
                    close.setRepeats(false);
                    close.start();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    bar.setString("failed");
                    title.setText("Update failed");
                    detail.setText("<html><body style='width:400px'>" + escape(e.getMessage())
                            + "<br><br>Installed pack: " + nz(installed) + ". The game will still start.</body></html>");
                    JButton cont = new JButton("Continue");
                    cont.addActionListener(ev -> frame.dispose());
                    JPanel south = new JPanel(new BorderLayout());
                    south.add(detail, BorderLayout.CENTER);
                    JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
                    east.add(cont);
                    south.add(east, BorderLayout.EAST);
                    panel.add(south, BorderLayout.SOUTH);
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    cont.requestFocusInWindow();
                    note(gameDir, "error: " + e.getMessage());
                });
            }
        }, "autis-updater-sync").start();
    }

    static String escape(String s) {
        if (s == null) {
            return "unknown error";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    static void swingError(String message, int timeout) {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        try {
            SwingUtilities.invokeAndWait(() -> {
                JOptionPane pane = new JOptionPane(message, JOptionPane.WARNING_MESSAGE, JOptionPane.DEFAULT_OPTION);
                JDialog dialog = pane.createDialog("Autisnautics 1.21 — updater");
                if (timeout > 0) {
                    Timer timer = new Timer(timeout * 1000, ev -> dialog.dispose());
                    timer.setRepeats(false);
                    timer.start();
                }
                dialog.setVisible(true);
                dialog.dispose();
            });
        } catch (Exception ignored) {
            // headless or a display that is not ready yet
        }
    }

    static interface Progress {
        void tick(long done, long total) throws Exception;
    }

    static final class Asset {
        final String name;
        final String url;

        Asset(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }

    /** Non-modal progress. Never blocks the sync, and a failure here is ignored. */
    static final class AgentWindow {
        final AtomicBoolean open = new AtomicBoolean();
        JFrame frame;
        JLabel label;
        JProgressBar bar;

        static AgentWindow tryOpen() {
            if (GraphicsEnvironment.isHeadless()) {
                return null;
            }
            try {
                AgentWindow window = new AgentWindow();
                SwingUtilities.invokeLater(window::build);
                return window;
            } catch (Throwable t) {
                log("progress window skipped: " + t.getMessage());
                return null;
            }
        }

        void build() {
            try {
                frame = new JFrame("Autisnautics 1.21 — pack update");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                JPanel panel = new JPanel(new BorderLayout(0, 8));
                panel.setBorder(new EmptyBorder(16, 18, 14, 18));
                label = new JLabel("Checking for a pack update…");
                bar = new JProgressBar(0, 1000);
                bar.setStringPainted(true);
                bar.setString(" ");
                panel.add(label, BorderLayout.NORTH);
                panel.add(bar, BorderLayout.CENTER);
                frame.setContentPane(panel);
                frame.setSize(460, 120);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                open.set(true);
            } catch (Throwable t) {
                log("progress window skipped: " + t.getMessage());
            }
        }

        void status(String text) {
            SwingUtilities.invokeLater(() -> {
                if (label != null) {
                    label.setText(text);
                }
            });
        }

        void progress(long done, long total, String kind) {
            int pct = (int) Math.min(100L, done * 100L / Math.max(1L, total));
            SwingUtilities.invokeLater(() -> {
                if (bar != null) {
                    bar.setValue(pct * 10);
                    bar.setString(pct + "% " + kind);
                }
            });
        }

        void close() {
            SwingUtilities.invokeLater(() -> {
                if (frame != null) {
                    frame.dispose();
                }
            });
        }
    }
}
