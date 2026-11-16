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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Patches the ipa10k jar into ipa10l: the square ring is the only LOD sender. */
public final class PatchIpa10l implements Opcodes {
    private static final String DISPATCH = "me/cortex/voxy/server/TerrainDispatch";
    private static final String AVAIL = "me/cortex/voxy/server/TerrainAvailability";
    private static final String ORDER = "me/cortex/voxy/server/PendingOrder";
    private static final String POLICY = "me/cortex/voxy/server/TerrainStreamPolicy";
    private static final String QUEUE = "me/cortex/voxy/common/network/ReliableTerrainQueue";

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            throw new IllegalArgumentException("PatchIpa10l <ipa10k.jar> <ipa10l.jar> <extra.class>...");
        }
        byte[][] extras = new byte[args.length - 2][];
        String[] extraNames = new String[args.length - 2];
        for (int i = 2; i < args.length; i++) {
            Path file = Path.of(args[i]);
            extras[i - 2] = Files.readAllBytes(file);
            extraNames[i - 2] = className(file);
        }
        byte[] jar;
        try (ZipFile zip = new ZipFile(Path.of(args[0]).toFile())) {
            jar = rewrite(zip, extraNames, extras);
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

    private static byte[] rewrite(ZipFile zip, String[] extraNames, byte[][] extras) throws IOException {
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
                byte[] next = patch(entry.getName(), data);
                if (next != data) {
                    patched++;
                }
                ZipEntry copy = new ZipEntry(entry.getName());
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
        if (patched != 6) {
            throw new IllegalStateException("patched " + patched + " entries, expected 6");
        }
        System.out.println("patched entries " + patched);
        return raw.toByteArray();
    }

    private static byte[] patch(String name, byte[] data) {
        if (name.equals("META-INF/MANIFEST.MF") || name.equals("META-INF/neoforge.mods.toml")) {
            String text = new String(data, StandardCharsets.UTF_8);
            if (!text.contains("ipa10k")) {
                throw new IllegalStateException(name + " has no ipa10k");
            }
            String bumped = text.replace("ipa10k", "ipa10l");
            if (bumped.contains("ipa10k") || !bumped.contains("ipa10l")) {
                throw new IllegalStateException(name + " was not bumped to ipa10l");
            }
            return bumped.getBytes(StandardCharsets.UTF_8);
        }
        String internal = switch (name) {
            case "me/cortex/voxy/server/VoxyServerChunkSync.class" -> "sync";
            case "me/cortex/voxy/server/VoxyServerGeneratedChunkWalker.class" -> "walker";
            case "me/cortex/voxy/common/world/service/VoxelIngestService.class" -> "ingest";
            case "me/cortex/voxy/common/network/ReliableTerrainQueue.class" -> "queue";
            default -> null;
        };
        if (internal == null) {
            return data;
        }
        ClassReader reader = new ClassReader(data);
        ClassWriter writer = new FrameWriter(reader);
        reader.accept(new Editor(writer, internal), ClassReader.SKIP_FRAMES);
        System.out.println("patched " + name);
        return writer.toByteArray();
    }

    private static final class Editor extends ClassVisitor {
        private final String kind;
        private int notes;
        private int requeues;

        Editor(ClassVisitor next, String kind) {
            super(ASM9, next);
            this.kind = kind;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            return capture(this.cv, access, name, descriptor, signature, exceptions, node -> edit(node));
        }

        @Override
        public void visitEnd() {
            if ("queue".equals(this.kind) && (this.notes != 4 || this.requeues != 3)) {
                throw new IllegalStateException("queue notes " + this.notes + " requeues " + this.requeues);
            }
            super.visitEnd();
        }

        private void edit(MethodNode node) {
            switch (this.kind) {
                case "sync" -> editSync(node);
                case "walker" -> editWalker(node);
                case "ingest" -> editIngest(node);
                case "queue" -> {
                    this.notes += noteCancels(node);
                    if ("ack".equals(node.name) || "tick".equals(node.name)) {
                        this.requeues += requeuePending(node);
                    }
                }
                default -> {
                }
            }
        }
    }

    private static void editSync(MethodNode node) {
        if ("onChunkLoad".equals(node.name)) {
            noteInsteadOfOffer(node);
        }
        if ("onServerStopped".equals(node.name)) {
            insertClear(node);
        }
        prefixPlayerLeft(node);
    }

    private static void editWalker(MethodNode node) {
        if ("processLevel".equals(node.name)) {
            replaceFindAndSend(node);
        }
        if ("onServerTickPost".equals(node.name)) {
            keepOneStepWhenHot(node);
        }
        if ("hasAnyEligibleRecipients".equals(node.name)) {
            dropExtraRotation(node);
            useEffectiveInner(node);
        }
        if ("onServerStopped".equals(node.name)) {
            insertClear(node);
        }
        prefixPlayerLeft(node);
    }

    private static void editIngest(MethodNode node) {
        if (!"forEachMippedSectionForTerrainSync".equals(node.name)) {
            return;
        }
        int removed = 0;
        List<AbstractInsnNode> kill = new ArrayList<AbstractInsnNode>();
        for (AbstractInsnNode insn = node.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (!(insn instanceof VarInsnNode load) || load.getOpcode() != ILOAD || load.var != 4) {
                continue;
            }
            AbstractInsnNode branch = nextReal(insn);
            AbstractInsnNode ret = nextReal(branch);
            if (branch == null || branch.getOpcode() != IFNE || ret == null || ret.getOpcode() != RETURN) {
                continue;
            }
            kill.add(insn);
            kill.add(branch);
            kill.add(ret);
            removed++;
        }
        if (removed != 1) {
            throw new IllegalStateException("light-gate returns " + removed);
        }
        for (AbstractInsnNode insn : kill) {
            node.instructions.remove(insn);
        }
    }

    private static void noteInsteadOfOffer(MethodNode node) {
        MethodInsnNode empty = null;
        for (AbstractInsnNode insn = node.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call && "isEmpty".equals(call.name) && "java/util/List".equals(call.owner)) {
                empty = call;
                break;
            }
        }
        if (empty == null) {
            throw new IllegalStateException("onChunkLoad has no isEmpty");
        }
        JumpInsnNode branch = (JumpInsnNode) nextReal(empty);
        if (branch.getOpcode() != IFEQ) {
            throw new IllegalStateException("onChunkLoad branch " + branch.getOpcode());
        }
        AbstractInsnNode cursor = branch.label.getNext();
        while (cursor != null) {
            AbstractInsnNode next = cursor.getNext();
            node.instructions.remove(cursor);
            cursor = next;
        }
        InsnList add = new InsnList();
        add.add(new VarInsnNode(ALOAD, 1));
        add.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/server/level/ServerLevel", "dimension", "()Lnet/minecraft/resources/ResourceKey;", false));
        add.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/resources/ResourceKey", "location", "()Lnet/minecraft/resources/ResourceLocation;", false));
        add.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/resources/ResourceLocation", "toString", "()Ljava/lang/String;", false));
        add.add(new VarInsnNode(ALOAD, 5));
        add.add(new org.objectweb.asm.tree.FieldInsnNode(GETFIELD, "net/minecraft/world/level/ChunkPos", "x", "I"));
        add.add(new VarInsnNode(ALOAD, 5));
        add.add(new org.objectweb.asm.tree.FieldInsnNode(GETFIELD, "net/minecraft/world/level/ChunkPos", "z", "I"));
        add.add(new MethodInsnNode(INVOKESTATIC, AVAIL, "note", "(Ljava/lang/String;II)V", false));
        add.add(new InsnNode(RETURN));
        node.instructions.add(add);
    }

    private static void replaceFindAndSend(MethodNode node) {
        MethodInsnNode find = null;
        int finds = 0;
        for (AbstractInsnNode insn = node.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call && "findNextMissingGeneratedChunk".equals(call.name)) {
                find = call;
                finds++;
            }
        }
        if (finds != 1) {
            throw new IllegalStateException("findNext sites " + finds);
        }
        AbstractInsnNode start = find;
        int[] vars = new int[4];
        int[] opcodes = new int[4];
        for (int i = 3; i >= 0; i--) {
            start = prevReal(start);
            opcodes[i] = start.getOpcode();
            if (start instanceof VarInsnNode var) {
                vars[i] = var.var;
            }
        }
        if (opcodes[0] != ALOAD || vars[0] != 0 || opcodes[1] != ALOAD || vars[1] != 11 || opcodes[2] != ILOAD || vars[2] != 3 || opcodes[3] != ILOAD || vars[3] != 12) {
            throw new IllegalStateException("unexpected findNext arguments");
        }
        AbstractInsnNode end = find;
        boolean sawSend = false;
        JumpInsnNode cont = null;
        while (end != null) {
            if (end instanceof MethodInsnNode call && "sendChunkColumn".equals(call.name)) {
                sawSend = true;
            }
            if (sawSend && end instanceof JumpInsnNode jump && jump.getOpcode() == GOTO) {
                cont = jump;
                break;
            }
            end = nextReal(end);
        }
        if (cont == null) {
            throw new IllegalStateException("send loop has no continue");
        }
        AbstractInsnNode anchor = cont.getNext();
        AbstractInsnNode cursor = start;
        while (cursor != anchor) {
            AbstractInsnNode next = cursor.getNext();
            node.instructions.remove(cursor);
            cursor = next;
        }
        InsnList add = new InsnList();
        add.add(new VarInsnNode(ALOAD, 0));
        add.add(new VarInsnNode(ALOAD, 7));
        add.add(new VarInsnNode(ALOAD, 11));
        add.add(new VarInsnNode(ILOAD, 3));
        add.add(new VarInsnNode(ILOAD, 12));
        add.add(new MethodInsnNode(INVOKESTATIC, DISPATCH, "trySend", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;II)Z", false));
        add.add(new JumpInsnNode(IFEQ, cont.label));
        add.add(new org.objectweb.asm.tree.IincInsnNode(2, 1));
        add.add(new JumpInsnNode(GOTO, cont.label));
        node.instructions.insertBefore(anchor, add);
    }

    private static void keepOneStepWhenHot(MethodNode node) {
        FieldInsnNode paused = null;
        int pauses = 0;
        for (AbstractInsnNode insn = node.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof FieldInsnNode field && insn.getOpcode() == PUTSTATIC && "debugPausedForLoadTicks".equals(field.name)) {
                paused = field;
                pauses++;
            }
        }
        if (pauses != 1) {
            throw new IllegalStateException("pause stores " + pauses);
        }
        AbstractInsnNode log = nextReal(paused);
        AbstractInsnNode ret = nextReal(log);
        if (!(log instanceof MethodInsnNode call) || !"maybeLogDebugSummary".equals(call.name) || ret.getOpcode() != RETURN) {
            throw new IllegalStateException("pause tail changed");
        }
        JumpInsnNode budget = null;
        int checks = 0;
        for (AbstractInsnNode insn = node.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn.getOpcode() != IFGT) {
                continue;
            }
            AbstractInsnNode next = nextReal(insn);
            if (next != null && next.getOpcode() == RETURN) {
                budget = (JumpInsnNode) insn;
                checks++;
            }
        }
        if (checks != 1) {
            throw new IllegalStateException("budget checks " + checks);
        }
        InsnList add = new InsnList();
        add.add(new InsnNode(ICONST_1));
        add.add(new VarInsnNode(ISTORE, 4));
        add.add(new JumpInsnNode(GOTO, budget.label));
        node.instructions.insert(paused, add);
        node.instructions.remove(log);
        node.instructions.remove(ret);
    }

    private static void dropExtraRotation(MethodNode node) {
        MethodInsnNode rotate = null;
        int rotates = 0;
        for (AbstractInsnNode insn = node.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call && "rotate".equals(call.name) && "java/util/Collections".equals(call.owner)) {
                rotate = call;
                rotates++;
            }
        }
        if (rotates != 1) {
            throw new IllegalStateException("eligibility rotates " + rotates);
        }
        AbstractInsnNode start = rotate;
        while (start != null && !(start instanceof MethodInsnNode call && "isEmpty".equals(call.name))) {
            start = prevReal(start);
        }
        if (start == null) {
            throw new IllegalStateException("eligibility rotate has no isEmpty");
        }
        AbstractInsnNode receiver = prevReal(start);
        if (receiver != null && receiver.getOpcode() == ALOAD) {
            start = receiver;
        }
        AbstractInsnNode cursor = start;
        AbstractInsnNode end = rotate.getNext();
        while (cursor != end) {
            AbstractInsnNode next = cursor.getNext();
            node.instructions.remove(cursor);
            cursor = next;
        }
    }

    private static void useEffectiveInner(MethodNode node) {
        FieldInsnNode inner = null;
        int reads = 0;
        for (AbstractInsnNode insn = node.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof FieldInsnNode field && insn.getOpcode() == GETFIELD && "serverTerrainSyncInnerRadiusChunks".equals(field.name)) {
                inner = field;
                reads++;
            }
        }
        if (reads != 1) {
            throw new IllegalStateException("eligibility inner reads " + reads);
        }
        AbstractInsnNode max = nextReal(inner);
        AbstractInsnNode config = prevReal(inner);
        AbstractInsnNode zero = prevReal(config);
        if (zero.getOpcode() != ICONST_0 || max.getOpcode() != INVOKESTATIC) {
            throw new IllegalStateException("eligibility inner shape");
        }
        InsnList add = new InsnList();
        add.add(new VarInsnNode(ALOAD, 5));
        add.add(new MethodInsnNode(INVOKESTATIC, POLICY, "innerRadius", "(Ljava/lang/Object;)I", false));
        node.instructions.insert(zero, add);
        node.instructions.remove(zero);
        node.instructions.remove(config);
        node.instructions.remove(inner);
        node.instructions.remove(max);
    }

    private static int noteCancels(MethodNode node) {
        int noted = 0;
        List<MethodInsnNode> cancels = new ArrayList<MethodInsnNode>();
        for (AbstractInsnNode insn = node.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call && "cancel".equals(call.name) && QUEUE.equals(call.owner)) {
                cancels.add(call);
            }
        }
        for (MethodInsnNode cancel : cancels) {
            if (!noteWorthy(cancel)) {
                continue;
            }
            InsnList add = new InsnList();
            add.add(new InsnNode(DUP));
            add.add(new MethodInsnNode(INVOKESTATIC, AVAIL, "noteCancelled", "(Ljava/lang/Object;)V", false));
            node.instructions.insertBefore(cancel, add);
            noted++;
        }
        return noted;
    }

    private static boolean noteWorthy(MethodInsnNode cancel) {
        boolean expired = false;
        boolean status2 = false;
        boolean test = false;
        AbstractInsnNode cursor = cancel;
        for (int i = 0; i < 16; i++) {
            cursor = prevReal(cursor);
            if (cursor == null) {
                break;
            }
            if (cursor instanceof MethodInsnNode call && "test".equals(call.name)) {
                test = true;
            }
            if (cursor instanceof FieldInsnNode field && cursor.getOpcode() == PUTFIELD && "expired".equals(field.name)) {
                expired = true;
            }
            if (cursor.getOpcode() == ICONST_2) {
                status2 = true;
            }
        }
        if (test && !expired && !status2) {
            return false;
        }
        return expired || status2;
    }

    private static int requeuePending(MethodNode node) {
        List<MethodInsnNode> puts = new ArrayList<MethodInsnNode>();
        for (AbstractInsnNode insn = node.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call && "put".equals(call.name) && "java/util/LinkedHashMap".equals(call.owner)) {
                puts.add(call);
            }
        }
        int rewritten = 0;
        for (MethodInsnNode put : puts) {
            FieldInsnNode map = null;
            AbstractInsnNode cursor = put;
            for (int i = 0; i < 8 && map == null; i++) {
                cursor = prevReal(cursor);
                if (cursor instanceof FieldInsnNode field && cursor.getOpcode() == GETFIELD && ("pending".equals(field.name) || "flight".equals(field.name) || "completed".equals(field.name))) {
                    map = field;
                }
            }
            if (map == null || !"pending".equals(map.name)) {
                continue;
            }
            AbstractInsnNode frame = prevReal(put);
            if (!(frame instanceof VarInsnNode load) || load.getOpcode() != ALOAD) {
                throw new IllegalStateException("pending put has no frame");
            }
            AbstractInsnNode pop = nextReal(put);
            AbstractInsnNode start = prevReal(map);
            if (start == null || start.getOpcode() != ALOAD) {
                start = map;
            }
            AbstractInsnNode anchor = pop != null && pop.getOpcode() == POP ? pop.getNext() : put.getNext();
            AbstractInsnNode walk = start;
            while (walk != anchor) {
                AbstractInsnNode next = walk.getNext();
                node.instructions.remove(walk);
                walk = next;
            }
            InsnList add = new InsnList();
            add.add(new VarInsnNode(ALOAD, 0));
            add.add(new FieldInsnNode(GETFIELD, QUEUE, "pending", "Ljava/util/LinkedHashMap;"));
            add.add(new VarInsnNode(ALOAD, load.var));
            add.add(new MethodInsnNode(INVOKESTATIC, ORDER, "requeueFront", "(Ljava/util/LinkedHashMap;Ljava/lang/Object;)V", false));
            if (anchor == null) {
                node.instructions.add(add);
            } else {
                node.instructions.insertBefore(anchor, add);
            }
            rewritten++;
        }
        return rewritten;
    }

    private static void insertClear(MethodNode node) {
        InsnList add = new InsnList();
        add.add(new MethodInsnNode(INVOKESTATIC, DISPATCH, "clearAll", "()V", false));
        node.instructions.insert(add);
    }

    private static void prefixPlayerLeft(MethodNode node) {
        List<MethodInsnNode> calls = new ArrayList<MethodInsnNode>();
        for (AbstractInsnNode insn = node.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call && "onPlayerLeft".equals(call.name) && POLICY.equals(call.owner)) {
                calls.add(call);
            }
        }
        for (MethodInsnNode call : calls) {
            InsnList add = new InsnList();
            add.add(new InsnNode(DUP));
            add.add(new MethodInsnNode(INVOKESTATIC, DISPATCH, "onPlayerLeft", "(Ljava/lang/Object;)V", false));
            node.instructions.insertBefore(call, add);
        }
    }

    private static MethodVisitor capture(ClassVisitor dest, int access, String name, String descriptor, String signature, String[] exceptions, java.util.function.Consumer<MethodNode> edit) {
        MethodNode node = new MethodNode(ASM9, access, name, descriptor, signature, exceptions);
        return new MethodVisitor(ASM9, node) {
            @Override
            public void visitEnd() {
                super.visitEnd();
                edit.accept(node);
                node.localVariables = null;
                node.visibleLocalVariableAnnotations = null;
                String[] thrown = node.exceptions == null || node.exceptions.isEmpty() ? null : node.exceptions.toArray(String[]::new);
                node.accept(dest.visitMethod(node.access, node.name, node.desc, node.signature, thrown));
            }
        };
    }

    private static AbstractInsnNode prevReal(AbstractInsnNode insn) {
        AbstractInsnNode cursor = insn == null ? null : insn.getPrevious();
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

    private PatchIpa10l() {
    }
}
