package dev.wyseman.fwachainfix;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Checks the compiled mixins against Fancy World Animations 1.3.7-patch.2.
 * Fails if {@code isFinished} no longer returns false immediately, or if the
 * methods the hooks target moved.
 */
public final class MixinAudit {
    private static final List<String> FAILURES = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            throw new IllegalArgumentException("classes mixins.json fwa.jar");
        }
        Path classes = Path.of(args[0]);
        String mixinConfig = Files.readString(Path.of(args[1]));
        Path fwa = Path.of(args[2]);

        if (!mixinConfig.contains("\"AbstractChainAnimationMixin\"")
            || !mixinConfig.contains("\"ChainAnimationMixin\"")) {
            fail("mixins.json is missing a hook");
        }
        if (mixinConfig.contains("\"server\"") || mixinConfig.contains("\n  \"mixins\"")) {
            fail("mixins.json applies hooks outside the client array");
        }

        ClassNode chain = classInJar(fwa, "fr/madu59/fwa/anims/AbstractChainAnimation.class");
        MethodNode finished = require(chain, "isFinished", "(D)Z");
        if (finished != null && !returnsFalseImmediately(finished)) {
            fail("AbstractChainAnimation.isFinished no longer returns false immediately; the patch premise changed");
        }
        require(chain, "update", "()V");
        require(chain, "render", "(Lfr/madu59/fwa/rendering/AnimationRenderingContext;)V");
        require(chain, "updateCache", "(J)V");
        require(chain, "updateChainCount", "()V");

        ClassNode chainAnim = classInJar(fwa, "fr/madu59/fwa/anims/ChainAnimation.class");
        require(chainAnim, "tick", "(D)V");

        ClassNode updateMixin = read(classes.resolve(
            "dev/wyseman/fwachainfix/mixin/AbstractChainAnimationMixin.class"));
        requireInject(updateMixin, "fwachainfix$skipColumnAllocation", "update");
        requireInject(updateMixin, "fwachainfix$reuseSettledPoses", "updateCache");
        requireInject(updateMixin, "fwachainfix$flushChain", "render");
        requireCalls(updateMixin, "fwachainfix$flushChain", "renderBuffers");

        ClassNode tickMixin = read(classes.resolve(
            "dev/wyseman/fwachainfix/mixin/ChainAnimationMixin.class"));
        requireInject(tickMixin, "fwachainfix$oneEndPerChain", "tick");
        requireCalls(tickMixin, "fwachainfix$oneEndPerChain", "setLast");
        requireCalls(tickMixin, "fwachainfix$oneEndPerChain", "markForRemoval");
        requireCalls(tickMixin, "fwachainfix$oneEndPerChain", "needUpdate");

        if (!FAILURES.isEmpty()) {
            throw new IllegalStateException(String.join("\n", FAILURES));
        }
        System.out.println("mixin audit ok");
    }

    private static boolean returnsFalseImmediately(MethodNode method) {
        for (AbstractInsnNode insn : method.instructions) {
            if (insn.getOpcode() == -1) {
                continue;
            }
            if (insn.getOpcode() == Opcodes.ICONST_0) {
                AbstractInsnNode next = nextReal(insn);
                return next != null && next.getOpcode() == Opcodes.IRETURN;
            }
            return false;
        }
        return false;
    }

    private static AbstractInsnNode nextReal(AbstractInsnNode insn) {
        AbstractInsnNode next = insn.getNext();
        while (next != null && next.getOpcode() == -1) {
            next = next.getNext();
        }
        return next;
    }

    private static void requireInject(ClassNode mixin, String method, String targetFragment) {
        MethodNode node = findByName(mixin, method);
        if (node == null) {
            fail("missing mixin method " + method);
            return;
        }
        boolean found = false;
        for (java.util.List<AnnotationNode> list : new java.util.List[]{node.visibleAnnotations, node.invisibleAnnotations}) {
            if (list == null) {
                continue;
            }
            for (AnnotationNode annotation : list) {
                if (annotation.desc != null && annotation.desc.contains("injection/Inject")
                    && annotationContains(annotation.values, targetFragment)) {
                    found = true;
                }
            }
        }
        if (!found) {
            fail(method + " does not inject " + targetFragment);
        }
    }

    private static boolean annotationContains(Object value, String fragment) {
        if (value == null) {
            return false;
        }
        if (value instanceof String text) {
            return text.contains(fragment);
        }
        if (value instanceof AnnotationNode nested) {
            return annotationContains(nested.values, fragment);
        }
        if (value instanceof List<?> list) {
            for (Object item : list) {
                if (annotationContains(item, fragment)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void requireCalls(ClassNode mixin, String method, String name) {
        MethodNode node = findByName(mixin, method);
        if (node == null) {
            return;
        }
        for (AbstractInsnNode insn : node.instructions) {
            if (insn instanceof MethodInsnNode call && call.name.equals(name)) {
                return;
            }
        }
        fail(method + " does not call " + name);
    }

    private static MethodNode findByName(ClassNode node, String name) {
        for (MethodNode method : node.methods) {
            if (method.name.equals(name)) {
                return method;
            }
        }
        return null;
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

    private static ClassNode classInJar(Path jar, String name) throws Exception {
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            var entry = zip.getEntry(name);
            if (entry == null) {
                fail("jar missing " + name);
                return null;
            }
            return read(zip.getInputStream(entry).readAllBytes());
        }
    }

    private static ClassNode read(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }

    private static ClassNode read(Path path) throws Exception {
        return read(Files.readAllBytes(path));
    }

    private static void fail(String message) {
        FAILURES.add(message);
    }
}
