package dev.wyseman.portalrenderguard;

/**
 * Immersive Portals publishes the portal clip as {@code n·(x - camera) + c}.
 * Voxy's blit reconstructs a world position, so {@code c} has to move with the camera.
 */
public final class ClipPlane {
    private ClipPlane() {}

    public static float[] shiftToWorld(float[] equation, double cameraX, double cameraY, double cameraZ) {
        if (equation == null || equation.length < 4) {
            return equation;
        }
        float[] shifted = equation.clone();
        shifted[3] = (float) (equation[3] - (equation[0] * cameraX + equation[1] * cameraY + equation[2] * cameraZ));
        return shifted;
    }
}
