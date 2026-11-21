package dev.wyseman.autisupdatersetup;

/**
 * Prism and MultiMC {@code instance.cfg}.
 *
 * <p>The value written is the same command the bootstrap script writes. Prism
 * keeps this file in memory and saves it when the game process exits (play
 * time and last launch), so a write made while the game is running can be
 * replaced. The caller schedules a second write after that save.
 */
final class PrismPatch {
    static final String COMMAND =
            "\"$INST_JAVA\" -jar \"$INST_MC_DIR/autis/updater.jar\" --game-dir \"$INST_MC_DIR\"";
    private static final String MARKER = "autis/updater.jar";

    private PrismPatch() {}

    static String patch(String text) {
        String nl = text.contains("\r\n") ? "\r\n" : "\n";
        boolean trailing = text.endsWith(nl);
        String body = trailing ? text.substring(0, text.length() - nl.length()) : text;
        String[] lines = body.isEmpty() ? new String[0] : body.split(java.util.regex.Pattern.quote(nl), -1);
        int overrideAt = -1;
        int commandAt = -1;
        int typeAt = -1;
        int generalAt = -1;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.startsWith("OverrideCommands=")) {
                overrideAt = i;
            } else if (line.startsWith("PreLaunchCommand=")) {
                commandAt = i;
            } else if (line.equals("InstanceType=OneSix")) {
                typeAt = i;
            } else if (line.equals("[General]")) {
                generalAt = i;
            }
        }
        String decoded = commandAt >= 0 ? decode(lines[commandAt].substring("PreLaunchCommand=".length())) : "";
        boolean ours = decoded.contains(MARKER);
        boolean overrideOk = overrideAt >= 0 && lines[overrideAt].equals("OverrideCommands=true");
        if (ours && overrideOk) {
            return text;
        }
        String nextCommand = ours || decoded.isBlank() ? COMMAND : COMMAND + " && " + decoded;
        String commandLine = ours && commandAt >= 0 ? lines[commandAt] : "PreLaunchCommand=" + encode(nextCommand);
        boolean commandOk = commandAt >= 0 && lines[commandAt].equals(commandLine);
        if (overrideOk && commandOk) {
            return text;
        }
        java.util.List<String> out = new java.util.ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            if (i == overrideAt) {
                out.add("OverrideCommands=true");
            } else if (i == commandAt) {
                out.add(commandLine);
            } else {
                out.add(lines[i]);
            }
        }
        if (overrideAt < 0 || commandAt < 0) {
            int insertAt = 0;
            if (typeAt >= 0) {
                insertAt = indexOf(out, "InstanceType=OneSix") + 1;
            } else if (generalAt >= 0) {
                insertAt = indexOf(out, "[General]") + 1;
            }
            if (overrideAt < 0) {
                out.add(insertAt, "OverrideCommands=true");
                insertAt++;
            }
            if (commandAt < 0) {
                out.add(insertAt, commandLine);
            }
        }
        String joined = String.join(nl, out);
        if (trailing || text.isEmpty()) {
            joined = joined + nl;
        }
        return joined;
    }

    private static int indexOf(java.util.List<String> lines, String exact) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).equals(exact)) {
                return i;
            }
        }
        return 0;
    }

    static String decode(String raw) {
        String value = raw;
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
            value = value.substring(1, value.length() - 1);
        }
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' && i + 1 < value.length()) {
                out.append(value.charAt(++i));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    static String encode(String command) {
        return "\"" + command.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

}
