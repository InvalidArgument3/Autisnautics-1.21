import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Rewrites the ipa10i Voxy jar into ipa10j. Every entry that this class does not list is copied
 * byte for byte from the input jar.
 */
public final class PatchVoxy implements Opcodes {
    private static final String SAFETY = "me/cortex/voxy/client/core/model/ColourTintSafety";
    private static final String BLOCK_COLOR = "net/minecraft/client/color/block/BlockColor";
    private static final String BLOCK_COLORS = "net/minecraft/client/color/block/BlockColors";
    private static final String GET_COLOR_DESC =
            "(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;I)I";
    private static final String TINT_DESC =
            "(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/ColorResolver;)I";
    private static final String BIOME = "Lnet/minecraft/world/level/biome/Biome;";
    private static final String RESOLVER = "Lnet/minecraft/world/level/ColorResolver;";

    private static int blockColorRewrites;
    private static int blockColorsRewrites;
    private static int captureRewrites;
    private static int dependencyRewrites;
    private static int lambdaRewrites;
    private static int probeRewrites;
    private static int tickRewrites;

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            throw new IllegalArgumentException("usage: PatchVoxy <ipa10i.jar> <safety.class> <ipa10j.jar>");
        }
        Path input = Path.of(args[0]);
        Path safetyClass = Path.of(args[1]);
        Path output = Path.of(args[2]);
        Map<String, byte[]> replacements = new LinkedHashMap<>();
        try (ZipFile zip = new ZipFile(input.toFile())) {
            replacements.put(
                    "me/cortex/voxy/client/core/model/ModelFactory.class",
                    transform(zip, "me/cortex/voxy/client/core/model/ModelFactory.class", PatchVoxy::rewriteModelFactory));
            replacements.put(
                    "me/cortex/voxy/client/core/model/ModelFactory$1.class",
                    transform(zip, "me/cortex/voxy/client/core/model/ModelFactory$1.class",
                            reader -> rewriteTintGetter(reader, "me/cortex/voxy/client/core/model/ModelFactory$1", "val$probeBiome", "safeProbeTint")));
            replacements.put(
                    "me/cortex/voxy/client/core/model/ModelFactory$2.class",
                    transform(zip, "me/cortex/voxy/client/core/model/ModelFactory$2.class",
                            reader -> rewriteTintGetter(reader, "me/cortex/voxy/client/core/model/ModelFactory$2", "val$biome", "safeBlockTint")));
            replacements.put(
                    "me/cortex/voxy/client/TimingStatistics.class",
                    transform(zip, "me/cortex/voxy/client/TimingStatistics.class", PatchVoxy::rewriteTimingStatistics));
            replacements.put(
                    "me/cortex/voxy/client/TimingStatistics$TimeSampler.class",
                    transform(zip, "me/cortex/voxy/client/TimingStatistics$TimeSampler.class", PatchVoxy::rewriteTimeSampler));
            replacements.put(
                    "me/cortex/voxy/client/core/VoxyRenderSystem.class",
                    transform(zip, "me/cortex/voxy/client/core/VoxyRenderSystem.class", PatchVoxy::rewriteRenderSystem));
        }
        require("captureColourConstant BlockColor.getColor rewrites", captureRewrites, 3);
        require("isBiomeDependentColour BlockColor.getColor rewrites", dependencyRewrites, 1);
        require("getColourProvider BlockColors.getColor rewrites", probeRewrites, 3);
        require("getColourProvider lambda rewrites", lambdaRewrites, 3);
        require("BlockColor.getColor rewrites", blockColorRewrites, 4);
        require("BlockColors.getColor rewrites", blockColorsRewrites, 6);
        require("ModelBakerySubsystem.tick rewrites", tickRewrites, 2);

        try (ZipFile zip = new ZipFile(input.toFile());
             OutputStream fileOut = Files.newOutputStream(output);
             ZipOutputStream out = new ZipOutputStream(fileOut)) {
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                InputStream in = zip.getInputStream(entry);
                byte[] bytes = in.readAllBytes();
                byte[] replacement = replacements.remove(entry.getName());
                if (replacement != null) {
                    bytes = replacement;
                } else if (entry.getName().equals("META-INF/MANIFEST.MF") || entry.getName().equals("META-INF/neoforge.mods.toml")) {
                    String text = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                    if (!text.contains("ipa10i")) {
                        throw new IllegalStateException(entry.getName() + " has no ipa10i version");
                    }
                    bytes = text.replace("ipa10i", "ipa10j").getBytes(java.nio.charset.StandardCharsets.UTF_8);
                }
                ZipEntry copy = new ZipEntry(entry.getName());
                copy.setTime(entry.getTime());
                out.putNextEntry(copy);
                out.write(bytes);
                out.closeEntry();
            }
            if (!replacements.isEmpty()) {
                throw new IllegalStateException("patched classes missing from the jar: " + replacements.keySet());
            }
            ZipEntry safety = new ZipEntry("me/cortex/voxy/client/core/model/ColourTintSafety.class");
            safety.setTime(entryTime());
            out.putNextEntry(safety);
            out.write(Files.readAllBytes(safetyClass));
            out.closeEntry();
        }
        System.out.println("wrote " + output);
    }

    private static void require(String label, int actual, int expected) {
        if (actual != expected) {
            throw new IllegalStateException(label + " expected " + expected + " but patched " + actual);
        }
        System.out.println(label + ": " + actual);
    }

    private static byte[] transform(ZipFile zip, String name, java.util.function.Function<ClassReader, byte[]> transform) throws IOException {
        ZipEntry entry = zip.getEntry(name);
        if (entry == null) {
            throw new IllegalStateException("missing " + name);
        }
        return transform.apply(new ClassReader(zip.getInputStream(entry).readAllBytes()));
    }

    private static byte[] rewriteModelFactory(ClassReader reader) {
        ClassWriter writer = new ClassWriter(reader, 0);
        reader.accept(new ClassVisitor(ASM9, writer) {
            private String method = "";

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                method = name;
                return new MethodVisitor(ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                        if (opcode == INVOKEINTERFACE && BLOCK_COLOR.equals(owner) && "getColor".equals(name) && GET_COLOR_DESC.equals(descriptor)) {
                            blockColorRewrites++;
                            if ("captureColourConstant".equals(method)) {
                                captureRewrites++;
                            } else if ("isBiomeDependentColour".equals(method)) {
                                dependencyRewrites++;
                            }
                            super.visitMethodInsn(INVOKESTATIC, SAFETY, "safeGetColor",
                                    "(L" + BLOCK_COLOR + ";" + GET_COLOR_DESC.substring(1), false);
                            return;
                        }
                        if (opcode == INVOKEVIRTUAL && BLOCK_COLORS.equals(owner) && "getColor".equals(name) && GET_COLOR_DESC.equals(descriptor)) {
                            blockColorsRewrites++;
                            if ("getColourProvider".equals(method)) {
                                probeRewrites++;
                            } else if (method.startsWith("lambda$getColourProvider$")) {
                                lambdaRewrites++;
                            }
                            super.visitMethodInsn(INVOKESTATIC, SAFETY, "safeBlockColorsGet",
                                    "(L" + BLOCK_COLORS + ";" + GET_COLOR_DESC.substring(1), false);
                            return;
                        }
                        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    }
                };
            }
        }, 0);
        return writer.toByteArray();
    }

    private static byte[] rewriteTintGetter(ClassReader reader, String internalName, String biomeField, String helper) {
        ClassWriter writer = new ClassWriter(reader, 0);
        reader.accept(new ClassVisitor(ASM9, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                if ("getBlockTint".equals(name) && TINT_DESC.equals(descriptor)) {
                    MethodVisitor method = super.visitMethod(access, name, descriptor, signature, exceptions);
                    method.visitCode();
                    method.visitVarInsn(ALOAD, 0);
                    method.visitFieldInsn(GETFIELD, internalName, biomeField, BIOME);
                    method.visitVarInsn(ALOAD, 2);
                    method.visitMethodInsn(INVOKESTATIC, SAFETY, helper, "(" + BIOME + RESOLVER + ")I", false);
                    method.visitInsn(IRETURN);
                    method.visitMaxs(2, 3);
                    method.visitEnd();
                    return new MethodVisitor(ASM9) {};
                }
                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }
        }, 0);
        return writer.toByteArray();
    }

    private static byte[] rewriteTimeSampler(ClassReader reader) {
        ClassWriter writer = new ClassWriter(reader, 0);
        reader.accept(new ClassVisitor(ASM9, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                if ("reset".equals(name) && "()V".equals(descriptor)) {
                    MethodVisitor method = super.visitMethod(access, name, descriptor, signature, exceptions);
                    method.visitCode();
                    method.visitVarInsn(ALOAD, 0);
                    method.visitInsn(ICONST_0);
                    method.visitFieldInsn(PUTFIELD, "me/cortex/voxy/client/TimingStatistics$TimeSampler", "running", "Z");
                    method.visitVarInsn(ALOAD, 0);
                    method.visitInsn(LCONST_0);
                    method.visitFieldInsn(PUTFIELD, "me/cortex/voxy/client/TimingStatistics$TimeSampler", "runtime", "J");
                    method.visitInsn(RETURN);
                    method.visitMaxs(3, 1);
                    method.visitEnd();
                    return new MethodVisitor(ASM9) {};
                }
                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }
        }, 0);
        return writer.toByteArray();
    }

    private static byte[] rewriteTimingStatistics(ClassReader reader) {
        ClassWriter writer = new ClassWriter(reader, 0);
        reader.accept(new ClassVisitor(ASM9, writer) {
            @Override
            public void visitEnd() {
                MethodVisitor method = super.visitMethod(ACC_PUBLIC | ACC_STATIC, "abandonRunningSamplers", "()V", null, null);
                method.visitCode();
                method.visitFieldInsn(GETSTATIC, "me/cortex/voxy/client/TimingStatistics", "allSamplers", "Ljava/util/ArrayList;");
                method.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "iterator", "()Ljava/util/Iterator;", false);
                method.visitVarInsn(ASTORE, 0);
                Label loop = new Label();
                Label end = new Label();
                method.visitLabel(loop);
                method.visitFrame(F_APPEND, 1, new Object[]{"java/util/Iterator"}, 0, null);
                method.visitVarInsn(ALOAD, 0);
                method.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
                method.visitJumpInsn(IFEQ, end);
                method.visitVarInsn(ALOAD, 0);
                method.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
                method.visitTypeInsn(CHECKCAST, "me/cortex/voxy/client/TimingStatistics$TimeSampler");
                method.visitInsn(ICONST_0);
                method.visitFieldInsn(PUTFIELD, "me/cortex/voxy/client/TimingStatistics$TimeSampler", "running", "Z");
                method.visitJumpInsn(GOTO, loop);
                method.visitLabel(end);
                method.visitFrame(F_SAME, 0, null, 0, null);
                method.visitInsn(RETURN);
                method.visitMaxs(2, 1);
                method.visitEnd();
                super.visitEnd();
            }
        }, 0);
        return writer.toByteArray();
    }

    private static byte[] rewriteRenderSystem(ClassReader reader) {
        ClassWriter writer = new ClassWriter(reader, 0);
        reader.accept(new ClassVisitor(ASM9, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                return new MethodVisitor(ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                        if (opcode == INVOKEVIRTUAL
                                && "me/cortex/voxy/client/core/model/ModelBakerySubsystem".equals(owner)
                                && "tick".equals(name)
                                && "(J)V".equals(descriptor)) {
                            tickRewrites++;
                            super.visitMethodInsn(INVOKESTATIC, SAFETY, "guardedModelTick", "(Ljava/lang/Object;J)V", false);
                            return;
                        }
                        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    }
                };
            }
        }, 0);
        return writer.toByteArray();
    }

    private static long entryTime() {
        return 315532800000L;
    }
}
