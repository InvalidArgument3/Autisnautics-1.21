package dev.wyseman.narratorkill.mixin;

import com.mojang.text2speech.Narrator;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.NarratorStatus;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The narrator engine is created once, in {@code GameNarrator.<init>}, by
 * {@code Narrator.getNarrator()} — that call loads the native text-to-speech
 * library. The redirect swaps it for Mojang's own {@code Narrator.EMPTY}, so the
 * library is never loaded and nothing can speak.
 *
 * Everything else here is belt and braces: the status setter is dropped (no
 * toast, no mode change), and every say path plus {@code isActive} exits early.
 * {@code isActive} matters on its own because {@code KeyboardHandler.keyPress}
 * checks it before the narrator hotkey, so Ctrl+B can never reach the toggle.
 */
@Mixin(value = GameNarrator.class, remap = false)
public class GameNarratorMixin {
    @Redirect(
        method = "<init>",
        at = @At(value = "INVOKE",
            target = "Lcom/mojang/text2speech/Narrator;getNarrator()Lcom/mojang/text2speech/Narrator;"),
        remap = false)
    private static Narrator narratorkill$noEngine() {
        return Narrator.EMPTY;
    }

    @Inject(method = "isActive", at = @At("HEAD"), cancellable = true, remap = false)
    private void narratorkill$inactive(CallbackInfoReturnable<Boolean> ci) {
        ci.setReturnValue(false);
    }

    @Inject(method = "updateNarratorStatus", at = @At("HEAD"), cancellable = true, remap = false)
    private void narratorkill$dropStatus(NarratorStatus status, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "sayChat", at = @At("HEAD"), cancellable = true, remap = false)
    private void narratorkill$sayChat(Component message, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "say", at = @At("HEAD"), cancellable = true, remap = false)
    private void narratorkill$say(Component message, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "sayNow(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void narratorkill$sayNowComponent(Component message, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "sayNow(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void narratorkill$sayNowString(String message, CallbackInfo ci) {
        ci.cancel();
    }
}
