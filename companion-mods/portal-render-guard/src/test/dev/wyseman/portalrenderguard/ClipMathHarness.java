package dev.wyseman.portalrenderguard;

public final class ClipMathHarness {
    public static void main(String[] args) {
        // Horizontal plane at world Y=64, camera at Y=100, normal +Y.
        // Camera-relative c = -n·(portal - camera) = -(64 - 100) = 36.
        float[] relative = new float[] {0f, 1f, 0f, 36f};
        float[] world = ClipPlane.shiftToWorld(relative, 0, 100, 0);
        expect(world[3], -64f, "plane constant");
        expect(dot(world, 0, 64, 0), 0f, "on the plane");
        expect(dot(world, 0, 80, 0), 16f, "above the plane");
        expect(dot(world, 10, 63, -4), -1f, "below the plane");
        if (ClipPlane.shiftToWorld(null, 0, 0, 0) != null) {
            throw new AssertionError("null equation");
        }
        float[] zero = ClipPlane.shiftToWorld(new float[] {0, 0, 0, 0}, 1000, 2000, 3000);
        expect(zero[3], 0f, "disabled plane stays zero");
        System.out.println("clip math ok");
    }

    private static float dot(float[] plane, float x, float y, float z) {
        return plane[0] * x + plane[1] * y + plane[2] * z + plane[3];
    }

    private static void expect(float actual, float expected, String label) {
        if (actual != expected) {
            throw new AssertionError(label + " got " + actual + " wanted " + expected);
        }
    }
}
