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
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Patches the ipa10q jar into ipa10r. */
public final class PatchIpa10r {
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            throw new IllegalArgumentException("PatchIpa10r <ipa10q.jar> <ipa10r.jar> <extra.class>...");
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
                    next = patch(entry.getName(), next);
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

    private static byte[] patch(String name, byte[] data) {
        if (name.equals("me/cortex/voxy/common/network/VoxySessionSupport.class")) {
            return acceptProtocol3(data);
        }
        if (name.equals("me/cortex/voxy/client/VoxyClientNetworkHooks.class")) {
            return helloProtocol3(data);
        }
        if (name.equals("me/cortex/voxy/common/network/TerrainSyncClientHandler.class")) {
            return expandOnNetworkThread(data);
        }
        if (name.equals("me/cortex/voxy/common/config/VoxyConfig.class")) {
            return addPlayerByteField(data);
        }
        return data;
    }

    private static byte[] acceptProtocol3(byte[] data) {
        ClassNode node = new ClassNode();
        new ClassReader(data).accept(node, 0);
        boolean found = false;
        for (MethodNode method : node.methods) {
            if (!method.name.equals("setClientProtocol")) {
                continue;
            }
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof JumpInsnNode jump) || jump.getOpcode() != Opcodes.IF_ICMPNE) {
                    continue;
                }
                AbstractInsnNode iconst = jump.getPrevious();
                AbstractInsnNode load = iconst == null ? null : iconst.getPrevious();
                if (iconst == null || iconst.getOpcode() != Opcodes.ICONST_2 || !(load instanceof VarInsnNode)) {
                    continue;
                }
                method.instructions.insert(load, new MethodInsnNode(Opcodes.INVOKESTATIC, "me/cortex/voxy/common/network/SectionWire", "accepted", "(I)Z", false));
                method.instructions.insert(load.getNext(), new JumpInsnNode(Opcodes.IFEQ, jump.label));
                method.instructions.remove(iconst);
                method.instructions.remove(jump);
                found = true;
                break;
            }
        }
        if (!found) {
            throw new IllegalStateException("setClientProtocol was not patched");
        }
        return frames(node);
    }

    private static byte[] helloProtocol3(byte[] data) {
        ClassNode node = new ClassNode();
        new ClassReader(data).accept(node, 0);
        boolean found = false;
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call) || !call.name.equals("<init>") || !call.owner.equals("me/cortex/voxy/common/network/VoxyHelloPayload")) {
                    continue;
                }
                AbstractInsnNode distance = call.getPrevious();
                AbstractInsnNode protocol = distance == null ? null : distance.getPrevious();
                if (protocol != null && protocol.getOpcode() == Opcodes.ICONST_2) {
                    method.instructions.set(protocol, new InsnNode(Opcodes.ICONST_3));
                    found = true;
                }
            }
        }
        if (!found) {
            throw new IllegalStateException("client hello was not bumped to protocol 3");
        }
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private static byte[] expandOnNetworkThread(byte[] data) {
        ClassNode node = new ClassNode();
        new ClassReader(data).accept(node, 0);
        boolean found = false;
        for (MethodNode method : node.methods) {
            if (!method.name.equals("handleSectionSyncBatch")) {
                continue;
            }
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof VarInsnNode load) || load.getOpcode() != Opcodes.ALOAD || load.var != 0) {
                    continue;
                }
                method.instructions.insert(load, new MethodInsnNode(Opcodes.INVOKESTATIC, "me/cortex/voxy/common/network/SectionWire", "expand", "(Lme/cortex/voxy/common/network/SectionSyncBatchPayload;)Lme/cortex/voxy/common/network/SectionSyncBatchPayload;", false));
                found = true;
                break;
            }
        }
        if (!found) {
            throw new IllegalStateException("batch handler was not patched");
        }
        return frames(node);
    }

    private static byte[] addPlayerByteField(byte[] data) {
        ClassNode node = new ClassNode();
        new ClassReader(data).accept(node, 0);
        for (FieldNode field : node.fields) {
            if (field.name.equals("serverTerrainSyncPlayerBytesPerTick")) {
                return data;
            }
        }
        node.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "serverTerrainSyncPlayerBytesPerTick", "I", null, null));
        boolean found = false;
        for (MethodNode method : node.methods) {
            if (!method.name.equals("<init>")) {
                continue;
            }
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn.getOpcode() == Opcodes.RETURN) {
                    method.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 0));
                    method.instructions.insertBefore(insn, new LdcInsnNode(Integer.valueOf(1048576)));
                    method.instructions.insertBefore(insn, new FieldInsnNode(Opcodes.PUTFIELD, "me/cortex/voxy/common/config/VoxyConfig", "serverTerrainSyncPlayerBytesPerTick", "I"));
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            throw new IllegalStateException("config default was not written");
        }
        return frames(node);
    }

    private static byte[] frames(ClassNode node) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES) {
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                try {
                    Class<?> first = Class.forName(type1.replace('/', '.'), false, Thread.currentThread().getContextClassLoader());
                    Class<?> second = Class.forName(type2.replace('/', '.'), false, Thread.currentThread().getContextClassLoader());
                    if (first.isAssignableFrom(second)) {
                        return type1;
                    }
                    if (second.isAssignableFrom(first)) {
                        return type2;
                    }
                    if (first.isInterface() || second.isInterface()) {
                        return "java/lang/Object";
                    }
                    do {
                        first = first.getSuperclass();
                    } while (first != null && !first.isAssignableFrom(second));
                    return first == null ? "java/lang/Object" : first.getName().replace('.', '/');
                } catch (Throwable failure) {
                    return "java/lang/Object";
                }
            }
        };
        node.accept(writer);
        return writer.toByteArray();
    }

    private static byte[] bump(String name, byte[] data) {
        if (!name.equals("META-INF/MANIFEST.MF") && !name.equals("META-INF/neoforge.mods.toml")) {
            return data;
        }
        String text = new String(data, StandardCharsets.UTF_8);
        String bumped = text.replace("ipa10q", "ipa10r");
        if (bumped.contains("ipa10q") || !bumped.contains("ipa10r")) {
            throw new IllegalStateException(name + " was not bumped to ipa10r");
        }
        return bumped.getBytes(StandardCharsets.UTF_8);
    }
}
