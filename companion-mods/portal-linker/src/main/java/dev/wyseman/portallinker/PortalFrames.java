package dev.wyseman.portallinker;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.nether_portal.BlockPortalShape;

/**
 * Portal basis for a rectangular frame.
 *
 * <p>Immersive Portals' {@code initPortalPosAxisShape(POSITIVE)} asks
 * {@code Helper.getPerpendicularDirections} for the in-plane axes. For an east-facing
 * vertical frame that puts world-up on {@code axisW} and a horizontal vector on
 * {@code axisH}, and it swaps width and height. {@code adjustRotationToConnect} then
 * turns 180° around that horizontal {@code axisH}, which is a roll: the player comes
 * out upside down and the controls invert. A south-facing frame happens to get
 * {@code axisH} = world up, so the same call only yaws.
 *
 * <p>Vertical frames here always use {@code axisH} = world up and {@code axisW} =
 * up × front, so width is the horizontal edge and height is the vertical edge.
 * Flat frames use a fixed in-plane basis whose cross product is world up. The front
 * of every frame is the positive axis direction. The other face is the flipped portal.
 */
public final class PortalFrames {
    private PortalFrames() {}

    public static void align(Portal portal, BlockPortalShape shape) {
        BlockPos size = shape.innerAreaBox.getSize();
        if (shape.axis == Direction.Axis.Y) {
            // axisW = west, axisH = south. west × south = up.
            portal.setOrientationAndSize(new Vec3(-1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0), size.getX(), size.getZ());
            return;
        }
        Vec3 normal = Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(shape.axis, Direction.AxisDirection.POSITIVE).getNormal());
        Vec3 axisH = new Vec3(0.0, 1.0, 0.0);
        Vec3 axisW = axisH.cross(normal);
        double width = shape.axis == Direction.Axis.X ? size.getZ() : size.getX();
        double height = size.getY();
        portal.setOrientationAndSize(axisW, axisH, width, height);
    }

    public static boolean positiveNormal(Direction.Axis axis, Vec3 normal) {
        return switch (axis) {
            case X -> normal.x > 0.0;
            case Y -> normal.y > 0.0;
            case Z -> normal.z > 0.0;
        };
    }
}
