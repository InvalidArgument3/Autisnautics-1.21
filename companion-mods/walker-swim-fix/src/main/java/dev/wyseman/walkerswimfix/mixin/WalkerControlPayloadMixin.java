package dev.wyseman.walkerswimfix.mixin;

import dev.letitwalk.content.WalkerCoreBlockEntity;
import dev.letitwalk.network.WalkerControlPayload;
import dev.wyseman.walkerswimfix.WalkerSwimFix;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WalkerControlPayload.class, remap = false)
public abstract class WalkerControlPayloadMixin {
    @Inject(method = "drive", at = @At("HEAD"))
    private static void walkerswimfix$trackWalker(
        WalkerControlPayload payload,
        ServerPlayer player,
        WalkerCoreBlockEntity core,
        CallbackInfo ci
    ) {
        WalkerSwimFix.recordActiveWalker(player, core);
    }
}
