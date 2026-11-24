package dev.wyseman.autisnauticsrifts;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class RiftRepairCommand {
    private static final int HARD_CAP = 2048;
    private static final int QUIET = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static int serial = 0;
    private static final Map<String, PreviewSnapshot> PREVIEWS = new HashMap<>();

    private record PreviewSnapshot(String token, String dim, int cx, int cz, String boundary, Map<String, Integer> counts) {}

    private RiftRepairCommand() {}

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("autisnautics_rift")
            .then(Commands.literal("locate")
                .executes(ctx -> locate(ctx, false))
                .then(Commands.literal("floor").executes(ctx -> locate(ctx, false)))
                .then(Commands.literal("ceiling").executes(ctx -> locate(ctx, true)))
            )
            .then(Commands.literal("preview")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("dimension", StringArgumentType.string())
                    .then(Commands.argument("chunkX", IntegerArgumentType.integer())
                        .then(Commands.argument("chunkZ", IntegerArgumentType.integer())
                            .then(Commands.argument("boundary", StringArgumentType.string())
                                .executes(RiftRepairCommand::preview)
                            )
                        )
                    )
                )
            )
            .then(Commands.literal("apply")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("dimension", StringArgumentType.string())
                    .then(Commands.argument("chunkX", IntegerArgumentType.integer())
                        .then(Commands.argument("chunkZ", IntegerArgumentType.integer())
                            .then(Commands.argument("boundary", StringArgumentType.string())
                                .then(Commands.argument("allowlist", StringArgumentType.string())
                                    .then(Commands.argument("maxBlocks", IntegerArgumentType.integer(1, HARD_CAP))
                                        .then(Commands.argument("token", StringArgumentType.string())
                                            .then(Commands.literal("confirm").executes(RiftRepairCommand::apply))
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        );
    }

    private static int locate(CommandContext<CommandSourceStack> ctx, boolean ceiling) {
        ServerLevel level = ctx.getSource().getLevel();
        BlockPos origin = BlockPos.containing(ctx.getSource().getPosition());
        RiftTarget target = RiftLocator.findNearestRift(level, origin, ceiling);
        if (!target.found()) {
            ctx.getSource().sendSuccess(() -> Component.literal("No dimensional rift found in " + level.dimension().location()), false);
            return 0;
        }
        RiftBoundary.Face face = RiftBoundary.resolveFace(level, target.ceilingMode());
        String dir = (face != null && face.isCeiling()) ? "Ceiling (↑ " : "Floor (↓ ";
        String neighbor = face != null ? face.neighborName() : target.boundary();
        double dx = target.targetX() - origin.getX();
        double dz = target.targetZ() - origin.getZ();
        int dist = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
        ctx.getSource().sendSuccess(() -> Component.literal(
            "Nearest " + dir + neighbor + ") Rift: X " + target.targetX() + ", Z " + target.targetZ() + " (" + dist + "m)"
        ), false);
        return 1;
    }

    private static ServerLevel findLevel(CommandContext<CommandSourceStack> ctx, String dim) {
        for (ServerLevel lvl : ctx.getSource().getServer().getAllLevels()) {
            if (lvl.dimension().location().toString().equals(dim)) return lvl;
        }
        return null;
    }

    private static int preview(CommandContext<CommandSourceStack> ctx) {
        String dim = StringArgumentType.getString(ctx, "dimension");
        int cx = IntegerArgumentType.getInteger(ctx, "chunkX");
        int cz = IntegerArgumentType.getInteger(ctx, "chunkZ");
        String boundary = StringArgumentType.getString(ctx, "boundary");

        ServerLevel level = findLevel(ctx, dim);
        if (level == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown dimension: " + dim));
            return 0;
        }
        LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
        if (chunk == null) {
            ctx.getSource().sendFailure(Component.literal("Chunk (" + cx + ", " + cz + ") is not loaded."));
            return 0;
        }
        DensityFunction density = level.getChunkSource().randomState().router().finalDensity();
        List<RiftBoundary.Face> faces = new ArrayList<>();
        for (RiftBoundary.Face f : RiftBoundary.resolveAllFaces(level)) {
            if ("all".equals(boundary) || f.boundary().equals(boundary)) faces.add(f);
        }
        if (faces.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("No matching boundary " + boundary + " in " + dim));
            return 0;
        }

        Map<String, Integer> counts = new HashMap<>();
        int total = 0;
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();
        for (RiftBoundary.Face s : faces) {
            int lo = Math.max(s.clearLo(), minY);
            int hi = Math.min(s.clearHi(), maxY);
            for (int lx = 0; lx < 16; lx++) {
                for (int lz = 0; lz < 16; lz++) {
                    int x = cx * 16 + lx;
                    int z = cz * 16 + lz;
                    if (!RiftLocator.isRiftColumn(level, density, s, x, z)) continue;
                    for (int y = lo; y < hi; y++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState state = chunk.getBlockState(pos);
                        if (state.hasBlockEntity() || chunk.getBlockEntity(pos) != null) continue;
                        if (s.wantsFluid()) {
                            if (state.is(s.fillBlockState().getBlock())) continue;
                        } else if (state.isAir()) continue;
                        String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
                        counts.merge(id, 1, Integer::sum);
                        total++;
                    }
                }
            }
        }
        String token = String.valueOf(++serial);
        if (PREVIEWS.size() > 64) PREVIEWS.clear();
        PREVIEWS.put(token, new PreviewSnapshot(token, dim, cx, cz, boundary, counts));
        final int finalTotal = total;
        ctx.getSource().sendSuccess(() -> Component.literal(
            "[RiftRepair] preview token=" + token + " total=" + finalTotal + " types=" + counts
        ), false);
        return 1;
    }

    private static int apply(CommandContext<CommandSourceStack> ctx) {
        String dim = StringArgumentType.getString(ctx, "dimension");
        int cx = IntegerArgumentType.getInteger(ctx, "chunkX");
        int cz = IntegerArgumentType.getInteger(ctx, "chunkZ");
        String boundary = StringArgumentType.getString(ctx, "boundary");
        String allowCsv = StringArgumentType.getString(ctx, "allowlist");
        int maxBlocks = Math.min(HARD_CAP, IntegerArgumentType.getInteger(ctx, "maxBlocks"));
        String token = StringArgumentType.getString(ctx, "token");

        PreviewSnapshot snap = PREVIEWS.get(token);
        if (snap == null || !snap.dim().equals(dim) || snap.cx() != cx || snap.cz() != cz || !snap.boundary().equals(boundary)) {
            ctx.getSource().sendFailure(Component.literal("Invalid or mismatched preview token."));
            return 0;
        }
        ServerLevel level = findLevel(ctx, dim);
        if (level == null) return 0;
        LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
        if (chunk == null) return 0;

        Set<String> allowed = new HashSet<>(Arrays.asList(allowCsv.split(",")));
        DensityFunction density = level.getChunkSource().randomState().router().finalDensity();
        int changed = 0;
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();

        for (RiftBoundary.Face s : RiftBoundary.resolveAllFaces(level)) {
            if (!s.boundary().equals(boundary)) continue;
            int lo = Math.max(s.clearLo(), minY);
            int hi = Math.min(s.clearHi(), maxY);
            for (int lx = 0; lx < 16 && changed < maxBlocks; lx++) {
                for (int lz = 0; lz < 16 && changed < maxBlocks; lz++) {
                    int x = cx * 16 + lx;
                    int z = cz * 16 + lz;
                    if (!RiftLocator.isRiftColumn(level, density, s, x, z)) continue;
                    for (int y = lo; y < hi && changed < maxBlocks; y++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState state = chunk.getBlockState(pos);
                        if (state.hasBlockEntity() || chunk.getBlockEntity(pos) != null) continue;
                        String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
                        if (!allowed.contains(id)) continue;
                        if (s.wantsFluid()) {
                            BlockState targetState = s.fillBlockState();
                            if (state.is(targetState.getBlock())) continue;
                            level.setBlock(pos, targetState, QUIET);
                        } else {
                            if (state.isAir()) continue;
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), QUIET);
                        }
                        changed++;
                    }
                }
            }
        }
        PREVIEWS.remove(token);
        final int finalChanged = changed;
        ctx.getSource().sendSuccess(() -> Component.literal("[RiftRepair] applied " + finalChanged + " block updates."), true);
        return 1;
    }
}
