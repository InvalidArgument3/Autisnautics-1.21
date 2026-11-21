package dev.wyseman.autisupdatersetup;

import java.util.ArrayList;
import java.util.List;

/**
 * CurseForge keeps {@code javaArgsOverride} in memory and writes that copy
 * back, so adding {@code -javaagent:autis/updater.jar} does not stick.
 * {@link Setup} does not call the patch methods anymore. What it does call
 * is the strip: remove only that agent token, and only when it is a whole
 * argument inside a JSON string. Anything else is left byte for byte.
 */
final class CursePatch {
    static final String AGENT = "-javaagent:autis/updater.jar";

    static final class Strip {
        static final int ABSENT = 0;
        static final int STRIPPED = 1;
        /** The agent is mentioned, but not as a plain JVM argument we can delete. */
        static final int UNSAFE = 2;
        /** The file is not the JSON shape these editors understand. */
        static final int UNREADABLE = 3;

        final String text;
        final int status;

        Strip(String text, int status) {
            this.text = text;
            this.status = status;
        }
    }

    private CursePatch() {}

    static String addAgent(String args) {
        if (args == null || args.isBlank()) {
            return AGENT;
        }
        if (hasAgent(args)) {
            return args;
        }
        return args + " " + AGENT;
    }

    static boolean hasAgent(String args) {
        return args.contains("-javaagent:autis/updater.jar")
                || args.contains("-javaagent:autis\\updater.jar")
                || args.contains("-javaagent:./autis/updater.jar");
    }

    static boolean mentionsAgent(String text) {
        return text.replace('\\', '/').contains("autis/updater.jar");
    }

    /**
     * Removes our agent from {@code javaArgsOverride} when it is a complete
     * argument. A value that mentions the jar in any other shape is refused,
     * and the original text is returned unchanged.
     */
    static Strip stripInstance(String text) {
        try {
            int start = indexOfBrace(text);
            int end = JsonText.objectEnd(text, start);
            return stripObject(text, start, end);
        } catch (RuntimeException e) {
            return new Strip(text, Strip.UNREADABLE);
        }
    }

    /**
     * Same removal, only for objects whose {@code installPath} is this instance.
     * One unsafe match leaves the whole file untouched.
     */
    static Strip stripMaster(String text, String instanceDir) {
        final List<int[]> objects;
        try {
            objects = JsonText.topLevelObjects(text);
        } catch (RuntimeException e) {
            return new Strip(text, Strip.UNREADABLE);
        }
        String current = text;
        boolean stripped = false;
        boolean matched = false;
        for (int i = objects.size() - 1; i >= 0; i--) {
            int start = objects.get(i)[0];
            int end = objects.get(i)[1];
            final JsonText.Span path;
            try {
                path = JsonText.findKey(current, start, end, "installPath");
            } catch (RuntimeException e) {
                return new Strip(text, Strip.UNREADABLE);
            }
            if (path == null || !path.raw(current).startsWith("\"")) {
                continue;
            }
            final String install;
            try {
                install = JsonText.unquote(path.raw(current));
            } catch (RuntimeException e) {
                return new Strip(text, Strip.UNREADABLE);
            }
            if (!sameInstall(install, instanceDir)) {
                continue;
            }
            matched = true;
            final Strip one;
            try {
                one = stripObject(current, start, end);
            } catch (RuntimeException e) {
                return new Strip(text, Strip.UNSAFE);
            }
            if (one.status == Strip.UNSAFE || one.status == Strip.UNREADABLE) {
                return new Strip(text, one.status);
            }
            if (one.status == Strip.STRIPPED) {
                current = one.text;
                stripped = true;
            }
        }
        if (!matched || !stripped) {
            return new Strip(text, Strip.ABSENT);
        }
        return new Strip(current, Strip.STRIPPED);
    }

