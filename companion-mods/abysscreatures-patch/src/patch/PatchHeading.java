import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
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
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Patches Abyss Creatures 0.2.2 movement so a vertical steer no longer spins the mob. */
public final class PatchHeading implements Opcodes {
    private static final String COURSE = "abysscreatures/ai/Course";
    private static final String FISH = "abysscreatures/entity/WaterAbyssCreature";
    private static final String VEC3 = "net/minecraft/world/phys/Vec3";
    private static final String BOID_MOVE = "abysscreatures/ai/BoidMoveControl";
    private static final String SWIM_MOVE = "abysscreatures/ai/SwimMoveControl";
    private static final String BOID = "abysscreatures/ai/Boid";

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            throw new IllegalArgumentException("PatchHeading <0.2.2.jar> <0.2.3.jar> <Course.class>");
        }
        byte[] course = Files.readAllBytes(Path.of(args[2]));
        byte[] jar;
        try (ZipFile zip = new ZipFile(Path.of(args[0]).toFile())) {
            jar = rewrite(zip, course);
        }
        Path out = Path.of(args[1]);
        Files.createDirectories(out.getParent());
        Files.write(out, jar);
        System.out.println("wrote " + out + " (" + jar.length + " bytes)");
    }

    private static byte[] rewrite(ZipFile zip, byte[] course) throws IOException {
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        int patched = 0;
        boolean sawToml = false;
        try (ZipOutputStream zout = new ZipOutputStream(raw)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                byte[] data;
                try (InputStream input = zip.getInputStream(entry)) {
                    data = input.readAllBytes();
                }
                String name = entry.getName();
                if (name.equals(COURSE + ".class")) {
                    throw new IllegalStateException("Course.class already in the jar");
                }
                byte[] next = data;
                if (name.equals(BOID_MOVE + ".class")) {
                    next = patchControl(data, false);
                    patched++;
                } else if (name.equals(SWIM_MOVE + ".class")) {
                    next = patchControl(data, true);
                    patched++;
                } else if (name.equals(BOID + ".class")) {
                    next = patchBoid(data);
                    patched++;
                } else if (name.equals("META-INF/neoforge.mods.toml")) {
                    next = bumpVersion(data);
                    sawToml = true;
                }
                ZipEntry copy = new ZipEntry(name);
                copy.setTime(entry.getTime());
                zout.putNextEntry(copy);
                zout.write(next);
                zout.closeEntry();
            }
            ZipEntry added = new ZipEntry(COURSE + ".class");
            added.setTime(0L);
            zout.putNextEntry(added);
            zout.write(course);
            zout.closeEntry();
        }
        if (patched != 3 || !sawToml) {
            throw new IllegalStateException("patched=" + patched + " toml=" + sawToml);
        }
        return raw.toByteArray();
    }

    private static byte[] bumpVersion(byte[] data) {
        String text = new String(data, StandardCharsets.UTF_8);
        String from = "version=\"0.2.2\"";
        String to = "version=\"0.2.3\"";
        if (!text.contains(from) || text.contains(to)) {
            throw new IllegalStateException("mods.toml version line was not 0.2.2");
        }
        String next = text.replace(from, to);
        if (next.contains(from) || !next.contains(to)) {
            throw new IllegalStateException("version replace failed");
        }
        return next.getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] patchBoid(byte[] data) {
        ClassNode cn = read(data);
        MethodNode update = method(cn, "update", "(FF)V");
        int replaced = 0;
        for (AbstractInsnNode insn = update.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (!(insn instanceof LdcInsnNode ldc) || !(ldc.cst instanceof Double value)) {
                continue;
            }
            if (Math.abs(value - 0.1d) > 1.0e-9d) {
                continue;
            }
            InsnList gain = new InsnList();
            gain.add(new VarInsnNode(DLOAD, 7));
            gain.add(new MethodInsnNode(INVOKESTATIC, COURSE, "separationGain", "(D)D", false));
            update.instructions.insert(insn, gain);
            update.instructions.remove(insn);
            replaced++;
        }
        if (replaced != 1) {
            throw new IllegalStateException("expected one separation scale of 0.1, found " + replaced);
        }
        return write(cn);
    }

    private static byte[] patchControl(byte[] data, boolean swim) {
        ClassNode cn = read(data);
        if (cn.fields.stream().anyMatch(field -> field.name.equals("course"))) {
            throw new IllegalStateException(cn.name + " already has course");
        }
        cn.fields.add(new FieldNode(ACC_PRIVATE | ACC_FINAL, "course", "L" + COURSE + ";", null, null));
        MethodNode init = method(cn, "<init>", null);
        init.instructions.insertBefore(lastReturn(init), newCourse(cn.name));
        MethodNode tick = method(cn, "tick", "()V");
        retargetYaw(cn.name, tick, swim);
        return write(cn);
    }

    private static InsnList newCourse(String owner) {
        InsnList ins = new InsnList();
        ins.add(new VarInsnNode(ALOAD, 0));
        ins.add(new TypeInsnNode(NEW, COURSE));
        ins.add(new InsnNode(DUP));
        ins.add(new MethodInsnNode(INVOKESPECIAL, COURSE, "<init>", "()V", false));
        ins.add(new FieldInsnNode(PUTFIELD, owner, "course", "L" + COURSE + ";"));
        return ins;
    }

    private static void retargetYaw(String owner, MethodNode tick, boolean swim) {
        MethodInsnNode atan = firstYawAtan(tick);
        VarInsnNode stored = nextFstore(atan);
        int yawLocal = stored.var;
        AbstractInsnNode start = swim ? expectSwimLoads(atan) : expectBoidLoads(atan);
        FieldInsnNode head = nextHeadWrite(stored);
        AbstractInsnNode afterHead = head.getNext();
        AbstractInsnNode writesInsn = stored.getNext();
        removeInclusive(tick, headAssignStart(head), head);

        LabelNode after = new LabelNode();
        tick.instructions.insertBefore(afterHead, after);
        LabelNode writes = new LabelNode();
        tick.instructions.insertBefore(writesInsn, writes);
        removeInclusive(tick, start, stored);
        tick.instructions.insertBefore(writes, steerBlock(owner, swim, yawLocal, writes, after));
    }

    private static InsnList steerBlock(String owner, boolean swim, int yawLocal, LabelNode writes, LabelNode after) {
        InsnList ins = new InsnList();
        ins.add(new VarInsnNode(ALOAD, 0));
        ins.add(new FieldInsnNode(GETFIELD, owner, "course", "L" + COURSE + ";"));
        if (swim) {
            ins.add(new VarInsnNode(DLOAD, 1));
            ins.add(new VarInsnNode(DLOAD, 3));
            ins.add(new VarInsnNode(DLOAD, 5));
        } else {
            ins.add(new VarInsnNode(ALOAD, 1));
            ins.add(new FieldInsnNode(GETFIELD, VEC3, "x", "D"));
            ins.add(new VarInsnNode(ALOAD, 1));
            ins.add(new FieldInsnNode(GETFIELD, VEC3, "y", "D"));
            ins.add(new VarInsnNode(ALOAD, 1));
            ins.add(new FieldInsnNode(GETFIELD, VEC3, "z", "D"));
        }
        ins.add(new MethodInsnNode(INVOKEVIRTUAL, COURSE, "steer", "(DDD)Z", false));
        LabelNode hold = new LabelNode();
        ins.add(new JumpInsnNode(IFEQ, hold));
        ins.add(new VarInsnNode(ALOAD, 0));
        ins.add(new FieldInsnNode(GETFIELD, owner, "course", "L" + COURSE + ";"));
        ins.add(new MethodInsnNode(INVOKEVIRTUAL, COURSE, "yaw", "()F", false));
        ins.add(new VarInsnNode(FSTORE, yawLocal));
        ins.add(new JumpInsnNode(GOTO, writes));
        ins.add(hold);
        ins.add(new VarInsnNode(ALOAD, 0));
        ins.add(new FieldInsnNode(GETFIELD, owner, "fish", "L" + FISH + ";"));
        ins.add(new MethodInsnNode(INVOKEVIRTUAL, FISH, "getYRot", "()F", false));
        ins.add(new VarInsnNode(FSTORE, yawLocal));
        ins.add(new JumpInsnNode(GOTO, after));
        return ins;
    }

    private static MethodInsnNode firstYawAtan(MethodNode tick) {
        for (AbstractInsnNode insn = tick.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call
                    && call.getOpcode() == INVOKESTATIC
                    && "net/minecraft/util/Mth".equals(call.owner)
                    && "atan2".equals(call.name)) {
                return call;
            }
        }
        throw new IllegalStateException("yaw atan2 not found");
    }

    private static AbstractInsnNode expectBoidLoads(MethodInsnNode atan) {
        AbstractInsnNode x = atan.getPrevious();
        AbstractInsnNode loadX = previous(x);
        AbstractInsnNode z = previous(loadX);
        AbstractInsnNode loadZ = previous(z);
        if (!(x instanceof FieldInsnNode fx) || !"x".equals(fx.name) || fx.getOpcode() != GETFIELD) {
            throw new IllegalStateException("boid yaw atan2 is not fed by Vec3.x");
        }
        if (!(z instanceof FieldInsnNode fz) || !"z".equals(fz.name) || fz.getOpcode() != GETFIELD) {
            throw new IllegalStateException("boid yaw atan2 is not fed by Vec3.z");
        }
        if (loadX.getOpcode() != ALOAD || loadZ.getOpcode() != ALOAD) {
            throw new IllegalStateException("boid yaw loads are not the direction local");
        }
        return loadZ;
    }

    private static AbstractInsnNode expectSwimLoads(MethodInsnNode atan) {
        AbstractInsnNode dx = atan.getPrevious();
        AbstractInsnNode dz = previous(dx);
        if (!(dx instanceof VarInsnNode vx) || vx.getOpcode() != DLOAD || vx.var != 1) {
            throw new IllegalStateException("swim yaw atan2 dx is not local 1");
        }
        if (!(dz instanceof VarInsnNode vz) || vz.getOpcode() != DLOAD || vz.var != 5) {
            throw new IllegalStateException("swim yaw atan2 dz is not local 5");
        }
        return dz;
    }

    private static VarInsnNode nextFstore(AbstractInsnNode from) {
        for (AbstractInsnNode insn = from.getNext(); insn != null; insn = insn.getNext()) {
            if (insn instanceof VarInsnNode var && var.getOpcode() == FSTORE) {
                return var;
            }
        }
        throw new IllegalStateException("yaw fstore not found");
    }

    private static FieldInsnNode nextHeadWrite(AbstractInsnNode from) {
        for (AbstractInsnNode insn = from.getNext(); insn != null; insn = insn.getNext()) {
            if (insn instanceof FieldInsnNode field
                    && field.getOpcode() == PUTFIELD
                    && "yHeadRot".equals(field.name)) {
                return field;
            }
        }
        throw new IllegalStateException("yHeadRot write not found");
    }

    /** The six instructions that copy {@code getYRot()} into {@code yHeadRot}. */
    private static AbstractInsnNode headAssignStart(FieldInsnNode head) {
        AbstractInsnNode start = head;
        for (int i = 0; i < 5; i++) {
            start = previous(start);
        }
        if (start.getOpcode() != ALOAD || head.getOpcode() != PUTFIELD) {
            throw new IllegalStateException("yHeadRot assignment shape changed");
        }
        return start;
    }

    private static void removeInclusive(MethodNode method, AbstractInsnNode start, AbstractInsnNode end) {
        AbstractInsnNode insn = start;
        while (true) {
            AbstractInsnNode next = insn.getNext();
            method.instructions.remove(insn);
            if (insn == end) {
                return;
            }
            if (next == null) {
                throw new IllegalStateException("ran off the instruction list");
            }
            insn = next;
        }
    }

    private static AbstractInsnNode previous(AbstractInsnNode insn) {
        AbstractInsnNode prev = insn.getPrevious();
        if (prev == null) {
            throw new IllegalStateException("missing previous instruction");
        }
        return prev;
    }

    private static AbstractInsnNode lastReturn(MethodNode method) {
        for (AbstractInsnNode insn = method.instructions.getLast(); insn != null; insn = insn.getPrevious()) {
            if (insn.getOpcode() == RETURN) {
                return insn;
            }
        }
        throw new IllegalStateException("constructor has no return");
    }

    private static MethodNode method(ClassNode cn, String name, String desc) {
        MethodNode found = null;
        for (MethodNode method : cn.methods) {
            if (!method.name.equals(name)) {
                continue;
            }
            if (desc != null && !method.desc.equals(desc)) {
                continue;
            }
            if (found != null) {
                throw new IllegalStateException("duplicate " + name + desc);
            }
            found = method;
        }
        if (found == null) {
            throw new IllegalStateException("missing " + name + " " + desc);
        }
        return found;
    }

    private static ClassNode read(byte[] data) {
        ClassNode cn = new ClassNode();
        new ClassReader(data).accept(cn, ClassReader.EXPAND_FRAMES);
        return cn;
    }

    private static byte[] write(ClassNode cn) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES) {
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                if (type1.equals(type2)) {
                    return type1;
                }
                return "java/lang/Object";
            }
        };
        cn.accept(cw);
        return cw.toByteArray();
    }
}
