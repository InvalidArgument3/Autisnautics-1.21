package dev.wyseman.autisupdatersetup;

/**
 * Modrinth App.
 *
 * <p>Older profiles keep a {@code profile.json} beside the instance. That file
 * has {@code extra_launch_args}, {@code java.extra_arguments}, or
 * {@code hooks.pre_launch}. The current app moved those fields into its SQLite
 * database ({@code override_extra_launch_args} and {@code override_hook_pre_launch}).
 * This mod edits the JSON file when it is the copy the app still reads. It does
 * not open the database: the app holds that file and rewrites it. A profile
 * with no JSON hook is reported as an unsupported launcher.
 */
final class ModrinthPatch {
    static final String AGENT = CursePatch.AGENT;
    /** Shell hook used only when the profile has {@code hooks} and no JVM-arg field. */
    static final String HOOK = "java -jar \"autis/updater.jar\" --game-dir \".\"";

    private ModrinthPatch() {}

    static boolean looksLikeProfile(String text) {
        return text.contains("\"hooks\"")
                || text.contains("\"extra_launch_args\"")
                || text.contains("\"game_version\"")
                || text.contains("\"modrinth\"");
    }

    static boolean looksLikeAppPath(String path) {
        String s = path.replace('\\', '/').toLowerCase(java.util.Locale.ROOT);
        return s.contains("com.modrinth.theseus")
                || s.contains("modrinthapp")
                || s.contains("/modrinth/");
    }

    /**
     * @return patched JSON, the same string when the agent is already there,
     *     or null when this profile has no hook or JVM-arg field to write
     */
    static String patch(String text) {
        int open = text.indexOf('{');
        if (open < 0) {
            return null;
        }
        int end = JsonText.objectEnd(text, open);
        JsonText.Span args = JsonText.findKey(text, open, end, "extra_launch_args");
        if (args != null) {
            return patchArgValue(text, args);
        }
        JsonText.Span java = JsonText.findKey(text, open, end, "java");
        if (java != null && java.raw(text).startsWith("{")) {
            int jStart = java.start;
            int jEnd = java.end;
            JsonText.Span extra = JsonText.findKey(text, jStart, jEnd, "extra_arguments");
            if (extra != null) {
                return patchArgValue(text, extra);
            }
        }
        JsonText.Span hooks = JsonText.findKey(text, open, end, "hooks");
        if (hooks != null && hooks.raw(text).startsWith("{")) {
            return patchHook(text, hooks.start, hooks.end);
        }
        return null;
    }

    private static String patchArgValue(String text, JsonText.Span args) {
        String raw = args.raw(text).trim();
        if (raw.equals("null")) {
            return JsonText.replace(text, args, "[" + JsonText.quote(AGENT) + "]");
        }
        if (!raw.startsWith("[")) {
            return text;
        }
        if (JsonText.stringArrayContains(text, args, "autis/updater.jar")) {
            return text;
        }
        return JsonText.appendString(text, args, AGENT);
    }

    private static String patchHook(String text, int hooksStart, int hooksEnd) {
        JsonText.Span pre = JsonText.findKey(text, hooksStart, hooksEnd, "pre_launch");
        if (pre == null) {
            return JsonText.insert(text, hooksStart, hooksEnd, "pre_launch", JsonText.quote(HOOK));
        }
        String raw = pre.raw(text).trim();
        if (raw.equals("null")) {
            return JsonText.replace(text, pre, JsonText.quote(HOOK));
        }
        if (!raw.startsWith("\"")) {
            return text;
        }
        String decoded = JsonText.unquote(raw);
        if (decoded.contains("autis/updater.jar")) {
            return text;
        }
        String next = decoded.isBlank() ? HOOK : HOOK + " && " + decoded;
        return JsonText.replace(text, pre, JsonText.quote(next));
    }
}
