package dev.wyseman.portallinker;

import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec3;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.q_misc_util.my_util.DQuaternion;

/**
 * What a player's view does when Immersive Portals applies a portal rotation.
 * Dedicated servers do not run the client camera, so this is the same math as
 * {@code TransformationManager.managePlayerRotationAndChangeGravity} with gravity left alone
 * (the default: portals do not change gravity).
 */
public final class ExitPose {
    public final float pitch;
    public final float yaw;
    /** Residual the client keeps as a view roll, in degrees, after pitch and yaw are stored on the player. */
    public final double roll;
    /** Vanilla look from the stored pitch and yaw. */
    public final Vec3 look;
    /** Up vector of the full camera quaternion, including the residual roll. */
    public final Vec3 up;
    /** Entry look direction after {@code portal.rotation}, which is the direction movement takes. */
    public final Vec3 velocityLook;

    public ExitPose(float pitch, float yaw, double roll, Vec3 look, Vec3 up, Vec3 velocityLook) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
        this.look = look;
        this.up = up;
        this.velocityLook = velocityLook;
    }

    public static ExitPose through(Portal portal, float entryPitch, float entryYaw) {
        DQuaternion rotation = DQuaternion.fromNullable(portal.getRotation());
        DQuaternion oldCamera = DQuaternion.getCameraRotation(entryPitch, entryYaw);
        DQuaternion immediate = oldCamera.hamiltonProduct(rotation.getConjugated());
        Tuple<Double, Double> extracted = DQuaternion.getPitchYawFromRotation(immediate);
        float pitch = foldPitch(extracted.getA().floatValue());
        float yaw = extracted.getB().floatValue();
        DQuaternion reconstructed = DQuaternion.getCameraRotation(pitch, yaw);
        DQuaternion residual = reconstructed.getConjugated().hamiltonProduct(immediate);
        double roll = smallestAngle(residual);
        Vec3 entryLook = Vec3.directionFromRotation(entryPitch, entryYaw);
        return new ExitPose(pitch, yaw, roll, Vec3.directionFromRotation(pitch, yaw), immediate.rotate(new Vec3(0.0, 1.0, 0.0)), rotation.rotate(entryLook));
    }

    /** Same fold as the Immersive Portals client, which stores pitch in [-90, 90]. */
    public static float foldPitch(float pitch) {
        if (pitch > 90.0f) {
            return 90.0f - (pitch - 90.0f);
        }
        if (pitch < -90.0f) {
            return -90.0f + (-90.0f - pitch);
        }
        return pitch;
    }

    /** Identity and its negative quaternion are the same rotation, so a stored roll of 360 is 0. */
    public static double smallestAngle(DQuaternion quaternion) {
        double w = Math.abs(quaternion.getW());
        if (w > 1.0) {
            w = 1.0;
        }
        return Math.toDegrees(Math.acos(w) * 2.0);
    }

    public static float[] anglesLooking(Vec3 look) {
        Vec3 direction = look.normalize();
        float pitch = (float) Math.toDegrees(Math.asin(clamp(-direction.y, -1.0, 1.0)));
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        return new float[]{pitch, yaw};
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
