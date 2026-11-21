package dev.wyseman.autisupdatersetup;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/** File-patch checks for Prism, CurseForge, and the Modrinth profile. */
public final class SetupHarness {
    private static int checks;

    public static void main(String[] args) throws Exception {
        Setup.allowHelper = false;
        prismFresh();
        prismChain();
        prismAlready();
        prismIdempotentFile();
        curseInstance();
        curseNullArgs();
        curseMaster();
        windowsPath();
        modrinthArgs();
        modrinthHook();
        modrinthDatabase();
        disabled();
        alreadyRan();
        previousStuck();
        previousOverwritten();
        brokenJson();
        atomicLeavesNoTemp();
        System.out.println("ok " + checks);
    }

    private static void prismFresh() {
        String in = "[General]\r\nInstanceType=OneSix\r\nname=Autisnautics 1.21\r\n";
        String out = PrismPatch.patch(in);
        check(out.contains("OverrideCommands=true"), "override");
        check(out.contains("PreLaunchCommand=\"" + escape(PrismPatch.COMMAND) + "\""), "command " + out);
        check(out.contains("name=Autisnautics 1.21"), "name kept");
        check(out.contains("\r\n"), "crlf kept");
        check(PrismPatch.patch(out).equals(out), "second pass");
    }

    private static void prismChain() {
        String in = "[General]\nInstanceType=OneSix\nOverrideCommands=true\nPreLaunchCommand=\"echo \\\"hi\\\"\"\n";
        String out = PrismPatch.patch(in);
        String decoded = PrismPatch.decode(lineValue(out, "PreLaunchCommand="));
        check(decoded.startsWith(PrismPatch.COMMAND + " && "), "chains ours first: " + decoded);
        check(decoded.endsWith("echo \"hi\""), "keeps the old command");
        check(PrismPatch.patch(out).equals(out), "chain is not duplicated");
    }

    private static void prismAlready() {
        String line = "PreLaunchCommand=\"" + escape(PrismPatch.COMMAND) + "\"";
        String in = "[General]\nInstanceType=OneSix\nOverrideCommands=false\n" + line + "\nnotes=leave\n";
        String out = PrismPatch.patch(in);
        check(out.contains("OverrideCommands=true"), "flips override");
        check(out.contains(line), "does not rewrite a command that is already ours");
        check(out.contains("notes=leave"), "other line");
        String done = PrismPatch.patch(out);
        check(done.equals(out), "already hooked is unchanged");
    }

    private static void prismIdempotentFile() throws Exception {
        Path root = Files.createTempDirectory("autis-prism");
        Path mc = root.resolve("minecraft");
        Files.createDirectories(mc.resolve("autis"));
        String cfg = "[General]\nInstanceType=OneSix\nname=Pack\n";
        Files.writeString(root.resolve("instance.cfg"), cfg);
        Setup.run(mc, Setup.Phase.LAUNCH);
        byte[] once = Files.readAllBytes(root.resolve("instance.cfg"));
        String log = Files.readString(mc.resolve("autis/updater-setup.log"));
        check(log.contains("patched prism"), "logged prism patch");
        Setup.run(mc, Setup.Phase.LAUNCH);
        check(java.util.Arrays.equals(once, Files.readAllBytes(root.resolve("instance.cfg"))), "file bytes stick");
        check(Files.readString(mc.resolve("autis/updater-setup.log")).contains("prism already hooked"), "second launch");
        check(new String(once, StandardCharsets.UTF_8).contains("name=Pack"), "name survives setup");
    }

