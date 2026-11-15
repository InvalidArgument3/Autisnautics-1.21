package dev.wyseman.portallinker;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

/** Loaded only on a client. Sneak-scroll cycles the exit and does not move the hotbar. */
public final class PortalLinkerClient {
    private PortalLinkerClient() {}

    public static void init() {
        NeoForge.EVENT_BUS.addListener(PortalLinkerClient::onScroll);
    }

    private static void onScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null || !minecraft.player.isShiftKeyDown()) {
            return;
        }
        if (!holdingLinker(minecraft)) {
            return;
        }
        if (event.getScrollDeltaY() == 0.0) {
            return;
        }
        event.setCanceled(true);
        int steps = event.getScrollDeltaY() > 0.0 ? 1 : -1;
        boolean targeted = false;
        BlockPos pos = BlockPos.ZERO;
        HitResult hit = minecraft.hitResult;
        if (hit instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK) {
            pos = blockHit.getBlockPos();
            if (minecraft.player.distanceToSqr(Vec3.atCenterOf(pos)) <= PortalLinkerNet.REACH_SQR) {
                targeted = true;
            }
        }
        PacketDistributor.sendToServer(new PortalLinkerNet.CycleLinkPayload(pos, targeted, steps));
    }

    private static boolean holdingLinker(Minecraft minecraft) {
        ItemStack main = minecraft.player.getMainHandItem();
        if (main.getItem() instanceof PortalLinkerItem) {
            return true;
        }
        return minecraft.player.getOffhandItem().getItem() instanceof PortalLinkerItem;
    }
}
