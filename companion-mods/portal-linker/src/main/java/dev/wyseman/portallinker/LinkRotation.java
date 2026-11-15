package dev.wyseman.portallinker;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.PortalManipulation;
import qouteall.imm_ptl.core.portal.nether_portal.GeneralBreakablePortal;
import qouteall.q_misc_util.my_util.DQuaternion;

/**
 * Rotation passed to Immersive Portals.
 *
 * <p>{@code computeDeltaTransformation} is {@code destOrientation * rotY(180) * conj(srcOrientation)}.
 * With {@code axisH} = world up on both vertical frames, that 180° is a yaw: the player stays
 * upright and walks out facing the destination front. {@code adjustRotationToConnect} instead
 * does {@code rot(dest.axisH, 180) * dest * conj(src)}, which rolls whenever {@code axisH} is
 * not world up.
 */
public final class LinkRotation {
    private LinkRotation() {}

    public static DQuaternion between(Portal from, Portal to, LinkOrientation orientation) {
        DQuaternion base = connect(from, to);
        DQuaternion extra = orientation.extra(to);
        if (extra == null) {
            return base;
        }
        return extra.hamiltonProduct(base);
    }

    /**
     * Upright connection, as the rotation stored on {@code from}. {@code to} stores the conjugate.
     *
     * <p>Two standing frames, or two flat ones, use {@code computeDeltaTransformation} directly.
     * A standing frame linked to a flat one needs an extra half-turn about world up on the
     * standing portal. Without it, walking off the floor into the standing frame keeps the
     * movement aimed out the front but leaves the camera upside down and looking the other way.
     * The half-turn is on the standing side so the two rotations stay inverses.
     */
    private static DQuaternion connect(Portal from, Portal to) {
        boolean fromFlat = Math.abs(from.getNormal().y) > 0.5;
        boolean toFlat = Math.abs(to.getNormal().y) > 0.5;
        DQuaternion base = PortalManipulation.computeDeltaTransformation(from.getOrientationRotation(), to.getOrientationRotation());
        if (fromFlat == toFlat) {
            return base;
        }
        DQuaternion yaw = DQuaternion.rotationByDegrees(new Vec3(0.0, 1.0, 0.0), 180.0);
        if (!fromFlat) {
            return yaw.hamiltonProduct(base);
        }
        DQuaternion standing = yaw.hamiltonProduct(PortalManipulation.computeDeltaTransformation(to.getOrientationRotation(), from.getOrientationRotation()));
        return standing.getConjugated();
    }

    /** Writes the link's rotation onto its four portals once they are all loaded. */
    public static boolean sync(MinecraftServer server, Links.Link link) {
        if (link.portals.size() != 4) {
            return false;
        }
        List<GeneralBreakablePortal> loaded = new ArrayList<>(4);
        for (Links.PortalRef ref : link.portals) {
            ServerLevel level = server.getLevel(ref.dimension());
            if (level == null) {
                return false;
            }
            Entity entity = level.getEntity(ref.id());
            if (!(entity instanceof GeneralBreakablePortal portal) || portal.isRemoved()) {
                return false;
            }
            loaded.add(portal);
        }
        Vec3 fromCenter = center(link.from);
        Vec3 toCenter = center(link.to);
        GeneralBreakablePortal fromPositive = null;
        GeneralBreakablePortal fromNegative = null;
        GeneralBreakablePortal toPositive = null;
        GeneralBreakablePortal toNegative = null;
        for (GeneralBreakablePortal portal : loaded) {
            boolean fromSide = onSide(portal, link.from, link.to, fromCenter, toCenter);
            boolean positive = PortalFrames.positiveNormal(fromSide ? link.from.axis : link.to.axis, portal.getNormal());
            if (fromSide && positive) {
                fromPositive = portal;
            } else if (fromSide) {
                fromNegative = portal;
            } else if (positive) {
                toPositive = portal;
            } else {
                toNegative = portal;
            }
        }
        if (fromPositive == null || fromNegative == null || toPositive == null || toNegative == null) {
            return false;
        }
        PortalFrames.align(fromPositive, link.from.shape());
        PortalFrames.align(toPositive, link.to.shape());
        DQuaternion rotation = between(fromPositive, toPositive, link.orientation);
        applyPair(fromPositive, fromNegative, rotation);
        applyPair(toPositive, toNegative, rotation.getConjugated());
        return true;
    }

    private static boolean onSide(Portal portal, FrameEnd from, FrameEnd to, Vec3 fromCenter, Vec3 toCenter) {
        boolean inFrom = portal.level().dimension().equals(from.dimension);
        boolean inTo = portal.level().dimension().equals(to.dimension);
        if (inFrom && !inTo) {
            return true;
        }
        if (inTo && !inFrom) {
            return false;
        }
        return portal.getOriginPos().distanceToSqr(fromCenter) <= portal.getOriginPos().distanceToSqr(toCenter);
    }

    private static Vec3 center(FrameEnd end) {
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        int n = 0;
        for (BlockPos pos : end.interior) {
            x += pos.getX() + 0.5;
            y += pos.getY() + 0.5;
            z += pos.getZ() + 0.5;
            n++;
        }
        if (n == 0) {
            return Vec3.atCenterOf(end.anchor);
        }
        return new Vec3(x / n, y / n, z / n);
    }

    private static void applyPair(GeneralBreakablePortal positive, GeneralBreakablePortal negative, DQuaternion rotation) {
        positive.setRotation(rotation);
        copyFlippedBody(negative, positive);
        negative.setRotation(rotation);
        positive.updateCache();
        negative.updateCache();
        if (!positive.level().isClientSide()) {
            positive.reloadAndSyncToClient();
            negative.reloadAndSyncToClient();
        }
    }

    /** Matches {@code PortalManipulation.createFlippedPortal}: negate axisW, keep axisH, same size. */
    private static void copyFlippedBody(Portal negative, Portal positive) {
        Vec3 axisW = positive.getAxisW().scale(-1.0);
        negative.setOrientationAndSize(axisW, positive.getAxisH(), positive.getWidth(), positive.getHeight());
    }
}
