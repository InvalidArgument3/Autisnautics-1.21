package dev.wyseman.harnessportals.mixin;

import com.kompot.adventurersharness.client.MiningInput;
import com.kompot.adventurersharness.network.C2S;
import dev.wyseman.harnessportals.PortalTargetPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.PortalUtils;

@Mixin(MiningInput.class)
public abstract class MiningInputMixin {

    @Redirect(
            method = { "target", "buildTarget" },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;pick(DFZ)Lnet/minecraft/world/phys/HitResult;"
            )
    )
    private static HitResult harnessPortals$portalAwarePick(LocalPlayer player, double reach, float partialTick, boolean hitFluids) {
        PortalUtils.PortalAwareRaytraceResult portalResult = PortalUtils.portalAwareRayTrace(player, reach);
        if (portalResult != null && portalResult.hitResult() != null
                && portalResult.hitResult().getType() == HitResult.Type.BLOCK
                && portalResult.world() != null
                && !portalResult.portalsPassingThrough().isEmpty()) {
            Vec3 eye = player.getEyePosition(partialTick);
            for (Portal portal : portalResult.portalsPassingThrough()) {
                eye = portal.transformPoint(eye);
            }
            PortalTargetPayload.clientTargetDim = portalResult.world().dimension();
            PortalTargetPayload.clientTransformedEye = eye;
            return portalResult.hitResult();
        }
        PortalTargetPayload.clientTargetDim = null;
        PortalTargetPayload.clientTransformedEye = null;
        return player.pick(reach, partialTick, hitFluids);
    }

    @Redirect(
            method = { "release", "building" },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/kompot/adventurersharness/network/ModNetwork;toServer(Lcom/kompot/adventurersharness/network/C2S$Packet;)V"
            )
    )
    private static void harnessPortals$sendPortalContextBeforeMining(C2S.Packet packet) {
        if (packet instanceof C2S.Mining) {
            var dim = PortalTargetPayload.clientTargetDim;
            Vec3 eye = PortalTargetPayload.clientTransformedEye;
            if (dim != null && eye != null) {
                PacketDistributor.sendToServer(new PortalTargetPayload(true, dim.location(), eye.x, eye.y, eye.z));
            } else {
                PacketDistributor.sendToServer(new PortalTargetPayload(false, Level.OVERWORLD.location(), 0.0, 0.0, 0.0));
            }
        }
        com.kompot.adventurersharness.network.ModNetwork.toServer(packet);
    }
}
