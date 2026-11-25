package dev.wyseman.walkerswimfix.mixin;

import dev.letitwalk.content.WalkerCoreBlockEntity;
import dev.letitwalk.walker.BuiltLeg;
import dev.letitwalk.walker.LegRuntime;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.compat.sable_integration.IPShipJoints;

import java.util.Map;
import java.util.UUID;

@Mixin(value = IPShipJoints.class, remap = false)
public abstract class IPShipJointsMixin {
    @Unique
    private static final Logger WALKERSWIMFIX_LOG = LoggerFactory.getLogger("walkerswimfix");

    @Inject(method = "shiftLoadedJoints", at = @At("HEAD"))
    private static void walkerswimfix$shiftWalkerLegs(
        ServerLevel level,
        Map<UUID, BlockPos> shifts,
        CallbackInfo ci
    ) {
        if (level == null || shifts == null || shifts.isEmpty()) {
            return;
        }
        ServerSubLevelContainer container = SubLevelContainer.getContainer(level);
        if (container == null) {
            return;
        }

        for (Map.Entry<UUID, BlockPos> entry : shifts.entrySet()) {
            UUID hullId = entry.getKey();
            SubLevel subLevel = container.getSubLevel(hullId);
            if (!(subLevel instanceof ServerSubLevel sub)) {
                continue;
            }
            BlockPos hullShift = entry.getValue() == null ? BlockPos.ZERO : entry.getValue();

            for (BlockEntitySubLevelActor actor : sub.getPlot().getBlockEntityActors()) {
                if (!(actor instanceof WalkerCoreBlockEntity core)) {
                    continue;
                }
                if (core.legs == null || core.legs.isEmpty()) {
                    continue;
                }

                int shiftedLegs = 0;
                for (LegRuntime runtime : core.legs) {
                    BuiltLeg leg = runtime.built;
                    if (leg == null) {
                        continue;
                    }

                    // Shift joint plot coordinates in each parent/child sublevel
                    for (int i = 0; i < leg.joints.size(); i++) {
                        BuiltLeg.Joint j = leg.joints.get(i);
                        BlockPos pShift = shifts.getOrDefault(j.parent, BlockPos.ZERO);
                        BlockPos cShift = shifts.getOrDefault(j.child, BlockPos.ZERO);
                        if (!pShift.equals(BlockPos.ZERO) || !cShift.equals(BlockPos.ZERO)) {
                            leg.joints.set(i, new BuiltLeg.Joint(
                                j.parent,
                                j.child,
                                j.parentPlot.add(pShift.getX(), pShift.getY(), pShift.getZ()),
                                j.childPlot.add(cShift.getX(), cShift.getY(), cShift.getZ()),
                                j.axis,
                                j.swivel
                            ));
                        }
                    }

                    // Shift foot plot coordinate in the last segment sublevel
                    if (!leg.segments.isEmpty() && leg.footPlot != null) {
                        UUID lastSeg = leg.segments.get(leg.segments.size() - 1);
                        BlockPos fShift = shifts.getOrDefault(lastSeg, BlockPos.ZERO);
                        if (!fShift.equals(BlockPos.ZERO)) {
                            leg.footPlot = leg.footPlot.add(fShift.getX(), fShift.getY(), fShift.getZ());
                        }
                    }

                    // Adjust segment offsets if any segment shifted differently from the hull
                    if (leg.segmentOffsets != null) {
                        int n = Math.min(leg.segments.size(), leg.segmentOffsets.length);
                        for (int i = 0; i < n; i++) {
                            BlockPos sShift = shifts.getOrDefault(leg.segments.get(i), BlockPos.ZERO);
                            int dx = sShift.getX() - hullShift.getX();
                            int dy = sShift.getY() - hullShift.getY();
                            int dz = sShift.getZ() - hullShift.getZ();
                            if ((dx != 0 || dy != 0 || dz != 0) && leg.segmentOffsets[i] != null) {
                                leg.segmentOffsets[i] = leg.segmentOffsets[i].add(dx, dy, dz);
                            }
                        }
                    }

                    // Clear stale physics filter handles and cached desired poses from the previous dimension
                    leg.contactFilters.clear();
                    leg.prevDesired = null;
                    leg.prevDesiredRot = null;
                    shiftedLegs++;
                }

                core.setChanged();
                WALKERSWIMFIX_LOG.info(
                    "[walkerswimfix] Updated {} walker legs for core at {} in {} (hull shift {})",
                    shiftedLegs, core.getBlockPos(), level.dimension().location(), hullShift
                );
            }
        }
    }
}
