package dev.wyseman.walkerswimfix.mixin;

import dev.letitwalk.content.PilotControlsBlockEntity;
import dev.letitwalk.content.WalkerCoreBlockEntity;
import dev.letitwalk.network.WalkerControlPayload;
import dev.letitwalk.walker.WalkerTracker;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.wyseman.walkerswimfix.CrossPortalHelper;
import dev.wyseman.walkerswimfix.WalkerSwimFix;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.portal.Portal;

import java.util.UUID;

@Mixin(value = WalkerControlPayload.class, remap = false)
public abstract class WalkerControlPayloadMixin {
    @Shadow
    private static WalkerCoreBlockEntity linkedCore(ServerPlayer player, UUID id, int keys) {
        return null;
    }

    @Shadow
    private static boolean jetJump(WalkerCoreBlockEntity core) {
        return false;
    }

    @Shadow
    private static void drive(WalkerControlPayload payload, ServerPlayer player, WalkerCoreBlockEntity core) {}

    @Inject(method = "drive", at = @At("HEAD"))
    private static void walkerswimfix$trackWalker(
        WalkerControlPayload payload,
        ServerPlayer player,
        WalkerCoreBlockEntity core,
        CallbackInfo ci
    ) {
        WalkerSwimFix.recordActiveWalker(player, core);
    }

    @Inject(method = "block", at = @At("HEAD"), cancellable = true)
    private static void walkerswimfix$crossPortalBlock(
        WalkerControlPayload payload,
        ServerPlayer player,
        CallbackInfo ci
    ) {
        if (player == null || player.server == null) {
            return;
        }
        if (player.level().getBlockEntity(payload.controls()) != null) {
            return;
        }

        Vec3 controlsCenter = Vec3.atCenterOf(payload.controls());
        for (ServerLevel targetLevel : player.server.getAllLevels()) {
            if (targetLevel == player.level()) {
                continue;
            }
            BlockEntity be = targetLevel.getBlockEntity(payload.controls());
            if (!(be instanceof PilotControlsBlockEntity controls)) {
                continue;
            }
            boolean inRange = false;
            for (Portal portal : CrossPortalHelper.portalsTo(player, targetLevel.dimension())) {
                Vec3 transformed = portal.transformPoint(player.position());
                double d2 = Sable.HELPER.distanceSquaredWithSubLevels(targetLevel, transformed, controlsCenter);
                if (d2 <= 144.0) {
                    inRange = true;
                    break;
                }
            }
            if (!inRange) {
                continue;
            }

            WalkerCoreBlockEntity core = null;
            if (controls.linkedWalker != null) {
                core = linkedCore(player, controls.linkedWalker, payload.keys());
            } else {
                SubLevel sub = Sable.HELPER.getContaining(targetLevel, payload.controls());
                BlockPos corePos = sub != null ? WalkerTracker.coreOf(sub) : null;
                if (corePos != null && targetLevel.getBlockEntity(corePos) instanceof WalkerCoreBlockEntity found && found.isAssembled()) {
                    core = found;
                }
            }

            controls.receive(
                player,
                payload.keys(),
                core != null,
                core != null && core.isPowered(),
                core != null ? Math.round(Math.abs(core.getSpeed())) : 0,
                jetJump(core)
            );
            if (core != null) {
                drive(payload, player, core);
            }
            ci.cancel();
            return;
        }
    }
}
