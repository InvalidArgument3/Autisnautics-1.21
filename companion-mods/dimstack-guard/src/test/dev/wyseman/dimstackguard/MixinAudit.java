package dev.wyseman.dimstackguard;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Checks the compiled mixin against the real Immersive Portals jar. This does
 * not apply the mixin.
 *
 * <p>Three things have to hold for the fix to be correct, and all three are
 * asserted against the shipped bytecode rather than assumed:
 *
 * <ol>
 *   <li>{@code O_O.getModName} really does dereference
 *       {@code getModFileById} with no null check, so a non-mod namespace
 *       throws.</li>
 *   <li>{@code McHelper.getDimensionName} really does guard the result with a
 *       null check, so returning null is the behaviour it expects.</li>
 *   <li>Both methods are display-only. {@code getModName} must be called by
 *       nothing but {@code McHelper}, and {@code getDimensionName} by nothing
 *       but {@code DimEntryWidget}. If either grows a caller that does real
 *       work, returning null stops being safe and this build should fail.</li>
 * </ol>
 */
public final class MixinAudit {
    private static final String O_O = "qouteall/imm_ptl/core/platform_specific/O_O";
    private static final String MC_HELPER = "qouteall/imm_ptl/core/McHelper";
    private static final String DIM_ENTRY_WIDGET = "qouteall/imm_ptl/peripheral/dim_stack/DimEntryWidget";
    private static final String GET_MOD_NAME = "(Ljava/lang/String;)Ljava/lang/String;";
    private static final String GET_DIMENSION_NAME =
        "(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/network/chat/Component;";
    private static final String LOADING_MOD_LIST = "net/neoforged/fml/loading/LoadingModList";
    private static final String MOD_FILE_INFO = "net/neoforged/fml/loading/moddiscovery/ModFileInfo";
    private static final List<String> FAILURES = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            throw new IllegalArgumentException("classes mixins.json ip.jar");
        }
        Path classes = Path.of(args[0]);
        String mixinConfig = Files.readString(Path.of(args[1]));
        Path ip = Path.of(args[2]);

        if (!mixinConfig.contains("\"OOModNameMixin\"")) {
            fail("mixins.json is missing OOModNameMixin");
        }
        if (mixinConfig.contains("\"server\"") || mixinConfig.contains("\n  \"mixins\"")) {
            fail("mixins.json applies hooks outside the client array");
        }
        if (!mixinConfig.contains("\"defaultRequire\": 1")) {
            fail("mixins.json must require each injector to find exactly one target");
        }

        ClassNode oo = classInJar(ip, O_O + ".class");
        ClassNode mcHelper = classInJar(ip, MC_HELPER + ".class");

        MethodNode getModName = requireOne(oo, "getModName", GET_MOD_NAME);
        requireNoNullCheck(getModName);
        requireNullGuard(mcHelper, "getDimensionName", GET_DIMENSION_NAME);
        requireOnlyCallers(ip);

        ClassNode mixin = read(Files.readAllBytes(classes.resolve(
            "dev/wyseman/dimstackguard/mixin/OOModNameMixin.class")));
        requireInject(mixin, "dimstack$missingMod", GET_MOD_NAME, "getModName");
        requireCalls(mixin, "dimstack$missingMod", "isMissingMod");

        ClassNode guard = read(Files.readAllBytes(classes.resolve(
            "dev/wyseman/dimstackguard/DimStackGuard.class")));
        requireGuardShape(guard);

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
        if ((found.access & Opcodes.ACC_STATIC) == 0) {
            fail(owner.name + "." + name + " is not static; the mixin handler is static");
        }
        System.out.println("TARGET " + name + " " + desc);
        return found;
    }

    /**
     * The bug. {@code getModFileById} must be followed straight by
     * {@code getMods} with no null branch in between, or the premise is wrong
     * and the guard is unnecessary.
     */
    private static void requireNoNullCheck(MethodNode method) {
        if (method == null) {
            fail("cannot check the bug: getModName is missing");
            return;
        }
        boolean lookedUp = false;
        boolean dereferenced = false;
        boolean nullChecked = false;
        AbstractInsnNode previous = null;
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode call) {
                if (LOADING_MOD_LIST.equals(call.owner) && "getModFileById".equals(call.name)) {
                    lookedUp = true;
                    previous = insn;
                    continue;
                }
                if (MOD_FILE_INFO.equals(call.owner) && "getMods".equals(call.name)) {
                    dereferenced = true;
                }
            }
            int op = insn.getOpcode();
            if (lookedUp && !dereferenced
                && (op == Opcodes.IFNULL || op == Opcodes.IFNONNULL || op == Opcodes.IF_ACMPEQ)) {
                nullChecked = true;
            }
            if (op >= 0) {
                previous = insn;
            }
        }
        if (!lookedUp) {
            fail("getModName does not call LoadingModList.getModFileById");
        } else if (!dereferenced) {
            fail("getModName does not call ModFileInfo.getMods");
        } else if (nullChecked) {
            fail("getModName now null-checks getModFileById; the guard may be unnecessary");
        } else {
            System.out.println("TARGET getModName dereferences getModFileById with no null check");
        }
    }

    /**
     * The safety net. {@code getDimensionName} must branch on the getModName
     * result, which is the {@code modName != null ? modName : namespace} the
     * source shows.
     */
    private static void requireNullGuard(ClassNode owner, String name, String desc) {
        MethodNode method = null;
        for (MethodNode candidate : owner.methods) {
            if (candidate.name.equals(name)) {
                method = candidate;
            }
        }
        if (method == null) {
            fail("McHelper." + name + " is missing");
            return;
        }
        boolean callsGetModName = false;
        boolean branches = false;
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode call
                && O_O.equals(call.owner) && "getModName".equals(call.name)) {
                callsGetModName = true;
                continue;
            }
            int op = insn.getOpcode();
            if (callsGetModName && (op == Opcodes.IFNULL || op == Opcodes.IFNONNULL)) {
                branches = true;
            }
        }
        if (!callsGetModName) {
            fail("McHelper." + name + " does not call O_O.getModName");
        } else if (!branches) {
            fail("McHelper." + name + " does not null-check the getModName result");
        } else {
            System.out.println("TARGET McHelper." + name + " null-checks the getModName result");
        }
    }

    /**
     * Display-only. Scans every class in the jar for calls to the two methods
     * and fails if anything other than the expected GUI path reaches them.
     */
    private static void requireOnlyCallers(Path jar) throws Exception {
        List<String> modNameCallers = new ArrayList<>();
        List<String> dimensionNameCallers = new ArrayList<>();
        int scanned = 0;
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) {
                    continue;
                }
                ClassNode node;
                try (InputStream in = zip.getInputStream(entry)) {
                    node = read(in.readAllBytes());
                } catch (Throwable ex) {
                    continue;
                }
                scanned++;
                if (node.methods == null) {
                    continue;
                }
                for (MethodNode method : node.methods) {
                    if (method.instructions == null) {
                        continue;
                    }
                    for (AbstractInsnNode insn : method.instructions) {
                        if (insn instanceof MethodInsnNode call) {
                            if (O_O.equals(call.owner) && "getModName".equals(call.name)) {
                                addOnce(modNameCallers, node.name);
                            }
                            if (MC_HELPER.equals(call.owner) && "getDimensionName".equals(call.name)) {
                                addOnce(dimensionNameCallers, node.name);
                            }
                        }
                    }
                }
            }
        }
        System.out.println("  scanned " + scanned + " classes");
        if (!modNameCallers.equals(List.of(MC_HELPER))) {
            fail("O_O.getModName callers are " + modNameCallers + ", expected only " + MC_HELPER);
        } else {
            System.out.println("TARGET O_O.getModName is called only by McHelper");
        }
        if (!dimensionNameCallers.equals(List.of(DIM_ENTRY_WIDGET))) {
            fail("McHelper.getDimensionName callers are " + dimensionNameCallers
                + ", expected only " + DIM_ENTRY_WIDGET);
        } else {
            System.out.println("TARGET McHelper.getDimensionName is called only by DimEntryWidget");
        }
    }

    private static void addOnce(List<String> list, String value) {
        if (!list.contains(value)) {
            list.add(value);
        }
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
            + "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V";
        if (!expected.equals(method.desc)) {
            fail(handler + " descriptor " + method.desc + " != " + expected);
        }
        if ((method.access & Opcodes.ACC_STATIC) == 0) {
            fail(handler + " is not static but getModName is");
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

    /** The decision must stay free of loader types so it can be tested. */
    private static void requireGuardShape(ClassNode guard) {
        MethodNode isMissing = find(guard, "isMissingMod", null);
        if (isMissing == null) {
            fail("DimStackGuard.isMissingMod is missing");
            return;
        }
        boolean usesPredicate = false;
        for (var arg : isMissing.desc.split(";")) {
            if (arg.contains("java/util/function/Predicate")) {
                usesPredicate = true;
            }
        }
        if (!usesPredicate) {
            fail("isMissingMod does not take a Predicate, so it cannot be tested without FML");
        }
        if ((isMissing.access & Opcodes.ACC_STATIC) == 0) {
            fail("DimStackGuard.isMissingMod is not static");
        }
        System.out.println("TARGET DimStackGuard.isMissingMod takes an injected lookup");
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
