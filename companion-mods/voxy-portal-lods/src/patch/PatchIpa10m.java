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
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Patches the ipa10l jar into ipa10m. The ring overlaps the view-distance square by one chunk. */
public final class PatchIpa10m implements Opcodes {
    private static final String POLICY = "me/cortex/voxy/server/TerrainStreamPolicy";
    private static final String CONFIG = "me/cortex/voxy/common/config/VoxyConfig";

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            throw new IllegalArgumentException("PatchIpa10m <ipa10l.jar> <ipa10m.jar> <extra.class>...");
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
        if (name.startsWith("Ring") || name.startsWith("Terrain") || name.startsWith("Pending")) {
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
        if (patched < 6) {
            throw new IllegalStateException("expected the ring classes, config, and version files to change, patched " + patched);
        }
        return raw.toByteArray();
    }

    private static byte[] patch(String name, byte[] data) {
        if (name.equals("META-INF/MANIFEST.MF") || name.equals("META-INF/neoforge.mods.toml")) {
            String text = new String(data, StandardCharsets.UTF_8);
            String bumped = text.replace("ipa10l", "ipa10m");
            if (bumped.contains("ipa10l") || !bumped.contains("ipa10m")) {
                throw new IllegalStateException(name + " was not bumped to ipa10m");
            }
            return bumped.getBytes(StandardCharsets.UTF_8);
        }
        if (name.equals(CONFIG + ".class")) {
            return addDebugField(data);
        }
        if (name.equals("me/cortex/voxy/server/ServerTerrainTransport.class")
                || name.equals("me/cortex/voxy/server/VoxyServerChunkSync.class")
                || name.equals("me/cortex/voxy/server/VoxyServerGeneratedChunkWalker.class")) {
            return retargetInner(name, data);
        }
        return data;
    }

    private static byte[] addDebugField(byte[] data) {
        ClassNode node = read(data);
        for (FieldNode field : node.fields) {
            if ("serverTerrainSyncDebug".equals(field.name)) {
                return data;
            }
        }
        node.fields.add(new FieldNode(ACC_PUBLIC, "serverTerrainSyncDebug", "Z", null, null));
        return write(node);
    }

    private static byte[] retargetInner(String name, byte[] data) {
        ClassNode node = read(data);
        int renamed = 0;
        int widened = 0;
        for (MethodNode method : node.methods) {
            if ("valid".equals(method.name) || "onChunkLoad".equals(method.name)) {
                for (AbstractInsnNode insn : method.instructions) {
                    if (insn instanceof MethodInsnNode call
                            && call.getOpcode() == INVOKESTATIC
                            && POLICY.equals(call.owner)
                            && "innerRadius".equals(call.name)
                            && "(Ljava/lang/Object;)I".equals(call.desc)) {
                        call.name = "lodExclusiveInner";
                        renamed++;
                    }
                }
            }
            if ("processLevel".equals(method.name) || "hasAnyEligibleRecipients".equals(method.name)) {
                for (AbstractInsnNode insn : method.instructions) {
                    if (!(insn instanceof JumpInsnNode jump)) {
                        continue;
                    }
                    if (jump.getOpcode() == IF_ICMPGT && loadedFromInnerRadius(jump)) {
                        jump.setOpcode(IF_ICMPGE);
                        widened++;
                    }
                    if (jump.getOpcode() == IF_ICMPLE && previous(jump) instanceof MethodInsnNode call
                            && call.getOpcode() == INVOKESTATIC
                            && POLICY.equals(call.owner)
                            && "innerRadius".equals(call.name)) {
                        jump.setOpcode(IF_ICMPLT);
                        widened++;
                    }
                }
            }
        }
        int expectRename = name.endsWith("VoxyServerGeneratedChunkWalker.class") ? 0 : 1;
        int expectWiden = name.endsWith("VoxyServerGeneratedChunkWalker.class") ? 3 : 0;
        if (renamed != expectRename || widened != expectWiden) {
            throw new IllegalStateException(name + " renamed " + renamed + " widened " + widened);
        }
        return write(node);
    }

    /** The jump compares a max radius with a value just stored from innerRadius(player). */
    private static boolean loadedFromInnerRadius(JumpInsnNode jump) {
        AbstractInsnNode innerLoad = previous(jump);
        AbstractInsnNode maxLoad = previous(innerLoad);
        if (!(innerLoad instanceof VarInsnNode inner) || inner.getOpcode() != ILOAD) {
            return false;
        }
        if (!(maxLoad instanceof VarInsnNode) || maxLoad.getOpcode() != ILOAD) {
            return false;
        }
        AbstractInsnNode cursor = maxLoad;
        VarInsnNode store = null;
        for (int i = 0; i < 6 && cursor != null; i++) {
            cursor = previous(cursor);
            if (cursor instanceof VarInsnNode var && var.getOpcode() == ISTORE && var.var == inner.var) {
                store = var;
                break;
            }
        }
        if (store == null) {
            return false;
        }
        AbstractInsnNode call = previous(store);
        return call instanceof MethodInsnNode method
                && method.getOpcode() == INVOKESTATIC
                && POLICY.equals(method.owner)
                && "innerRadius".equals(method.name);
    }

    private static AbstractInsnNode previous(AbstractInsnNode insn) {
        AbstractInsnNode cursor = insn == null ? null : insn.getPrevious();
        while (cursor != null && cursor.getOpcode() == -1) {
            cursor = cursor.getPrevious();
        }
        return cursor;
    }

    private static ClassNode read(byte[] data) {
        ClassNode node = new ClassNode();
        new ClassReader(data).accept(node, 0);
        return node;
    }

    private static byte[] write(ClassNode node) {
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }
}
