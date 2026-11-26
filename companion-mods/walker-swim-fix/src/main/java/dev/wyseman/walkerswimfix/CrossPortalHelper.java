package dev.wyseman.walkerswimfix;

import dev.ryanhcode.sable.Sable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.global_portals.GlobalPortalStorage;

import java.util.ArrayList;
import java.util.List;

public final class CrossPortalHelper {
    private CrossPortalHelper() {}

    public static List<Portal> portalsTo(ServerPlayer player, ResourceKey<Level> targetDim) {
        ServerLevel playerLevel = player.serverLevel();
        List<Portal> matches = new ArrayList<>();
        List<Portal> globals = GlobalPortalStorage.getGlobalPortals(playerLevel);
        if (globals != null) {
            for (Portal p : globals) {
                if (p != null && targetDim.equals(p.getDestDim())) {
                    matches.add(p);
                }
            }
        }
        List<Portal> locals = playerLevel.getEntitiesOfClass(
            Portal.class,
            player.getBoundingBox().inflate(64.0),
            p -> p != null && p.isAlive() && targetDim.equals(p.getDestDim())
        );
        matches.addAll(locals);
        return matches;
    }

    public static boolean isWithinCrossPortalRange(
        ServerPlayer player,
        ServerLevel targetLevel,
        Vec3 targetPos,
        double maxDistSq
    ) {
        for (Portal portal : portalsTo(player, targetLevel.dimension())) {
            Vec3 transformed = portal.transformPoint(player.position());
            double d2 = Sable.HELPER.distanceSquaredWithSubLevels(targetLevel, transformed, targetPos);
            if (d2 <= maxDistSq) {
                return true;
            }
        }
        return false;
    }
}
