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
import org.objectweb.asm.Handle;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Turns the ipa10x jar into ipa10y. Terrain-sync registration and tick entry
 * points return immediately, and the streaming hooks in the local renderer are
 * removed.
 */
public final class PatchIpa10y {
    private static final String VIS = "me/cortex/voxy/client/TerrainVisibility";
    private static final String STATS = "me/cortex/voxy/client/TerrainRenderStats";

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException("PatchIpa10y <ipa10x.jar> <ipa10y.jar>");
        }
        byte[] jar;
        try (ZipFile zip = new ZipFile(Path.of(args[0]).toFile())) {
            jar = rewrite(zip);
        }
        Path out = Path.of(args[1]);
        Files.createDirectories(out.getParent() == null ? Path.of(".") : out.getParent());
        Files.write(out, jar);
        System.out.println("wrote " + out);
    }

    private static byte[] rewrite(ZipFile zip) throws IOException {
        Map<String, String[]> gates = new LinkedHashMap<String, String[]>();
        gates.put("me/cortex/voxy/common/network/VoxyNetworking.class", new String[] {"register", "registerPayloads"});
        gates.put("me/cortex/voxy/server/VoxyServerChunkSync.class", new String[] {"register"});
        gates.put("me/cortex/voxy/server/VoxyServerGeneratedChunkWalker.class", new String[] {"register"});
        gates.put("me/cortex/voxy/server/ServerTerrainTransport.class", new String[] {"register"});
        gates.put("me/cortex/voxy/server/ServerTerrainSyncBudget.class", new String[] {"register"});
        gates.put("me/cortex/voxy/server/VoxyTerrainCommands.class", new String[] {"register"});
        gates.put("me/cortex/voxy/VoxyNeoForgeClientEvents.class", new String[] {"onClientTickPost"});
        gates.put("me/cortex/voxy/client/VoxyClientNetworkHooks.class", new String[] {"onClientTick"});
        gates.put("me/cortex/voxy/common/network/TerrainSyncClientHandler.class", new String[] {"tickDrainApplyQueue", "handleSectionSync", "handleSectionSyncBatch"});
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        try (ZipOutputStream zout = new ZipOutputStream(raw)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                byte[] data;
                try (InputStream input = zip.getInputStream(entry)) {
                    data = input.readAllBytes();
                }
                byte[] next = bump(entry.getName(), data);
                String[] methods = gates.get(entry.getName());
                if (methods != null) {
                    next = gate(next, methods);
                } else if (entry.getName().equals("me/cortex/voxy/client/core/rendering/hierachical/AsyncNodeManager.class")
                        || entry.getName().equals("me/cortex/voxy/client/core/rendering/hierachical/NodeManager.class")
                        || entry.getName().equals("me/cortex/voxy/client/core/rendering/building/RenderGenerationService.class")) {
                    next = stripStreamingCalls(next);
                } else if (entry.getName().equals("me/cortex/voxy/client/VoxyClientInstance.class")) {
                    next = restoreThreadFloor(next);
                } else if (entry.getName().equals("me/cortex/voxy/VoxyMod.class")) {
                    next = unlinkStreaming(next);
                }
                ZipEntry copy = new ZipEntry(entry.getName());
                copy.setTime(entry.getTime());
                zout.putNextEntry(copy);
                zout.write(next);
                zout.closeEntry();
            }
        }
        return raw.toByteArray();
    }

    private static byte[] gate(byte[] data, String[] names) {
        ClassNode node = read(data);
        for (String name : names) {
            boolean found = false;
            for (MethodNode method : node.methods) {
                if (!method.name.equals(name) || !method.desc.endsWith(")V")) {
                    continue;
                }
                method.instructions.insert(new InsnNode(Opcodes.RETURN));
                found = true;
            }
            if (!found) {
                throw new IllegalStateException(node.name + " has no void " + name);
            }
        }
        return write(node);
    }

    private static byte[] stripStreamingCalls(byte[] data) {
        ClassNode node = read(data);
        int removed = 0;
        for (MethodNode method : node.methods) {
            AbstractInsnNode insn = method.instructions.getFirst();
            while (insn != null) {
                AbstractInsnNode next = insn.getNext();
                if (insn instanceof MethodInsnNode call && (VIS.equals(call.owner) || STATS.equals(call.owner))) {
                    removeCall(method, call);
                    removed++;
                    insn = method.instructions.getFirst();
                    continue;
                }
                insn = next;
            }
        }
        if (removed == 0) {
            throw new IllegalStateException(node.name + " had no streaming calls to remove");
        }
        return write(node);
    }

    private static void removeCall(MethodNode method, MethodInsnNode call) {
        InsnList list = method.instructions;
        if (call.desc.equals("(Lme/cortex/voxy/client/core/rendering/SectionUpdateRouter;)V")) {
            AbstractInsnNode field = previousReal(call);
            AbstractInsnNode load = previousReal(field);
            if (!(field instanceof FieldInsnNode) || load == null) {
                throw new IllegalStateException("router bind shape");
            }
            list.remove(load);
            list.remove(field);
            list.remove(call);
            return;
        }
        AbstractInsnNode arg = previousReal(call);
        if (arg == null) {
            throw new IllegalStateException("missing argument for " + call.name);
        }
        list.remove(arg);
        list.remove(call);
    }

    private static AbstractInsnNode previousReal(AbstractInsnNode insn) {
        AbstractInsnNode prev = insn.getPrevious();
        while (prev != null && prev.getOpcode() < 0) {
            prev = prev.getPrevious();
        }
        return prev;
    }

    /** Drop the calls that load the streaming registrars. Local setup stays. */
    private static byte[] unlinkStreaming(byte[] data) {
        ClassNode node = read(data);
        int removed = 0;
        for (MethodNode method : node.methods) {
            AbstractInsnNode insn = method.instructions.getFirst();
            while (insn != null) {
                AbstractInsnNode next = insn.getNext();
                if (insn instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKESTATIC && "register".equals(call.name)
                        && (call.owner.equals("me/cortex/voxy/common/network/VoxyNetworking")
                        || call.owner.equals("me/cortex/voxy/server/VoxyServerChunkSync")
                        || call.owner.equals("me/cortex/voxy/server/VoxyServerGeneratedChunkWalker"))) {
                    if (call.desc.contains("IEventBus")) {
                        AbstractInsnNode arg = previousReal(call);
                        method.instructions.remove(arg);
                    }
                    method.instructions.remove(call);
                    removed++;
                    insn = method.instructions.getFirst();
                    continue;
                }
                if (insn instanceof InvokeDynamicInsnNode dynamic && references(dynamic, "me/cortex/voxy/server/VoxyTerrainCommands")) {
                    AbstractInsnNode bus = previousReal(insn);
                    AbstractInsnNode listener = next;
                    AbstractInsnNode after = listener.getNext();
                    method.instructions.remove(bus);
                    method.instructions.remove(insn);
                    method.instructions.remove(listener);
                    removed++;
                    insn = after;
                    continue;
                }
                insn = next;
            }
        }
        if (removed != 4) {
            StringBuilder seen = new StringBuilder();
            for (MethodNode method : node.methods) {
                for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKESTATIC) {
                        seen.append(call.owner).append('.').append(call.name).append(call.desc).append('\n');
                    }
                    if (insn instanceof InvokeDynamicInsnNode dynamic) {
                        seen.append("dyn ").append(dynamic.name);
                        for (Object arg : dynamic.bsmArgs) {
                            seen.append(" | ").append(arg.getClass().getSimpleName()).append(':').append(arg);
                        }
                        seen.append('\n');
                    }
                }
            }
            throw new IllegalStateException("removed " + removed + " streaming links from VoxyMod, expected 4\n" + seen);
        }
        return write(node);
    }

    private static boolean references(InvokeDynamicInsnNode dynamic, String owner) {
        for (Object arg : dynamic.bsmArgs) {
            if (arg instanceof Handle handle && owner.equals(handle.getOwner())) {
                return true;
            }
        }
        return false;
    }

    private static byte[] restoreThreadFloor(byte[] data) {
        ClassNode node = read(data);
        boolean restored = false;
        for (MethodNode method : node.methods) {
            if (!"updateDedicatedThreads".equals(method.name)) {
                continue;
            }
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn.getOpcode() == Opcodes.ICONST_3) {
                    method.instructions.set(insn, new InsnNode(Opcodes.ICONST_1));
                    restored = true;
                    break;
                }
            }
        }
        if (!restored) {
            throw new IllegalStateException("thread floor was not iconst_3");
        }
        return write(node);
    }

    private static ClassNode read(byte[] data) {
        ClassNode node = new ClassNode();
        new ClassReader(data).accept(node, 0);
        return node;
    }

    private static byte[] write(ClassNode node) {
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
        String bumped = text.replace("ipa10x", "ipa10y");
        if (bumped.contains("ipa10x") || !bumped.contains("ipa10y")) {
            throw new IllegalStateException(name + " was not bumped to ipa10y");
        }
        return bumped.getBytes(StandardCharsets.UTF_8);
    }
}
