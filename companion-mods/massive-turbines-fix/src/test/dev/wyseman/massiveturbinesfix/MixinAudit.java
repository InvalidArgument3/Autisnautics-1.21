package dev.wyseman.massiveturbinesfix;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Checks the dedicated-server hook against Create: Massive Turbines 1.0.1.
 * The crash is {@code CasingHelper.renderAt} naming {@code LocalPlayer}.
 * Replacing that method body must drop every client class reference, and the
 * mixin class itself must not name one.
 */
public final class MixinAudit {
    private static final String HELPER =
        "com/kompot/massiveturbines/placement/MassiveTurbinesPlacement$CasingHelper.class";
    private static final String RENDER = "renderAt";
    private static final String RENDER_DESC =
        "(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;"
            + "Lnet/minecraft/world/phys/BlockHitResult;Lnet/createmod/catnip/placement/PlacementOffset;)V";
    private static final List<String> FAILURES = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            throw new IllegalArgumentException("classes mixins.json turbines.jar");
        }
        Path classes = Path.of(args[0]);
        String mixinConfig = Files.readString(Path.of(args[1]));
        Path turbines = Path.of(args[2]);

        if (!mixinConfig.contains("\"CasingHelperMixin\"")) {
            fail("mixins.json is missing CasingHelperMixin");
        }
        if (!mixinConfig.contains("\"server\"")) {
            fail("mixins.json has no dedicated-server list");
        }
        if (mixinConfig.contains("\"client\"") || mixinConfig.contains("\n  \"mixins\"")) {
            fail("mixins.json would also apply on the client");
        }

        ClassNode helper = classInJar(turbines, HELPER);
        MethodNode render = require(helper, RENDER, RENDER_DESC);
        if (render != null && !mentions(render, "net/minecraft/client/player/LocalPlayer")) {
            fail("CasingHelper.renderAt no longer names LocalPlayer; the patch premise changed");
        }
        for (MethodNode method : helper.methods) {
            if (method.name.equals(RENDER)) {
                continue;
            }
            if (mentionsClient(method)) {
                fail(method.name + " also names a client class");
            }
        }

        MethodNode emptied = new MethodNode(
            Opcodes.ASM9, render.access, render.name, render.desc, render.signature, null);
        emptied.instructions.add(new InsnNode(Opcodes.RETURN));
        emptied.maxStack = 0;
        emptied.maxLocals = 5;
        helper.methods.remove(render);
        helper.methods.add(emptied);
        ClassWriter writer = new ClassWriter(0);
        helper.accept(writer);
        byte[] rewritten = writer.toByteArray();
        String rewrittenText = new String(rewritten, java.nio.charset.StandardCharsets.ISO_8859_1);
        if (rewrittenText.contains("LocalPlayer")
            || rewrittenText.contains("net/minecraft/client/")
            || rewrittenText.contains("ClientLevel")) {
            fail("emptying renderAt left a client class in CasingHelper");
        }

        ClassNode mixin = read(classes.resolve(
            "dev/wyseman/massiveturbinesfix/mixin/CasingHelperMixin.class"));
        MethodNode hook = require(mixin, RENDER, RENDER_DESC);
        if (hook != null && !hasAnnotation(hook, "Lorg/spongepowered/asm/mixin/Overwrite;")) {
            fail("CasingHelperMixin.renderAt is not an Overwrite");
        }
        if (!hasAnnotation(mixin, "Lorg/spongepowered/asm/mixin/Mixin;")) {
            fail("CasingHelperMixin is not a mixin");
        }
        String mixinText = Files.readString(classes.resolve(
            "dev/wyseman/massiveturbinesfix/mixin/CasingHelperMixin.class"),
            java.nio.charset.StandardCharsets.ISO_8859_1);
        if (mixinText.contains("LocalPlayer") || mixinText.contains("net/minecraft/client/")) {
            fail("the mixin class names a client class");
        }

        if (!FAILURES.isEmpty()) {
            for (String failure : FAILURES) {
                System.err.println(failure);
            }
            throw new IllegalStateException(FAILURES.size() + " audit failure(s)");
        }
        System.out.println("massiveturbinesfix audit ok");
    }

    private static boolean mentionsClient(MethodNode method) {
        return mentions(method, "net/minecraft/client/");
    }

    private static boolean mentions(MethodNode method, String needle) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call && (call.owner.contains(needle) || call.desc.contains(needle))) {
                return true;
            }
            if (insn instanceof org.objectweb.asm.tree.FieldInsnNode field
                && (field.owner.contains(needle) || field.desc.contains(needle))) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasAnnotation(MethodNode method, String desc) {
        return has(method.visibleAnnotations, desc) || has(method.invisibleAnnotations, desc);
    }

    private static boolean hasAnnotation(ClassNode node, String desc) {
        return has(node.visibleAnnotations, desc) || has(node.invisibleAnnotations, desc);
    }

    private static boolean has(List<AnnotationNode> annotations, String desc) {
        if (annotations == null) {
            return false;
        }
        for (AnnotationNode annotation : annotations) {
            if (desc.equals(annotation.desc)) {
                return true;
            }
        }
        return false;
    }

    private static MethodNode require(ClassNode node, String name, String desc) {
        if (node == null) {
            return null;
        }
        for (MethodNode method : node.methods) {
            if (method.name.equals(name) && method.desc.equals(desc)) {
                return method;
            }
        }
        fail(node.name + " is missing " + name + desc);
        return null;
    }

    private static ClassNode classInJar(Path jar, String entry) throws Exception {
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            var item = zip.getEntry(entry);
            if (item == null) {
                fail("missing " + entry);
                return null;
            }
            ClassReader reader = new ClassReader(zip.getInputStream(item));
            ClassNode node = new ClassNode();
            reader.accept(node, 0);
            return node;
        }
    }

    private static ClassNode read(Path path) throws Exception {
        ClassReader reader = new ClassReader(Files.readAllBytes(path));
        ClassNode node = new ClassNode();
        reader.accept(node, 0);
        return node;
    }

    private static void fail(String message) {
        FAILURES.add(message);
    }
}
