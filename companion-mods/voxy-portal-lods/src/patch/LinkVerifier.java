import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Loads every class in a Voxy jar on the application classpath under
 * {@code -Xverify:all}. HotSpot does not verify a method body, including
 * {@code <clinit>}, on {@code Class.forName(name, false)}. Reading the
 * declared members forces that verification. The two classes named by the
 * join crash are then initialized.
 */
public final class LinkVerifier {
    static final String ASYNC = "me.cortex.voxy.client.core.rendering.hierachical.AsyncNodeManager";
    static final String RENDER = "me.cortex.voxy.client.core.VoxyRenderSystem";
    static final String NEEDLE = "is not assignable to 'java/lang/Throwable'";

    public static void main(String[] args) throws Exception {
        boolean expect = false;
        int index = 0;
        if (args.length > 0 && "--expect-verify-error".equals(args[0])) {
            expect = true;
            index = 1;
        }
        if (args.length < index + 1) {
            throw new IllegalArgumentException("LinkVerifier [--expect-verify-error] <jar>");
        }
        Path jar = Path.of(args[index]);
        ClassLoader loader = LinkVerifier.class.getClassLoader();
        prepareClientDist(loader);
        int checked = 0;
        int optional = 0;
        int failed = 0;
        VerifyError asyncError = null;
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String file = entry.getName();
                if (!file.endsWith(".class") || file.startsWith("META-INF/") || file.contains("module-info")) {
                    continue;
                }
                String name = file.substring(0, file.length() - 6).replace('/', '.');
                checked++;
                try {
                    link(name, loader);
                } catch (VerifyError error) {
                    failed++;
                    if (ASYNC.equals(name)) {
                        asyncError = error;
                    }
                    System.out.println("VERIFY " + name);
                    System.out.println(error.getMessage());
                } catch (Throwable error) {
                    String missing = text(error);
                    if (optional(missing)) {
                        optional++;
                        System.out.println("OPTIONAL " + name + " " + error.getClass().getSimpleName() + ": " + missing);
                        continue;
                    }
                    failed++;
                    System.out.println("LINK " + name + " " + error.getClass().getSimpleName() + ": " + missing);
                }
            }
        }
        String asyncInit = initialize(ASYNC, loader);
        String renderInit = initialize(RENDER, loader);
        System.out.println("checked " + checked + " optional " + optional + " failed " + failed);
        System.out.println("clinit " + ASYNC + " " + asyncInit);
        System.out.println("clinit " + RENDER + " " + renderInit);
        boolean saw = containsNeedle(asyncError) || asyncInit.contains(NEEDLE);
        if (expect) {
            if (!saw) {
                throw new IllegalStateException("ipa10y jar did not fail AsyncNodeManager.<clinit> with the Throwable frame error");
            }
            System.out.println("link EXPECT_FAIL AsyncNodeManager.<clinit> VerifyError");
            return;
        }
        if (failed > 0 || asyncInit.startsWith("FAIL") || renderInit.startsWith("FAIL")) {
            throw new IllegalStateException("link verifier failed");
        }
        System.out.println("link PASS " + checked + " classes in " + jar);
    }

    private static void link(String name, ClassLoader loader) throws Exception {
        Class<?> type = Class.forName(name, false, loader);
        type.getDeclaredFields();
        type.getDeclaredConstructors();
        type.getDeclaredMethods();
    }

    private static String initialize(String name, ClassLoader loader) {
        try {
            Class.forName(name, true, loader);
            return "OK";
        } catch (Throwable error) {
            System.out.println("CLINIT " + name);
            printChain(error);
            return "FAIL " + summary(error);
        }
    }

    private static void prepareClientDist(ClassLoader loader) throws Exception {
        Class<?> fml = Class.forName("net.neoforged.fml.loading.FMLLoader", false, loader);
        Field dist = fml.getDeclaredField("dist");
        dist.setAccessible(true);
        if (dist.get(null) != null) {
            return;
        }
        @SuppressWarnings({"unchecked", "rawtypes"})
        Class distEnum = Class.forName("net.neoforged.api.distmarker.Dist", false, loader);
        @SuppressWarnings({"unchecked", "rawtypes"})
        Object client = Enum.valueOf(distEnum, "CLIENT");
        dist.set(null, client);
        System.out.println("FMLLoader.dist=" + dist.get(null));
    }

    private static boolean optional(String missing) {
        return missing.contains("sodium") || missing.contains("iris") || missing.contains("cglib");
    }

    private static boolean containsNeedle(Throwable error) {
        while (error != null) {
            String message = error.getMessage();
            if (message != null && message.contains(NEEDLE) && message.contains("AsyncNodeManager.<clinit>")) {
                return true;
            }
            error = error.getCause();
        }
        return false;
    }

    private static String summary(Throwable error) {
        StringBuilder text = new StringBuilder();
        Throwable cursor = error;
        while (cursor != null) {
            if (text.length() > 0) {
                text.append(" caused by ");
            }
            text.append(cursor.getClass().getName());
            if (cursor.getMessage() != null) {
                String line = cursor.getMessage();
                int newline = line.indexOf('\n');
                text.append(": ").append(newline < 0 ? line : line.substring(0, newline));
            }
            cursor = cursor.getCause();
        }
        return text.toString();
    }

    private static void printChain(Throwable error) {
        Throwable cursor = error;
        while (cursor != null) {
            System.out.println(cursor.getClass().getName() + ": " + cursor.getMessage());
            cursor = cursor.getCause();
        }
    }

    private static String text(Throwable error) {
        String message = error.getMessage();
        return message == null ? "" : message;
    }
}
