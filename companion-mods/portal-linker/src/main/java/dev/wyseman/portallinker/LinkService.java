package dev.wyseman.portallinker;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import qouteall.imm_ptl.core.api.PortalAPI;
import qouteall.imm_ptl.core.portal.PortalExtension;
import qouteall.imm_ptl.core.portal.PortalManipulation;
import qouteall.imm_ptl.core.portal.nether_portal.BlockPortalShape;
import qouteall.imm_ptl.core.portal.nether_portal.GeneralBreakablePortal;
import qouteall.imm_ptl.core.portal.nether_portal.NetherPortalGeneration;

public final class LinkService {
    public record Attempt(boolean success, String key, Object[] args, @Nullable UUID linkId) {
        public static Attempt fail(String key, Object... args) {
            return new Attempt(false, key, args, null);
        }

        public static Attempt ok(String key, @Nullable UUID linkId, Object... args) {
            return new Attempt(true, key, args, linkId);
        }
    }

    private LinkService() {}

    public static Attempt use(@Nullable ServerPlayer player, ServerLevel level, BlockPos clicked, ItemStack stack) {
        Links links = Links.get(level.getServer());
        UUID token = LinkerItems.token(stack);
        if (token == null) {
            return store(level, clicked, stack, links);
        }
        FrameEnd.Selection selection = links.selection(token);
        if (selection == null) {
            LinkerItems.clear(stack);
            return Attempt.fail("portal_linker.gone");
        }
        return link(player, level, clicked, stack, links, selection);
    }

    public static Attempt clear(ServerLevel level, ItemStack stack) {
        UUID token = LinkerItems.token(stack);
        if (token == null) {
            return Attempt.fail("portal_linker.none_stored");
        }
        Links.get(level.getServer()).removeSelection(token);
        LinkerItems.clear(stack);
        return Attempt.ok("portal_linker.cleared", null);
    }

    private static Attempt store(ServerLevel level, BlockPos clicked, ItemStack stack, Links links) {
        int max = Config.maxInterior();
        FrameFinder.Result found = scan(level, clicked, max);
        Attempt problem = scanProblem(found, max);
        if (problem != null) {
            return problem;
        }
        FrameEnd end = FrameEnd.fromShape(level.dimension(), found.only());
        problem = validate(level.getServer(), end);
        if (problem != null) {
            return problem;
        }
        if (links.occupied(end)) {
            return Attempt.fail("portal_linker.already");
        }
        UUID token = UUID.randomUUID();
        links.putSelection(new FrameEnd.Selection(token, end));
        LinkerItems.write(stack, token, end.u, end.v, end.dimension.location().toString());
        return Attempt.ok("portal_linker.stored", null, end.u, end.v);
    }

