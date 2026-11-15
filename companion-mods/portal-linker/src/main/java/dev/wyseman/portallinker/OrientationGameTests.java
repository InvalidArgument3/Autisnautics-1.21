package dev.wyseman.portallinker;

import com.mojang.authlib.GameProfile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import qouteall.imm_ptl.core.portal.nether_portal.GeneralBreakablePortal;

/**
 * Headless exit check. The dedicated server does not run the Immersive Portals camera, and a
 * player teleport does not write yaw. Each case moves a fake player with {@code transformPoint}
 * (the same mapping the server uses for the eye) and then applies the client camera formula.
 */
@GameTestHolder(PortalLinker.ID)
@PrefixGameTestTemplate(false)
public class OrientationGameTests {
    @GameTest(template = "pad", timeoutTicks = 200)
    public static void exitsStayUprightAndModesTurnTheView(GameTestHelper helper) {
        Report report = new Report();
        try {
            ServerLevel level = helper.getLevel();
            ServerPlayer player = FakePlayerFactory.get(level, new GameProfile(UUID.nameUUIDFromBytes("portal-linker-orient".getBytes()), "PortalExit"));
            Pair zz = link(helper, level, new BlockPos(2, 2, 2), Direction.Axis.Z, new BlockPos(2, 2, 10), Direction.Axis.Z, 1, 2);
            Pair xx = link(helper, level, new BlockPos(14, 2, 2), Direction.Axis.X, new BlockPos(22, 2, 2), Direction.Axis.X, 1, 2);
            Pair zx = link(helper, level, new BlockPos(2, 2, 18), Direction.Axis.Z, new BlockPos(14, 2, 18), Direction.Axis.X, 1, 2);
            Pair zy = link(helper, level, new BlockPos(28, 2, 2), Direction.Axis.Z, new BlockPos(28, 6, 12), Direction.Axis.Y, 1, 2);
            Pair yy = link(helper, level, new BlockPos(40, 6, 2), Direction.Axis.Y, new BlockPos(40, 6, 12), Direction.Axis.Y, 1, 2);
            Pair swapped = link(helper, level, new BlockPos(2, 2, 30), Direction.Axis.Z, new BlockPos(16, 2, 30), Direction.Axis.X, 3, 5, 5, 3);

            report.line("gravity changes by default " + facing(level, zz.id, zz.fromCenter, axis(Direction.Axis.Z)).getTeleportChangesGravity());
            bothWays(report, player, level, "Z-Z", zz, Direction.Axis.Z, Direction.Axis.Z, Expect.UPRIGHT);
            bothWays(report, player, level, "X-X", xx, Direction.Axis.X, Direction.Axis.X, Expect.UPRIGHT);
            bothWays(report, player, level, "Z-X", zx, Direction.Axis.Z, Direction.Axis.X, Expect.UPRIGHT);
            bothWays(report, player, level, "Z-Y", zy, Direction.Axis.Z, Direction.Axis.Y, Expect.UPRIGHT);
            bothWays(report, player, level, "Y-Y", yy, Direction.Axis.Y, Direction.Axis.Y, Expect.UPRIGHT);
            bothWays(report, player, level, "Z3x5-X5x3", swapped, Direction.Axis.Z, Direction.Axis.X, Expect.UPRIGHT);
            GeneralBreakablePortal wide = facing(level, swapped.id, swapped.fromCenter, axis(Direction.Axis.Z));
            GeneralBreakablePortal tall = facing(level, swapped.id, swapped.toCenter, axis(Direction.Axis.X));
            report.check("swapped Z width", Math.round(wide.getWidth()) == 3 && Math.round(wide.getHeight()) == 5, wide.getWidth() + "x" + wide.getHeight());
            report.check("swapped X width", Math.round(tall.getWidth()) == 5 && Math.round(tall.getHeight()) == 3, tall.getWidth() + "x" + tall.getHeight());
            yawOffsetPreserved(report, player, level, zz);

            itemKeepsMode(report, helper);
            sneakScroll(report, helper, level, player, xx);
            repairAfterBadRotation(report, helper, level, player, xx);
            eachMode(report, helper, level, player, zz);
        } catch (RuntimeException e) {
            report.line("FAIL exception " + e);
            report.failures.add(String.valueOf(e));
            throw e;
        } finally {
            report.flush();
        }
        helper.assertTrue(report.failures.isEmpty(), String.join(" | ", report.failures));
        helper.succeed();
    }

