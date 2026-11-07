import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.ContextFactory;
import dev.latvian.mods.rhino.Scriptable;

import java.nio.file.Files;
import java.nio.file.Path;

/** Evaluate scripts in one KubeJS Rhino 2101 scope. */
public class RhinoRun {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("usage: RhinoRun file.js ...");
            System.exit(2);
        }
        Context cx = new ContextFactory().enter();
        Scriptable scope = cx.initStandardObjects();
        try {
            for (String arg : args) {
                cx.evaluateString(scope, Files.readString(Path.of(arg)), arg, 1, null);
            }
            System.out.println("OK");
        } catch (Throwable thrown) {
            System.out.println("FAIL " + thrown.getClass().getName() + ": " + thrown.getMessage());
            thrown.printStackTrace(System.out);
            System.exit(1);
        }
    }
}
