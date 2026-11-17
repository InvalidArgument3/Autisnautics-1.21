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
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Patches the ipa10w jar into ipa10x. */
public final class PatchIpa10x {
    private static final String VIS = "me/cortex/voxy/client/TerrainVisibility";
    private static final String STATS = "me/cortex/voxy/client/TerrainRenderStats";
    private static final String NODE = "me/cortex/voxy/client/core/rendering/hierachical/AsyncNodeManager";
    private static final String ROUTER = "me/cortex/voxy/client/core/rendering/SectionUpdateRouter";
    private static final String MESH = "me/cortex/voxy/client/core/rendering/building/RenderGenerationService";

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            throw new IllegalArgumentException("PatchIpa10x <ipa10w.jar> <ipa10x.jar> <extra.class>...");
        }
        Map<String, byte[]> extras = new LinkedHashMap<String, byte[]>();
        for (int i = 2; i < args.length; i++) {
            extras.put(className(Path.of(args[i])), Files.readAllBytes(Path.of(args[i])));
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
        if (path.contains("/common/network/")) {
            return "me/cortex/voxy/common/network/" + name;
        }
        if (path.contains("/client/")) {
            return "me/cortex/voxy/client/" + name;
        }
        if (name.startsWith("Ring") || name.startsWith("Terrain") || name.startsWith("Column") || name.startsWith("Server")) {
            return "me/cortex/voxy/server/" + name;
        }
        throw new IllegalStateException("unexpected class " + file);
    }

    private static byte[] rewrite(ZipFile zip, Map<String, byte[]> extras) throws IOException {
        boolean node = false;
        boolean manager = false;
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
                if (replacement == null && entry.getName().equals(NODE + ".class")) {
                    next = hookNodeManager(next);
                    node = true;
                } else if (replacement == null && entry.getName().equals("me/cortex/voxy/client/core/rendering/hierachical/NodeManager.class")) {
                    next = hookRemoval(next);
                    manager = true;
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
        if (!node || !manager) {
            throw new IllegalStateException("draw hooks were not installed");
        }
        return raw.toByteArray();
    }

    private static byte[] hookNodeManager(byte[] data) {
        ClassNode node = new ClassNode();
        new ClassReader(data).accept(node, 0);
        boolean bound = false;
        boolean dirtied = false;
        for (MethodNode method : node.methods) {
            if ("<init>".equals(method.name)) {
                AbstractInsnNode superCall = null;
                AbstractInsnNode routerStore = null;
                for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                    if (superCall == null && insn instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKESPECIAL) {
                        superCall = insn;
                    }
                    if (insn instanceof FieldInsnNode field && field.getOpcode() == Opcodes.PUTFIELD && "router".equals(field.name)) {
                        routerStore = insn;
                    }
                }
                if (superCall == null || routerStore == null) {
                    throw new IllegalStateException("node manager constructor shape changed");
                }
                InsnList hold = new InsnList();
                hold.add(new VarInsnNode(Opcodes.ALOAD, 3));
                hold.add(new MethodInsnNode(Opcodes.INVOKESTATIC, VIS, "holdService", "(L" + MESH + ";)V", false));
                method.instructions.insert(superCall, hold);
                InsnList bind = new InsnList();
                bind.add(new VarInsnNode(Opcodes.ALOAD, 0));
                bind.add(new FieldInsnNode(Opcodes.GETFIELD, NODE, "router", "L" + ROUTER + ";"));
                bind.add(new MethodInsnNode(Opcodes.INVOKESTATIC, VIS, "bindRouter", "(L" + ROUTER + ";)V", false));
                method.instructions.insert(routerStore, bind);
                method.maxStack = Math.max(method.maxStack, 3);
                bound = true;
            }
            if ("worldEvent".equals(method.name)) {
                InsnList call = new InsnList();
                call.add(new VarInsnNode(Opcodes.ALOAD, 1));
                call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, VIS, "onDirty", "(Lme/cortex/voxy/common/world/WorldSection;)V", false));
                method.instructions.insert(call);
                method.maxStack = Math.max(method.maxStack, 2);
                dirtied = true;
            }
        }
        if (!bound || !dirtied) {
            throw new IllegalStateException("node manager hooks missing");
        }
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private static byte[] hookRemoval(byte[] data) {
        ClassNode node = new ClassNode();
        new ClassReader(data).accept(node, 0);
        boolean found = false;
        for (MethodNode method : node.methods) {
            if (!"removeNodeGeometry".equals(method.name)) {
                continue;
            }
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call) || !"removed".equals(call.name) || !STATS.equals(call.owner)) {
                    continue;
                }
                InsnList replacement = new InsnList();
                replacement.add(new VarInsnNode(Opcodes.LLOAD, 1));
                replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, STATS, "removed", "(J)V", false));
                method.instructions.insert(insn, replacement);
                method.instructions.remove(insn);
                method.maxStack = Math.max(method.maxStack, 3);
                found = true;
                break;
            }
        }
        if (!found) {
            throw new IllegalStateException("geometry removal hook missing");
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
        String bumped = text.replace("ipa10w", "ipa10x");
        if (bumped.contains("ipa10w") || !bumped.contains("ipa10x")) {
            throw new IllegalStateException(name + " was not bumped to ipa10x");
        }
        return bumped.getBytes(StandardCharsets.UTF_8);
    }
}
