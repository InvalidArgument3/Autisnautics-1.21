import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Patches the ipa10m jar into ipa10n and registers the optional coverage packet. */
public final class PatchIpa10n implements Opcodes {
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            throw new IllegalArgumentException("PatchIpa10n <ipa10m.jar> <ipa10n.jar> <extra.class>...");
        }
        Map<String, byte[]> extras = new LinkedHashMap<String, byte[]>();
        for (int i = 2; i < args.length; i++) {
            Path file = Path.of(args[i]);
            extras.put(className(file), Files.readAllBytes(file));
        }
        byte[] jar;
        try (ZipFile zip = new ZipFile(Path.of(args[0]).toFile())) {
            jar = rewrite(zip, extras);
        }
        Path out = Path.of(args[1]);
        Files.createDirectories(out.getParent() == null ? Path.of(".") : out.getParent());
        Files.write(out, jar);
        System.out.println("wrote " + out);
    }

    private static String className(Path file) {
        String name = file.getFileName().toString();
        if (name.startsWith("ColumnCoveragePayload")) {
            return "me/cortex/voxy/common/network/" + name;
        }
        if (name.startsWith("ClientColumnCoverage")) {
            return "me/cortex/voxy/client/" + name;
        }
        if (name.startsWith("Terrain") || name.startsWith("Column")) {
            return "me/cortex/voxy/server/" + name;
        }
        throw new IllegalStateException("unexpected class " + file);
    }

    private static byte[] rewrite(ZipFile zip, Map<String, byte[]> extras) throws IOException {
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        int patched = 0;
        try (ZipOutputStream zout = new ZipOutputStream(raw)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                byte[] data;
                try (InputStream input = zip.getInputStream(entry)) {
                    data = input.readAllBytes();
                }
                byte[] replacement = extras.remove(entry.getName());
                byte[] next = replacement != null ? replacement : patch(entry.getName(), data);
                if (next != data) {
                    patched++;
                }
                ZipEntry copy = new ZipEntry(entry.getName());
                copy.setTime(entry.getTime());
                zout.putNextEntry(copy);
                zout.write(next);
                zout.closeEntry();
            }
            for (Map.Entry<String, byte[]> extra : extras.entrySet()) {
                ZipEntry copy = new ZipEntry(extra.getKey());
                zout.putNextEntry(copy);
                zout.write(extra.getValue());
                zout.closeEntry();
                patched++;
            }
        }
        if (patched < 4) {
            throw new IllegalStateException("expected coverage classes and the version files to change, patched " + patched);
        }
        return raw.toByteArray();
    }

    private static byte[] patch(String name, byte[] data) {
        if (name.equals("META-INF/MANIFEST.MF") || name.equals("META-INF/neoforge.mods.toml")) {
            String text = new String(data, StandardCharsets.UTF_8);
            String bumped = text.replace("ipa10m", "ipa10n");
            if (bumped.contains("ipa10m") || !bumped.contains("ipa10n")) {
                throw new IllegalStateException(name + " was not bumped to ipa10n");
            }
            return bumped.getBytes(StandardCharsets.UTF_8);
        }
        if (name.equals("me/cortex/voxy/common/network/VoxyNetworking.class")) {
            return insertBeforeLastReturn(data, "registerPayloads", new InsnList() {{
                add(new VarInsnNode(ALOAD, 1));
                add(new MethodInsnNode(INVOKESTATIC, "me/cortex/voxy/common/network/ColumnCoveragePayload", "register", "(Lnet/neoforged/neoforge/network/registration/PayloadRegistrar;)V", false));
            }});
        }
        if (name.equals("me/cortex/voxy/client/VoxyClientNetworkHooks.class")) {
            byte[] hooked = insertBeforeLastReturn(data, "onClientTick", call("onClientTick"));
            return insertBeforeLastReturn(hooked, "resetConnectionState", call("reset"));
        }
        return data;
    }

    private static InsnList call(String method) {
        InsnList list = new InsnList();
        list.add(new MethodInsnNode(INVOKESTATIC, "me/cortex/voxy/client/ClientColumnCoverage", method, "()V", false));
        return list;
    }

    private static byte[] insertBeforeLastReturn(byte[] data, String methodName, InsnList insert) {
        ClassNode node = new ClassNode();
        new ClassReader(data).accept(node, 0);
        MethodNode method = null;
        for (MethodNode candidate : node.methods) {
            if (methodName.equals(candidate.name)) {
                method = candidate;
            }
        }
        if (method == null) {
            throw new IllegalStateException("missing " + methodName);
        }
        AbstractInsnNode lastReturn = null;
        for (AbstractInsnNode insn : method.instructions) {
            if (insn.getOpcode() == RETURN) {
                lastReturn = insn;
            }
        }
        if (lastReturn == null) {
            throw new IllegalStateException(methodName + " has no return");
        }
        method.instructions.insertBefore(lastReturn, insert);
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }
}
