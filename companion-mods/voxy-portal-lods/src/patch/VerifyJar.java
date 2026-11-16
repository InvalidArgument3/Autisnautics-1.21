import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.SimpleVerifier;
import org.objectweb.asm.util.CheckClassAdapter;

/**
 * Bytecode-verifies every class in a jar. {@link CheckClassAdapter#verify}
 * loads referenced types from the Mojmap client and server, NeoForge, and the
 * other jars named on the command line, including JarJar nests and
 * multi-release classes. A {@code Level} used as a {@code ClientLevel} fails.
 */
public final class VerifyJar {
    private static final int RUNTIME = Runtime.version().feature();

    private VerifyJar() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException("VerifyJar <jar> <classpath-entry>...");
        }
        Path jar = Path.of(args[0]);
        List<Path> classpath = new ArrayList<Path>();
        classpath.add(jar);
        for (int i = 1; i < args.length; i++) {
            for (String part : args[i].split(File.pathSeparator)) {
                if (!part.isEmpty()) {
                    Path path = Path.of(part);
                    if (!Files.exists(path)) {
                        throw new IllegalArgumentException("missing classpath entry " + path);
                    }
                    classpath.add(path);
                }
            }
        }
        IndexedLoader loader = new IndexedLoader(classpath);
        System.out.println("indexed " + loader.size() + " classes from " + classpath.size() + " entries");
        int checked = 0;
        int failed = 0;
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) {
                    continue;
                }
                byte[] data;
                try (InputStream input = zip.getInputStream(entry)) {
                    data = input.readAllBytes();
                }
                checked++;
                String text = verify(data, loader);
                if (!text.isBlank()) {
                    failed++;
                    System.out.println("FAIL " + entry.getName());
                    System.out.print(text);
                    if (!text.endsWith("\n")) {
                        System.out.println();
                    }
                }
            }
        }
        boolean linked = linkClientHandler(loader);
        if (failed > 0 || !linked) {
            System.out.println("verifier FAIL " + failed + " of " + checked + " classes in " + jar);
            System.exit(1);
        }
        System.out.println("verifier PASS " + checked + " classes in " + jar);
    }

    private static boolean linkClientHandler(ClassLoader loader) {
        String name = "me.cortex.voxy.common.network.TerrainSyncClientHandler";
        try {
            Class<?> handler = Class.forName(name, false, loader);
            Class.forName("me.cortex.voxy.client.TerrainReceive", false, loader);
            // HotSpot reports this VerifyError when the method is first used, not when the class is loaded.
            handler.getDeclaredMethod("tickDrainApplyQueue").invoke(null);
            System.out.println("link OK " + name + " tickDrainApplyQueue returned");
            return true;
        } catch (java.lang.reflect.InvocationTargetException thrown) {
            Throwable cause = thrown.getCause() == null ? thrown : thrown.getCause();
            if (cause instanceof VerifyError) {
                System.out.println("link FAIL " + name);
                cause.printStackTrace(System.out);
                return false;
            }
            System.out.println("link OK " + name + " ran until " + cause.getClass().getName());
            return true;
        } catch (Throwable thrown) {
            System.out.println("link FAIL " + name);
            thrown.printStackTrace(System.out);
            return false;
        }
    }

    private static String verify(byte[] data, ClassLoader loader) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        CheckClassAdapter.verify(new ClassReader(data), loader, false, printWriter);
        printWriter.flush();
        String text = writer.toString();
        if (text.isBlank()) {
            return "";
        }
        ClassNode node = new ClassNode();
        new ClassReader(data).accept(node, 0);
        StringBuilder named = new StringBuilder();
        List<Type> interfaces = new ArrayList<Type>();
        for (String interfaceName : node.interfaces) {
            interfaces.add(Type.getObjectType(interfaceName));
        }
        for (MethodNode method : node.methods) {
            SimpleVerifier verifier = new SimpleVerifier(
                    Type.getObjectType(node.name),
                    node.superName == null ? null : Type.getObjectType(node.superName),
                    interfaces,
                    (node.access & Opcodes.ACC_INTERFACE) != 0);
            verifier.setClassLoader(loader);
            Analyzer<BasicValue> analyzer = new Analyzer<BasicValue>(verifier);
            try {
                analyzer.analyze(node.name, method);
            } catch (AnalyzerException exception) {
                named.append(node.name).append('.').append(method.name).append(method.desc).append('\n');
                StringWriter methodWriter = new StringWriter();
                exception.printStackTrace(new PrintWriter(methodWriter));
                named.append(methodWriter);
            }
        }
        return named.length() == 0 ? text : named.toString();
    }

    private static final class IndexedLoader extends ClassLoader {
        private final Map<String, byte[]> classes = new LinkedHashMap<String, byte[]>();

        IndexedLoader(List<Path> jars) throws IOException {
            super(ClassLoader.getPlatformClassLoader());
            for (Path jar : jars) {
                if (Files.isDirectory(jar)) {
                    continue;
                }
                indexZip(Files.readAllBytes(jar));
            }
        }

        int size() {
            return classes.size();
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] data = classes.get(name.replace('.', '/'));
            if (data == null) {
                throw new ClassNotFoundException(name);
            }
            return defineClass(name, data, 0, data.length);
        }

        private void indexZip(byte[] jarBytes) throws IOException {
            Map<String, byte[]> local = new HashMap<String, byte[]>();
            Map<String, Integer> versions = new HashMap<String, Integer>();
            List<byte[]> nested = new ArrayList<byte[]>();
            try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(jarBytes))) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    if (entry.isDirectory()) {
                        continue;
                    }
                    String name = entry.getName();
                    byte[] data = zip.readAllBytes();
                    if (name.startsWith("META-INF/jarjar/") && name.endsWith(".jar")) {
                        nested.add(data);
                        continue;
                    }
                    ClassHit hit = classHit(name);
                    if (hit == null) {
                        continue;
                    }
                    Integer previous = versions.get(hit.name);
                    if (previous == null || hit.version > previous.intValue()) {
                        local.put(hit.name, data);
                        versions.put(hit.name, Integer.valueOf(hit.version));
                    }
                }
            }
            for (Map.Entry<String, byte[]> entry : local.entrySet()) {
                classes.putIfAbsent(entry.getKey(), entry.getValue());
            }
            for (byte[] child : nested) {
                indexZip(child);
            }
        }

        private static ClassHit classHit(String name) {
            if (!name.endsWith(".class")) {
                return null;
            }
            if (name.startsWith("META-INF/versions/")) {
                String rest = name.substring("META-INF/versions/".length());
                int slash = rest.indexOf('/');
                if (slash <= 0) {
                    return null;
                }
                int version;
                try {
                    version = Integer.parseInt(rest.substring(0, slash));
                } catch (NumberFormatException exception) {
                    return null;
                }
                if (version <= 0 || version > RUNTIME) {
                    return null;
                }
                String inner = rest.substring(slash + 1);
                if (!inner.endsWith(".class") || inner.startsWith("META-INF/")) {
                    return null;
                }
                return new ClassHit(inner.substring(0, inner.length() - ".class".length()), version);
            }
            if (name.startsWith("META-INF/")) {
                return null;
            }
            return new ClassHit(name.substring(0, name.length() - ".class".length()), 0);
        }
    }

    private static final class ClassHit {
        final String name;
        final int version;

        ClassHit(String name, int version) {
            this.name = name;
            this.version = version;
        }
    }
}