    private static void curseInstance() throws Exception {
        Path dir = Files.createTempDirectory("autis-cf");
        String json = "{\n  \"name\": \"Autisnautics\",\n  \"javaArgsOverride\": \"-Xmx4G\",\n  \"isUnlocked\": false\n}\n";
        Files.writeString(dir.resolve("minecraftinstance.json"), json);
        Path master = dir.resolve("MinecraftGameInstance.json");
        String install = dir.toString().replace("\\", "\\\\");
        if (!install.endsWith("\\")) {
            // JSON stores the windows trailing separator as \\. On this host the path uses /.
        }
        String masterJson = "[{\"installPath\":\"" + jsonEscape(dir.toString() + separator()) + "\",\"javaArgsOverride\":\"-Xmx1G\",\"name\":\"target\"}]";
        Files.writeString(master, masterJson);
        Setup.testMasters = java.util.List.of(master);
        try {
            Setup.run(dir, Setup.Phase.LAUNCH);
        } finally {
            Setup.testMasters = java.util.List.of();
        }
        String instance = Files.readString(dir.resolve("minecraftinstance.json"));
        check(instance.contains("\"javaArgsOverride\": \"-Xmx4G -javaagent:autis/updater.jar\""), "instance args " + instance);
        check(instance.contains("\"name\": \"Autisnautics\""), "name kept");
        check(instance.contains("\"isUnlocked\": false"), "flag kept");
        byte[] bak = Files.readAllBytes(dir.resolve("minecraftinstance.json.pre-updater.bak"));
        check(new String(bak, StandardCharsets.UTF_8).equals(json), "backup is the original");
        String store = Files.readString(master);
        check(store.contains("-Xmx1G -javaagent:autis/updater.jar"), "master args");
        check(store.contains("\"name\":\"target\""), "master name");
        Setup.testMasters = java.util.List.of(master);
        try {
            Setup.run(dir, Setup.Phase.LAUNCH);
        } finally {
            Setup.testMasters = java.util.List.of();
        }
        check(java.util.Arrays.equals(bak, Files.readAllBytes(dir.resolve("minecraftinstance.json.pre-updater.bak"))), "backup once");
        check(Files.readString(dir.resolve("autis/updater-setup.log")).contains("curseforge instance already hooked"), "second instance");
    }

    private static void curseNullArgs() {
        String in = "{\"name\":\"A\",\"javaArgsOverride\":null}";
        String out = CursePatch.patchInstance(in);
        check(out.contains("\"javaArgsOverride\":\"-javaagent:autis/updater.jar\""), "null replaced " + out);
        check(out.contains("\"name\":\"A\""), "name");
        check(CursePatch.patchInstance(out).equals(out), "null stays put");
    }

    private static void curseMaster() {
        String keep = "{\"installPath\":\"Z:\\\\other\\\\\",\"javaArgsOverride\":\"-Xmx1G\",\"name\":\"keep\"}";
        String target = "{\"installPath\":\"C:\\\\Games\\\\Pack\\\\\",\"javaArgsOverride\":\"-Xmx4G\",\"name\":\"target\"}";
        StringBuilder array = new StringBuilder(2_600_000);
        array.append('[');
        int copies = 36_000;
        for (int i = 0; i < copies; i++) {
            if (i > 0) {
                array.append(',');
            }
            array.append(i == copies / 2 ? target : keep);
        }
        array.append(']');
        String text = array.toString();
        check(text.length() > 2_500_000, "master is about 2.5MB, was " + text.length());
        int keeps = count(text, "\"name\":\"keep\"");
        String patched = CursePatch.patchMaster(text, "c:\\games\\pack");
        check(patched.contains("\"javaArgsOverride\":\"-Xmx4G -javaagent:autis/updater.jar\""), "target args");
        check(!patched.contains("\"javaArgsOverride\":\"-Xmx4G\""), "old target args gone");
        check(count(patched, "\"name\":\"keep\"") == keeps, "other instances stay");
        check(count(patched, "\"javaArgsOverride\":\"-Xmx1G\"") == keeps, "other args stay");
        int at = text.indexOf("\"javaArgsOverride\":\"-Xmx4G\"");
        check(at > 0, "target sits in the middle");
        check(text.substring(0, at).equals(patched.substring(0, at)), "prefix");
        String oldTail = text.substring(at + "\"javaArgsOverride\":\"-Xmx4G\"".length());
        String newMarker = "\"javaArgsOverride\":\"-Xmx4G -javaagent:autis/updater.jar\"";
        int newAt = patched.indexOf(newMarker);
        check(oldTail.equals(patched.substring(newAt + newMarker.length())), "suffix");
        check(CursePatch.patchMaster(patched, "C:/Games/Pack").equals(patched), "master second pass");
    }