    private static void bothWays(Report report, ServerPlayer player, ServerLevel level, String name, Pair pair, Direction.Axis fromAxis, Direction.Axis toAxis, Expect expect) {
        Vec3 fromFront = axis(fromAxis);
        Vec3 toFront = axis(toAxis);
        pass(report, player, level, name + " front", pair.id, pair.fromCenter, fromFront, pair.toCenter, toFront, expect);
        pass(report, player, level, name + " back", pair.id, pair.fromCenter, fromFront.scale(-1), pair.toCenter, toFront.scale(-1), expect);
        pass(report, player, level, name + " return-front", pair.id, pair.toCenter, toFront, pair.fromCenter, fromFront, expect);
        pass(report, player, level, name + " return-back", pair.id, pair.toCenter, toFront.scale(-1), pair.fromCenter, fromFront.scale(-1), expect);
    }

    private static void pass(Report report, ServerPlayer player, ServerLevel level, String name, UUID id, Vec3 entryCenter, Vec3 entryNormal, Vec3 exitCenter, Vec3 exitNormal, Expect expect) {
        GeneralBreakablePortal portal = facing(level, id, entryCenter, entryNormal);
        if (portal == null) {
            report.check(name, false, "missing portal normal " + entryNormal);
            return;
        }
        float[] angles = ExitPose.anglesLooking(entryNormal.scale(-1));
        probe(report, player, name, portal, angles[0], angles[1], exitNormal, expect);
    }

    private static void probe(Report report, ServerPlayer player, String name, GeneralBreakablePortal portal, float entryPitch, float entryYaw, Vec3 exitNormal, Expect expect) {
        Vec3 eye = portal.getOriginPos().add(portal.getNormal().scale(-0.25));
        player.setPos(eye.x, eye.y - player.getEyeHeight(), eye.z);
        player.setXRot(entryPitch);
        player.setYRot(entryYaw);
        Vec3 before = player.position();
        report.line(name + " before pitch=" + fmt(player.getXRot()) + " yaw=" + fmt(player.getYRot()) + " pos=" + fmt(before) + " entryLook=" + fmt(Vec3.directionFromRotation(entryPitch, entryYaw)));
        Vec3 newEye = portal.transformPoint(eye);
        player.setPos(newEye.x, newEye.y - player.getEyeHeight(), newEye.z);
        ExitPose pose = ExitPose.through(portal, entryPitch, entryYaw);
        player.setXRot(pose.pitch);
        player.setYRot(pose.yaw);
        double side = newEye.subtract(portal.getDestPos()).dot(exitNormal);
        report.line(name
                + " after entity pitch=" + fmt(player.getXRot())
                + " yaw=" + fmt(player.getYRot())
                + " pos=" + fmt(player.position())
                + " camera pitch=" + fmt(pose.pitch)
                + " yaw=" + fmt(pose.yaw)
                + " roll=" + fmt(pose.roll)
                + " look=" + fmt(pose.look)
                + " up=" + fmt(pose.up)
                + " velLook=" + fmt(pose.velocityLook)
                + " gravity=" + portal.getTeleportChangesGravity()
                + " axisH=" + fmt(portal.getAxisH())
                + " side=" + fmt(side));
        boolean facing = pose.look.dot(exitNormal) > 0.9;
        boolean controls = pose.velocityLook.dot(pose.look) > 0.85;
        boolean out = side > 0.05;
        boolean gravityOff = !portal.getTeleportChangesGravity();
        boolean ok = switch (expect) {
            case UPRIGHT -> pose.roll < 8.0 && gravityOff && out && (exitNormal.y * exitNormal.y > 0.5
                    ? Math.abs(Math.abs(pose.pitch) - 90.0f) < 12.0f && facing && controls
                    : Math.abs(pose.pitch) < 12.0f && pose.up.y > 0.85 && facing && controls);
            case BACK -> pose.roll < 8.0 && gravityOff && out && pose.up.y > 0.85 && facing && controls && Math.abs(pose.pitch) < 12.0f;
            case ROLL_90, ROLL_270 -> gravityOff && Math.abs(pose.up.y) < 0.35;
            case ROLL_180, FLIP -> gravityOff && pose.up.y < -0.85;
        };
        report.check(name, ok, "pitch=" + fmt(pose.pitch) + " yaw=" + fmt(pose.yaw) + " roll=" + fmt(pose.roll) + " up=" + fmt(pose.up) + " look=" + fmt(pose.look));
    }