    private static Attempt link(@Nullable ServerPlayer player, ServerLevel level, BlockPos clicked, ItemStack stack, Links links, FrameEnd.Selection selection) {
        int max = Config.maxInterior();
        FrameFinder.Result found = scan(level, clicked, max);
        Attempt problem = scanProblem(found, max);
        if (problem != null) {
            return problem;
        }
        FrameEnd second = FrameEnd.fromShape(level.dimension(), found.only());
        FrameEnd first = selection.end;
        if (first.samePlace(second)) {
            return Attempt.fail("portal_linker.same");
        }
        if (!first.sameOpening(second)) {
            return Attempt.fail("portal_linker.mismatch");
        }
        if (first.overlaps(second)) {
            return Attempt.fail("portal_linker.overlap");
        }
        problem = validate(level.getServer(), first);
        if (problem != null) {
            links.removeSelection(selection.token);
            LinkerItems.clear(stack);
            return "portal_linker.ship".equals(problem.key) || "portal_linker.border".equals(problem.key) || "portal_linker.dimension".equals(problem.key)
                    ? problem
                    : Attempt.fail("portal_linker.gone");
        }
        problem = validate(level.getServer(), second);
        if (problem != null) {
            return problem;
        }
        if (links.occupied(first) || links.occupied(second)) {
            return Attempt.fail("portal_linker.already");
        }
        ServerLevel fromLevel = level.getServer().getLevel(first.dimension);
        int claim = ClaimGuard.check(player, fromLevel, blocks(first), level, blocks(second));
        if (claim == ClaimGuard.DENY) {
            return Attempt.fail("portal_linker.claim");
        }
        if (claim == ClaimGuard.ERROR) {
            return Attempt.fail("portal_linker.claim_error");
        }
        if (player != null && Config.catalystEnabled() && !payCatalyst(player, stack, false)) {
            return Attempt.fail("portal_linker.catalyst", Config.catalystCount(), Config.catalystId().toString());
        }

        UUID id = UUID.randomUUID();
        List<GeneralBreakablePortal> portals;
        try {
            portals = build(level.getServer(), id, first, second);
        } catch (RuntimeException e) {
            PortalLinker.LOG.error("Portal link failed before the entities existed", e);
            clearInterior(level.getServer(), first);
            clearInterior(level.getServer(), second);
            return Attempt.fail("portal_linker.no_frame", max);
        }
        List<Links.PortalRef> refs = new ArrayList<>();
        for (GeneralBreakablePortal portal : portals) {
            refs.add(new Links.PortalRef(portal.getUUID(), portal.level().dimension(), portal.blockPosition().asLong()));
        }
        links.begin(new Links.Link(id, first, second, refs, true));
        try {
            for (GeneralBreakablePortal portal : portals) {
                PortalAPI.spawnServerEntity(portal);
                portal.reloadAndSyncToClient();
            }
        } catch (RuntimeException e) {
            PortalLinker.LOG.error("Portal link failed while spawning {}", id, e);
            links.abort(level.getServer(), id);
            return Attempt.fail("portal_linker.no_frame", max);
        }
        links.commit(id);
        links.removeSelection(selection.token);
        LinkerItems.clear(stack);
        if (player != null && Config.catalystEnabled()) {
            payCatalyst(player, stack, true);
        }
        PortalLinker.LOG.info("Linked portals {} ({}x{}) {} -> {}", id, first.u, first.v, first.dimension.location(), second.dimension.location());
        return Attempt.ok("portal_linker.linked", id, first.u, first.v);
    }

    private static List<GeneralBreakablePortal> build(MinecraftServer server, UUID id, FrameEnd from, FrameEnd to) {
        ServerLevel fromLevel = server.getLevel(from.dimension);
        ServerLevel toLevel = server.getLevel(to.dimension);
        BlockPortalShape fromShape = from.shape();
        BlockPortalShape toShape = to.shape();
        NetherPortalGeneration.fillInPlaceHolderBlocks(fromLevel, fromShape);
        NetherPortalGeneration.fillInPlaceHolderBlocks(toLevel, toShape);

        GeneralBreakablePortal fromFace = GeneralBreakablePortal.ENTITY_TYPE.create(fromLevel);
        GeneralBreakablePortal toFace = GeneralBreakablePortal.ENTITY_TYPE.create(toLevel);
        if (fromFace == null || toFace == null) {
            throw new IllegalStateException("Immersive Portals did not create a breakable portal");
        }
        fromShape.initPortalPosAxisShape(fromFace, Direction.AxisDirection.POSITIVE);
        toShape.initPortalPosAxisShape(toFace, Direction.AxisDirection.POSITIVE);
        fromFace.setDestDim(toLevel.dimension());
        fromFace.setDestination(toShape.innerAreaBox.getCenterVec());
        toFace.setDestDim(fromLevel.dimension());
        toFace.setDestination(fromShape.innerAreaBox.getCenterVec());
        PortalManipulation.adjustRotationToConnect(fromFace, toFace);
        fromFace.updateCache();
        toFace.updateCache();

        GeneralBreakablePortal fromBack = PortalManipulation.createFlippedPortal(fromFace, GeneralBreakablePortal.ENTITY_TYPE);
        GeneralBreakablePortal toBack = PortalManipulation.createFlippedPortal(toFace, GeneralBreakablePortal.ENTITY_TYPE);
        fromFace.blockPortalShape = fromShape;
        fromBack.blockPortalShape = fromShape;
        toFace.blockPortalShape = toShape;
        toBack.blockPortalShape = toShape;
        fromFace.reversePortalId = toFace.getUUID();
        toFace.reversePortalId = fromFace.getUUID();
        fromBack.reversePortalId = toBack.getUUID();
        toBack.reversePortalId = fromBack.getUUID();
        String tag = id.toString();
        List<GeneralBreakablePortal> portals = List.of(fromFace, fromBack, toFace, toBack);
        for (GeneralBreakablePortal portal : portals) {
            portal.portalTag = tag;
            portal.updateCache();
        }
        PortalExtension.initializeClusterBind(fromFace, fromBack, toFace, toBack);
        return portals;
    }