    /**
     * @return the arguments with our agent removed, or null when the jar is
     * mentioned but not as one of the tokens we added
     */
    static String removeAgentToken(String args) {
        String[] parts = args.trim().split("\\s+");
        List<String> keep = new ArrayList<>();
        boolean removed = false;
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (isOurAgent(part)) {
                removed = true;
                continue;
            }
            keep.add(part);
        }
        if (!removed) {
            return null;
        }
        String joined = String.join(" ", keep);
        if (mentionsAgent(joined)) {
            return null;
        }
        return joined;
    }

    static boolean isOurAgent(String token) {
        String n = token.replace('\\', '/');
        if (n.length() >= 2 && n.startsWith("\"") && n.endsWith("\"")) {
            n = n.substring(1, n.length() - 1);
        }
        return n.equals("-javaagent:autis/updater.jar") || n.equals("-javaagent:./autis/updater.jar");
    }

    /**
     * @param instanceDir the directory that contains {@code minecraftinstance.json}
     * @return patched text, or the same string when nothing changed
     */
    static String patchInstance(String text) {
        return patchObject(text, 0, JsonText.objectEnd(text, indexOfBrace(text)));
    }

    /**
     * Patches the one array element whose {@code installPath} is this instance.
     * Returns the original string when no object matches or the args are already set.
     */
    static String patchMaster(String text, String instanceDir) {
        List<int[]> objects = JsonText.topLevelObjects(text);
        for (int[] bounds : objects) {
            JsonText.Span path = JsonText.findKey(text, bounds[0], bounds[1], "installPath");
            if (path == null || !path.raw(text).startsWith("\"")) {
                continue;
            }
            String install = JsonText.unquote(path.raw(text));
            if (!sameInstall(install, instanceDir)) {
                continue;
            }
            return patchObject(text, bounds[0], bounds[1]);
        }
        return text;
    }

    static boolean masterHasMatch(String text, String instanceDir) {
        for (int[] bounds : JsonText.topLevelObjects(text)) {
            JsonText.Span path = JsonText.findKey(text, bounds[0], bounds[1], "installPath");
            if (path != null && path.raw(text).startsWith("\"")) {
                if (sameInstall(JsonText.unquote(path.raw(text)), instanceDir)) {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean sameInstall(String installPath, String instanceDir) {
        String a = normalize(installPath);
        String b = normalize(instanceDir);
        boolean windows = (a.length() >= 2 && a.charAt(1) == ':') || (b.length() >= 2 && b.charAt(1) == ':');
        if (windows) {
            return a.equalsIgnoreCase(b);
        }
        return a.equals(b);
    }

    static List<java.nio.file.Path> masterStores() {
        List<java.nio.file.Path> out = new ArrayList<>();
        String appdata = System.getenv("APPDATA");
        String home = System.getProperty("user.home", "");
        String xdg = System.getenv("XDG_CONFIG_HOME");
        if (appdata != null && !appdata.isBlank()) {
            out.add(java.nio.file.Path.of(appdata, "CurseForge", "agent", "GameInstances", "MinecraftGameInstance.json"));
            out.add(java.nio.file.Path.of(appdata, "Curse Client", "GameInstances", "MinecraftGameInstance.json"));
        }
        if (!home.isBlank()) {
            out.add(java.nio.file.Path.of(home, "Library", "Application Support", "CurseForge", "agent", "GameInstances", "MinecraftGameInstance.json"));
            out.add(java.nio.file.Path.of(home, ".config", "CurseForge", "agent", "GameInstances", "MinecraftGameInstance.json"));
            out.add(java.nio.file.Path.of(home, ".local", "share", "CurseForge", "agent", "GameInstances", "MinecraftGameInstance.json"));
        }
        if (xdg != null && !xdg.isBlank()) {
            out.add(java.nio.file.Path.of(xdg, "CurseForge", "agent", "GameInstances", "MinecraftGameInstance.json"));
        }
        return out;
    }

    private static Strip stripObject(String text, int start, int end) {
        JsonText.Span args = JsonText.findKey(text, start, end, "javaArgsOverride");
        if (args == null) {
            return new Strip(text, Strip.ABSENT);
        }
        String raw = args.raw(text).trim();
        if (raw.equals("null")) {
            return new Strip(text, Strip.ABSENT);
        }
        if (!raw.startsWith("\"")) {
            if (mentionsAgent(raw)) {
                return new Strip(text, Strip.UNSAFE);
            }
            return new Strip(text, Strip.ABSENT);
        }
        final String decoded;
        try {
            decoded = JsonText.unquote(raw);
        } catch (RuntimeException e) {
            return new Strip(text, Strip.UNSAFE);
        }
        if (!mentionsAgent(decoded)) {
            return new Strip(text, Strip.ABSENT);
        }
        String next = removeAgentToken(decoded);
        if (next == null) {
            return new Strip(text, Strip.UNSAFE);
        }
        String replacement = next.isBlank() ? "null" : JsonText.quote(next);
        return new Strip(JsonText.replace(text, args, replacement), Strip.STRIPPED);
    }

    private static String patchObject(String text, int start, int end) {
        JsonText.Span args = JsonText.findKey(text, start, end, "javaArgsOverride");
        if (args == null) {
            return JsonText.insert(text, start, end, "javaArgsOverride", JsonText.quote(AGENT));
        }
        String raw = args.raw(text).trim();
        if (raw.equals("null")) {
            return JsonText.replace(text, args, JsonText.quote(AGENT));
        }
        if (!raw.startsWith("\"")) {
            return text;
        }
        String decoded = JsonText.unquote(raw);
        String next = addAgent(decoded);
        if (next.equals(decoded)) {
            return text;
        }
        return JsonText.replace(text, args, JsonText.quote(next));
    }

    private static int indexOfBrace(String text) {
        int i = 0;
        if (text.startsWith("\uFEFF")) {
            i = 1;
        }
        while (i < text.length() && Character.isWhitespace(text.charAt(i))) {
            i++;
        }
        if (i >= text.length() || text.charAt(i) != '{') {
            throw new IllegalArgumentException("minecraftinstance.json has no object");
        }
        return i;
    }

    private static String normalize(String path) {
        String s = path.trim().replace('\\', '/');
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

}
