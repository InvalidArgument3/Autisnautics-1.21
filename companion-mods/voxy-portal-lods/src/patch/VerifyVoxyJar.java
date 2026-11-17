import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Loads every class in a jar through the application class loader, which is the
 * loader that actually runs the JVM verifier. {@code Class.forName(name, false)}
 * does not verify method bodies on HotSpot, so the declared members are resolved
 * too. That is what reports a bad {@code <clinit>} stack map. Launch this with
 * the jar, the 1.21.1 client, and the 1.21.1 server already on {@code -cp},
 * under Java 25.
 */
public final class VerifyVoxyJar {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException("VerifyVoxyJar <jar>");
        }
        int checked = 0;
        int bad = 0;
        int optional = 0;
        try (ZipFile zip = new ZipFile(Path.of(args[0]).toFile())) {
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
                    Class<?> type = Class.forName(name, false, VerifyVoxyJar.class.getClassLoader());
                    type.getDeclaredFields();
                    type.getDeclaredConstructors();
                    type.getDeclaredMethods();
                } catch (VerifyError error) {
                    bad++;
                    System.out.println("VERIFY " + name);
                    System.out.println("  " + firstLine(error.getMessage()));
                } catch (Throwable error) {
                    String missing = error.getMessage() == null ? "" : error.getMessage();
                    if (missing.contains("sodium") || missing.contains("iris") || missing.contains("cglib")) {
                        optional++;
                        System.out.println("OPTIONAL " + name + " " + error.getClass().getSimpleName() + ": " + missing);
                        continue;
                    }
                    bad++;
                    System.out.println("LINK " + name + " " + error.getClass().getSimpleName() + ": " + missing);
                }
            }
        }
        System.out.println("checked " + checked + " optional " + optional + " failed " + bad);
        if (bad > 0) {
            throw new IllegalStateException(bad + " classes failed verification");
        }
    }

    private static String firstLine(String message) {
        if (message == null) {
            return "";
        }
        int newline = message.indexOf('\n');
        return newline < 0 ? message : message.substring(0, newline);
    }
}
