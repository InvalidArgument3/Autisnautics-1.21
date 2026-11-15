package dev.wyseman.portalrenderguard;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Checks the compiled mixins against the real Voxy, Immersive Portals, and
 * Mojmap Minecraft class files. This is not a live Mixin apply: there is no
 * GL context here. It fails if a target class, method, descriptor, injection
 * point, or GlStateManager call would not match those jars.
 */
public final class MixinAudit {
    private static final List<String> FAILURES = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        if (args.length != 5) {
            throw new IllegalArgumentException("classes mixins.json voxy.jar ip.jar minecraft.jar");
        }
        Path classes = Path.of(args[0]);
        String mixinConfig = Files.readString(Path.of(args[1]));
        Map<String, byte[]> jars = new HashMap<>();
        loadJar(jars, Path.of(args[2]), "voxy");
        loadJar(jars, Path.of(args[3]), "ip");
        loadJar(jars, Path.of(args[4]), "minecraft");

        if (!mixinConfig.contains("\"VoxyRenderSystemMixin\"") || !mixinConfig.contains("\"PortalCompositeMixin\"")) {
            fail("mixins.json is missing a client mixin");
        }
        if (mixinConfig.contains("VoxyClipMixin")) {
            fail("mixins.json still lists VoxyClipMixin");
        }
        if (!mixinConfig.contains("GuardMixinPlugin")) {
            fail("mixins.json has no plugin");
        }
        if (mixinConfig.contains("\"server\"") || mixinConfig.contains("\n  \"mixins\"")) {
            fail("mixins.json applies hooks outside the client array");
        }

        auditMixin(classes, jars, "dev/wyseman/portalrenderguard/mixin/VoxyRenderSystemMixin.class");
        auditMixin(classes, jars, "dev/wyseman/portalrenderguard/mixin/PortalCompositeMixin.class");
        auditGlCalls(classes, jars);
        auditPlugin(classes);

