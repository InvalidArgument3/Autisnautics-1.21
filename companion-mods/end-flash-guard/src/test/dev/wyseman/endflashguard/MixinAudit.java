package dev.wyseman.endflashguard;

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
 * Checks the compiled mixin against VanillaBackport 1.2.2 and Immersive Portals
 * Aeronautics 2.8.17. This does not apply the mixin. It fails if a target
 * method, descriptor, or the world-switch order the guard depends on is absent.
 */
public final class MixinAudit {
    private static final String TICK =
        "(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/multiplayer/ClientLevel;"
            + "Lcom/blackgear/vanillabackport/client/api/modules/end_flashes/EndFlashState;)V";
    private static final String RENDER =
        "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/Minecraft;)V";
    private static final String LIGHT = "(Lnet/minecraft/client/Minecraft;F)F";
    private static final List<String> FAILURES = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            throw new IllegalArgumentException("classes mixins.json vanillabackport.jar ip.jar");
        }
        Path classes = Path.of(args[0]);
        String mixinConfig = Files.readString(Path.of(args[1]));
        ClassNode renderer = classInJar(Path.of(args[2]),
            "com/blackgear/vanillabackport/client/api/modules/end_flashes/EndFlashRenderer.class");
        ClassNode clientLevelMixin = classInJar(Path.of(args[2]),
            "com/blackgear/vanillabackport/core/mixin/client/end_flashes/ClientLevelMixin.class");
        ClassNode lightMixin = classInJar(Path.of(args[2]),
            "com/blackgear/vanillabackport/core/mixin/client/end_flashes/LightTextureMixin.class");
        ClassNode levelRendererMixin = classInJar(Path.of(args[2]),
            "com/blackgear/vanillabackport/core/mixin/client/end_flashes/LevelRendererMixin.class");
        ClassNode loader = classInJar(Path.of(args[3]), "qouteall/imm_ptl/core/ClientWorldLoader.class");
        ClassNode gameRenderer = classInJar(Path.of(args[3]), "qouteall/imm_ptl/core/render/MyGameRenderer.class");
        String sounds = textInJar(Path.of(args[2]), "assets/minecraft/sounds.json");
        String lang = textInJar(Path.of(args[2]), "assets/vanillabackport/lang/en_us.json");

        if (!mixinConfig.contains("\"EndFlashRendererMixin\"")) {
            fail("mixins.json is missing EndFlashRendererMixin");
        }
        if (mixinConfig.contains("\"server\"") || mixinConfig.contains("\n  \"mixins\"")) {
            fail("mixins.json applies hooks outside the client array");
        }

        requireOne(renderer, "tick", TICK);
        requireOne(renderer, "render", RENDER);
        requireOne(renderer, "calculateLightIntensity", LIGHT);
        requireSoundPath(renderer);
        requireUnconditionalTick(clientLevelMixin);
        requireRendererReadsLevel(renderer, "render", RENDER);
        requireRendererReadsLevel(renderer, "calculateLightIntensity", LIGHT);
        requireCallsRenderer(levelRendererMixin, "render", RENDER);
        requireCallsRenderer(lightMixin, "calculateLightIntensity", LIGHT);
        requireWorldSwitchBeforeTick(loader);
        requirePortalRenderSwapsLevel(gameRenderer);
        requireSubtitle(sounds, lang);

        ClassNode mixin = read(Files.readAllBytes(classes.resolve(
            "dev/wyseman/endflashguard/mixin/EndFlashRendererMixin.class")));
        requireInject(mixin, "endflash$tick", TICK, "tick", false);
        requireInject(mixin, "endflash$render", RENDER, "render", false);
        requireInject(mixin, "endflash$light", LIGHT, "calculateLightIntensity", true);
        requireCalls(mixin, "endflash$tick", "observe", "allowsTick", "worldSwitched");
        requireCalls(mixin, "endflash$render", "allowsVisual");
        requireCalls(mixin, "endflash$light", "allowsVisual");

        if (!FAILURES.isEmpty()) {
            for (String failure : FAILURES) {
                System.out.println("FAIL " + failure);
            }
            throw new AssertionError(FAILURES.size() + " mixin audit failure(s)");
        }
        System.out.println("mixin audit ok");
    }

    private static void requireOne(ClassNode owner, String name, String desc) {
        int matches = 0;
        for (MethodNode method : owner.methods) {
            if (method.name.equals(name) && method.desc.equals(desc)) {
                matches++;
                if ((method.access & Opcodes.ACC_STATIC) == 0) {
                    fail(owner.name + "." + name + " is not static");
                }
            }
        }
        if (matches != 1) {
            fail(owner.name + "." + name + desc + " matches " + matches);
        } else {
            System.out.println("TARGET " + name + " " + desc);
        }
    }

    private static void requireSoundPath(ClassNode renderer) {
        MethodNode tick = find(renderer, "tick", TICK);
        boolean play = false;
        boolean flash = false;
        if (tick != null) {
            for (AbstractInsnNode insn : tick.instructions) {
                if (insn instanceof MethodInsnNode call) {
                    if ("flashStartedThisTick".equals(call.name) && "()Z".equals(call.desc)) {
                        flash = true;
                    }
                    if ("playDelayed".equals(call.name)
                        && "(Lnet/minecraft/client/resources/sounds/SoundInstance;I)V".equals(call.desc)
                        && "net/minecraft/client/sounds/SoundManager".equals(call.owner)) {
                        play = true;
                    }
                }
            }
        }
        if (!flash || !play) {
            fail("EndFlashRenderer.tick does not play a delayed sound from flashStartedThisTick");
        } else {
            System.out.println("TARGET tick plays SoundManager.playDelayed when flashStartedThisTick");
        }
    }

    private static void requireUnconditionalTick(ClassNode mixin) {
        MethodNode tick = null;
        for (MethodNode method : mixin.methods) {
            if ("vb$tick".equals(method.name)) {
                tick = method;
            }
        }
        if (tick == null) {
            fail("ClientLevelMixin.vb$tick is missing");
            return;
        }
        boolean called = false;
        boolean branch = false;
        for (AbstractInsnNode insn : tick.instructions) {
            int op = insn.getOpcode();
            if (op >= Opcodes.IFEQ && op <= Opcodes.IF_ACMPNE) {
                branch = true;
            }
            if (insn instanceof MethodInsnNode call
                && "com/blackgear/vanillabackport/client/api/modules/end_flashes/EndFlashRenderer".equals(call.owner)
                && "tick".equals(call.name)
                && TICK.equals(call.desc)) {
                called = true;
            }
        }
        if (!called || branch) {
            fail("ClientLevelMixin.vb$tick does not call EndFlashRenderer.tick unconditionally");
        } else {
            System.out.println("TARGET ClientLevelMixin.vb$tick calls EndFlashRenderer.tick with no branch");
        }
    }

    private static void requireRendererReadsLevel(ClassNode renderer, String name, String desc) {
        MethodNode method = find(renderer, name, desc);
        boolean reads = false;
        if (method != null) {
            for (AbstractInsnNode insn : method.instructions) {
                if (insn instanceof FieldInsnNode field
                    && field.getOpcode() == Opcodes.GETFIELD
                    && "net/minecraft/client/Minecraft".equals(field.owner)
                    && "level".equals(field.name)
                    && "Lnet/minecraft/client/multiplayer/ClientLevel;".equals(field.desc)) {
                    reads = true;
                }
            }
        }
        if (!reads) {
            fail("EndFlashRenderer." + name + " does not read Minecraft.level");
        } else {
            System.out.println("TARGET " + name + " reads Minecraft.level");
        }
    }

    private static void requireCallsRenderer(ClassNode mixin, String name, String desc) {
        boolean calls = false;
        for (MethodNode method : mixin.methods) {
            if (method.instructions == null) {
                continue;
            }
            for (AbstractInsnNode insn : method.instructions) {
                if (insn instanceof MethodInsnNode call
                    && "com/blackgear/vanillabackport/client/api/modules/end_flashes/EndFlashRenderer".equals(call.owner)
                    && name.equals(call.name)
                    && desc.equals(call.desc)) {
                    calls = true;
                }
            }
        }
        if (!calls) {
            fail(mixin.name + " does not call EndFlashRenderer." + name + desc);
        } else {
            System.out.println("TARGET " + mixin.name + " calls " + name);
        }
    }

    private static void requireWorldSwitchBeforeTick(ClassNode loader) {
        MethodNode swap = find(loader, "withSwitchedWorld",
            "(Lnet/minecraft/client/multiplayer/ClientLevel;Ljava/util/function/Supplier;)Ljava/lang/Object;");
        MethodNode flag = find(loader, "getIsWorldSwitched", "()Z");
        MethodNode remote = null;
        for (MethodNode method : loader.methods) {
            if ("lambda$tickRemoteWorld$6".equals(method.name)) {
                remote = method;
            }
        }
        if (flag == null || (flag.access & Opcodes.ACC_STATIC) == 0 || (flag.access & Opcodes.ACC_PUBLIC) == 0) {
            fail("ClientWorldLoader.getIsWorldSwitched()Z is not public static");
        } else {
            System.out.println("TARGET ClientWorldLoader.getIsWorldSwitched ()Z");
        }
        if (swap == null) {
            fail("ClientWorldLoader.withSwitchedWorld is missing");
            return;
        }
        int setFlag = -1;
        int run = -1;
        int index = 0;
        for (AbstractInsnNode insn : swap.instructions) {
            if (insn instanceof InsnNode node && node.getOpcode() == Opcodes.ICONST_1) {
                AbstractInsnNode next = nextOpcode(node);
                if (next instanceof FieldInsnNode field
                    && field.getOpcode() == Opcodes.PUTSTATIC
                    && "isWorldSwitched".equals(field.name)
                    && "Z".equals(field.desc)) {
                    setFlag = index;
                }
            }
            if (insn instanceof MethodInsnNode call
                && "java/util/function/Supplier".equals(call.owner)
                && "get".equals(call.name)) {
                run = index;
            }
            index++;
        }
        if (setFlag < 0 || run < 0 || setFlag > run) {
            fail("withSwitchedWorld does not set isWorldSwitched before Supplier.get");
        } else {
            System.out.println("TARGET withSwitchedWorld sets isWorldSwitched before the runnable");
        }
        boolean ticks = false;
        if (remote != null) {
            for (AbstractInsnNode insn : remote.instructions) {
                if (insn instanceof MethodInsnNode call
                    && "net/minecraft/client/multiplayer/ClientLevel".equals(call.owner)
                    && "tick".equals(call.name)
                    && "(Ljava/util/function/BooleanSupplier;)V".equals(call.desc)) {
                    ticks = true;
                }
            }
        }
        if (!ticks) {
            fail("tickRemoteWorld's runnable does not call ClientLevel.tick");
        } else {
            System.out.println("TARGET remote tick calls ClientLevel.tick inside the switched runnable");
        }
    }

    private static void requirePortalRenderSwapsLevel(ClassNode gameRenderer) {
        MethodNode swap = find(gameRenderer, "switchAndRenderTheWorld",
            "(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Ljava/util/function/Consumer;IZ)V");
        if (swap == null) {
            fail("MyGameRenderer.switchAndRenderTheWorld is missing");
            return;
        }
        int putLevel = -1;
        int light = -1;
        boolean setsFlag = false;
        int index = 0;
        for (AbstractInsnNode insn : swap.instructions) {
            if (insn instanceof FieldInsnNode field
                && field.getOpcode() == Opcodes.PUTFIELD
                && "net/minecraft/client/Minecraft".equals(field.owner)
                && "level".equals(field.name)) {
                if (putLevel < 0) {
                    putLevel = index;
                }
            }
            if (insn instanceof MethodInsnNode call && "updateLightTexture".equals(call.name)) {
                light = index;
            }
            if (insn instanceof FieldInsnNode field && "isWorldSwitched".equals(field.name)) {
                setsFlag = true;
            }
            index++;
        }
        if (putLevel < 0 || light < 0 || putLevel > light || setsFlag) {
            fail("switchAndRenderTheWorld does not swap Minecraft.level before updateLightTexture without isWorldSwitched");
        } else {
            System.out.println("TARGET portal render swaps Minecraft.level and does not set isWorldSwitched");
        }
    }

    private static void requireSubtitle(String sounds, String lang) {
        if (!sounds.contains("\"weather.end_flash\"") || !sounds.contains("\"subtitles.weather.end_flash\"")) {
            fail("sounds.json is missing weather.end_flash");
        }
        if (!lang.contains("\"subtitles.weather.end_flash\"") || !lang.contains("End Flash rumbles")) {
            fail("lang is missing the End Flash rumbles subtitle");
        } else {
            System.out.println("TARGET subtitle End Flash rumbles for weather.end_flash");
        }
    }

    private static void requireInject(ClassNode mixin, String handler, String targetDesc, String targetName, boolean returnable) {
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
        String callback = returnable
            ? "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;"
            : "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;";
        String expected = "(" + args + callback + ")V";
        if (!expected.equals(method.desc)) {
            fail(handler + " descriptor " + method.desc + " != " + expected);
        }
        if ((method.access & Opcodes.ACC_STATIC) == 0) {
            fail(handler + " is not static");
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

    private static AbstractInsnNode nextOpcode(AbstractInsnNode insn) {
        AbstractInsnNode next = insn.getNext();
        while (next != null && next.getOpcode() < 0) {
            next = next.getNext();
        }
        return next;
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
                return empty;
            }
            try (InputStream in = zip.getInputStream(entry)) {
                return read(in.readAllBytes());
            }
        }
    }

    private static String textInJar(Path jar, String name) throws Exception {
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            ZipEntry entry = zip.getEntry(name);
            if (entry == null) {
                fail("missing " + name);
                return "";
            }
            try (InputStream in = zip.getInputStream(entry)) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
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
