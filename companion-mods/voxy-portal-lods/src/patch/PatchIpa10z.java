import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Turns the ipa10y jar into ipa10z by putting back the render classes ipa10w,
 * ipa10x, and ipa10y rewrote. The replacement is the exact class bytes from
 * ipa10h, which still match ipa10j. Nothing here runs ASM or recomputes frames.
 */
public final class PatchIpa10z {
    private static final Map<String, String> RESTORE = new LinkedHashMap<String, String>();

    static {
        RESTORE.put(
                "me/cortex/voxy/client/core/rendering/hierachical/AsyncNodeManager.class",
                "8e42836346fa9a4400aa749d2016f538a43198c5");
        RESTORE.put(
                "me/cortex/voxy/client/core/rendering/hierachical/NodeManager.class",
                "5f1b53feff15b4f4d198d4d6c6d893992aff4c00");
        RESTORE.put(
                "me/cortex/voxy/client/core/rendering/building/RenderGenerationService.class",
                "d6b57cb1675e6a167a3f2105e81814c9ee0ca444");
        RESTORE.put(
                "me/cortex/voxy/client/VoxyClientInstance.class",
                "9a782cc730b82da063c9af79d9d2c2849addf274");
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            throw new IllegalArgumentException("PatchIpa10z <ipa10y.jar> <ipa10z.jar> <baseline-dir>");
        }
        Path baseline = Path.of(args[2]);
        byte[] jar;
        try (ZipFile zip = new ZipFile(Path.of(args[0]).toFile())) {
            jar = rewrite(zip, baseline);
        }
        Path out = Path.of(args[1]);
        Files.createDirectories(out.getParent() == null ? Path.of(".") : out.getParent());
        Files.write(out, jar);
        System.out.println("wrote " + out + " restored " + RESTORE.size() + " classes from " + baseline);
    }

    private static byte[] rewrite(ZipFile zip, Path baseline) throws Exception {
        Map<String, byte[]> restored = new LinkedHashMap<String, byte[]>();
        for (Map.Entry<String, String> entry : RESTORE.entrySet()) {
            Path file = baseline.resolve(entry.getKey());
            byte[] data = Files.readAllBytes(file);
            String digest = sha1(data);
            if (!digest.equals(entry.getValue())) {
                throw new IllegalStateException(entry.getKey() + " sha1 " + digest + " != " + entry.getValue());
            }
            restored.put(entry.getKey(), data);
        }
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        try (ZipOutputStream zout = new ZipOutputStream(raw)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                byte[] data;
                try (InputStream input = zip.getInputStream(entry)) {
                    data = input.readAllBytes();
                }
                byte[] replacement = restored.remove(entry.getName());
                if (replacement != null) {
                    if (java.util.Arrays.equals(data, replacement)) {
                        throw new IllegalStateException(entry.getName() + " was already the baseline bytes");
                    }
                    data = replacement;
                } else {
                    data = bump(entry.getName(), data);
                }
                ZipEntry out = new ZipEntry(entry.getName());
                zout.putNextEntry(out);
                zout.write(data);
                zout.closeEntry();
            }
        }
        if (!restored.isEmpty()) {
            throw new IllegalStateException("baseline classes missing from the source jar: " + restored.keySet());
        }
        return raw.toByteArray();
    }

    private static byte[] bump(String name, byte[] data) {
        if (!name.equals("META-INF/MANIFEST.MF") && !name.equals("META-INF/neoforge.mods.toml")) {
            return data;
        }
        String text = new String(data, StandardCharsets.UTF_8);
        String bumped = text.replace("ipa10y", "ipa10z");
        if (bumped.contains("ipa10y") || !bumped.contains("ipa10z")) {
            throw new IllegalStateException(name + " was not bumped to ipa10z");
        }
        return bumped.getBytes(StandardCharsets.UTF_8);
    }

    private static String sha1(byte[] data) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-1").digest(data);
        StringBuilder text = new StringBuilder();
        for (byte value : digest) {
            text.append(String.format("%02x", value));
        }
        return text.toString();
    }
}