    private static void windowsPath() {
        check(CursePatch.sameInstall("C:\\Games\\Pack\\", "c:/games/pack"), "windows case and slash");
        check(!CursePatch.sameInstall("C:\\Games\\Other\\", "C:\\Games\\Pack"), "different dir");
        check(CursePatch.sameInstall("/tmp/pack/", "/tmp/pack"), "posix");
        check(!CursePatch.sameInstall("/tmp/Pack", "/tmp/pack"), "posix case");
    }

    private static void modrinthArgs() throws Exception {
        Path dir = Files.createTempDirectory("autis-mr");
        String json = "{\"game_version\":\"1.21.1\",\"extra_launch_args\":[\"-Xmx4G\"],\"hooks\":{\"pre_launch\":null}}";
        Files.writeString(dir.resolve("profile.json"), json);
        Setup.run(dir, Setup.Phase.LAUNCH);
        String out = Files.readString(dir.resolve("profile.json"));
        check(out.contains("\"-Xmx4G\",\"-javaagent:autis/updater.jar\""), "jvm args " + out);
        check(out.contains("\"pre_launch\":null"), "hook left alone");
        byte[] once = Files.readAllBytes(dir.resolve("profile.json"));
        Setup.run(dir, Setup.Phase.LAUNCH);
        check(java.util.Arrays.equals(once, Files.readAllBytes(dir.resolve("profile.json"))), "modrinth idempotent");
    }

    private static void modrinthHook() {
        String in = "{\"name\":\"Pack\",\"hooks\":{\"pre_launch\":\"echo hi\",\"wrapper\":null}}";
        String out = ModrinthPatch.patch(in);
        check(out.contains(JsonText.quote(ModrinthPatch.HOOK + " && echo hi")), "hook chained " + out);
        check(out.contains("\"wrapper\":null"), "wrapper untouched");
        check(ModrinthPatch.patch(out).equals(out), "hook second pass");
    }

    private static void modrinthDatabase() throws Exception {
        Path dir = Files.createTempDirectory("theseus").resolve("com.modrinth.theseus").resolve("profiles").resolve("pack");
        Files.createDirectories(dir);
        Setup.run(dir, Setup.Phase.LAUNCH);
        String log = Files.readString(dir.resolve("autis/updater-setup.log"));
        check(log.contains("unsupported launcher"), "database launcher");
        check(log.contains("override_hook_pre_launch"), "names the field");
        check(!Files.exists(dir.resolve("profile.json")), "does not invent a profile");
    }

    private static void disabled() throws Exception {
        Path root = Files.createTempDirectory("autis-off");
        Path mc = root.resolve("minecraft");
        Files.createDirectories(mc.resolve("config"));
        Files.writeString(root.resolve("instance.cfg"), "[General]\nInstanceType=OneSix\n");
        Files.writeString(mc.resolve("config/autis_updater_setup-client.toml"), "enabled = false\n");
        Setup.run(mc, Setup.Phase.LAUNCH);
        check(Files.readString(root.resolve("instance.cfg")).equals("[General]\nInstanceType=OneSix\n"), "disabled does not write");
        check(Files.readString(mc.resolve("autis/updater-setup.log")).contains("disabled by config"), "disabled log");
    }