        if (!FAILURES.isEmpty()) {
            for (String failure : FAILURES) {
                System.out.println("FAIL " + failure);
            }
            throw new AssertionError(FAILURES.size() + " mixin audit failure(s)");
        }
        System.out.println("mixin audit ok");
    }

    private static void auditMixin(Path classes, Map<String, byte[]> jars, String relative) throws Exception {
        ClassNode mixin = read(Files.readAllBytes(classes.resolve(relative)));
        AnnotationNode mixinAnn = annotation(annotations(mixin.visibleAnnotations, mixin.invisibleAnnotations), "Lorg/spongepowered/asm/mixin/Mixin;");
        if (mixinAnn == null) {
            fail(mixin.name + " has no @Mixin");
            return;
        }
        String target = stringValue(mixinAnn, "targets");
        if (target == null) {
            fail(mixin.name + " has no @Mixin target");
            return;
        }
        byte[] targetBytes = jars.get(target.replace('.', '/') + ".class");
        if (targetBytes == null) {
            fail("target class missing from the real jars: " + target);
            return;
        }
        ClassNode targetNode = read(targetBytes);
        System.out.println("TARGET " + target + " from real jar, mixin " + mixin.name
            + " priority " + intValue(mixinAnn, "priority", 1000));
        boolean sawInject = false;
        for (MethodNode method : mixin.methods) {
            List<AnnotationNode> methodAnnotations = annotations(method.visibleAnnotations, method.invisibleAnnotations);
            AnnotationNode inject = annotation(methodAnnotations, "Lorg/spongepowered/asm/mixin/injection/Inject;");
            if (inject == null) {
                for (AnnotationNode ann : methodAnnotations) {
                    if (ann.desc.contains("Redirect") || ann.desc.contains("Modify") || ann.desc.contains("Wrap")
                        || ann.desc.contains("Shadow") || ann.desc.contains("Overwrite")) {
                        fail(mixin.name + "." + method.name + " uses " + ann.desc);
                    }
                }
                continue;
            }
            sawInject = true;
            String targetMethod = stringValue(inject, "method");
            String at = atValue(inject);
            MethodNode found = null;
            int matches = 0;
            for (MethodNode candidate : targetNode.methods) {
                if (candidate.name.equals(targetMethod)) {
                    matches++;
                    found = candidate;
                }
            }
            if (matches != 1 || found == null) {
                fail(target + "." + targetMethod + " matches " + matches + " methods in the real class");
                continue;
            }
            String expectedHandler = handlerDescriptor(found.desc, inject);
            boolean staticTarget = (found.access & Opcodes.ACC_STATIC) != 0;
            boolean staticHandler = (method.access & Opcodes.ACC_STATIC) != 0;
            int returns = countReturns(found);
            System.out.println("  INJECT " + targetMethod + " " + found.desc
                + " at " + at
                + " static=" + staticTarget
                + " returns=" + returns
                + " handler=" + method.name + method.desc);
            if (!method.desc.equals(expectedHandler)) {
                fail(mixin.name + "." + method.name + " descriptor " + method.desc
                    + " != " + expectedHandler + " for " + found.desc);
            }
            if (staticTarget != staticHandler) {
                fail(mixin.name + "." + method.name + " static mismatch with " + targetMethod);
            }
            if (returns < 1) {
                fail(target + "." + targetMethod + " has no return instruction");
            }
            if (found.instructions == null || found.instructions.size() == 0) {
                fail(target + "." + targetMethod + " has no code for HEAD");
            }
            if (!"HEAD".equals(at) && !"RETURN".equals(at)) {
                fail(mixin.name + "." + method.name + " injection point " + at + " is not checked");
            }
        }
        if (!sawInject) {
            fail(mixin.name + " has no @Inject");
        }
    }

    private static void auditGlCalls(Path classes, Map<String, byte[]> jars) throws Exception {
        ClassNode manager = read(jars.get("com/mojang/blaze3d/platform/GlStateManager.class"));
        if (manager == null) {
            fail("GlStateManager missing from the Mojmap client");
            return;
        }
        Map<String, String> real = new HashMap<>();
        for (MethodNode method : manager.methods) {
            real.put(method.name + method.desc, method.name);
        }
        try (var walk = Files.walk(classes)) {
            for (Path classFile : walk.filter(path -> path.toString().endsWith(".class")).toList()) {
                ClassNode owner = read(Files.readAllBytes(classFile));
                for (MethodNode method : owner.methods) {
                    for (AbstractInsnNode insn : method.instructions) {
                        if (!(insn instanceof MethodInsnNode call)) {
                            continue;
                        }
                        if (!"com/mojang/blaze3d/platform/GlStateManager".equals(call.owner)) {
                            continue;
                        }
                        String key = call.name + call.desc;
                        System.out.println("  GL " + owner.name + "." + method.name + " " + key);
                        if (!real.containsKey(key)) {
                            fail("GlStateManager." + call.name + call.desc + " is not in the Mojmap client");
                        }
                    }
                }
            }
        }
    }

    private static void auditPlugin(Path classes) throws Exception {
        ClassNode plugin = read(Files.readAllBytes(classes.resolve("dev/wyseman/portalrenderguard/GuardMixinPlugin.class")));
        boolean found = false;
        if (plugin.interfaces != null) {
            found = plugin.interfaces.contains("org/spongepowered/asm/mixin/extensibility/IMixinConfigPlugin");
        }
        if (!found) {
            fail("GuardMixinPlugin does not implement IMixinConfigPlugin");
        } else {
            System.out.println("PLUGIN dev.wyseman.portalrenderguard.GuardMixinPlugin");
        }
    }

    private static String handlerDescriptor(String targetDesc, AnnotationNode inject) {
        String args = targetDesc.substring(targetDesc.indexOf('(') + 1, targetDesc.indexOf(')'));
        boolean cancellableReturn = targetDesc.charAt(targetDesc.indexOf(')') + 1) != 'V';
        String callback = cancellableReturn
            ? "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;"
            : "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;";
        // activeClipEquation would be a returnable. Void targets use CallbackInfo.
        if (booleanValue(inject, "cancellable") && cancellableReturn) {
            return "(" + args + callback + ")V";
        }
        return "(" + args + callback + ")V";
    }

    private static int countReturns(MethodNode method) {
        int count = 0;
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof InsnNode node) {
                int op = node.getOpcode();
                if (op == Opcodes.RETURN || op == Opcodes.ARETURN || op == Opcodes.IRETURN
                    || op == Opcodes.DRETURN || op == Opcodes.FRETURN || op == Opcodes.LRETURN) {
                    count++;
                }
            }
        }
        return count;
    }

    private static ClassNode read(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }

    private static void loadJar(Map<String, byte[]> into, Path jar, String label) throws Exception {
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                copyEntry(zip, entries.nextElement(), into);
            }
        }
        System.out.println("JAR " + label + " " + jar);
    }

    private static void copyEntry(ZipFile zip, ZipEntry entry, Map<String, byte[]> into) {
        if (!entry.getName().endsWith(".class")) {
            return;
        }
        try (InputStream in = zip.getInputStream(entry)) {
            into.put(entry.getName(), in.readAllBytes());
        } catch (Exception ex) {
            throw new IllegalStateException(entry.getName(), ex);
        }
    }

    private static List<AnnotationNode> annotations(List<AnnotationNode> visible, List<AnnotationNode> invisible) {
        List<AnnotationNode> all = new ArrayList<>();
        if (visible != null) {
            all.addAll(visible);
        }
        if (invisible != null) {
            all.addAll(invisible);
        }
        return all;
    }

    private static AnnotationNode annotation(List<AnnotationNode> annotations, String desc) {
        if (annotations == null) {
            return null;
        }
        for (AnnotationNode annotation : annotations) {
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
        Object value = value(node, "value");
        return value instanceof String text ? text : null;
    }

    private static int intValue(AnnotationNode annotation, String key, int fallback) {
        Object value = value(annotation, key);
        return value instanceof Integer number ? number : fallback;
    }

    private static boolean booleanValue(AnnotationNode annotation, String key) {
        Object value = value(annotation, key);
        return value instanceof Boolean flag && flag;
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
