package dev.wyseman.autisupdatersetup;

import java.util.ArrayList;
import java.util.List;

/**
 * CurseForge has no pre-launch command. The profile JVM arguments gain
 * {@code -javaagent:autis/updater.jar}, in the instance {@code minecraftinstance.json}
 * and in the matching {@code installPath} entry of the master store. Other
 * arguments stay. A {@code .pre-updater.bak} is taken once, by the caller.
 */
final class CursePatch {
    static final String AGENT = "-javaagent:autis/updater.jar";

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