    private static void yawOffsetPreserved(Report report, ServerPlayer player, ServerLevel level, Pair zz) {
        GeneralBreakablePortal portal = facing(level, zz.id, zz.fromCenter, axis(Direction.Axis.Z));
        float[] angles = ExitPose.anglesLooking(new Vec3(0, 0, -1));
        ExitPose straight = ExitPose.through(portal, angles[0], angles[1]);
        ExitPose offset = ExitPose.through(portal, angles[0], angles[1] + 15.0f);
        float delta = wrap(offset.yaw - straight.yaw);
        report.line("yaw-offset straight=" + fmt(straight.yaw) + " offset=" + fmt(offset.yaw) + " delta=" + fmt(delta));
        report.check("yaw offset 15", Math.abs(delta - 15.0f) < 3.0f, "delta " + delta);
    }

    private static void itemKeepsMode(Report report, GameTestHelper helper) {
        ItemStack stack = new ItemStack(ModItems.LINKER.get());
        LinkerItems.cycle(stack, 1);
        report.check("item cycles to back", LinkerItems.orientation(stack) == LinkOrientation.BACK, String.valueOf(LinkerItems.orientation(stack)));
        LinkerItems.write(stack, UUID.randomUUID(), 1, 2, "minecraft:overworld");
        LinkerItems.clear(stack);
        report.check("clear keeps mode", LinkerItems.token(stack) == null && LinkerItems.orientation(stack) == LinkOrientation.BACK, String.valueOf(LinkerItems.orientation(stack)));
        helper.assertTrue(true, "item");
    }

