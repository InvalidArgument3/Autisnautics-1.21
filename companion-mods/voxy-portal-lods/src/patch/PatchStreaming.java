import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Patches ipa10j server terrain streaming into ipa10k. Other zip entries stay byte-identical. */
public final class PatchStreaming implements Opcodes {
    private static final String POLICY = "me/cortex/voxy/server/TerrainStreamPolicy";
    private static final String PLAYER = "net/minecraft/server/level/ServerPlayer";
    private static final String CURSOR = "me/cortex/voxy/server/VoxyServerGeneratedChunkWalker$WalkCursor";

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            throw new IllegalArgumentException("PatchStreaming <ipa10j.jar> <ipa10k.jar> <extra.class>...");
        }
        byte[][] extras = new byte[args.length - 2][];
        String[] extraNames = new String[args.length - 2];
        for (int i = 2; i < args.length; i++) {
            Path file = Path.of(args[i]);
            extras[i - 2] = Files.readAllBytes(file);
            extraNames[i - 2] = "me/cortex/voxy/server/" + file.getFileName();
        }
        byte[] jar;
        try (ZipFile zip = new ZipFile(Path.of(args[0]).toFile())) {
            jar = rewrite(zip, extraNames, extras);
        }
        Path out = Path.of(args[1]);
        Files.write(out, jar);
        System.out.println("wrote " + out);
    }

    private static byte[] rewrite(ZipFile zip, String[] extraNames, byte[][] extras) throws IOException {
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        int replaced = 0;
        try (ZipOutputStream zout = new ZipOutputStream(raw)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                byte[] data;
                try (InputStream input = zip.getInputStream(entry)) {
                    data = input.readAllBytes();
                }
                String name = entry.getName();
                for (String extraName : extraNames) {
                    if (name.equals(extraName)) {
                        throw new IllegalStateException(extraName + " already present");
                    }
                }
                byte[] next = patch(name, data);
                if (next != data) {
                    replaced++;
                }
                ZipEntry copy = new ZipEntry(name);
                copy.setTime(entry.getTime());
                zout.putNextEntry(copy);
                zout.write(next);
                zout.closeEntry();
            }
            for (int i = 0; i < extraNames.length; i++) {
                ZipEntry extra = new ZipEntry(extraNames[i]);
                extra.setTime(0L);
                zout.putNextEntry(extra);
                zout.write(extras[i]);
                zout.closeEntry();
            }
        }
        if (replaced != 8) {
            throw new IllegalStateException("replaced " + replaced + " entries, expected 8");
        }
        System.out.println("replaced entries " + replaced);
        return raw.toByteArray();
    }

    private static byte[] patch(String name, byte[] data) {
        if (name.equals("META-INF/MANIFEST.MF") || name.equals("META-INF/neoforge.mods.toml")) {
            String text = new String(data, StandardCharsets.UTF_8);
            if (!text.contains("ipa10j")) {
                throw new IllegalStateException(name + " has no ipa10j");
            }
            String bumped = text.replace("ipa10j", "ipa10k");
            if (bumped.contains("ipa10j")) {
                throw new IllegalStateException(name + " still says ipa10j");
            }
            return bumped.getBytes(StandardCharsets.UTF_8);
        }
        ClassVisitorFactory factory = switch (name) {
            case "me/cortex/voxy/common/config/VoxyConfig.class" -> ConfigPatcher::new;
            case "me/cortex/voxy/server/VoxyServerChunkSync.class" -> ChunkSyncPatcher::new;
            case "me/cortex/voxy/server/ServerTerrainTransport.class" -> TransportPatcher::new;
            case "me/cortex/voxy/server/ServerTerrainSyncBudget.class" -> BudgetPatcher::new;
            case "me/cortex/voxy/server/VoxyServerGeneratedChunkWalker.class" -> WalkerPatcher::new;
            case "me/cortex/voxy/common/network/ReliableTerrainQueue.class" -> QueuePatcher::new;
            default -> null;
        };
        if (factory == null) {
            return data;
        }
        ClassReader reader = new ClassReader(data);
        ClassWriter writer = new FrameWriter(reader);
        reader.accept(factory.create(writer), ClassReader.SKIP_FRAMES);
        System.out.println("patched " + name);
        return writer.toByteArray();
    }

    private interface ClassVisitorFactory {
        ClassVisitor create(ClassVisitor next);
    }

    private static final class FrameWriter extends ClassWriter {
        FrameWriter(ClassReader reader) {
            super(reader, ClassWriter.COMPUTE_FRAMES);
        }

        @Override
        protected String getCommonSuperClass(String left, String right) {
            try {
                return super.getCommonSuperClass(left, right);
            } catch (Throwable ignored) {
                return "java/lang/Object";
            }
        }
    }

    private static MethodVisitor capture(ClassVisitor dest, int access, String name, String descriptor, String signature, String[] exceptions, java.util.function.Consumer<MethodNode> edit) {
        MethodNode node = new MethodNode(ASM9, access, name, descriptor, signature, exceptions);
        return new MethodVisitor(ASM9, node) {
            @Override
            public void visitEnd() {
                super.visitEnd();
                edit.accept(node);
                String[] thrown = node.exceptions == null || node.exceptions.isEmpty() ? null : node.exceptions.toArray(String[]::new);
                node.accept(dest.visitMethod(node.access, node.name, node.desc, node.signature, thrown));
            }
        };
    }

    private static final class ConfigPatcher extends ClassVisitor {
        ConfigPatcher(ClassVisitor next) {
            super(ASM9, next);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            if (!"<init>".equals(name)) {
                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }
            return capture(cv, access, name, descriptor, signature, exceptions, ConfigPatcher::retune);
        }

        private static void retune(MethodNode node) {
            int changed = 0;
            for (AbstractInsnNode insn = node.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn.getOpcode() != PUTFIELD) {
                    continue;
                }
                FieldInsnNode field = (FieldInsnNode) insn;
                AbstractInsnNode previous = previous(insn);
                switch (field.name) {
                    case "serverTerrainSyncInnerRadiusChunks" -> {
                        ((IntInsnNode) previous).operand = 0;
                        changed++;
                    }
                    case "serverTerrainSyncBackgroundWalkEnabled" -> {
                        node.instructions.set(previous, new InsnNode(ICONST_1));
                        changed++;
                    }
                    case "serverTerrainSyncBackgroundTickInterval" -> {
                        node.instructions.set(previous, new InsnNode(ICONST_1));
                        changed++;
                    }
                    case "serverTerrainSyncMaxSectionsPerTick" -> {
                        ((IntInsnNode) previous).operand = 32;
                        changed++;
                    }
                    case "serverTerrainSyncMaxBytesPerTick" -> {
                        ((LdcInsnNode) previous).cst = Integer.valueOf(1048576);
                        changed++;
                    }
                    case "serverTerrainSyncBackgroundPauseMspt" -> {
                        ((LdcInsnNode) previous).cst = Float.valueOf(40.0f);
                        changed++;
                    }
                    default -> {
                    }
                }
            }
            if (changed != 6) {
                throw new IllegalStateException("retuned " + changed + " config defaults");
            }
        }
    }

    private static final class ChunkSyncPatcher extends ClassVisitor {
        ChunkSyncPatcher(ClassVisitor next) {
            super(ASM9, next);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            return capture(cv, access, name, descriptor, signature, exceptions, node -> {
                if ("onChunkLoad".equals(name)) {
                    int hits = insertInnerBefore(node, 10, 6, IF_ICMPLE, 9);
                    if (hits != 1) {
                        throw new IllegalStateException("onChunkLoad inner sites " + hits);
                    }
                }
                if ("onPlayerLogout".equals(name)) {
                    prefixClear(node);
                }
                if ("onServerStopped".equals(name)) {
                    insertCall(node, "clearAll", "()V");
                }
            });
        }
    }

    private static final class TransportPatcher extends ClassVisitor {
        TransportPatcher(ClassVisitor next) {
            super(ASM9, next);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            return capture(cv, access, name, descriptor, signature, exceptions, node -> {
                if ("valid".equals(name)) {
                    replaceFixedInner(node);
                    InsnList note = new InsnList();
                    note.add(new VarInsnNode(ALOAD, 1));
                    call(note, "notePlayer", "(Ljava/lang/Object;)V");
                    node.instructions.insert(note);
                }
                if ("lambda$tick$1".equals(name)) {
                    int swapped = 0;
                    for (AbstractInsnNode insn = node.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                        if (insn instanceof MethodInsnNode call && "tryConsume".equals(call.name)) {
                            call.owner = POLICY;
                            call.name = "consume";
                            swapped++;
                        }
                    }
                    if (swapped != 1) {
                        throw new IllegalStateException("budget lambda swaps " + swapped);
                    }
                }
            });
        }
    }

    private static final class BudgetPatcher extends ClassVisitor {
        BudgetPatcher(ClassVisitor next) {
            super(ASM9, next);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            return capture(cv, access, name, descriptor, signature, exceptions, node -> {
                if ("onServerTickPre".equals(name)) {
                    insertCall(node, "resetTick", "()V");
                }
            });
        }
    }

    private static final class WalkerPatcher extends ClassVisitor {
        WalkerPatcher(ClassVisitor next) {
            super(ASM9, next);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            if ("findNextMissingGeneratedChunk".equals(name)) {
                return new FindNextAdapter(super.visitMethod(access, name, descriptor, signature, exceptions), access, name, descriptor);
            }
            return capture(cv, access, name, descriptor, signature, exceptions, node -> {
                if ("processLevel".equals(name)) {
                    int first = insertInnerBefore(node, 7, 3, IF_ICMPGT, 6);
                    int second = insertInnerBefore(node, 12, 3, IF_ICMPGT, 11);
                    if (first != 1 || second != 1) {
                        throw new IllegalStateException("processLevel inner sites " + first + " " + second);
                    }
                }
                if ("onPlayerLogout".equals(name)) {
                    prefixClear(node);
                }
                if ("onServerStopped".equals(name)) {
                    insertCall(node, "clearAll", "()V");
                }
            });
        }
    }

    /** Pauses the walk, and restarts the square ring by nudging the existing retarget method. */
    private static final class FindNextAdapter extends AdviceAdapter {
        private final int mode;

        FindNextAdapter(MethodVisitor next, int access, String name, String descriptor) {
            super(ASM9, next, access, name, descriptor);
            this.mode = newLocal(Type.INT_TYPE);
        }

        @Override
        protected void onMethodEnter() {
            loadArg(1);
            invokeStatic(Type.getObjectType(POLICY), new org.objectweb.asm.commons.Method("prepareWalk", "(Ljava/lang/Object;)I"));
            storeLocal(mode);
            loadLocal(mode);
            push(1);
            Label walk = new Label();
            ifICmp(NE, walk);
            visitInsn(ACONST_NULL);
            visitInsn(ARETURN);
            visitLabel(walk);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean iface) {
            super.visitMethodInsn(opcode, owner, name, descriptor, iface);
            if (!"retargetIfMoved".equals(name)) {
                return;
            }
            loadLocal(mode);
            push(2);
            Label skip = new Label();
            ifICmp(NE, skip);
            retarget(true);
            retarget(false);
            visitLabel(skip);
        }

        private void retarget(boolean nudgeX) {
            super.visitVarInsn(ALOAD, 4);
            super.visitVarInsn(ALOAD, 1);
            super.visitMethodInsn(INVOKEVIRTUAL, PLAYER, "chunkPosition", "()Lnet/minecraft/world/level/ChunkPos;", false);
            super.visitFieldInsn(GETFIELD, "net/minecraft/world/level/ChunkPos", "x", "I");
            if (nudgeX) {
                super.visitInsn(ICONST_1);
                super.visitInsn(IADD);
            }
            super.visitVarInsn(ALOAD, 1);
            super.visitMethodInsn(INVOKEVIRTUAL, PLAYER, "chunkPosition", "()Lnet/minecraft/world/level/ChunkPos;", false);
            super.visitFieldInsn(GETFIELD, "net/minecraft/world/level/ChunkPos", "z", "I");
            super.visitVarInsn(ILOAD, 2);
            super.visitInsn(ICONST_1);
            super.visitInsn(IADD);
            super.visitMethodInsn(INVOKEVIRTUAL, CURSOR, "retargetIfMoved", "(III)V", false);
        }
    }

    private static final class QueuePatcher extends ClassVisitor {
        QueuePatcher(ClassVisitor next) {
            super(ASM9, next);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            return capture(cv, access, name, descriptor, signature, exceptions, node -> {
                if (!"ack".equals(name)) {
                    return;
                }
                int hits = 0;
                for (AbstractInsnNode insn = node.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn instanceof IntInsnNode value && insn.getOpcode() == SIPUSH && value.operand == 1024) {
                        value.operand = 32767;
                        hits++;
                    }
                }
                if (hits != 1) {
                    throw new IllegalStateException("dedup caps " + hits);
                }
            });
        }
    }

    private static int insertInnerBefore(MethodNode node, int distanceLocal, int innerLocal, int compare, int playerLocal) {
        List<AbstractInsnNode> hits = new ArrayList<>();
        for (AbstractInsnNode insn = node.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (!(insn instanceof VarInsnNode first) || first.getOpcode() != ILOAD || first.var != distanceLocal) {
                continue;
            }
            AbstractInsnNode second = nextReal(insn);
            AbstractInsnNode third = nextReal(second);
            if (second instanceof VarInsnNode inner && inner.getOpcode() == ILOAD && inner.var == innerLocal && third != null && third.getOpcode() == compare) {
                hits.add(insn);
            }
        }
        for (AbstractInsnNode hit : hits) {
            InsnList add = new InsnList();
            add.add(new VarInsnNode(ALOAD, playerLocal));
            call(add, "innerRadius", "(Ljava/lang/Object;)I");
            add.add(new VarInsnNode(ISTORE, innerLocal));
            node.instructions.insertBefore(hit, add);
        }
        return hits.size();
    }

    private static void replaceFixedInner(MethodNode node) {
        AbstractInsnNode field = null;
        for (AbstractInsnNode insn = node.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof FieldInsnNode get && insn.getOpcode() == GETFIELD && "serverTerrainSyncInnerRadiusChunks".equals(get.name)) {
                if (field != null) {
                    throw new IllegalStateException("two inner reads in valid");
                }
                field = insn;
            }
        }
        if (field == null) {
            throw new IllegalStateException("valid() has no inner radius");
        }
        AbstractInsnNode max = nextReal(field);
        AbstractInsnNode config = previous(field);
        AbstractInsnNode zero = previous(config);
        if (max == null || max.getOpcode() != INVOKESTATIC || zero == null || zero.getOpcode() != ICONST_0) {
            throw new IllegalStateException("unexpected inner radius shape");
        }
        InsnList add = new InsnList();
        add.add(new VarInsnNode(ALOAD, 1));
        call(add, "innerRadius", "(Ljava/lang/Object;)I");
        node.instructions.insert(zero, add);
        node.instructions.remove(zero);
        node.instructions.remove(config);
        node.instructions.remove(field);
        node.instructions.remove(max);
    }

    private static void prefixClear(MethodNode node) {
        List<MethodInsnNode> calls = new ArrayList<>();
        for (AbstractInsnNode insn = node.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call && "clearPlayer".equals(call.name) && "me/cortex/voxy/server/ServerTerrainTransport".equals(call.owner)) {
                calls.add(call);
            }
        }
        if (calls.size() != 1) {
            throw new IllegalStateException(node.name + " clearPlayer sites " + calls.size());
        }
        InsnList add = new InsnList();
        add.add(new InsnNode(DUP));
        call(add, "onPlayerLeft", "(Ljava/lang/Object;)V");
        node.instructions.insertBefore(calls.get(0), add);
    }

    private static void insertCall(MethodNode node, String name, String descriptor) {
        InsnList add = new InsnList();
        call(add, name, descriptor);
        node.instructions.insert(add);
    }

    private static void call(InsnList list, String name, String descriptor) {
        list.add(new MethodInsnNode(INVOKESTATIC, POLICY, name, descriptor, false));
    }

    private static AbstractInsnNode previous(AbstractInsnNode insn) {
        AbstractInsnNode cursor = insn.getPrevious();
        while (cursor != null && cursor.getOpcode() == -1) {
            cursor = cursor.getPrevious();
        }
        return cursor;
    }

    private static AbstractInsnNode nextReal(AbstractInsnNode insn) {
        AbstractInsnNode cursor = insn == null ? null : insn.getNext();
        while (cursor != null && cursor.getOpcode() == -1) {
            cursor = cursor.getNext();
        }
        return cursor;
    }

    private PatchStreaming() {
    }
}
