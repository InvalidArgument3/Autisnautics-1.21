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
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Patches the ipa10r jar into ipa10s. */
public final class PatchIpa10s {
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            throw new IllegalArgumentException("PatchIpa10s <ipa10r.jar> <ipa10s.jar> <extra.class>...");
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
        if (name.equals("me/cortex/voxy/server/ServerTerrainTransport.class")) {
            return widenWindow(data);
        }
        if (name.equals("me/cortex/voxy/common/network/TerrainSyncClientHandler.class")) {
            return applyOffThread(data);
        }
        if (name.equals("me/cortex/voxy/common/network/ReliableTerrainQueue.class")) {
            return moreSendsPerTick(data);
        }
        return data;
    }

    /**
     * One player made {@code roundRobin.size() * 2} send attempts per tick, so
     * maxWork of 64 still moved two frames. That is about two sections batches
     * a tick no matter how large the in-flight window is.
     */
    private static byte[] moreSendsPerTick(byte[] data) {
        ClassNode node = new ClassNode();
        new ClassReader(data).accept(node, 0);
        boolean found = false;
        for (MethodNode method : node.methods) {
            if (!method.name.equals("tick") || !method.desc.contains("Ljava/util/function/Predicate;")) {
                continue;
            }
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn.getOpcode() != Opcodes.IMUL) {
                    continue;
                }
                AbstractInsnNode previous = insn.getPrevious();
                if (previous == null || previous.getOpcode() != Opcodes.ICONST_2) {
                    continue;
                }
                method.instructions.set(previous, new IntInsnNode(Opcodes.BIPUSH, 64));
                found = true;
            }
        }
        if (!found) {
            throw new IllegalStateException("send attempt cap was not raised");
        }
        return frames(node);
    }

    /** In-flight window was 2 frames. Owner cap was 128. Both stall a real client. */
    private static byte[] widenWindow(byte[] data) {
        ClassNode node = new ClassNode();
        new ClassReader(data).accept(node, 0);
        boolean found = false;
        for (MethodNode method : node.methods) {
            if (!method.name.equals("<clinit>")) {
                continue;
            }
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call) || !call.name.equals("<init>") || !call.owner.equals("me/cortex/voxy/common/network/ReliableTerrainQueue")) {
                    continue;
                }
                AbstractInsnNode windowBytes = call.getPrevious();
                AbstractInsnNode window = windowBytes == null ? null : windowBytes.getPrevious();
                AbstractInsnNode ownerBytes = window == null ? null : window.getPrevious();
                AbstractInsnNode owner = ownerBytes == null ? null : ownerBytes.getPrevious();
                AbstractInsnNode maxBytes = owner == null ? null : owner.getPrevious();
                AbstractInsnNode max = maxBytes == null ? null : maxBytes.getPrevious();
                if (!(window instanceof InsnNode) || window.getOpcode() != Opcodes.ICONST_2) {
                    throw new IllegalStateException("window constant was not iconst_2");
                }
                if (!(owner instanceof IntInsnNode) || ((IntInsnNode) owner).operand != 128) {
                    throw new IllegalStateException("owner cap was not 128");
                }
                if (!(max instanceof IntInsnNode) || ((IntInsnNode) max).operand != 512) {
                    throw new IllegalStateException("queue cap was not 512");
                }
                method.instructions.set(window, new IntInsnNode(Opcodes.BIPUSH, 48));
                ((IntInsnNode) owner).operand = 1024;
                ((IntInsnNode) max).operand = 2048;
                found = true;
            }
        }
        if (!found) {
            throw new IllegalStateException("reliable queue constructor was not patched");
        }
        return frames(node);
    }

    private static byte[] applyOffThread(byte[] data) {
        ClassNode node = new ClassNode();
        new ClassReader(data).accept(node, 0);
        boolean found = false;
        for (MethodNode method : node.methods) {
            if (!method.name.equals("tickDrainApplyQueue")) {
                continue;
            }
            method.instructions.clear();
            method.tryCatchBlocks.clear();
            if (method.localVariables != null) {
                method.localVariables.clear();
            }
            InsnList body = new InsnList();
            body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "me/cortex/voxy/client/ClientSectionApply", "drain", "()V", false));
            body.add(new InsnNode(Opcodes.RETURN));
            method.instructions.add(body);
            found = true;
        }
        if (!found) {
            throw new IllegalStateException("apply drain was not redirected");
        }
        return frames(node);
    }

    private static byte[] frames(ClassNode node) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES) {
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                try {
                    ClassLoader loader = Thread.currentThread().getContextClassLoader();
                    Class<?> first = Class.forName(type1.replace('/', '.'), false, loader);
                    Class<?> second = Class.forName(type2.replace('/', '.'), false, loader);
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
        String bumped = text.replace("ipa10r", "ipa10s");
        if (bumped.contains("ipa10r") || !bumped.contains("ipa10s")) {
            throw new IllegalStateException(name + " was not bumped to ipa10s");
        }
        return bumped.getBytes(StandardCharsets.UTF_8);
    }
}
