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
        Setup.curseAsync = false;
        Setup.curseLookup = () -> "0.0.0";
        prismFresh();
        prismChain();
        prismAlready();
        prismIdempotentFile();
        curseInstance();
        curseUpToDate();
        curseUpdateFound();
        cursePackSource();
        curseStrip();
        curseTwoNames();
        curseLaunchesUpdater();
        curseNullArgs();
        curseMaster();
        windowsPath();
        versionCompare();
        windowsJava();
        modrinthArgs();
        modrinthHook();
        modrinthDatabase();
        disabled();
        alreadyRan();
        previousStuck();
        previousOverwritten();
        previousPendingDespiteAgent();
        helperJarFromClasses();
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
        String masterJson = "[{\"installPath\":\"" + jsonEscape(dir.toString() + separator()) + "\",\"javaArgsOverride\":\"-Xmx1G\",\"name\":\"Autisnautics-1.21\"},"
                + "{\"installPath\":\"" + jsonEscape(dir.toString() + "-other" + separator()) + "\",\"javaArgsOverride\":null,\"name\":\"Autisnautics-1.21\"}]";
        Files.writeString(master, masterJson);
        Files.createDirectories(dir.resolve(".autis"));
        Files.writeString(dir.resolve(".autis/version.txt"), "0.1.52");
        Setup.curseLookup = () -> "0.1.52";
        Setup.testMasters = java.util.List.of(master);
        try {
            Setup.run(dir, Setup.Phase.LAUNCH);
        } finally {
            Setup.testMasters = java.util.List.of();
            Setup.curseLookup = () -> "0.0.0";
        }
        check(Files.readString(dir.resolve("minecraftinstance.json")).equals(json), "instance file is not patched");
        check(Files.readString(master).equals(masterJson), "master file is not patched");
        check(!Files.exists(dir.resolve("minecraftinstance.json.pre-updater.bak")), "no instance backup");
        String log = Files.readString(dir.resolve("autis/updater-setup.log"));
        check(log.contains("curseforge mode: up to date"), "up to date " + log);
        check(!log.contains("already hooked"), "no false already-hooked " + log);
        check(!log.contains("patched curseforge"), "no curse patch " + log);
        check(Files.readString(dir.resolve("logs/latest.log")).contains("[autis_updater_setup] curseforge mode: up to date"), "latest.log");
    }

    private static void curseUpToDate() {
        check(CurseUpdate.compareVersions("0.1.52", "0.1.52") <= 0, "same version is current");
        check(CurseUpdate.compareVersions("0.1.51", "0.1.52") <= 0, "older github tag is current");
    }

    private static void curseUpdateFound() throws Exception {
        Path dir = Files.createTempDirectory("autis-cf-update");
        String json = "{\"name\":\"A\",\"javaArgsOverride\":\"-Xmx4G -javaagent:autis/updater.jar\"}";
        Files.writeString(dir.resolve("minecraftinstance.json"), json);
        Files.createDirectories(dir.resolve("autis"));
        Files.createDirectories(dir.resolve(".autis"));
        Files.writeString(dir.resolve(".autis/version.txt"), "0.1.52");
        Files.write(dir.resolve("autis/updater.jar"), new byte[] {1, 2, 3});
        Setup.curseLookup = () -> "0.9.9";
        try {
            Setup.run(dir, Setup.Phase.LAUNCH);
        } finally {
            Setup.curseLookup = () -> "0.0.0";
        }
        check(Files.readString(dir.resolve("minecraftinstance.json")).equals(json), "launch does not edit the instance");
        String log = Files.readString(dir.resolve("autis/updater-setup.log"));
        check(log.contains("curseforge mode: update 0.9.9 found, post-exit updater not scheduled"), "schedules only when the helper is allowed " + log);
        check(log.contains("curseforge mode: javaagent present, removal waits until the game exits"), "agent noticed " + log);
        check(log.contains("curseforge mode: updater java "), "java path logged " + log);
        check(log.contains("curseforge mode: game java "), "game java logged " + log);
        check(!log.contains("already hooked"), "not the old hook check");
    }

    private static void cursePackSource() throws Exception {
        Path dir = Files.createTempDirectory("autis-cf-src");
        String json = "{\"name\":\"A\",\"javaArgsOverride\":null}";
        Files.writeString(dir.resolve("minecraftinstance.json"), json);
        Files.writeString(dir.resolve("pakku.json"), "{}");
        Setup.curseLookup = () -> {
            throw new AssertionError("pack source must not check GitHub");
        };
        try {
            Setup.run(dir, Setup.Phase.LAUNCH);
        } finally {
            Setup.curseLookup = () -> "0.0.0";
        }
        String log = Files.readString(dir.resolve("autis/updater-setup.log"));
        check(log.contains("curseforge mode: pack source, not checking"), log);
        check(Files.readString(dir.resolve("minecraftinstance.json")).equals(json), "pack source does not edit");
    }

    private static void curseStrip() throws Exception {
        Path dir = Files.createTempDirectory("autis-cf-strip");
        String json = "{\"name\":\"A\",\"javaArgsOverride\":\"-Xmx4G -javaagent:autis/updater.jar -Dfoo=1\"}";
        Files.writeString(dir.resolve("minecraftinstance.json"), json);
        Path master = dir.resolve("MinecraftGameInstance.json");
        String keep = "{\"installPath\":\"" + jsonEscape("/other/") + "\",\"javaArgsOverride\":\"-javaagent:autis/updater.jar\",\"name\":\"Autisnautics-1.21\"}";
        String target = "{\"installPath\":\"" + jsonEscape(dir.toString() + separator()) + "\",\"javaArgsOverride\":\"-Xmx1G -javaagent:autis/updater.jar\",\"name\":\"Autisnautics-1.21\"}";
        String masterJson = "[" + keep + "," + target + "]";
        Files.writeString(master, masterJson);
        Setup.testMasters = java.util.List.of(master);
        try {
            Setup.curseAfterExit(dir, -1, "/usr/bin/false", "-", true);
        } finally {
            Setup.testMasters = java.util.List.of();
        }
        String instance = Files.readString(dir.resolve("minecraftinstance.json"));
        check(instance.contains("\"javaArgsOverride\":\"-Xmx4G -Dfoo=1\""), "instance args " + instance);
        check(!instance.contains("javaagent"), "instance agent gone");
        String store = Files.readString(master);
        check(store.contains("\"javaArgsOverride\":\"-Xmx1G\""), "master target " + store);
        check(store.contains("\"javaArgsOverride\":\"-javaagent:autis/updater.jar\""), "other instance keeps its agent");
        check(store.contains("\"name\":\"Autisnautics-1.21\""), "names stay");
        String only = CursePatch.stripInstance("{\"javaArgsOverride\":\"-javaagent:autis/updater.jar\"}").text;
        check(only.contains("\"javaArgsOverride\":null"), "sole agent becomes null " + only);
        String other = CursePatch.stripInstance("{\"javaArgsOverride\":\"-javaagent:other.jar\"}").text;
        check(other.contains("-javaagent:other.jar"), "a different agent stays");
        CursePatch.Strip unsafe = CursePatch.stripInstance("{\"javaArgsOverride\":{\"-javaagent:autis/updater.jar\":true}}");
        check(unsafe.status == CursePatch.Strip.UNSAFE, "nested agent is not edited");
        check(unsafe.text.contains("{\"-javaagent:autis/updater.jar\":true}"), "unsafe text unchanged");
        String log = Files.readString(dir.resolve("autis/updater-setup.log"));
        check(log.contains("curseforge mode: removed javaagent from minecraftinstance.json"), log);
        check(log.contains("curseforge mode: removed javaagent from MinecraftGameInstance.json"), log);
    }

    private static void curseTwoNames() throws Exception {
        String keep = "{\"installPath\":\"Z:\\\\other\\\\\",\"javaArgsOverride\":null,\"name\":\"Autisnautics-1.21\"}";
        String other = "{\"installPath\":\"Z:\\\\second\\\\\",\"javaArgsOverride\":\"-Xmx2G\",\"name\":\"Autisnautics-1.21\"}";
        String text = "[" + keep + "," + other + "]";
        CursePatch.Strip strip = CursePatch.stripMaster(text, "C:\\Games\\Pack");
        check(strip.status == CursePatch.Strip.ABSENT, "no path match is not a hook");
        check(strip.text.equals(text), "unmatched master stays byte for byte");
        Path dir = Files.createTempDirectory("autis-cf-names");
        Files.writeString(dir.resolve("minecraftinstance.json"), "{\"name\":\"A\",\"javaArgsOverride\":null}");
        Path master = dir.resolve("MinecraftGameInstance.json");
        Files.writeString(master, text);
        Files.createDirectories(dir.resolve(".autis"));
        Files.writeString(dir.resolve(".autis/version.txt"), "0.1.52");
        Setup.curseLookup = () -> "0.1.52";
        Setup.testMasters = java.util.List.of(master);
        try {
            Setup.run(dir, Setup.Phase.LAUNCH);
        } finally {
            Setup.testMasters = java.util.List.of();
            Setup.curseLookup = () -> "0.0.0";
        }
        String log = Files.readString(dir.resolve("autis/updater-setup.log"));
        check(!log.contains("already hooked"), "two named entries are not already hooked " + log);
        check(Files.readString(master).equals(text), "launch leaves both entries");
    }

    private static void curseLaunchesUpdater() throws Exception {
        Path dir = Files.createTempDirectory("autis-cf-run");
        Files.createDirectories(dir.resolve("autis"));
        Files.writeString(dir.resolve("minecraftinstance.json"), "{\"name\":\"A\",\"javaArgsOverride\":null}");
        Files.write(dir.resolve("autis/updater.jar"), new byte[] {0});
        Path argv = dir.resolve("autis/argv.txt");
        Path script = dir.resolve("fake-java.sh");
        String body = "#!/bin/sh\nprintf '%s\\n' \"$@\" > \"" + argv + "\"\n";
        Files.writeString(script, body);
        if (!script.toFile().setExecutable(true)) {
            throw new AssertionError("script not executable");
        }
        Setup.curseAfterExit(dir, -1, script.toString(), "0.9.9", false);
        String args = Files.readString(argv);
        check(args.contains("-jar"), args);
        check(args.contains("updater.jar"), args);
        check(args.contains("--game-dir"), args);
        check(args.contains(dir.toString()), args);
        String log = Files.readString(dir.resolve("autis/updater-setup.log"));
        check(log.contains("curseforge mode: starting updater for 0.9.9"), log);
        check(log.contains("java=" + script), log);
        check(log.contains("curseforge mode: updater exited 0"), log);
        check(Files.readString(dir.resolve("minecraftinstance.json")).contains("\"javaArgsOverride\":null"), "launch path does not add an agent");
    }

    private static void versionCompare() {
        check(CurseUpdate.compareVersions("0.1.10", "0.1.9") > 0, "0.1.10 is newer");
        check(CurseUpdate.compareVersions("0.1.52", "0.1.52") == 0, "equal");
        check(CurseUpdate.compareVersions("0.1.52", "0.1.53") < 0, "older");
        check(CurseUpdate.compareVersions("v0.1.2", "0.1.2") == 0, "v prefix ignored");
        check(!CurseUpdate.acceptableTag("0.1.52\n"), "tag cannot carry a newline");
        check(CurseUpdate.acceptableTag("0.1.52"), "plain tag");
    }

    private static void windowsJava() throws Exception {
        Path bin = Files.createTempDirectory("autis-java").resolve("bin");
        Files.createDirectories(bin);
        Files.write(bin.resolve("java.exe"), new byte[0]);
        Files.write(bin.resolve("javaw.exe"), new byte[0]);
        String chosen = CurseUpdate.windowsUpdaterJava(bin.resolve("java.exe").toString());
        check(chosen.equals(bin.resolve("javaw.exe").toString()), "java.exe swaps to javaw " + chosen);
        String already = CurseUpdate.windowsUpdaterJava(bin.resolve("javaw.exe").toString());
        check(already.equals(bin.resolve("javaw.exe").toString()), "javaw stays " + already);
        check(!CurseUpdate.gameJava().isBlank(), "this process has a java path");
        if (!System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("win")) {
            check(CurseUpdate.updaterJava().equals(CurseUpdate.gameJava()), "non-windows uses the game java");
        }
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

    private static void previousPendingDespiteAgent() throws Exception {
        Path root = Files.createTempDirectory("autis-pending-agent");
        Path mc = root.resolve("minecraft");
        Files.createDirectories(mc.resolve("autis"));
        Files.writeString(root.resolve("instance.cfg"), "[General]\nInstanceType=OneSix\n");
        Files.writeString(mc.resolve("autis/updater-setup.state"), "pending");
        String stamp = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
        Files.writeString(mc.resolve("autis/updater.log"), stamp + " mode=agent installed=0.1.51 latest=0.1.51 result=up-to-date\n");
        Setup.run(mc, Setup.Phase.LAUNCH);
        String log = Files.readString(mc.resolve("autis/updater-setup.log"));
        check(log.contains("previous write was overwritten"), "pending survives an agent skip " + log);
        check(Files.readString(root.resolve("instance.cfg")).equals("[General]\nInstanceType=OneSix\n"), "agent skip still does not write");
    }

    private static void helperJarFromClasses() throws Exception {
        Path root = Files.createTempDirectory("autis-helper-classes");
        Path mc = root.resolve("minecraft");
        Files.createDirectories(mc);
        Files.writeString(root.resolve("instance.cfg"), "[General]\nInstanceType=OneSix\n");
        Path helper = Setup.materializeHelper(mc);
        check(helper != null && Files.isRegularFile(helper), "helper jar exists");
        check(Files.readString(mc.resolve("autis/updater-setup.log")).contains("helper jar built from loaded classes"), "built from classes");
    }

    private static void brokenJson() throws Exception {
        Path dir = Files.createTempDirectory("autis-bad");
        Files.writeString(dir.resolve("minecraftinstance.json"), "{");
        Setup.curseLookup = () -> "0.9.9";
        try {
            Setup.run(dir, Setup.Phase.LAUNCH);
        } finally {
            Setup.curseLookup = () -> "0.0.0";
        }
        String log = Files.readString(dir.resolve("autis/updater-setup.log"));
        check(log.contains("could not be checked for a javaagent"), "bad json is logged " + log);
        check(log.contains("autis/updater.jar is missing"), "missing updater is logged " + log);
        check(!log.contains("patched curseforge"), "bad json is not patched");
        check(Files.readString(dir.resolve("minecraftinstance.json")).equals("{"), "bad json not replaced");
        CursePatch.Strip unread = CursePatch.stripInstance("{");
        check(unread.status == CursePatch.Strip.UNREADABLE, "strip refuses a broken object");
        check(unread.text.equals("{"), "strip leaves a broken object");
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
