package dev.wyseman.walkerswimfix.mixin;

import dev.autisnautics.octolegs.config.OctoLegsConfig;
import dev.autisnautics.octolegs.content.controls.LegControlsBlockEntity;
import dev.wyseman.walkerswimfix.CrossPortalHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(value = LegControlsBlockEntity.class, remap = false)
public abstract class LegControlsBlockEntityMixin {
    @Shadow
    private UUID driver;

    @Shadow
    private boolean onShip;

    @Shadow
    private boolean helmInWorld;

    @Shadow
    private long lastShipTime;

    @Shadow
    private double helmX;

    @Shadow
    private double helmY;

    @Shadow
    private double helmZ;

    @Inject(method = "checkPilot", at = @At("HEAD"), cancellable = true)
    private void walkerswimfix$keepCrossPortalPilot(CallbackInfo ci) {
        LegControlsBlockEntity self = (LegControlsBlockEntity) (Object) this;
        if (this.driver == null || !(self.getLevel() instanceof ServerLevel beLevel) || beLevel.isClientSide) {
            return;
        }
        ServerPlayer player = beLevel.getServer().getPlayerList().getPlayer(this.driver);
        if (player == null || !player.isAlive() || player.isDeadOrDying() || player.level() == beLevel) {
            return;
        }

        boolean shipActive = this.onShip
            || (this.lastShipTime != Long.MIN_VALUE && beLevel.getGameTime() - this.lastShipTime <= 2L);
        if (shipActive && !this.helmInWorld) {
            ci.cancel();
            return;
        }

        Vec3 helmPos = shipActive
            ? new Vec3(this.helmX, this.helmY, this.helmZ)
            : Vec3.atCenterOf(self.getBlockPos());

        double maxDist = 8.0;
        try {
            maxDist = Math.max(OctoLegsConfig.MAX_PILOT_DISTANCE.get(), 16.0);
        } catch (Throwable ignored) {
            maxDist = 16.0;
        }

        if (CrossPortalHelper.isWithinCrossPortalRange(player, beLevel, helmPos, maxDist * maxDist)) {
            ci.cancel();
        }
    }
}
