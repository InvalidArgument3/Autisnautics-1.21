package dev.wyseman.enigmaticlegacyfix;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.Handle;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

/**
 * Confirms EnigmaticLegacy+ 1.21.1-1.1.2's common setup is only
 * {@code PROXY.init()}, and that the fix redirects that call.
 * Args: classes-dir mixins.json enigmaticlegacyplus.jar
 */
public final class MixinAudit {
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            throw new IllegalArgumentException("expected classes-dir mixins.json enigmatic.jar");
        }
        Path classes = Path.of(args[0]);
        String mixinJson = Files.readString(Path.of(args[1]));
        if (!mixinJson.contains("\"EnigmaticLegacyCommonSetupMixin\"")) {
            throw new AssertionError("mixin config does not name EnigmaticLegacyCommonSetupMixin");
        }
        if (mixinJson.contains("\"client\"") || mixinJson.contains("\"server\"")) {
            throw new AssertionError("mixin config must apply on both sides");
        }

        ClassNode mixin = read(Files.readAllBytes(classes.resolve(
                "dev/wyseman/enigmaticlegacyfix/mixin/EnigmaticLegacyCommonSetupMixin.class")));
        String mixinTarget = annotationValue(annotations(mixin.visibleAnnotations, mixin.invisibleAnnotations), "Lorg/spongepowered/asm/mixin/Mixin;", "value");
        if (!"Lauviotre/enigmatic/legacy/EnigmaticLegacy;".equals(mixinTarget)) {
            throw new AssertionError("mixin target is " + mixinTarget);
        }
        MethodNode defer = method(mixin, "deferInit");
        if (!"(Lauviotre/enigmatic/legacy/proxy/CommonProxy;Lnet/neoforged/fml/event/lifecycle/FMLCommonSetupEvent;)V"
                .equals(defer.desc)) {
            throw new AssertionError("deferInit descriptor is " + defer.desc);
        }
        String redirectTarget = annotationValue(annotations(defer.visibleAnnotations, defer.invisibleAnnotations), "Lorg/spongepowered/asm/mixin/injection/Redirect;", "method");
        if (!"onCommonSetup".equals(redirectTarget)) {
            throw new AssertionError("redirect method is " + redirectTarget);
        }
        if (!contains(defer, "enqueueWork") || !enqueuesInit(defer)) {
            throw new AssertionError("deferInit does not enqueue CommonProxy.init");
        }

        ClassNode enigmatic;
        ClassNode proxy;
        try (JarFile jar = new JarFile(args[2])) {
            enigmatic = read(jar.getInputStream(jar.getJarEntry("auviotre/enigmatic/legacy/EnigmaticLegacy.class")).readAllBytes());
            proxy = read(jar.getInputStream(jar.getJarEntry("auviotre/enigmatic/legacy/proxy/CommonProxy.class")).readAllBytes());
        }
        MethodNode setup = method(enigmatic, "onCommonSetup");
        List<AbstractInsnNode> real = new ArrayList<>();
        for (AbstractInsnNode insn : setup.instructions) {
            if (insn.getOpcode() >= 0) {
                real.add(insn);
            }
        }
        if (real.size() != 3
                || real.get(0).getOpcode() != Opcodes.GETSTATIC
                || real.get(1).getOpcode() != Opcodes.INVOKEVIRTUAL
                || real.get(2).getOpcode() != Opcodes.RETURN) {
            throw new AssertionError("onCommonSetup is no longer only PROXY.init()");
        }
        MethodInsnNode call = (MethodInsnNode) real.get(1);
        if (!"auviotre/enigmatic/legacy/proxy/CommonProxy".equals(call.owner)
                || !"init".equals(call.name)
                || !"()V".equals(call.desc)) {
            throw new AssertionError("onCommonSetup calls " + call.owner + "." + call.name + call.desc);
        }
        MethodNode init = method(proxy, "init");
        boolean registers = false;
        for (AbstractInsnNode insn : init.instructions) {
            if (insn instanceof MethodInsnNode callInsn
                    && "net/minecraft/world/level/block/DispenserBlock".equals(callInsn.owner)
                    && "registerBehavior".equals(callInsn.name)) {
                registers = true;
            }
        }
        if (!registers) {
            throw new AssertionError("CommonProxy.init no longer calls DispenserBlock.registerBehavior");
        }
        System.out.println("enigmaticlegacyfix audit ok");
    }

    private static boolean enqueuesInit(MethodNode method) {
        for (AbstractInsnNode insn : method.instructions) {
            if (!(insn instanceof InvokeDynamicInsnNode dynamic)) {
                continue;
            }
            for (Object arg : dynamic.bsmArgs) {
                if (arg instanceof Handle handle
                        && "auviotre/enigmatic/legacy/proxy/CommonProxy".equals(handle.getOwner())
                        && "init".equals(handle.getName())
                        && "()V".equals(handle.getDesc())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean contains(MethodNode method, String name) {
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode call && name.equals(call.name)) {
                return true;
            }
        }
        return false;
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

    private static String annotationValue(List<AnnotationNode> annotations, String desc, String key) {
        if (annotations == null) {
            throw new AssertionError("missing annotations");
        }
        for (AnnotationNode annotation : annotations) {
            if (!desc.equals(annotation.desc) || annotation.values == null) {
                continue;
            }
            for (int i = 0; i < annotation.values.size(); i += 2) {
                if (key.equals(annotation.values.get(i))) {
                    Object value = annotation.values.get(i + 1);
                    if (value instanceof List<?> list) {
                        if (list.size() != 1) {
                            throw new AssertionError(key + " has " + list.size() + " values");
                        }
                        value = list.get(0);
                    }
                    if (value instanceof Type type) {
                        return type.getDescriptor();
                    }
                    return String.valueOf(value);
                }
            }
        }
        throw new AssertionError("annotation " + desc + " has no " + key);
    }

    private static MethodNode method(ClassNode node, String name) {
        for (MethodNode method : node.methods) {
            if (name.equals(method.name)) {
                return method;
            }
        }
        throw new AssertionError("missing method " + name);
    }

    private static ClassNode read(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }
}
