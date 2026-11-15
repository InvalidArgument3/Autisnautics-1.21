package dev.wyseman.portallinker;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
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
    public static void gateRecipeDiamondCornersAndLegacyFrames(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        assertGateRecipe(helper, level);

        Built built = placeMaterial(helper, new BlockPos(2, 2, 2), Direction.Axis.Z, 2, 3, false);
        Built other = placeMaterial(helper, new BlockPos(2, 2, 14), Direction.Axis.Z, 2, 3, false);
        ItemStack stack = new ItemStack(ModItems.LINKER.get());
        LinkService.Attempt refused = LinkService.use(null, level, built.click, stack);
        helper.assertFalse(refused.success(), "crying frame linked: " + refused.key());
        helper.assertTrue("portal_linker.no_frame".equals(refused.key()), refused.key());

        Built diamond = placeMaterial(helper, new BlockPos(24, 2, 2), Direction.Axis.Z, 2, 3, true);
        Built diamondOther = placeMaterial(helper, new BlockPos(24, 2, 14), Direction.Axis.Z, 2, 3, true);
        LinkService.Attempt stored = LinkService.use(null, level, diamond.click, stack);
        helper.assertTrue(stored.success(), stored.key());
        LinkService.Attempt linked = LinkService.use(null, level, diamondOther.click, stack);
        helper.assertTrue(linked.success(), linked.key());
        helper.assertTrue(linked.linkId() != null, "diamond link id");
        Links.Link made = Links.get(level.getServer()).link(linked.linkId());
        helper.assertTrue(made != null && !made.legacyFrames, "new link was grandfathered");

        assertLegacyLoad(helper, level);

        for (BlockPos pos : built.interior) {
            level.setBlockAndUpdate(pos, PortalPlaceholderBlock.instance.defaultBlockState());
        }
        for (BlockPos pos : other.interior) {
            level.setBlockAndUpdate(pos, PortalPlaceholderBlock.instance.defaultBlockState());
        }
        FrameEnd from = new FrameEnd(level.dimension(), Direction.Axis.Z, built.click, 2, 3, built.interior, built.frame);
        FrameEnd to = new FrameEnd(level.dimension(), Direction.Axis.Z, other.click, 2, 3, other.interior, other.frame);
        UUID legacyId = UUID.randomUUID();
        Links.Link legacy = new Links.Link(legacyId, from, to, List.of(), false, LinkOrientation.FRONT, true);
        legacy.orientSynced = true;
        Links links = Links.get(level.getServer());
        links.begin(legacy);
        links.commit(legacyId);
        links.sweep(level.getServer());
        Links.Link kept = links.link(legacyId);
        helper.assertTrue(kept != null && !kept.breaking, "legacy crying frame was closed");
        BlockPos edge = null;
        for (BlockPos pos : built.frame) {
            if (!FrameFinder.isCornerPosition(built.frame, pos)) {
                edge = pos;
                break;
            }
        }
        helper.assertTrue(edge != null, "no edge");
        level.setBlockAndUpdate(edge, Blocks.AIR.defaultBlockState());
        links.sweep(level.getServer());
        Links.Link after = links.link(legacyId);
        helper.assertTrue(after == null || after.breaking, "broken legacy frame stayed open");
        helper.succeed();
    }

    private static void assertLegacyLoad(GameTestHelper helper, ServerLevel level) {
        FrameEnd from = new FrameEnd(Level.OVERWORLD, Direction.Axis.Z, BlockPos.ZERO, 1, 2, Set.of(BlockPos.ZERO), Set.of(new BlockPos(0, 1, 0)));
        FrameEnd to = new FrameEnd(Level.OVERWORLD, Direction.Axis.Z, new BlockPos(0, 0, 8), 1, 2, Set.of(new BlockPos(0, 0, 8)), Set.of(new BlockPos(0, 1, 8)));
        UUID id = UUID.randomUUID();
        Links.Link fresh = new Links.Link(id, from, to, List.of(), false, LinkOrientation.FRONT);
        CompoundTag linkTag = fresh.save();
        linkTag.remove("legacyFrames");
        CompoundTag root = new CompoundTag();
        root.putInt("version", 2);
        ListTag list = new ListTag();
        list.add(linkTag);
        root.put("links", list);
        Links loaded = Links.load(root, level.registryAccess());
        Links.Link back = loaded.link(id);
        helper.assertTrue(back != null, "1.1.0 link did not load");
        helper.assertTrue(back.legacyFrames, "1.1.0 link was not grandfathered");
        helper.assertTrue(back.orientation == LinkOrientation.FRONT, "orientation dropped");
        CompoundTag again = new CompoundTag();
        loaded.save(again, level.registryAccess());
        helper.assertTrue(again.getInt("version") == 3, "resave version " + again.getInt("version"));
        helper.assertTrue(again.getList("links", Tag.TAG_COMPOUND).getCompound(0).getBoolean("legacyFrames"), "resave dropped the grandfather flag");
    }

    private static void assertGateRecipe(GameTestHelper helper, ServerLevel level) {
        var holder = level.getServer().getRecipeManager().byKey(ResourceLocation.parse("portal_linker:portal_linker"));
        helper.assertTrue(holder.isPresent(), "recipe missing");
        helper.assertTrue(holder.get().value() instanceof ShapedRecipe, "recipe type");
        ShapedRecipe shaped = (ShapedRecipe) holder.get().value();
        helper.assertTrue(shaped.getWidth() == 1 && shaped.getHeight() == 3, shaped.getWidth() + "x" + shaped.getHeight());
        helper.assertTrue(shaped.getIngredients().get(0).test(new ItemStack(Items.CRYING_OBSIDIAN)), "top is not crying obsidian");
        helper.assertTrue(shaped.getIngredients().get(2).test(new ItemStack(Items.ENDER_EYE)), "bottom is not an eye of ender");
        Ingredient gate = shaped.getIngredients().get(1);
        helper.assertTrue(gate.getValues().length == 1 && gate.getValues()[0] instanceof Ingredient.TagValue, "middle is not a tag");
        Ingredient.TagValue tag = (Ingredient.TagValue) gate.getValues()[0];
        helper.assertTrue(PortalLinker.LINKER_GATE.equals(tag.tag()), "gate tag " + tag.tag());
        HolderSet.Named<net.minecraft.world.item.Item> named = BuiltInRegistries.ITEM.getTag(PortalLinker.LINKER_GATE).orElseThrow();
        helper.assertTrue(named.size() == 0, "gate tag was already filled, size " + named.size());
        try {
            Method bind = HolderSet.Named.class.getDeclaredMethod("bind", List.class);
            bind.setAccessible(true);
            bind.invoke(named, List.<Holder<net.minecraft.world.item.Item>>of(Items.DIAMOND.builtInRegistryHolder()));
            Field cached = Ingredient.class.getDeclaredField("itemStacks");
            cached.setAccessible(true);
            cached.set(gate, null);
            Field stacking = Ingredient.class.getDeclaredField("stackingIds");
            stacking.setAccessible(true);
            stacking.set(gate, null);
        } catch (ReflectiveOperationException e) {
            helper.assertTrue(false, "could not fill the gate tag: " + e);
            return;
        }
        CraftingInput old = CraftingInput.of(1, 3, List.of(new ItemStack(Items.CRYING_OBSIDIAN), new ItemStack(Items.ENDER_EYE), new ItemStack(Items.CRYING_OBSIDIAN)));
        helper.assertTrue(level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, old, level).isEmpty(), "old column still crafts");
        helper.assertFalse(gate.test(new ItemStack(Items.ENDER_EYE)), "eye matches the gate");
        helper.assertFalse(gate.test(new ItemStack(Items.DIAMOND_BLOCK)), "diamond block matches the gate");
        CraftingInput gated = CraftingInput.of(1, 3, List.of(new ItemStack(Items.CRYING_OBSIDIAN), new ItemStack(Items.DIAMOND), new ItemStack(Items.ENDER_EYE)));
        var found = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, gated, level);
        helper.assertTrue(found.isPresent(), "gated column did not craft");
        ItemStack result = found.get().value().assemble(gated, level.registryAccess());
        helper.assertTrue(result.is(ModItems.LINKER.get()), "crafted " + result);
    }

    private record Built(BlockPos click, Set<BlockPos> frame, Set<BlockPos> interior) {}

    private static Built placeMaterial(GameTestHelper helper, BlockPos origin, Direction.Axis axis, int u, int v, boolean diamondCorners) {
        ServerLevel level = helper.getLevel();
        Set<BlockPos> frame = new HashSet<>();
        Set<BlockPos> interior = new HashSet<>();
        BlockPos click = null;
        for (int i = -1; i <= u; i++) {
            for (int j = -1; j <= v; j++) {
                BlockPos abs = helper.absolutePos(at(origin, axis, i, j));
                boolean inside = i >= 0 && i < u && j >= 0 && j < v;
                boolean corner = (i == -1 || i == u) && (j == -1 || j == v);
                BlockState state = inside
                        ? Blocks.AIR.defaultBlockState()
                        : (diamondCorners && corner ? Blocks.DIAMOND_BLOCK.defaultBlockState() : Blocks.CRYING_OBSIDIAN.defaultBlockState());
                level.setBlockAndUpdate(abs, state);
                if (inside) {
                    interior.add(abs);
                } else {
                    frame.add(abs);
                }
                if (!inside && i == -1 && j == 0) {
                    click = abs;
                }
            }
        }
        return new Built(click, frame, interior);
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
        for (int i = -1; i <= u; i++) {
            for (int j = -1; j <= v; j++) {
                BlockPos abs = helper.absolutePos(at(origin, axis, i, j));
                boolean interior = i >= 0 && i < u && j >= 0 && j < v;
                boolean corner = (i == -1 || i == u) && (j == -1 || j == v);
                BlockState frame = corner ? Blocks.DIAMOND_BLOCK.defaultBlockState() : Blocks.CRYING_OBSIDIAN.defaultBlockState();
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
