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
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/** Patches the ipa10v jar into ipa10w. The ack hook from ipa10v is already present. */
public final class PatchIpa10w {
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            throw new IllegalArgumentException("PatchIpa10w <ipa10v.jar> <ipa10w.jar> <extra.class>...");
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
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        boolean threads = false;
        boolean requested = false;
        boolean completed = false;
        boolean uploaded = false;
        boolean tracker = false;
        boolean removed = false;
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
                if (replacement == null) {
                    if (entry.getName().equals("me/cortex/voxy/client/VoxyClientInstance.class")) {
                        next = raiseThreadFloor(next);
                        threads = true;
                    } else if (entry.getName().equals("me/cortex/voxy/client/core/rendering/building/RenderGenerationService.class")) {
                        next = prefix(next, "enqueueTask", "(J)V", longCall("requested"));
                        requested = true;
                    } else if (entry.getName().equals("me/cortex/voxy/client/core/rendering/hierachical/AsyncNodeManager.class")) {
                        next = prefix(next, "submitGeometryResult", "(Lme/cortex/voxy/client/core/rendering/building/BuiltSection;)V", objectCall("meshCompleted"));
                        completed = true;
                    } else if (entry.getName().equals("me/cortex/voxy/client/core/rendering/hierachical/NodeManager.class")) {
                        next = prefix(next, "processGeometryResult", "(Lme/cortex/voxy/client/core/rendering/building/BuiltSection;)V", objectCall("uploaded"));
                        next = prefix(next, "insertTopLevelNode", "(J)V", longCall("tracker"));
                        next = prefix(next, "removeNodeGeometry", "(J)V", noArg("removed"));
                        uploaded = true;
                        tracker = true;
                        removed = true;
                    }
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
        if (!threads || !requested || !completed || !uploaded || !tracker || !removed) {
            throw new IllegalStateException("render counters were not hooked");
        }
        return raw.toByteArray();
    }

    private static org.objectweb.asm.tree.InsnList longCall(String method) {
        org.objectweb.asm.tree.InsnList list = new org.objectweb.asm.tree.InsnList();
        list.add(new org.objectweb.asm.tree.VarInsnNode(Opcodes.LLOAD, 1));
        list.add(stat(method, "(J)V"));
        return list;
    }

    private static org.objectweb.asm.tree.InsnList objectCall(String method) {
        org.objectweb.asm.tree.InsnList list = new org.objectweb.asm.tree.InsnList();
        list.add(new org.objectweb.asm.tree.VarInsnNode(Opcodes.ALOAD, 1));
        list.add(stat(method, "(Ljava/lang/Object;)V"));
        return list;
    }

    private static org.objectweb.asm.tree.InsnList noArg(String method) {
        org.objectweb.asm.tree.InsnList list = new org.objectweb.asm.tree.InsnList();
        list.add(stat(method, "()V"));
        return list;
    }

    private static org.objectweb.asm.tree.MethodInsnNode stat(String method, String desc) {
        return new org.objectweb.asm.tree.MethodInsnNode(Opcodes.INVOKESTATIC, "me/cortex/voxy/client/TerrainRenderStats", method, desc, false);
    }

    /** Insert a stack-neutral call at the start of one method. Later frames stay valid. */
    private static byte[] prefix(byte[] data, String method, String desc, org.objectweb.asm.tree.InsnList call) {
        org.objectweb.asm.tree.ClassNode node = new org.objectweb.asm.tree.ClassNode();
        new ClassReader(data).accept(node, 0);
        boolean found = false;
        for (org.objectweb.asm.tree.MethodNode candidate : node.methods) {
            if (candidate.name.equals(method) && candidate.desc.equals(desc)) {
                candidate.instructions.insert(call);
                candidate.maxStack = Math.max(candidate.maxStack, 3);
                found = true;
            }
        }
        if (!found) {
            throw new IllegalStateException("missing " + method + desc);
        }
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    /** Keep at least three Voxy service threads after Sodium takes its builders. */
    private static byte[] raiseThreadFloor(byte[] data) {
        ClassReader reader = new ClassReader(data);
        ClassWriter writer = new ClassWriter(reader, 0);
        reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor visitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (!"updateDedicatedThreads".equals(name)) {
                    return visitor;
                }
                return new MethodVisitor(Opcodes.ASM9, visitor) {
                    private boolean replaced;

                    @Override
                    public void visitInsn(int opcode) {
                        if (!replaced && opcode == Opcodes.ICONST_1) {
                            replaced = true;
                            super.visitInsn(Opcodes.ICONST_3);
                            return;
                        }
                        super.visitInsn(opcode);
                    }

                    @Override
                    public void visitEnd() {
                        if (!replaced) {
                            throw new IllegalStateException("service thread floor was not raised");
                        }
                        super.visitEnd();
                    }
                };
            }
        }, 0);
        return writer.toByteArray();
    }

    private static byte[] bump(String name, byte[] data) {
        if (!name.equals("META-INF/MANIFEST.MF") && !name.equals("META-INF/neoforge.mods.toml")) {
            return data;
        }
        String text = new String(data, StandardCharsets.UTF_8);
        String bumped = text.replace("ipa10v", "ipa10w");
        if (bumped.contains("ipa10v") || !bumped.contains("ipa10w")) {
            throw new IllegalStateException(name + " was not bumped to ipa10w");
        }
        return bumped.getBytes(StandardCharsets.UTF_8);
    }
}
