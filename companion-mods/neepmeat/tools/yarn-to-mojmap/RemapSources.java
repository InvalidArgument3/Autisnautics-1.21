import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;
import org.cadixdev.mercury.Mercury;
import org.cadixdev.mercury.remapper.MercuryRemapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Remaps Yarn 1.20.1 sources to Mojmap with Mercury and tiny-remapper.
 * Arguments: yarn-to-mojmap.tiny official-to-yarn.tiny client.jar source-root output-dir
 */
public final class RemapSources {
    public static void main(String[] args) throws Exception {
        Path yarnToMoj = Path.of(args[0]);
        Path officialToYarn = Path.of(args[1]);
        Path client = Path.of(args[2]);
        Path source = Path.of(args[3]);
        Path output = Path.of(args[4]);
        Path yarnJar = Path.of(args.length > 5 ? args[5] : "/tmp/remap/minecraft-yarn.jar");

        if (!Files.exists(yarnJar) || Files.size(yarnJar) < 1_000_000) {
            System.out.println("remapping client jar official -> intermediary -> yarn");
            Path intermediaryJar = yarnJar.resolveSibling("minecraft-intermediary.jar");
            remapJar(client, intermediaryJar, officialToYarn.resolveSibling("intermediary.tiny"), "official", "intermediary");
            remapJar(intermediaryJar, yarnJar, officialToYarn.resolveSibling("yarn.tiny"), "intermediary", "named");
            System.out.println("wrote " + yarnJar);
        }

        TinyRemapper toMoj = TinyRemapper.newRemapper()
                .withMappings(TinyUtils.createTinyMappingProvider(yarnToMoj, "yarn", "mojmap"))
                .ignoreConflicts(true)
                .build();
        toMoj.readClassPath(yarnJar);
        Mercury mercury = new Mercury();
        mercury.setSourceCompatibilityFromRelease(17);
        mercury.setGracefulClasspathChecks(true);
        mercury.getClassPath().add(yarnJar);
        Path meatlib = Path.of("/tmp/meatlib-src");
        if (Files.isDirectory(meatlib)) {
            mercury.getSourcePath().add(meatlib);
        }
        mercury.getProcessors().add(MercuryRemapper.create(toMoj.getEnvironment()));
        System.out.println("rewriting " + source + " -> " + output);
        try {
            mercury.rewrite(source, output);
            System.out.println("done");
        } catch (RuntimeException failure) {
            System.out.println("FAIL " + failure);
            failure.printStackTrace(System.out);
        }
        toMoj.finish();
    }

    private static void remapJar(Path input, Path output, Path mappings, String from, String to) throws Exception {
        Files.deleteIfExists(output);
        TinyRemapper remapper = TinyRemapper.newRemapper()
                .withMappings(TinyUtils.createTinyMappingProvider(mappings, from, to))
                .ignoreConflicts(true)
                .build();
        remapper.readInputs(input);
        try (OutputConsumerPath consumer = new OutputConsumerPath(output)) {
            remapper.apply(consumer);
        }
        remapper.finish();
    }
}
