package dev.wyseman.portallinker;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import qouteall.imm_ptl.core.portal.PortalPlaceholderBlock;
import qouteall.imm_ptl.core.portal.nether_portal.GeneralBreakablePortal;

@GameTestHolder(PortalLinker.ID)
@PrefixGameTestTemplate(false)
public class FrameGameTests {
    private static final int MAX = 32;

    @GameTest(template = "pad", timeoutTicks = 100)
    public static void openingsFromOneByTwoThroughThirtyTwo(GameTestHelper helper) {
        for (Direction.Axis axis : Direction.Axis.values()) {
            expect(helper, axis, 1, 2, FrameFinder.Status.FOUND);
            expect(helper, axis, 2, 3, FrameFinder.Status.FOUND);
            expect(helper, axis, 32, 32, FrameFinder.Status.FOUND);
            expect(helper, axis, 1, 32, FrameFinder.Status.FOUND);
        }
        expect(helper, Direction.Axis.Z, 1, 1, FrameFinder.Status.TOO_SMALL);
        expect(helper, Direction.Axis.Z, 33, 2, FrameFinder.Status.TOO_BIG);
        expect(helper, Direction.Axis.X, 32, 33, FrameFinder.Status.TOO_BIG);
        expect(helper, Direction.Axis.Y, 33, 33, FrameFinder.Status.TOO_BIG);
        expectMissingCorner(helper);
        expectGap(helper);
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse("portal_linker:portal_linker")), "linker item");
        helper.assertTrue(Config.MAX_INTERIOR.getDefault() == 32, "max default");
        helper.assertTrue(
                helper.getLevel().getServer().getRecipeManager().byKey(ResourceLocation.parse("portal_linker:portal_linker")).isPresent(),
                "recipe");
        helper.succeed();
    }

    @GameTest(template = "pad", timeoutTicks = 200)
    public static void linkedPairSurvivesTicksAndBreaksTogether(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        UUID same = link(helper, level, new BlockPos(2, 2, 2), Direction.Axis.Z, new BlockPos(2, 2, 12), Direction.Axis.Z, 1, 2);
        UUID crossed = link(helper, level, new BlockPos(18, 2, 2), Direction.Axis.Z, new BlockPos(18, 2, 16), Direction.Axis.X, 2, 3);
        UUID flat = link(helper, level, new BlockPos(36, 4, 2), Direction.Axis.Y, new BlockPos(36, 2, 16), Direction.Axis.Z, 2, 2);
        assertLive(helper, level, same, Direction.Axis.Z, Direction.Axis.Z, 1, 2);
        assertLive(helper, level, crossed, Direction.Axis.Z, Direction.Axis.X, 2, 3);
        assertLive(helper, level, flat, Direction.Axis.Y, Direction.Axis.Z, 2, 2);
        helper.runAtTickTime(20, () -> {
            assertLive(helper, level, same, Direction.Axis.Z, Direction.Axis.Z, 1, 2);
            assertLive(helper, level, crossed, Direction.Axis.Z, Direction.Axis.X, 2, 3);
            assertLive(helper, level, flat, Direction.Axis.Y, Direction.Axis.Z, 2, 2);
            BlockPos frame = helper.absolutePos(new BlockPos(1, 2, 2));
            level.setBlockAndUpdate(frame, Blocks.AIR.defaultBlockState());
            helper.assertTrue(count(level, same) == 0, "same-axis portals left " + count(level, same));
            helper.assertTrue(count(level, crossed) == 4, "crossed portals were removed");
            BlockPos interior = helper.absolutePos(new BlockPos(2, 2, 2));
            helper.assertTrue(level.getBlockState(interior).isAir(), "placeholder left behind");
            helper.succeed();
        });
    }

    private static UUID link(GameTestHelper helper, ServerLevel level, BlockPos from, Direction.Axis fromAxis, BlockPos to, Direction.Axis toAxis, int u, int v) {
        BlockPos first = place(helper, from, fromAxis, u, v);
        BlockPos second = place(helper, to, toAxis, u, v);
        ItemStack stack = new ItemStack(ModItems.LINKER.get());
        LinkService.Attempt stored = LinkService.use(null, level, first, stack);
        helper.assertTrue(stored.success(), stored.key());
        LinkService.Attempt linked = LinkService.use(null, level, second, stack);
        helper.assertTrue(linked.success(), linked.key());
        helper.assertTrue(linked.linkId() != null, "missing link id");
        BlockPos interior = helper.absolutePos(at(from, fromAxis, 0, 0));
        helper.assertTrue(level.getBlockState(interior).getBlock() == PortalPlaceholderBlock.instance, "placeholder");
        return linked.linkId();
    }

    private static void assertLive(GameTestHelper helper, ServerLevel level, UUID id, Direction.Axis from, Direction.Axis to, int u, int v) {
        int count = 0;
        Set<Direction.Axis> axes = new HashSet<>();
        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof GeneralBreakablePortal portal) || portal.isRemoved() || !id.toString().equals(portal.portalTag)) {
                continue;
            }
            count++;
            Vec3 normal = portal.getNormal();
            Direction.Axis aligned = aligned(normal);
            helper.assertTrue(aligned != null, "portal normal " + normal);
            axes.add(aligned);
            helper.assertTrue(Math.abs(portal.getScaling() - 1.0) < 1.0e-6, "scaled portal");
            helper.assertTrue(
                    OpeningSize.sameOpening((int) Math.round(portal.getWidth()), (int) Math.round(portal.getHeight()), u, v),
                    "size " + portal.getWidth() + "x" + portal.getHeight());
        }
        helper.assertTrue(count == 4, "expected 4 portals, found " + count);
        helper.assertTrue(axes.contains(from) && axes.contains(to), "axes " + axes);
    }

    private static Direction.Axis aligned(Vec3 normal) {
        if (Math.abs(normal.x) > 0.9) {
            return Direction.Axis.X;
        }
        if (Math.abs(normal.y) > 0.9) {
            return Direction.Axis.Y;
        }
        if (Math.abs(normal.z) > 0.9) {
            return Direction.Axis.Z;
        }
        return null;
    }

    private static int count(ServerLevel level, UUID id) {
        int count = 0;
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof GeneralBreakablePortal portal && !portal.isRemoved() && id.toString().equals(portal.portalTag)) {
                count++;
            }
        }
        return count;
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

    private static void expect(GameTestHelper helper, Direction.Axis axis, int u, int v, FrameFinder.Status status) {
        Set<BlockPos> frame = new HashSet<>();
        Set<BlockPos> air = new HashSet<>();
        BlockPos origin = new BlockPos(0, 0, 0);
        for (int i = -1; i <= u; i++) {
            for (int j = -1; j <= v; j++) {
                BlockPos pos = at(origin, axis, i, j);
                if (i >= 0 && i < u && j >= 0 && j < v) {
                    air.add(pos);
                } else {
                    frame.add(pos);
                }
            }
        }
        FrameFinder.Result result = FrameFinder.find(at(origin, axis, -1, 0), frame::contains, air::contains, MAX);
        helper.assertTrue(result.status() == status, axis + " " + u + "x" + v + " -> " + result.status());
        if (status == FrameFinder.Status.FOUND) {
            helper.assertTrue(OpeningSize.sameOpening(result.only().u(), result.only().v(), u, v), "measured " + result.only().u() + "x" + result.only().v());
        }
    }

    private static void expectMissingCorner(GameTestHelper helper) {
        Set<BlockPos> frame = new HashSet<>();
        Set<BlockPos> air = new HashSet<>();
        BlockPos origin = BlockPos.ZERO;
        for (int i = -1; i <= 2; i++) {
            for (int j = -1; j <= 3; j++) {
                BlockPos pos = at(origin, Direction.Axis.Z, i, j);
                if (i >= 0 && i < 2 && j >= 0 && j < 3) {
                    air.add(pos);
                } else if (!(i == -1 && j == -1)) {
                    frame.add(pos);
                }
            }
        }
        FrameFinder.Result result = FrameFinder.find(at(origin, Direction.Axis.Z, -1, 0), frame::contains, air::contains, MAX);
        helper.assertTrue(result.status() == FrameFinder.Status.NONE, "corner " + result.status());
    }

    private static void expectGap(GameTestHelper helper) {
        Set<BlockPos> frame = new HashSet<>();
        Set<BlockPos> air = new HashSet<>();
        BlockPos origin = BlockPos.ZERO;
        for (int i = -1; i <= 2; i++) {
            for (int j = -1; j <= 3; j++) {
                BlockPos pos = at(origin, Direction.Axis.X, i, j);
                if (i >= 0 && i < 2 && j >= 0 && j < 3) {
                    air.add(pos);
                } else if (!(i == 2 && j == 1)) {
                    frame.add(pos);
                }
            }
        }
        FrameFinder.Result result = FrameFinder.find(at(origin, Direction.Axis.X, -1, 0), frame::contains, air::contains, MAX);
        helper.assertTrue(result.status() == FrameFinder.Status.NONE, "gap " + result.status());
    }

    private static BlockPos at(BlockPos origin, Direction.Axis axis, int i, int j) {
        return switch (axis) {
            case X -> origin.offset(0, j, i);
            case Y -> origin.offset(i, 0, j);
            case Z -> origin.offset(i, j, 0);
        };
    }
}
