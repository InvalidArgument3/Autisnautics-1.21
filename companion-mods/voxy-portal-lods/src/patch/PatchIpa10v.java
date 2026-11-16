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
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Patches the ipa10u jar into ipa10v. */
public final class PatchIpa10v {
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            throw new IllegalArgumentException("PatchIpa10v <ipa10u.jar> <ipa10v.jar> <extra.class>...");
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
                if (replacement == null && entry.getName().equals("me/cortex/voxy/server/ServerTerrainTransport.class")) {
                    next = noteAcks(next);
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

    /** Read the in-flight frame before the queue consumes the ack. */
    private static byte[] noteAcks(byte[] data) {
        ClassNode node = new ClassNode();
        new ClassReader(data).accept(node, 0);
        boolean found = false;
        for (MethodNode method : node.methods) {
            if (!method.name.equals("ack") || !method.desc.contains("ServerPlayer")) {
                continue;
            }
            InsnList call = new InsnList();
            call.add(new VarInsnNode(Opcodes.ALOAD, 0));
            call.add(new VarInsnNode(Opcodes.LLOAD, 1));
            call.add(new VarInsnNode(Opcodes.ILOAD, 3));
            call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "me/cortex/voxy/server/TerrainDelivery", "beforeAck", "(Ljava/lang/Object;JI)V", false));
            method.instructions.insert(call);
            found = true;
        }
        if (!found) {
            throw new IllegalStateException("server ack was not hooked");
        }
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
                    return "java/lang/Object";
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
        String bumped = text.replace("ipa10u", "ipa10v");
        if (bumped.contains("ipa10u") || !bumped.contains("ipa10v")) {
            throw new IllegalStateException(name + " was not bumped to ipa10v");
        }
        return bumped.getBytes(StandardCharsets.UTF_8);
    }
}
