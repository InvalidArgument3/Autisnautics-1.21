package dev.wyseman.autisupdatersetup;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

/**
 * The same release check {@code tools/autis-updater} uses in {@code latestTag}
 * and {@code compareVersions}. The installed version is {@code .autis/version.txt}.
 * A newer GitHub tag is the only reason CurseForge schedules the updater.
 */
final class CurseUpdate {
    static final String RELEASES_LATEST = "https://github.com/InvalidArgument3/Autisnautics-1.21/releases/latest";
    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    @FunctionalInterface
    interface Lookup {
        String latest() throws Exception;
    }

    private CurseUpdate() {}

    static String latestTag() throws Exception {
        HttpClient http = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(TIMEOUT)
                .build();
        HttpResponse<Void> response = http.send(HttpRequest.newBuilder(URI.create(RELEASES_LATEST))
                .timeout(TIMEOUT)
                .header("User-Agent", "autis-updater/2.0")
                .build(), HttpResponse.BodyHandlers.discarding());
        String path = response.uri().getPath();
        int n = path.indexOf("/tag/");
        if (n < 0) {
            return null;
        }
        String tag = path.substring(n + 5);
        int slash = tag.indexOf('/');
        if (slash >= 0) {
            tag = tag.substring(0, slash);
        }
        return tag.isBlank() ? null : tag;
    }

    /** Same numeric compare as {@code Updater.compareVersions}. */
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

    static String readTrimmed(Path file) {
        try {
            return Files.exists(file) ? Files.readString(file, StandardCharsets.UTF_8).trim() : null;
        } catch (java.io.IOException e) {
            return null;
        }
    }

    static boolean isPackSource(Path gameDir) {
        return Files.exists(gameDir.resolve("pakku.json")) || Files.exists(gameDir.resolve("pakku-lock.json"));
    }

    static boolean acceptableTag(String tag) {
        return tag != null && tag.matches("[A-Za-z0-9._+-]+");
    }

    /** The executable that launched this game, or {@code java.home}'s java. */
    static String gameJava() {
        try {
            Optional<String> command = ProcessHandle.current().info().command();
            if (command.isPresent() && !command.get().isBlank()) {
                return command.get();
            }
        } catch (Throwable ignored) {
            // fall through to java.home
        }
        String name = windows() ? "java.exe" : "java";
        return Path.of(System.getProperty("java.home", ""), "bin", name).toString();
    }

    /**
     * Java for the updater window. On Windows that is {@code javaw.exe}: the
     * game's own executable when CurseForge already started it with javaw,
     * otherwise the javaw beside {@code java.exe}, otherwise {@code java.home}.
     * Other systems use the game's java as-is.
     */
    static String updaterJava() {
        String game = gameJava();
        if (!windows()) {
            return game;
        }
        return windowsUpdaterJava(game);
    }

    static String windowsUpdaterJava(String command) {
        if (command != null && !command.isBlank()) {
            Path path = Path.of(command.trim());
            String name = path.getFileName() == null ? "" : path.getFileName().toString();
            if (name.equalsIgnoreCase("javaw.exe")) {
                return path.toString();
            }
            Path sibling = path.resolveSibling("javaw.exe");
            if (Files.isRegularFile(sibling)) {
                return sibling.toString();
            }
            if (name.equalsIgnoreCase("java.exe") && Files.isRegularFile(path)) {
                return path.toString();
            }
            if (Files.isRegularFile(path)) {
                return path.toString();
            }
        }
        Path javaw = Path.of(System.getProperty("java.home", ""), "bin", "javaw.exe");
        if (Files.isRegularFile(javaw)) {
            return javaw.toString();
        }
        Path java = Path.of(System.getProperty("java.home", ""), "bin", "java.exe");
        if (Files.isRegularFile(java)) {
            return java.toString();
        }
        return command == null ? "" : command.trim();
    }

    private static boolean windows() {
        return System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("win");
    }
}