    private static void alreadyRan() throws Exception {
        Path root = Files.createTempDirectory("autis-ran");
        Path mc = root.resolve("minecraft");
        Files.createDirectories(mc.resolve("autis"));
        Files.writeString(root.resolve("instance.cfg"), "[General]\nInstanceType=OneSix\n");
        String stamp = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
        Files.writeString(mc.resolve("autis/updater.log"), stamp + " mode=agent installed=0.1.50 latest=0.1.50 result=up-to-date\n");
        Setup.run(mc, Setup.Phase.LAUNCH);
        check(Files.readString(root.resolve("instance.cfg")).equals("[General]\nInstanceType=OneSix\n"), "agent skip");
        check(Files.readString(mc.resolve("autis/updater-setup.log")).contains("updater already ran this launch"), "agent log");
    }

    private static void previousStuck() throws Exception {
        Path root = Files.createTempDirectory("autis-stuck");
        Path mc = root.resolve("minecraft");
        Files.createDirectories(mc.resolve("autis"));
        String cfg = "[General]\nInstanceType=OneSix\nOverrideCommands=true\nPreLaunchCommand=\""
                + escape(PrismPatch.COMMAND) + "\"\n";
        Files.writeString(root.resolve("instance.cfg"), cfg);
        Files.writeString(mc.resolve("autis/updater-setup.state"), "pending");
        Setup.run(mc, Setup.Phase.LAUNCH);
        check(Files.readString(mc.resolve("autis/updater-setup.log")).contains("previous write stuck"), "stuck");
        check(Files.readString(root.resolve("instance.cfg")).equals(cfg), "stuck does not rewrite");
        check(Files.readString(mc.resolve("autis/updater-setup.state")).equals("idle"), "state clears");
    }

    private static void previousOverwritten() throws Exception {
        Path root = Files.createTempDirectory("autis-lost");
        Path mc = root.resolve("minecraft");
        Files.createDirectories(mc.resolve("autis"));
        Files.writeString(root.resolve("instance.cfg"), "[General]\nInstanceType=OneSix\n");
        Files.writeString(mc.resolve("autis/updater-setup.state"), "pending");
        Setup.run(mc, Setup.Phase.LAUNCH);
        String log = Files.readString(mc.resolve("autis/updater-setup.log"));
        check(log.contains("previous write was overwritten"), "overwritten");
        check(Files.readString(root.resolve("instance.cfg")).contains("autis/updater.jar"), "rewritten");
    }

    private static void brokenJson() throws Exception {
        Path dir = Files.createTempDirectory("autis-bad");
        Files.writeString(dir.resolve("minecraftinstance.json"), "{");
        Setup.run(dir, Setup.Phase.LAUNCH);
        String log = Files.readString(dir.resolve("autis/updater-setup.log"));
        check(log.contains("error:"), "bad json is logged " + log);
        check(Files.readString(dir.resolve("minecraftinstance.json")).equals("{"), "bad json not replaced");
    }

    private static void atomicLeavesNoTemp() throws Exception {
        Path dir = Files.createTempDirectory("autis-atomic");
        Path file = dir.resolve("instance.cfg");
        Files.writeString(file, "x");
        AtomicFiles.write(file, "y".getBytes(StandardCharsets.UTF_8));
        check(Files.readString(file).equals("y"), "atomic replace");
        check(!Files.exists(dir.resolve("instance.cfg.autis-tmp")), "temp removed");
    }

    private static String escape(String command) {
        return command.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String lineValue(String text, String key) {
        for (String line : text.split("\n")) {
            if (line.startsWith(key)) {
                return line.substring(key.length()).replace("\r", "");
            }
        }
        return "";
    }

    private static String jsonEscape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String separator() {
        return java.io.File.separator;
    }

    private static int count(String text, String needle) {
        int n = 0;
        int at = 0;
        while ((at = text.indexOf(needle, at)) >= 0) {
            n++;
            at += needle.length();
        }
        return n;
    }

    private static void check(boolean ok, String message) {
        checks++;
        if (!ok) {
            throw new AssertionError(message);
        }
    }
}