    private static void sneakScroll(Report report, GameTestHelper helper, ServerLevel level, ServerPlayer player, Pair xx) {
        Links.Link link = Links.get(level.getServer()).linkAt(level, xx.fromClick);
        ItemStack stack = new ItemStack(ModItems.LINKER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.setPos(xx.fromClick.getX() + 0.5, xx.fromClick.getY(), xx.fromClick.getZ() + 0.5);
        player.setShiftKeyDown(false);
        PortalLinkerNet.apply(player, xx.fromClick, true, 1);
        report.check("scroll ignored without sneak", link.orientation == LinkOrientation.FRONT, String.valueOf(link.orientation));
        player.setShiftKeyDown(true);
        PortalLinkerNet.apply(player, xx.fromClick, true, 1);
        report.check("sneak-scroll on frame", link.orientation == LinkOrientation.BACK && LinkerItems.orientation(stack) == LinkOrientation.FRONT, link.orientation + " item " + LinkerItems.orientation(stack));
        PortalLinkerNet.apply(player, BlockPos.ZERO, false, 1);
        report.check("sneak-scroll in air", LinkerItems.orientation(stack) == LinkOrientation.BACK, String.valueOf(LinkerItems.orientation(stack)));
        link.orientation = LinkOrientation.FRONT;
        link.orientSynced = false;
        report.check("restore front", LinkRotation.sync(level.getServer(), link), "sync");
        link.orientSynced = true;
        GeneralBreakablePortal portal = facing(level, xx.id, xx.fromCenter, axis(Direction.Axis.X));
        probe(report, player, "X-X restored", portal, 0.0f, 90.0f, axis(Direction.Axis.X), Expect.UPRIGHT);
        helper.assertTrue(link.orientation == LinkOrientation.FRONT, "restored");
    }

    private static void repairAfterBadRotation(Report report, GameTestHelper helper, ServerLevel level, ServerPlayer player, Pair xx) {
        GeneralBreakablePortal portal = facing(level, xx.id, xx.fromCenter, axis(Direction.Axis.X));
        portal.setRotation(qouteall.q_misc_util.my_util.DQuaternion.rotationByDegrees(new Vec3(0, 0, 1), 180));
        Links.Link link = Links.get(level.getServer()).linkAt(level, xx.fromClick);
        link.orientation = LinkOrientation.FRONT;
        link.orientSynced = false;
        Links.get(level.getServer()).sweep(level.getServer());
        report.check("sweep realigns", link.orientSynced, "flag");
        portal = facing(level, xx.id, xx.fromCenter, axis(Direction.Axis.X));
        probe(report, player, "X-X repaired", portal, 0.0f, 90.0f, axis(Direction.Axis.X), Expect.UPRIGHT);
        helper.assertTrue(link.orientSynced, "repaired");
    }

    private static void eachMode(Report report, GameTestHelper helper, ServerLevel level, ServerPlayer player, Pair zz) {
        Links.Link link = Links.get(level.getServer()).linkAt(level, zz.fromClick);
        LinkOrientation mode = LinkOrientation.FRONT;
        for (int i = 0; i < LinkOrientation.values().length; i++) {
            GeneralBreakablePortal portal = facing(level, zz.id, zz.fromCenter, axis(Direction.Axis.Z));
            Expect expect = switch (mode) {
                case FRONT -> Expect.UPRIGHT;
                case BACK -> Expect.BACK;
                case ROLL_90 -> Expect.ROLL_90;
                case ROLL_180 -> Expect.ROLL_180;
                case ROLL_270 -> Expect.ROLL_270;
                case FLIP -> Expect.FLIP;
            };
            Vec3 exit = mode == LinkOrientation.BACK ? axis(Direction.Axis.Z).scale(-1) : axis(Direction.Axis.Z);
            probe(report, player, "mode " + mode, portal, 0.0f, 180.0f, exit, expect);
            if (i + 1 < LinkOrientation.values().length) {
                LinkService.Attempt attempt = LinkService.retune(level, zz.fromClick, 1);
                mode = mode.cycle(1);
                report.check("retune " + mode, attempt.success() && link.orientation == mode, attempt.key() + " " + link.orientation);
            }
        }
        helper.assertTrue(link.orientation == LinkOrientation.FLIP, "ended on flip");
    }

    private static Pair link(GameTestHelper helper, ServerLevel level, BlockPos from, Direction.Axis fromAxis, BlockPos to, Direction.Axis toAxis, int u, int v) {
        return link(helper, level, from, fromAxis, to, toAxis, u, v, u, v);
    }

    private static Pair link(GameTestHelper helper, ServerLevel level, BlockPos from, Direction.Axis fromAxis, BlockPos to, Direction.Axis toAxis, int u, int v, int u2, int v2) {
        BlockPos first = place(helper, from, fromAxis, u, v);
        BlockPos second = place(helper, to, toAxis, u2, v2);
        ItemStack stack = new ItemStack(ModItems.LINKER.get());
        LinkService.Attempt stored = LinkService.use(null, level, first, stack);
        helper.assertTrue(stored.success(), stored.key());
        LinkService.Attempt linked = LinkService.use(null, level, second, stack);
        helper.assertTrue(linked.success(), linked.key());
        return new Pair(linked.linkId(), center(helper, from, fromAxis, u, v), center(helper, to, toAxis, u2, v2), first);
    }

    private static GeneralBreakablePortal facing(ServerLevel level, UUID id, Vec3 center, Vec3 normal) {
        GeneralBreakablePortal best = null;
        double bestDistance = 9.0;
        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof GeneralBreakablePortal portal) || portal.isRemoved() || !id.toString().equals(portal.portalTag)) {
                continue;
            }
            if (portal.getNormal().dot(normal) < 0.9) {
                continue;
            }
            double distance = portal.getOriginPos().distanceToSqr(center);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = portal;
            }
        }
        return best;
    }

    private static Vec3 center(GameTestHelper helper, BlockPos origin, Direction.Axis axis, int u, int v) {
        BlockPos corner = helper.absolutePos(origin);
        return switch (axis) {
            case Z -> new Vec3(corner.getX() + u / 2.0, corner.getY() + v / 2.0, corner.getZ() + 0.5);
            case X -> new Vec3(corner.getX() + 0.5, corner.getY() + v / 2.0, corner.getZ() + u / 2.0);
            case Y -> new Vec3(corner.getX() + u / 2.0, corner.getY() + 0.5, corner.getZ() + v / 2.0);
        };
    }

    private static Vec3 axis(Direction.Axis axis) {
        return switch (axis) {
            case X -> new Vec3(1, 0, 0);
            case Y -> new Vec3(0, 1, 0);
            case Z -> new Vec3(0, 0, 1);
        };
    }

    private static BlockPos place(GameTestHelper helper, BlockPos origin, Direction.Axis axis, int u, int v) {
        ServerLevel level = helper.getLevel();
        BlockPos click = null;
        BlockState frame = Blocks.CRYING_OBSIDIAN.defaultBlockState();
        for (int i = -1; i <= u; i++) {
            for (int j = -1; j <= v; j++) {
                BlockPos abs = helper.absolutePos(at(origin, axis, i, j));
                boolean interior = i >= 0 && i < u && j >= 0 && j < v;
                level.setBlockAndUpdate(abs, interior ? Blocks.AIR.defaultBlockState() : frame);
                if (!interior && i == -1 && j == 0) {
                    click = abs;
                }
            }
        }
        return click;
    }

    private static BlockPos at(BlockPos origin, Direction.Axis axis, int i, int j) {
        return switch (axis) {
            case X -> origin.offset(0, j, i);
            case Y -> origin.offset(i, 0, j);
            case Z -> origin.offset(i, j, 0);
        };
    }

    private static float wrap(float degrees) {
        float wrapped = degrees % 360.0f;
        if (wrapped > 180.0f) {
            wrapped -= 360.0f;
        }
        if (wrapped < -180.0f) {
            wrapped += 360.0f;
        }
        return wrapped;
    }

    private static String fmt(double value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    private static String fmt(Vec3 value) {
        return "(" + fmt(value.x) + "," + fmt(value.y) + "," + fmt(value.z) + ")";
    }

    private enum Expect {
        UPRIGHT, BACK, ROLL_90, ROLL_180, ROLL_270, FLIP
    }

    private record Pair(UUID id, Vec3 fromCenter, Vec3 toCenter, BlockPos fromClick) {}

    private static final class Report {
        final List<String> failures = new ArrayList<>();
        final StringBuilder body = new StringBuilder();

        void line(String text) {
            body.append(text).append('\n');
            PortalLinker.LOG.info(text);
        }

        void check(String name, boolean ok, String detail) {
            line((ok ? "PASS " : "FAIL ") + name + " " + detail);
            if (!ok) {
                failures.add(name + " " + detail);
            }
        }

        void flush() {
            try {
                Path report = Path.of("/workspace/companion-mods/portal-linker/build/reports/portal-orientation.log");
                Path artifact = Path.of("/opt/cursor/artifacts/portal-orientation.log");
                Files.createDirectories(report.getParent());
                Files.createDirectories(artifact.getParent());
                Files.writeString(report, body.toString());
                Files.writeString(artifact, body.toString());
            } catch (java.io.IOException e) {
                PortalLinker.LOG.error("Could not write the orientation log", e);
            }
        }
    }
}
