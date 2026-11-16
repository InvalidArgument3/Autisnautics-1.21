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
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Patches the ipa10o jar into ipa10p. */
public final class PatchIpa10p {
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            throw new IllegalArgumentException("PatchIpa10p <ipa10o.jar> <ipa10p.jar> <extra.class>...");
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
        String path = file.toString().replace('\\', '/');
        if (path.contains("/client/")) {
            return "me/cortex/voxy/client/" + name;
        }
        if (name.startsWith("Ring") || name.startsWith("Terrain") || name.startsWith("Column")) {
            return "me/cortex/voxy/server/" + name;
        }
        throw new IllegalStateException("unexpected class " + file);
    }

    private static byte[] rewrite(ZipFile zip, Map<String, byte[]> extras) throws IOException {
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        try (ZipOutputStream zout = new ZipOutputStream(raw)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                byte[] data;
                try (InputStream input = zip.getInputStream(entry)) {
                    data = input.readAllBytes();
                }
                byte[] replacement = extras.remove(entry.getName());
                byte[] next = replacement != null ? replacement : bump(entry.getName(), data);
                if (entry.getName().equals("me/cortex/voxy/common/network/TerrainSyncClientHandler.class")) {
                    next = patchHandler(next);
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
            }
        }
        return raw.toByteArray();
    }

    private static byte[] patchHandler(byte[] data) {
        ClassNode node = new ClassNode();
        new ClassReader(data).accept(node, 0);
        boolean level = false;
        boolean report = false;
        for (MethodNode method : node.methods) {
            if (method.name.equals("tickDrainApplyQueue")) {
                for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; ) {
                    AbstractInsnNode next = insn.getNext();
                    if (insn instanceof FieldInsnNode field && field.getOpcode() == Opcodes.GETFIELD && field.name.equals("level") && field.owner.equals("net/minecraft/client/Minecraft")) {
                        method.instructions.set(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "me/cortex/voxy/client/TerrainReceive", "acceptLevel", "(Ljava/lang/Object;)Lnet/minecraft/world/level/Level;", false));
                        level = true;
                    }
                    insn = next;
                }
            }
            if (method.name.equals("report")) {
                for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; ) {
                    AbstractInsnNode next = insn.getNext();
                    if (insn instanceof FieldInsnNode field && field.name.equals("DEBUG")) {
                        method.instructions.set(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "me/cortex/voxy/client/TerrainClientStats", "enabled", "()Z", false));
                        report = true;
                    }
                    if (insn instanceof LdcInsnNode ldc && ldc.cst instanceof String text && text.startsWith("[terrain-sync][client-v2]")) {
                        ldc.cst = "[terrain-sync-client]";
                    }
                    if (insn instanceof MethodInsnNode call && call.name.equals("status") && call.owner.equals("me/cortex/voxy/common/network/TerrainSyncClientHandler")) {
                        call.owner = "me/cortex/voxy/client/TerrainClientStats";
                        call.name = "line";
                        call.desc = "()Ljava/lang/String;";
                    }
                    insn = next;
                }
            }
        }
        if (!level || !report) {
            throw new IllegalStateException("client handler patch missed level=" + level + " report=" + report);
        }
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private static byte[] bump(String name, byte[] data) {
        if (!name.equals("META-INF/MANIFEST.MF") && !name.equals("META-INF/neoforge.mods.toml")) {
            return data;
        }
        String text = new String(data, StandardCharsets.UTF_8);
        String bumped = text.replace("ipa10o", "ipa10p");
        if (bumped.contains("ipa10o") || !bumped.contains("ipa10p")) {
            throw new IllegalStateException(name + " was not bumped to ipa10p");
        }
        return bumped.getBytes(StandardCharsets.UTF_8);
    }
}
