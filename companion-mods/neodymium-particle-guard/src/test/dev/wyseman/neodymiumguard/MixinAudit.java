package dev.wyseman.neodymiumguard;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Checks the compiled mixin against the real Alex's Caves jar. This does not
 * apply the mixin. It fails if the target class or method is absent, if the
 * method signature moved, or if the unconditional spawn the guard relies on is
 * no longer there.
 *
 * <p>The premise of the whole mod is that {@code animateTick} spawns a particle
 * on every call, because its guard is {@code random.nextInt(1) == 0} and
 * {@code nextInt(1)} is always {@code 0}. That is asserted here against the
 * shipped bytecode rather than assumed, so a future Alex's Caves build that
 * fixes or changes the spawn makes this build fail instead of shipping a mixin
 * that cancels a method which no longer only spawns particles.
 */
public final class MixinAudit {
    private static final String TARGET_CLASS =
        "com/github/alexmodguy/alexscaves/server/block/NeodymiumNodeBlock.class";
    private static final String TARGET_OWNER =
        "com/github/alexmodguy/alexscaves/server/block/NeodymiumNodeBlock";
    private static final String ANIMATE_TICK =
        "(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;"
            + "Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V";
    private static final String RANDOM_SOURCE = "net/minecraft/util/RandomSource";
    private static final String LEVEL = "net/minecraft/world/level/Level";
    private static final List<String> FAILURES = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            throw new IllegalArgumentException("classes mixins.json alexscaves.jar");
        }
        Path classes = Path.of(args[0]);
        String mixinConfig = Files.readString(Path.of(args[1]));
        Path alexscaves = Path.of(args[2]);

        if (!mixinConfig.contains("\"NeodymiumNodeBlockMixin\"")) {
            fail("mixins.json is missing NeodymiumNodeBlockMixin");
        }
        if (mixinConfig.contains("\"server\"") || mixinConfig.contains("\n  \"mixins\"")) {
            fail("mixins.json applies hooks outside the client array");
        }
        if (!mixinConfig.contains("\"defaultRequire\": 1")) {
            fail("mixins.json must require each injector to find exactly one target");
        }

        ClassNode target = classInJar(alexscaves, TARGET_CLASS);

        MethodNode animate = requireOne(target, "animateTick", ANIMATE_TICK);
        if (animate != null && (animate.access & Opcodes.ACC_STATIC) != 0) {
            fail("NeodymiumNodeBlock.animateTick is static; the mixin handler is an instance method");
        }
        requireUnconditionalSpawn(animate);
        requireOrbitParticles(animate);
        requireOnlySpawns(animate);

        ClassNode mixin = read(Files.readAllBytes(classes.resolve(
            "dev/wyseman/neodymiumguard/mixin/NeodymiumNodeBlockMixin.class")));
        requireInject(mixin, "neodymium$throttleOrbitParticle", ANIMATE_TICK, "animateTick");
        requireCalls(mixin, "neodymium$throttleOrbitParticle", "shouldSpawn");

        ClassNode guard = read(Files.readAllBytes(classes.resolve(
            "dev/wyseman/neodymiumguard/NeodymiumParticleGuard.class")));
        ClassNode throttle = read(Files.readAllBytes(classes.resolve(
            "dev/wyseman/neodymiumguard/NeodymiumThrottle.class")));
        requireGuardShape(guard, throttle);

        if (!FAILURES.isEmpty()) {
            for (String failure : FAILURES) {
                System.out.println("FAIL " + failure);
            }
            throw new AssertionError(FAILURES.size() + " mixin audit failure(s)");
        }
        System.out.println("mixin audit ok");
    }

    private static MethodNode requireOne(ClassNode owner, String name, String desc) {
        MethodNode found = null;
        int matches = 0;
        for (MethodNode method : owner.methods) {
            if (method.name.equals(name) && method.desc.equals(desc)) {
                matches++;
                found = method;
            }
        }
        if (matches != 1) {
            fail(owner.name + "." + name + desc + " matches " + matches);
            return null;
        }
        System.out.println("TARGET " + name + " " + desc);
        return found;
    }

    /**
     * The spawn must be guarded by {@code nextInt(1)}, which is always 0. If the
     * constant is anything else the guard is a real random check and cancelling
     * the method would drop particles the mod intended to spawn.
     */
    private static void requireUnconditionalSpawn(MethodNode method) {
        if (method == null) {
            fail("cannot check the spawn guard: animateTick is missing");
            return;
        }
        boolean nextIntOne = false;
        AbstractInsnNode previous = null;
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode call
                && RANDOM_SOURCE.equals(call.owner)
                && "nextInt".equals(call.name)
                && "(I)I".equals(call.desc)) {
                if (previous instanceof InsnNode push && push.getOpcode() == Opcodes.ICONST_1) {
                    nextIntOne = true;
                } else {
                    fail("RandomSource.nextInt is not called with the constant 1");
                }
            }
            if (insn.getOpcode() >= 0) {
                previous = insn;
            }
        }
        if (!nextIntOne) {
            fail("animateTick does not guard its spawn with RandomSource.nextInt(1)");
        } else {
            System.out.println("TARGET animateTick guards the spawn with nextInt(1), which is always 0");
        }
    }

    /** Confirms the method spawns the magnetic orbit particle, not something else. */
    private static void requireOrbitParticles(MethodNode method) {
        if (method == null) {
            return;
        }
        boolean azure = false;
        boolean scarlet = false;
        boolean addParticle = false;
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof FieldInsnNode field) {
                if ("AZURE_MAGNETIC_ORBIT".equals(field.name)) {
                    azure = true;
                }
                if ("SCARLET_MAGNETIC_ORBIT".equals(field.name)) {
                    scarlet = true;
                }
            }
            if (insn instanceof MethodInsnNode call
                && LEVEL.equals(call.owner)
                && "addParticle".equals(call.name)) {
                addParticle = true;
            }
        }
        if (!azure || !scarlet || !addParticle) {
            fail("animateTick does not spawn both magnetic orbit particles via Level.addParticle"
                + " (azure=" + azure + " scarlet=" + scarlet + " addParticle=" + addParticle + ")");
        } else {
            System.out.println("TARGET animateTick spawns AZURE/SCARLET_MAGNETIC_ORBIT via Level.addParticle");
        }
    }

    /**
     * Cancelling at HEAD is only safe while the method does nothing but spawn.
     * Any other effect, or a return value, would be silently dropped.
     */
    private static void requireOnlySpawns(MethodNode method) {
        if (method == null) {
            return;
        }
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode call) {
                boolean allowed = (RANDOM_SOURCE.equals(call.owner) && "nextInt".equals(call.name))
                    || (LEVEL.equals(call.owner) && "addParticle".equals(call.name))
                    || "net/minecraft/world/phys/Vec3".equals(call.owner)
                    || "net/minecraft/core/particles/ParticleOptions".equals(call.owner)
                    || call.owner.startsWith("net/neoforged/neoforge/registries/DeferredHolder")
                    || call.owner.startsWith("com/github/alexmodguy/alexscaves/");
                if (!allowed) {
                    fail("animateTick calls " + call.owner + "." + call.name
                        + "; cancelling at HEAD would drop that effect");
                }
            }
            if (insn instanceof FieldInsnNode field
                && field.getOpcode() == Opcodes.PUTFIELD
                && !field.owner.startsWith("net/minecraft/world/phys/Vec3")) {
                fail("animateTick writes " + field.owner + "." + field.name
                    + "; cancelling at HEAD would drop that write");
            }
        }
        System.out.println("TARGET animateTick has no side effects other than the particle spawn");
    }

    private static void requireInject(ClassNode mixin, String handler, String targetDesc, String targetName) {
        MethodNode method = find(mixin, handler, null);
        if (method == null) {
            fail("missing handler " + handler);
            return;
        }
        AnnotationNode inject = annotation(method, "Lorg/spongepowered/asm/mixin/injection/Inject;");
        if (inject == null) {
            fail(handler + " has no @Inject");
            return;
        }
        String named = stringValue(inject, "method");
        String at = atValue(inject);
        if (!targetName.equals(named) || !"HEAD".equals(at) || !booleanValue(inject, "cancellable")) {
            fail(handler + " must cancel " + targetName + " at HEAD");
        }
        String args = targetDesc.substring(1, targetDesc.indexOf(')'));
        String expected = "(" + args
            + "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V";
        if (!expected.equals(method.desc)) {
            fail(handler + " descriptor " + method.desc + " != " + expected);
        }
        if ((method.access & Opcodes.ACC_STATIC) != 0) {
            fail(handler + " is static but animateTick is an instance method");
        }
        System.out.println("  INJECT " + targetName + " " + targetDesc + " handler " + method.desc);
    }

    private static void requireCalls(ClassNode mixin, String handler, String... names) {
        MethodNode method = find(mixin, handler, null);
        if (method == null) {
            return;
        }
        for (String name : names) {
            boolean found = false;
            for (AbstractInsnNode insn : method.instructions) {
                if (insn instanceof MethodInsnNode call && name.equals(call.name)) {
                    found = true;
                }
            }
            if (!found) {
                fail(handler + " does not call " + name);
            }
        }
    }

    /** The runtime half must read the divisor once and stay Minecraft-free in its maths. */
    private static void requireGuardShape(ClassNode guard, ClassNode throttle) {
        boolean hasThrottleField = false;
        for (var field : guard.fields) {
            if ("THROTTLE".equals(field.name) && "Ldev/wyseman/neodymiumguard/NeodymiumThrottle;".equals(field.desc)) {
                hasThrottleField = true;
                if ((field.access & Opcodes.ACC_STATIC) == 0 || (field.access & Opcodes.ACC_FINAL) == 0) {
                    fail("NeodymiumParticleGuard.THROTTLE must be static final");
                }
            }
        }
        if (!hasThrottleField) {
            fail("NeodymiumParticleGuard has no THROTTLE field");
        }
        MethodNode shouldSpawn = find(guard, "shouldSpawn", null);
        if (shouldSpawn == null) {
            fail("NeodymiumParticleGuard.shouldSpawn is missing");
        } else {
            boolean callsAllows = false;
            boolean callsThrottled = false;
            for (AbstractInsnNode insn : shouldSpawn.instructions) {
                if (insn instanceof MethodInsnNode call) {
                    if ("allows".equals(call.name)) {
                        callsAllows = true;
                    }
                    if ("throttled".equals(call.name)) {
                        callsThrottled = true;
                    }
                }
            }
            if (!callsAllows) {
                fail("shouldSpawn does not call NeodymiumThrottle.allows");
            }
            if (!callsThrottled) {
                fail("shouldSpawn does not short-circuit on NeodymiumThrottle.throttled");
            }
        }
        MethodNode allows = find(throttle, "allows", null);
        if (allows == null) {
            fail("NeodymiumThrottle.allows is missing");
        } else {
            boolean floorMod = false;
            for (AbstractInsnNode insn : allows.instructions) {
                if (insn instanceof MethodInsnNode call && "floorMod".equals(call.name)) {
                    floorMod = true;
                }
            }
            if (!floorMod) {
                fail("NeodymiumThrottle.allows does not use Math.floorMod");
            }
        }
        System.out.println("TARGET guard reads the divisor once and throttles through NeodymiumThrottle");
    }

    private static MethodNode find(ClassNode owner, String name, String desc) {
        for (MethodNode method : owner.methods) {
            if (method.name.equals(name) && (desc == null || method.desc.equals(desc))) {
                return method;
            }
        }
        return null;
    }

    private static ClassNode classInJar(Path jar, String name) throws Exception {
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            ZipEntry entry = zip.getEntry(name);
            if (entry == null) {
                fail("missing " + name + " in " + jar);
                ClassNode empty = new ClassNode();
                empty.name = name;
                empty.methods = List.of();
                empty.fields = List.of();
                return empty;
            }
            try (InputStream in = zip.getInputStream(entry)) {
                return read(in.readAllBytes());
            }
        }
    }

    private static ClassNode read(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }

    private static AnnotationNode annotation(MethodNode method, String desc) {
        List<AnnotationNode> all = new ArrayList<>();
        if (method.visibleAnnotations != null) {
            all.addAll(method.visibleAnnotations);
        }
        if (method.invisibleAnnotations != null) {
            all.addAll(method.invisibleAnnotations);
        }
        for (AnnotationNode annotation : all) {
            if (desc.equals(annotation.desc)) {
                return annotation;
            }
        }
        return null;
    }

    private static String stringValue(AnnotationNode annotation, String key) {
        Object value = value(annotation, key);
        if (value instanceof String text) {
            return text;
        }
        if (value instanceof List<?> list && list.size() == 1 && list.get(0) instanceof String text) {
            return text;
        }
        return null;
    }

    private static String atValue(AnnotationNode inject) {
        Object at = value(inject, "at");
        AnnotationNode node = null;
        if (at instanceof AnnotationNode direct) {
            node = direct;
        } else if (at instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof AnnotationNode first) {
            node = first;
        }
        if (node == null) {
            return null;
        }
        Object found = value(node, "value");
        return found instanceof String text ? text : null;
    }

    private static boolean booleanValue(AnnotationNode annotation, String key) {
        Object value = value(annotation, key);
        if (value instanceof Boolean flag) {
            return flag;
        }
        return value instanceof Integer number && number != 0;
    }

    private static Object value(AnnotationNode annotation, String key) {
        if (annotation == null || annotation.values == null) {
            return null;
        }
        for (int i = 0; i + 1 < annotation.values.size(); i += 2) {
            if (key.equals(annotation.values.get(i))) {
                return annotation.values.get(i + 1);
            }
        }
        return null;
    }

    private static void fail(String message) {
        FAILURES.add(message);
    }
}