    private static void clearInterior(MinecraftServer server, FrameEnd end) {
        ServerLevel level = server.getLevel(end.dimension);
        if (level == null) {
            return;
        }
        for (BlockPos pos : end.interior) {
            if (level.getBlockState(pos).getBlock() == qouteall.imm_ptl.core.portal.PortalPlaceholderBlock.instance) {
                level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
        }
    }

    private static Attempt validate(MinecraftServer server, FrameEnd end) {
        ServerLevel level = server.getLevel(end.dimension);
        if (level == null) {
            return Attempt.fail("portal_linker.dimension");
        }
        List<BlockPos> all = blocks(end);
        for (BlockPos pos : all) {
            if (!level.getWorldBorder().isWithinBounds(pos)) {
                return Attempt.fail("portal_linker.border");
            }
        }
        if (ShipGuard.onShip(level, all)) {
            return Attempt.fail("portal_linker.ship");
        }
        for (BlockPos pos : end.frame) {
            level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
            if (!FrameFinder.isFrame(level.getBlockState(pos))) {
                return Attempt.fail("portal_linker.gone");
            }
        }
        for (BlockPos pos : end.interior) {
            level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
            BlockState state = level.getBlockState(pos);
            if (!state.isAir()) {
                return Attempt.fail("portal_linker.gone");
            }
        }
        return null;
    }

    private static List<BlockPos> blocks(FrameEnd end) {
        List<BlockPos> all = new ArrayList<>(end.frame.size() + end.interior.size());
        all.addAll(end.frame);
        all.addAll(end.interior);
        return all;
    }

    private static FrameFinder.Result scan(ServerLevel level, BlockPos clicked, int max) {
        return FrameFinder.find(
                clicked,
                pos -> FrameFinder.isFrame(level.getBlockState(pos)),
                pos -> level.getBlockState(pos).isAir(),
                max);
    }

    private static Attempt scanProblem(FrameFinder.Result found, int max) {
        return switch (found.status()) {
            case FOUND -> null;
            case TOO_SMALL -> Attempt.fail("portal_linker.too_small");
            case TOO_BIG -> Attempt.fail("portal_linker.too_big", max);
            case AMBIGUOUS -> Attempt.fail("portal_linker.ambiguous");
            case NONE -> Attempt.fail("portal_linker.no_frame", max);
        };
    }

    private static boolean payCatalyst(ServerPlayer player, ItemStack hand, boolean consume) {
        ResourceLocation id = Config.catalystId();
        if (!net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(id)) {
            return false;
        }
        Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(id);
        int need = Config.catalystCount();
        if (count(player, item, hand) < need) {
            return false;
        }
        if (!consume) {
            return true;
        }
        int left = need;
        left = shrink(player.getInventory().items, item, hand, left);
        shrink(player.getInventory().offhand, item, hand, left);
        return true;
    }

    private static int count(ServerPlayer player, Item item, ItemStack hand) {
        return count(player.getInventory().items, item, hand) + count(player.getInventory().offhand, item, hand);
    }

    private static int count(List<ItemStack> stacks, Item item, ItemStack hand) {
        int n = 0;
        for (ItemStack stack : stacks) {
            if (stack != hand && stack.is(item)) {
                n += stack.getCount();
            }
        }
        return n;
    }

    private static int shrink(List<ItemStack> stacks, Item item, ItemStack hand, int left) {
        for (ItemStack stack : stacks) {
            if (left <= 0) {
                return 0;
            }
            if (stack == hand || !stack.is(item)) {
                continue;
            }
            int take = Math.min(left, stack.getCount());
            stack.shrink(take);
            left -= take;
        }
        return left;
    }
}
