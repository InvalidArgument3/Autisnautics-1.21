package dev.wyseman.harnessportals.mixin;

import com.kompot.adventurersharness.ability.HarnessMining;
import com.kompot.adventurersharness.ability.HarnessServer;
import com.kompot.adventurersharness.harness.HarnessTier;
import dev.wyseman.harnessportals.PortalTargetPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(HarnessMining.class)
public abstract class HarnessMiningMixin {

    @Inject(method = "click", at = @At("HEAD"))
    private static void harnessPortals$enterClick(ServerPlayer player, BlockPos pos, CallbackInfo ci) {
        PortalTargetPayload.ACTIVE_CONTEXT.set(PortalTargetPayload.PENDING_BY_PLAYER.get(player.getUUID()));
    }

    @Inject(method = "click", at = @At("RETURN"))
    private static void harnessPortals$exitClick(ServerPlayer player, BlockPos pos, CallbackInfo ci) {
        PortalTargetPayload.ACTIVE_CONTEXT.remove();
    }

    @Inject(method = "select", at = @At("HEAD"))
    private static void harnessPortals$enterSelect(ServerPlayer player, BlockPos a, BlockPos b, CallbackInfo ci) {
        PortalTargetPayload.ACTIVE_CONTEXT.set(PortalTargetPayload.PENDING_BY_PLAYER.get(player.getUUID()));
    }

    @Inject(method = "select", at = @At("RETURN"))
    private static void harnessPortals$exitSelect(ServerPlayer player, BlockPos a, BlockPos b, CallbackInfo ci) {
        PortalTargetPayload.ACTIVE_CONTEXT.remove();
    }

    @Inject(method = "build", at = @At("HEAD"))
    private static void harnessPortals$enterBuild(ServerPlayer player, int shape, BlockPos a, BlockPos b, BlockPos c, CallbackInfo ci) {
        PortalTargetPayload.ACTIVE_CONTEXT.set(PortalTargetPayload.PENDING_BY_PLAYER.get(player.getUUID()));
    }

    @Inject(method = "build", at = @At("RETURN"))
    private static void harnessPortals$exitBuild(ServerPlayer player, int shape, BlockPos a, BlockPos b, BlockPos c, CallbackInfo ci) {
        PortalTargetPayload.ACTIVE_CONTEXT.remove();
    }

    @Inject(method = "add", at = @At("RETURN"))
    private static void harnessPortals$bindJobContext(ServerPlayer player, HarnessServer.Session session, List<BlockPos> blocks, int[] outline, boolean build, CallbackInfo ci) {
        if (session.job != null) {
            PortalTargetPayload.PortalContext ctx = PortalTargetPayload.PENDING_BY_PLAYER.get(player.getUUID());
            if (ctx != null) {
                PortalTargetPayload.JOB_CONTEXTS.put(session.job, ctx);
            } else {
                PortalTargetPayload.JOB_CONTEXTS.remove(session.job);
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private static void harnessPortals$enterTick(ServerPlayer player, HarnessServer.Session session, ItemStack stack, HarnessTier tier, long gameTime, CallbackInfo ci) {
        if (session.job != null) {
            PortalTargetPayload.ACTIVE_CONTEXT.set(PortalTargetPayload.JOB_CONTEXTS.get(session.job));
        } else {
            PortalTargetPayload.ACTIVE_CONTEXT.remove();
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private static void harnessPortals$exitTick(ServerPlayer player, HarnessServer.Session session, ItemStack stack, HarnessTier tier, long gameTime, CallbackInfo ci) {
        PortalTargetPayload.ACTIVE_CONTEXT.remove();
    }

    @Redirect(
            method = { "click", "select", "build", "add", "tick", "start" },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;serverLevel()Lnet/minecraft/server/level/ServerLevel;"
            ),
            require = 0
    )
    private static ServerLevel harnessPortals$redirectServerLevel(ServerPlayer player) {
        PortalTargetPayload.PortalContext ctx = PortalTargetPayload.ACTIVE_CONTEXT.get();
        if (ctx != null && ctx.targetDim() != null) {
            ServerLevel target = player.server.getLevel(ctx.targetDim());
            if (target != null) {
                return target;
            }
        }
        return player.serverLevel();
    }

    @Redirect(
            method = { "click", "select", "build", "tick", "dig" },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;getEyePosition()Lnet/minecraft/world/phys/Vec3;"
            ),
            require = 0
    )
    private static Vec3 harnessPortals$redirectEyePosition(ServerPlayer player) {
        PortalTargetPayload.PortalContext ctx = PortalTargetPayload.ACTIVE_CONTEXT.get();
        if (ctx != null && ctx.transformedEye() != null) {
            return ctx.transformedEye();
        }
        return player.getEyePosition();
    }
}
