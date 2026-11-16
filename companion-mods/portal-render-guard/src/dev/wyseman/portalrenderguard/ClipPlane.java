package dev.wyseman.portalrenderguard;

/**
 * Converts {@code n·(x - camera) + c} into an absolute-world plane.
 *
 * <p>Not used by the mixins. ipa10o's {@code blit_texture_depth_cutout.frag}
 * unprojects with {@code inverse(projection * modelView)}, and that modelView
 * is Sodium's camera pose, so the point is already camera-relative. Immersive
 * Portals' {@code getActiveClipPlaneEquationBeforeModelView} is the same space.
 * Shifting {@code c} by {@code -n·camera} moves the plane off the portal.
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
